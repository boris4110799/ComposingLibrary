package tw.boris4110799.composing.library.ui.util

import androidx.compose.ui.draganddrop.DragAndDropEvent

interface DragAndDropManager {
    /**
     * Get the list of file paths from the drag and drop event.
     */
    fun getFileList(): List<String>
}

expect fun getDragAndDropManager(event: DragAndDropEvent): DragAndDropManager
