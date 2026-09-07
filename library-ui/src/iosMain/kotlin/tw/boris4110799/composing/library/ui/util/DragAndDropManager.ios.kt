package tw.boris4110799.composing.library.ui.util

import androidx.compose.ui.draganddrop.DragAndDropEvent

class IosDragAndDropManager(private val event: DragAndDropEvent) : DragAndDropManager {
    override fun getFileList(): List<String> = emptyList()
}

actual fun getDragAndDropManager(event: DragAndDropEvent): DragAndDropManager =
    IosDragAndDropManager(event)
