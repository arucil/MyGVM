package plodsoft.mygvm.gui

import plodsoft.mygvm.Config
import plodsoft.mygvm.keyboard.DefaultKeyboardModel
import plodsoft.mygvm.runtime.Runtime
import plodsoft.mygvm.runtime.VMException
import plodsoft.mygvm.screen.DefaultScreenModel
import util.swing.dsl.*
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.*
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class Window : JFrame(APP_NAME) {
    companion object {
        const val APP_NAME = "MyGVM"

        /**
         * Swing键值映射到lava键值
         */
        @JvmStatic
        private val KeyCodeMapping = HashMap<Int, Int>().apply {
            fun HashMap<Int, Int>.put(key: Int, value: Char) {
                put(key, value.toInt())
            }

            put(KeyEvent.VK_1, 'b')
            put(KeyEvent.VK_2, 'n')
            put(KeyEvent.VK_3, 'm')
            put(KeyEvent.VK_4, 'g')
            put(KeyEvent.VK_5, 'h')
            put(KeyEvent.VK_6, 'j')
            put(KeyEvent.VK_7, 't')
            put(KeyEvent.VK_8, 'y')
            put(KeyEvent.VK_9, 'u')

            put(KeyEvent.VK_NUMPAD1, 'b')
            put(KeyEvent.VK_NUMPAD2, 'n')
            put(KeyEvent.VK_NUMPAD3, 'm')
            put(KeyEvent.VK_NUMPAD4, 'g')
            put(KeyEvent.VK_NUMPAD5, 'h')
            put(KeyEvent.VK_NUMPAD6, 'j')
            put(KeyEvent.VK_NUMPAD7, 't')
            put(KeyEvent.VK_NUMPAD8, 'y')
            put(KeyEvent.VK_NUMPAD9, 'u')

            put(KeyEvent.VK_A, 'a')
            put(KeyEvent.VK_B, 'b')
            put(KeyEvent.VK_C, 'c')
            put(KeyEvent.VK_D, 'd')
            put(KeyEvent.VK_E, 'e')
            put(KeyEvent.VK_F, 'f')
            put(KeyEvent.VK_G, 'g')
            put(KeyEvent.VK_H, 'h')
            put(KeyEvent.VK_I, 'i')
            put(KeyEvent.VK_J, 'j')
            put(KeyEvent.VK_K, 'k')
            put(KeyEvent.VK_L, 'l')
            put(KeyEvent.VK_M, 'm')
            put(KeyEvent.VK_N, 'n')
            put(KeyEvent.VK_O, 'o')
            put(KeyEvent.VK_P, 'p')
            put(KeyEvent.VK_Q, 'q')
            put(KeyEvent.VK_R, 'r')
            put(KeyEvent.VK_S, 's')
            put(KeyEvent.VK_T, 't')
            put(KeyEvent.VK_U, 'u')
            put(KeyEvent.VK_V, 'v')
            put(KeyEvent.VK_W, 'w')
            put(KeyEvent.VK_X, 'x')
            put(KeyEvent.VK_Y, 'y')
            put(KeyEvent.VK_Z, 'z')

            put(KeyEvent.VK_PERIOD, '.')
            put(KeyEvent.VK_SPACE, ' ')
            put(KeyEvent.VK_0, '0')

            put(KeyEvent.VK_F1, 28)
            put(KeyEvent.VK_F2, 29)
            put(KeyEvent.VK_F3, 30)
            put(KeyEvent.VK_F4, 31)

            put(KeyEvent.VK_ENTER, 13)
            put(KeyEvent.VK_PAGE_UP, 19)
            put(KeyEvent.VK_PAGE_DOWN, 14)
            put(KeyEvent.VK_ESCAPE, 27)
            put(KeyEvent.VK_SHIFT, 26)
            put(KeyEvent.VK_UP, 20)
            put(KeyEvent.VK_DOWN, 21)
            put(KeyEvent.VK_LEFT, 23)
            put(KeyEvent.VK_RIGHT, 22)
            put(KeyEvent.VK_CONTROL, 25) // Ctrl -> 帮助
            put(KeyEvent.VK_BACK_QUOTE, 18) // ` -> CAPS
        }

        @JvmStatic
        private val BmpHeader = byteArrayOf(
                0x42, 0x4D, 0x7E, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x00, 0x00, 0x00, 0x28, 0x00,
                0x00, 0x00, 0xA0.toByte(), 0x00, 0x00, 0x00, 0xb0.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0x01, 0x00, 0x01, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x40, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00)
    }

    enum class Status {
        Initial, Loaded, Running, Paused
    }

    private var mnuLoad: JMenuItem? = null
    private var mnuRun: JMenuItem? = null
    private var mnuStop: JMenuItem? = null

    private var labelFile: JLabel? = null
    private var labelStatus: JLabel? = null

    private val fileChooser = JFileChooser(Runtime.FS_ROOT)

    private var screen: Screen? = null

    private val runtime = Runtime.create()

    private var vmThread: VMThread? = null

    private var cycleSteps = Config.steps
    private var isDelayEnabled = Config.isDelayEnabled
    private var delay = Config.delay

    private var status = Status.Initial

    private val pressedKeys = HashSet<Int>()

    init {
        fileChooser.addChoosableFileFilter(FileNameExtensionFilter("GVmaker应用程序", "lav"))

        setupMenu()
        setupKeyboard()

        screen = Screen(runtime.screenModel as DefaultScreenModel, Config.backgroundColor, Config.foregroundColor, Config.pixelScale)
        add(screen)

        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.border = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 3, 3, 3))

        labelFile = JLabel("a")
        panel.add(labelFile)

        panel.preferredSize = Dimension(screen!!.preferredSize.width, labelFile!!.preferredSize.height + panel.insets.top + panel.insets.bottom)
        labelFile!!.text = ""

        labelStatus = JLabel("")
        labelStatus!!.border = BorderFactory.createEmptyBorder(0, 5, 0, 0)
        panel.add(labelStatus, BorderLayout.EAST)

        add(panel, BorderLayout.SOUTH)

        pack()

        updateStatus(Status.Initial)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                stop()
            }
        })

        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        isResizable = false
    }

    private fun setupMenu() {
        menuBar {
            menu("文件") {
                item("打开", accelerator = KeyStroke.getKeyStroke("control O")) {
                    mnuLoad = this
                    addActionListener { loadFile() }
                }
                item("运行", accelerator = KeyStroke.getKeyStroke("F5")) {
                    mnuRun = this
                    addActionListener {
                        when (status) {
                            Status.Loaded -> start()
                            Status.Running -> pause()
                            Status.Paused -> resume()
                        }
                    }
                }
                item("停止", accelerator = KeyStroke.getKeyStroke("F6")) {
                    mnuStop = this
                    addActionListener {
                        stop()
                    }
                }
                separator()
                item("退出", accelerator = KeyStroke.getKeyStroke("alt F4")) {
                    addActionListener {
                        this@Window.dispatchEvent(WindowEvent(this@Window, WindowEvent.WINDOW_CLOSING))
                    }
                }
            }

            menu("工具") {
                checkBoxItem("减速运行", isSelected = isDelayEnabled, accelerator = KeyStroke.getKeyStroke("F11")) {
                    addItemListener {
                        isDelayEnabled = this.isSelected
                    }
                }
                separator()
                item("截图", accelerator = KeyStroke.getKeyStroke("F9")) {
                    addActionListener {
                        takeScreenshot()
                    }
                }
            }

            menu("帮助") {
                item("内容") {
                    addActionListener {
                        JOptionPane.showMessageDialog(this,
                                """
                                    按键：
                                    [`] 对应 CAPS
                                    [Ctrl] 对应 帮助
                                """.trimIndent(), APP_NAME, JOptionPane.PLAIN_MESSAGE)
                    }
                }
                item("关于") {
                    addActionListener {
                        JOptionPane.showMessageDialog(this,
                                """
                                    Github: arucil
                                    参考了Eastsun的JGVM
                                """.trimIndent(), APP_NAME, JOptionPane.PLAIN_MESSAGE)
                    }
                }
            }
        }
    }

    private fun setupKeyboard() {
        val keyboardModel = runtime.keyboardModel as DefaultKeyboardModel
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher {
            if (status == Status.Running || status == Status.Paused) {
                when (it.id) {
                    KeyEvent.KEY_PRESSED -> {
                        KeyCodeMapping[it.keyCode]?.let {
                            if (pressedKeys.add(it)) {
                                keyboardModel.keyPressed(it)
                            }
                        }
                    }
                    KeyEvent.KEY_RELEASED -> {
                        KeyCodeMapping[it.keyCode]?.let {
                            pressedKeys.remove(it)
                            keyboardModel.keyReleased(it)
                        }
                    }
                }
            }
            false
        }
    }

    private fun updateStatus(newStatus: Status) {
        status = newStatus
        when (newStatus) {
            Status.Initial -> {
                mnuLoad!!.isEnabled = true
                mnuRun!!.isEnabled = false
                mnuStop!!.isEnabled = false
                mnuRun!!.text = "运行"
                labelStatus!!.text = "准备就绪"
            }
            Status.Loaded -> {
                mnuLoad!!.isEnabled = true
                mnuRun!!.isEnabled = true
                mnuStop!!.isEnabled = false
                mnuRun!!.text = "运行"
                labelStatus!!.text = "文件已加载"
            }
            Status.Running -> {
                mnuLoad!!.isEnabled = false
                mnuRun!!.isEnabled = true
                mnuStop!!.isEnabled = true
                mnuRun!!.text = "暂停"
                labelStatus!!.text = "正在运行"
            }
            Status.Paused -> {
                mnuLoad!!.isEnabled = false
                mnuRun!!.isEnabled = true
                mnuStop!!.isEnabled = true
                mnuRun!!.text = "继续"
                labelStatus!!.text = "已暂停"
            }
        }
    }

    private fun loadFile() {
        val ret = fileChooser.showOpenDialog(this)
        if (ret == JFileChooser.APPROVE_OPTION) {
            BufferedInputStream(FileInputStream(fileChooser.selectedFile)).use {
                try {
                    runtime.loadCode(it)
                    labelFile!!.text = fileChooser.selectedFile.nameWithoutExtension
                    updateStatus(Status.Loaded)
                } catch (e: IOException) {
                    JOptionPane.showMessageDialog(this, "文件 ${fileChooser.selectedFile.name} 加载失败:\n$e", APP_NAME, JOptionPane.ERROR_MESSAGE)
                } catch (e: IllegalArgumentException) {
                    JOptionPane.showMessageDialog(this, e.message, APP_NAME, JOptionPane.ERROR_MESSAGE)
                }
            }
        }
    }

    private fun start() {
        pressedKeys.clear()

        runtime.prepare()

        vmThread = VMThread()
        vmThread!!.start()

        screen!!.startRendering()

        updateStatus(Status.Running)
    }

    private fun pause() {
        vmThread!!.isPaused = true
        screen!!.stopRendering()

        updateStatus(Status.Paused)
    }

    private fun resume() {
        vmThread!!.isPaused = false
        screen!!.startRendering()

        updateStatus(Status.Running)
    }

    private fun stop() {
        screen!!.stopRendering()

        if (vmThread !== null && vmThread!!.isAlive) {
            vmThread!!.interrupt()
            try {
                vmThread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private var shotNum = 0

    private fun takeScreenshot() {
        var file = File("Screenshot_${++shotNum}.bmp")
        while (file.exists()) {
            file = File("Screenshot_${++shotNum}.bmp")
        }

        BufferedOutputStream(FileOutputStream(file)).use {
            it.write(BmpHeader)
            screen!!.dataBuffer.let { db ->
                it.write(db.data, db.offset, db.size)
            }
        }
    }

    private inner class VMThread : Thread() {
        private val lock = Object()
        var isPaused = false
            get() = synchronized(lock) { field }
            set(value) {
                synchronized(lock) {
                    if (field != value) {
                        field = value
                        lock.notify()
                    }
                }
            }

        override fun run() {
            try {
                var steps = 0
                while (!isInterrupted && !runtime.runOneStep()) {
                    while (isPaused) {
                        synchronized(lock) {
                            lock.wait()
                        }
                    }

                    if (isDelayEnabled) {
                        if (++steps >= cycleSteps) {
                            steps = 0
                            Thread.sleep(0, delay)
                        }
                    }
                }
            } catch (e: InterruptedException) {
            } catch (e: VMException) {
                EventQueue.invokeLater {
                    JOptionPane.showMessageDialog(this@Window, "运行时错误:\n${e.message}")
                }
            } catch (e: Exception) {
                val stackTrace = with (ByteArrayOutputStream()) {
                    PrintStream(this).use {
                        e.printStackTrace(it)
                    }
                    String(toByteArray())
                }
                EventQueue.invokeLater {
                    JOptionPane.showMessageDialog(this@Window, "未预料的错误:\n$stackTrace")
                }
            } finally {
                runtime.cleanUp()
                EventQueue.invokeLater {
                    updateStatus(Status.Loaded)
                }
            }
        }
    }
}