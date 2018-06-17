package plodsoft.mygvm.gui.hexedit

import java.awt.AWTEvent

class CaretChangeEvent(source: ContentArea) : AWTEvent(source, CARET_CHANGE_EVENT) {

    companion object {
        const val CARET_CHANGE_EVENT = AWTEvent.RESERVED_ID_MAX + 1
    }

}