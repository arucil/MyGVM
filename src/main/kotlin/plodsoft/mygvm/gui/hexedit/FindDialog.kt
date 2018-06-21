package plodsoft.mygvm.gui.hexedit

import java.awt.Color
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.nio.charset.Charset
import javax.swing.*

class FindDialog(owner: Window, private val contentArea: ContentArea) : JDialog(owner, "查找", ModalityType.MODELESS) {
    var bytes = ByteArray(0)
        private set

    private val textField: JTextField
    private val comboType: JComboBox<String>
    private val btnNext: JButton
    private val btnPrev: JButton
    private val label: JLabel

    init {
        val gl = GroupLayout(contentPane)

        layout = gl

        gl.autoCreateGaps = true
        gl.autoCreateContainerGaps = true

        textField = JTextField()
        comboType = JComboBox(arrayOf("十六进制", "字符串(GB2312)"))
        btnNext = JButton("查找下一个")
        btnPrev = JButton("查找上一个")
        label = JLabel()

        gl.setHorizontalGroup(
                gl.createSequentialGroup()
                        .addGroup(gl.createParallelGroup()
                                .addComponent(textField)
                                .addGroup(gl.createSequentialGroup()
                                        .addComponent(btnNext)
                                        .addComponent(btnPrev)))
                        .addGroup(gl.createParallelGroup()
                                .addComponent(comboType)
                                .addComponent(label)))
        gl.setVerticalGroup(
                gl.createSequentialGroup()
                        .addGroup(gl.createParallelGroup()
                                .addComponent(textField)
                                .addComponent(comboType))
                        .addGroup(gl.createParallelGroup()
                                .addComponent(btnNext)
                                .addComponent(btnPrev)
                                .addComponent(label)))

        pack()

        rootPane.defaultButton = btnNext

        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        setLocationRelativeTo(null)

        setup()

        textField.requestFocus()
    }

    private fun setup() {
        val findNext = ActionListener {
            if (!textField.text.isEmpty() && convertTextToBytes()) {
                contentArea.findBytes(bytes, true)
            }
        }

        textField.addActionListener(findNext)
        btnNext.addActionListener(findNext)

        val findPrev = ActionListener {
            if (!textField.text.isEmpty() && convertTextToBytes()) {
                contentArea.findBytes(bytes, false)
            }
        }

        btnPrev.addActionListener(findPrev)
    }

    fun showAndFocus() {
        isVisible = true
        textField.requestFocus()
    }

    /**
     * 返回是否转换成功
     */
    private fun convertTextToBytes(): Boolean {
        val text = textField.text

        when (comboType.selectedIndex) {
            0 -> { // 十六进制
                if (text.length % 2 != 0) {
                    label.foreground = Color.RED
                    label.text = "长度必须为偶数"
                    return false
                }

                bytes = text.chunked(2).map { Integer.parseInt(it, 16).toByte() }.toByteArray()
                return true
            }

            1 -> { // 字符串
                bytes = text.toByteArray(Charset.forName("gb2312"))
                return true
            }
        }
        return false
    }
}

