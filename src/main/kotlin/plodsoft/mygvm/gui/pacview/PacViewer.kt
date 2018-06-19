package plodsoft.mygvm.gui.pacview

import plodsoft.mygvm.runtime.Runtime
import plodsoft.mygvm.util.readAll
import plodsoft.mygvm.util.swing.dsl.*
import java.awt.Window
import java.awt.event.WindowEvent
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.text.DecimalFormat
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.table.DefaultTableColumnModel
import javax.swing.table.DefaultTableModel
import kotlin.math.floor
import kotlin.math.log

class PacViewer(owner: Window) : JDialog(owner, "PAC文件提取", ModalityType.APPLICATION_MODAL) {
    companion object {
        fun formatFileSize(size: Int): String {
            val units = arrayOf("B", "kB", "MB", "GB", "TB")
            val digitGroups = floor(log(size.toDouble(), 1024.0))
            return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups)) + " " + units[digitGroups.toInt()]
        }
    }

    private lateinit var table: JTable
    private lateinit var tableModel: DefaultTableModel

    private lateinit var menuExtractAll: JMenuItem
    private lateinit var menuExtractSel: JMenuItem

    private val openFileChooser = JFileChooser(Runtime.FS_ROOT)
    private val saveFileChooser = JFileChooser(Runtime.FS_ROOT)

    private val fileItems = ArrayList<FileItem>()

    init {
        initUI()
        initMenu()

        openFileChooser.addChoosableFileFilter(FileNameExtensionFilter("PAC文件", "pac"))

        saveFileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

        setSize(350, 450)

        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        setLocationRelativeTo(null)
    }

    private fun initUI() {

        tableModel = object : DefaultTableModel(arrayOf("文件名称", "大小"), 0) {
            override fun isCellEditable(row: Int, column: Int) = false
        }
        table = JTable(tableModel, DefaultTableColumnModel())
        table.createDefaultColumnsFromModel()

        table.fillsViewportHeight = true

        table.selectionModel.addListSelectionListener {
            menuExtractSel.isEnabled = !table.selectionModel.isSelectionEmpty
        }

        add(JScrollPane(table))

        pack()
    }

    private fun initMenu() {
        menuBar {
            menu("文件") {
                item("打开", accelerator = KeyStroke.getKeyStroke("control O")) {
                    addActionListener {
                        loadFile()
                    }
                }
                separator()
                item("退出", accelerator = KeyStroke.getKeyStroke("alt F4")) {
                    addActionListener {
                        this@PacViewer.dispatchEvent(WindowEvent(this@PacViewer, WindowEvent.WINDOW_CLOSING))
                    }
                }
            }
        }

        table.componentPopupMenu =
            popupMenu {
                item("提取所有文件") {
                    menuExtractAll = this
                    isEnabled = false
                    addActionListener {
                        extractFiles()
                    }
                }
                item("提取选中的文件") {
                    menuExtractSel = this
                    isEnabled = false
                    addActionListener {
                        extractFiles(table.selectedRows)
                    }
                }
            }
    }

    private fun loadFile() {
        if (openFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            val file = openFileChooser.selectedFile
            try {
                val bytes = file.readAll()
                if (bytes.size < 18 || "PAC " != String(bytes, 0, 4)) {
                    JOptionPane.showMessageDialog(this, "不是有效的PAC文件!", title, JOptionPane.ERROR_MESSAGE)
                    return
                }

                val totalItems = bytes[16].i() or (bytes[17].i() shl 8)

                tableModel.setNumRows(0)

                var offset = 18
                for (i in 0 until totalItems) {
                    val size = bytes[offset].i() or (bytes[offset + 1].i() shl 8) or
                            (bytes[offset + 2].i() shl 16) or (bytes[offset + 3].i() shl 24)
                    val name = String(bytes, offset + 4, 60, Charset.forName("gb2312")).substringBefore('\u0000')
                    offset += 64
                    tableModel.addRow(arrayOf(name, formatFileSize(size)))
                    fileItems.add(FileItem(name, bytes.copyOfRange(offset, offset + size)))
                    offset += size
                }

                menuExtractAll.isEnabled = totalItems > 0

                revalidate()
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(this, "文件读取失败: $e", title, JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    private inline fun Byte.i() = toInt() and 0xff

    private fun extractFiles(selectedIndices: IntArray = (0 until fileItems.size).toList().toIntArray()) {
        if (saveFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            val root = saveFileChooser.selectedFile.absolutePath
            var replaceAll = false
            var extracted = false

            outer@for (index in selectedIndices) {
                val item = fileItems[index]
                val file = File(root + "/" + item.name)
                Files.createDirectories(file.parentFile.toPath())
                if (file.exists() && !replaceAll) {
                    when (JOptionPane.showOptionDialog(this, "文件 “${item.name}“ 已存在, 是否覆盖?", title, 0,
                            JOptionPane.QUESTION_MESSAGE, null, arrayOf("覆盖", "覆盖所有", "跳过该文件", "跳过所有"), null)) {
                        JOptionPane.CLOSED_OPTION -> continue@outer
                        0 -> {}
                        1 -> replaceAll = true
                        2 -> continue@outer
                        3 -> break@outer
                    }
                }
                BufferedOutputStream(FileOutputStream(file)).use {
                    it.write(item.data)
                }
                extracted = true
            }

            if (extracted) {
                JOptionPane.showMessageDialog(this, "提取完毕", title, JOptionPane.INFORMATION_MESSAGE)
            }
        }
    }

    private class FileItem(val name: String, val data: ByteArray)
}