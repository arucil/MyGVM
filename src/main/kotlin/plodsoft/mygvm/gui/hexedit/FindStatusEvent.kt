package plodsoft.mygvm.gui.hexedit

import java.awt.AWTEvent

class FindStatusEvent(source: ContentArea, val isFound: Boolean) : AWTEvent(source, FIND_STATUS_EVENT) {
    companion object {
        const val FIND_STATUS_EVENT = AWTEvent.RESERVED_ID_MAX + 2
    }
}
