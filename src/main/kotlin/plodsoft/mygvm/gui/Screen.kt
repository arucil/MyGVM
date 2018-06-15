package plodsoft.mygvm.gui

import plodsoft.mygvm.memory.DefaultRamModel
import plodsoft.mygvm.screen.DefaultScreenModel
import plodsoft.mygvm.screen.ScreenModel
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.Point
import java.awt.image.*
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JPanel

class Screen(screenModel: DefaultScreenModel, bgColor: Int, fgColor: Int, pixelScale: Int) : JPanel() {
    private val bufImage: BufferedImage
    private val screenWidth: Int
    private val screenHeight: Int
    private val timerRender = Timer()
    private var timerTaskRender: TimerTask? = null

    val dataBuffer: DataBufferByte

    init {
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        screenWidth = ScreenModel.WIDTH * pixelScale
        screenHeight = ScreenModel.HEIGHT * pixelScale

        preferredSize = Dimension(screenWidth + insets.left + insets.right, screenHeight + insets.top + insets.bottom)

        val cm = IndexColorModel(1, 2, intArrayOf(bgColor, fgColor), 0, false, -1, DataBuffer.TYPE_BYTE)
        dataBuffer = DataBufferByte((screenModel.graphicsRam.backingRam as DefaultRamModel).data, ScreenModel.RAM_SIZE,
                screenModel.graphicsRam.startingAddress)
        val raster = WritableRaster.createPackedRaster(dataBuffer, ScreenModel.WIDTH, ScreenModel.HEIGHT, 1, Point(0, 0))
        bufImage = BufferedImage(cm, raster, cm.isAlphaPremultiplied, null)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        g.drawImage(bufImage, insets.left, insets.right, screenWidth, screenHeight, null)
    }

    /**
     * 开始实时刷新屏幕
     */
    fun startRendering() {
        val delay = 1000L / 60L
        assert(timerTaskRender === null)
        timerTaskRender = RenderScreenTask()
        timerRender.scheduleAtFixedRate(timerTaskRender, delay, delay)
    }

    /**
     * 停止实时刷新屏幕
     */
    fun stopRendering() {
        timerTaskRender?.cancel()
        timerTaskRender = null
    }

    private inner class RenderScreenTask : TimerTask() {
        override fun run() {
            EventQueue.invokeLater { repaint() }
        }
    }
}
