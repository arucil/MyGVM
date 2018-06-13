package plodsoft.mygvm

import plodsoft.mygvm.gui.Window
import java.awt.EventQueue

fun main(args: Array<String>) {
    EventQueue.invokeLater {
        Window().isVisible = true
    }
}