package plodsoft.mygvm.gui

import plodsoft.mygvm.gui.hexedit.HexEditor
import java.awt.BorderLayout
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.WindowConstants

class MemEditDialog(owner: JFrame, data: ByteArray, offset: Int, count: Int) : JDialog(owner) {
    init {
        modalityType = ModalityType.APPLICATION_MODAL

        layout = BorderLayout()

        add(HexEditor(this, data, offset, count))
        pack()

        title = "内存编辑"
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        setLocationRelativeTo(parent)
    }
}