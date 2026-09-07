package tw.boris4110799.composing.library.ui.components

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import tw.boris4110799.composing.library.ui.util.CameraManager

@Composable
fun CameraView(
    surfaceRequests: StateFlow<SurfaceRequest?>,
    modifier: Modifier = Modifier,
    implementationMode: ImplementationMode = ImplementationMode.EXTERNAL,
    coordinateTransformer: MutableCoordinateTransformer? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop
) {
    val surfaceRequest by surfaceRequests.collectAsStateWithLifecycle()

    surfaceRequest?.let {
        CameraXViewfinder(
            surfaceRequest = it,
            modifier = modifier,
            implementationMode = implementationMode,
            coordinateTransformer = coordinateTransformer,
            alignment = alignment,
            contentScale = contentScale
        )
    }
}

@Composable
fun CameraView(
    cameraManager: CameraManager,
    modifier: Modifier = Modifier,
    implementationMode: ImplementationMode = ImplementationMode.EXTERNAL,
    coordinateTransformer: MutableCoordinateTransformer? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        cameraManager.init(context, lifecycleOwner)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.stop()
        }
    }

    CameraView(
        surfaceRequests = cameraManager.surfaceRequests,
        modifier = modifier,
        implementationMode = implementationMode,
        coordinateTransformer = coordinateTransformer,
        alignment = alignment,
        contentScale = contentScale
    )
}
