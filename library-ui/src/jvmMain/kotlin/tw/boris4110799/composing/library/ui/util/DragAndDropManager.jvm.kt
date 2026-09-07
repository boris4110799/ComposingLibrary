package tw.boris4110799.composing.library.ui.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import java.io.File
import java.net.URI

class JvmDragAndDropManager(private val event: DragAndDropEvent) : DragAndDropManager {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun getFileList(): List<String> {
        val dragData = event.dragData()

        if (dragData is DragData.FilesList) {
            return dragData.readFiles().map { File(URI(it)).canonicalPath }
        }

        return emptyList()
    }
}

actual fun getDragAndDropManager(event: DragAndDropEvent): DragAndDropManager =
    JvmDragAndDropManager(event)
