package plodsoft.mygvm

import plodsoft.mygvm.gui.Window
import java.awt.EventQueue
import javax.swing.JOptionPane

fun main(args: Array<String>) {
    EventQueue.invokeLater {
        if (Config.configLoadingException !== null) {
            JOptionPane.showMessageDialog(null, "配置文件加载失败：\n${Config.configLoadingException}", Window.APP_NAME, JOptionPane.ERROR_MESSAGE)
        } else {
            Window().isVisible = true
        }
    }
}