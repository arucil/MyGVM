package plodsoft.mygvm.model

import plodsoft.mygvm.util.Rect


interface ScreenModel {
    companion object {
        const val WIDTH = 160
        const val HEIGHT = 80
        const val BITS_PER_PIXEL = 1
    }

    val dirtyRegion: Rect

    fun clear()
}