package tw.boris4110799.composing.library.ui.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.OutputStream
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/**
 * Manage `Camera` service.
 */
class CameraManager {
    private var resolutionSelector = ResolutionSelector.Builder().build()

    private var preview = Preview.Builder().setResolutionSelector(resolutionSelector).build()

    private var imageCapture = ImageCapture.Builder()
        .setTargetRotation(Surface.ROTATION_0)
        .setResolutionSelector(resolutionSelector)
        .setIoExecutor(Executors.newSingleThreadExecutor())
        .build()

    private var imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    private var processCameraProvider: ProcessCameraProvider? = null

    private val _surfaceRequests = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequests = _surfaceRequests.asStateFlow()

    /**
     * Initialize camera.
     */
    suspend fun init(
        context: Context,
        lifecycleOwner: LifecycleOwner
    ) {
        val cameraProvider = processCameraProvider ?: getCameraProvider(context)

        preview.setSurfaceProvider { surfaceRequest ->
            _surfaceRequests.update { surfaceRequest }
        }
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, imageAnalysis
        )
    }

    /**
     * Stop camera.
     */
    fun stop() {
        processCameraProvider?.unbindAll()
    }

    /**
     * Take a picture.
     */
    fun takePicture(
        context: Context,
        file: File
    ) = takePicture(context, ImageCapture.OutputFileOptions.Builder(file).build())

    /**
     * Take a picture.
     */
    fun takePicture(
        context: Context,
        outputStream: OutputStream
    ) = takePicture(context, ImageCapture.OutputFileOptions.Builder(outputStream).build())

    /**
     * Take a picture.
     */
    fun takePicture(
        context: Context,
        fileName: String,
        filePath: String = "DCIM/Camera",
        mimeType: String = "image/jpeg"
    ): Flow<Uri> {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, filePath)
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        return takePicture(context, outputOptions)
    }

    fun setAnalyzer(analyzer: ImageAnalysis.Analyzer) {
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
    }

    fun clearAnalyzer() {
        imageAnalysis.clearAnalyzer()
    }

    fun setResolutionSelector(builder: ResolutionSelector.Builder.() -> Unit) {
        resolutionSelector = ResolutionSelector.Builder().apply(builder).build()
    }

    fun setPreview(builder: Preview.Builder.() -> Unit) {
        preview = Preview.Builder().apply(builder).build()
    }

    fun setImageCapture(builder: ImageCapture.Builder.() -> Unit) {
        imageCapture = ImageCapture.Builder().apply(builder).build()
    }

    fun setImageAnalysis(builder: ImageAnalysis.Builder.() -> Unit) {
        imageAnalysis = ImageAnalysis.Builder().apply(builder).build()
    }

    /**
     * Get [ProcessCameraProvider].
     */
    private suspend fun getCameraProvider(context: Context): ProcessCameraProvider =
        suspendCancellableCoroutine { continuation ->
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener(
                    { MainScope().launch { continuation.resume(future.get()) } },
                    ContextCompat.getMainExecutor(context)
                )
            }
        }

    private fun takePicture(
        context: Context,
        options: ImageCapture.OutputFileOptions
    ) = callbackFlow {
        imageCapture.takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.let {
                        trySend(it)
                    }
                    channel.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    channel.close(exception)
                }
            })

        awaitClose()
    }
}
