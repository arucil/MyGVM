package plodsoft.mygvm.gui.hexedit

import java.awt.*
import java.awt.event.*
import java.util.*
import javax.swing.*
import javax.swing.undo.AbstractUndoableEdit
import javax.swing.undo.UndoManager
import kotlin.math.max
import kotlin.math.min

/**
 * 十六进制数据编辑器
 */
class ContentArea(val data: ByteArray,
                  val offset: Int = 0,
                  val count: Int = data.size)
    : JComponent(), Scrollable, KeyListener {

    companion object {
        val TextFont = Font("Monospaced", Font.PLAIN, 12)

        private const val ROW_BYTES = 16

        const val BORDER_LEFT = 2
        const val BORDER_TOP = 2
        const val BORDER_RIGHT = 2
        const val BORDER_BOTTOM = 2
    }

    private val maxRows = (count + (ROW_BYTES - 1)) / ROW_BYTES // 总共的行数

    var charAscent: Int = 0
        private set
    var charWidth: Int = 0
        private set
    private var charHeight: Int = 0
    var lineHeight: Int = 0
        private set

    var leftMargin: Int = 0
        private set
    var topMargin: Int = 0
        private set

    private var firstDisplayedRow: Int = 0
    private var lastDisplayedRow: Int = 0

    // 光标的绝对位置
    var caretRow = 0
        private set
    var caretCol = 0
        private set
    var caretAddress = offset // 光标所指向的data地址
        private set


    private val caretListeners = ArrayList<CaretChangeListener>()
    private val findListeners = ArrayList<FindStatusListener>()

    private val undoMan = UndoManager()



    init {
        border = BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT)

        val fm = getFontMetrics(TextFont)
        charAscent = fm.ascent
        charWidth = fm.charWidth('1')
        charHeight = fm.height
        lineHeight = charHeight + fm.leading
        leftMargin = BORDER_LEFT + 5 * charWidth
        topMargin = BORDER_TOP

        MouseListener().let {
            addMouseListener(it)
            addMouseMotionListener(it)
        }

        isFocusable = true
        addKeyListener(this)
    }


    override fun getPreferredSize() =
        Dimension(BORDER_LEFT + BORDER_RIGHT + 70 * charWidth,
                max(maxRows, 16) * lineHeight + BORDER_TOP + BORDER_BOTTOM)

    override fun getScrollableTracksViewportWidth(): Boolean = true

    override fun getScrollableTracksViewportHeight(): Boolean = false

    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
        if (orientation == SwingConstants.VERTICAL) {
            var result = visibleRect.height - lineHeight
            result += getScrollableUnitIncrement(Rectangle(visibleRect.x,
                    result + visibleRect.y,
                    visibleRect.width,
                    visibleRect.height), SwingConstants.VERTICAL, direction)
            return result
        }

        return 0
    }

    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
        if (orientation == SwingConstants.VERTICAL) {
            var result =
                    if (direction < 0)
                        (visibleRect.y - topMargin) % lineHeight
                    else
                        lineHeight - (visibleRect.y - topMargin) % lineHeight

            if (result <= 0) result = lineHeight
            return result
        }

        return 0
    }

    override fun getPreferredScrollableViewportSize(): Dimension {
        val d = preferredSize
        return Dimension(d.width, min(d.height, 24 * lineHeight + BORDER_TOP))
    }


    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g as Graphics2D

        g2d.color = background
        g2d.fill(g2d.clip)

        g2d.font = TextFont
        g2d.color = Color.BLACK

        val bounds = g2d.clipBounds

        firstDisplayedRow = (bounds.y - topMargin) / lineHeight
        lastDisplayedRow = (bounds.y + bounds.height - 1 - topMargin) / lineHeight
        if (lastDisplayedRow >= maxRows) {
            lastDisplayedRow = maxRows - 1
        }

        var index = firstDisplayedRow * ROW_BYTES
        var y = topMargin + charAscent + firstDisplayedRow * lineHeight
        for (row in firstDisplayedRow..lastDisplayedRow) {
            g2d.drawString(String.format("%04X", offset + index), BORDER_LEFT, y)

            val hex = StringBuilder()
            val char = StringBuilder()

            for (i in 0 until getMaxColumns(row)) {
                data[offset + index + i].toInt().let {
                    hex.append(String.format("%02X ", it and 0xff))
                    char.append(if (it in 0x20..0x7e) it.toChar() else '.')
                }
            }

            g2d.drawString(hex.toString(), leftMargin, y)
            g2d.drawString(char.toString(), leftMargin + 48 * charWidth, y)

            index += ROW_BYTES

            y += lineHeight
        }

        // 光标
        g2d.color = Color.BLACK
        g2d.setXORMode(Color.WHITE)
        (leftMargin + caretCol * charWidth).let { x ->
            g2d.fillRect(x, topMargin + caretRow * lineHeight, charWidth, charHeight)
        }
        (leftMargin + (48 + caretCol / 3) * charWidth).let { x ->
            g2d.fillRect(x, topMargin + caretRow * lineHeight, charWidth, charHeight)
        }

        // 分割线
        g2d.color = Color.GRAY
        (leftMargin - charWidth / 2).let { x ->
            g2d.drawLine(x, topMargin, x, height - insets.bottom)
        }
        (leftMargin + 47 * charWidth + charWidth / 2).let { x ->
            g2d.drawLine(x, topMargin, x, height - insets.bottom)
        }

        g2d.color = Color.LIGHT_GRAY
        (leftMargin + 11 * charWidth + charWidth / 2).let { x ->
            g2d.drawLine(x, topMargin, x, height - insets.bottom)
        }
        (leftMargin + 23 * charWidth + charWidth / 2).let { x ->
            g2d.drawLine(x, topMargin, x, height - insets.bottom)
        }
        (leftMargin + 35 * charWidth + charWidth / 2).let { x ->
            g2d.drawLine(x, topMargin, x, height - insets.bottom)
        }
    }

    fun addCaretChangeListener(l: CaretChangeListener) {
        caretListeners -= l
        caretListeners += l
    }

    fun removeCaretChangeListener(l: CaretChangeListener) {
        caretListeners -= l
    }

    private fun fireCaretChangeEvent() {
        val e = CaretChangeEvent(this)
        for (l in caretListeners) {
            l.caretMoved(e)
        }
    }

    fun undo() {
        if (undoMan.canUndo()) {
            undoMan.undo()
        }
    }

    fun redo() {
        if (undoMan.canRedo()) {
            undoMan.redo()
        }
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_UP -> {
                val row = caretRow - 1
                if (row >= 0) {
                    setCaretPosition(row, caretCol)
                }
            }
            KeyEvent.VK_DOWN -> {
                var row = caretRow + 1
                var col = caretCol
                if (row >= maxRows - 1) {
                    row = maxRows - 1
                    val cols = count % ROW_BYTES * 3 - 1
                    if (col >= cols) {
                        col = cols - 1
                    }
                }
                setCaretPosition(row, col)
            }
            KeyEvent.VK_LEFT -> {
                var col = caretCol - 1
                if (col >= 0) {
                    if (col % 3 == 2) {
                        --col
                    }
                    setCaretPosition(caretRow, col)
                } else if (caretRow > 0) {
                    setCaretPosition(caretRow - 1, ROW_BYTES * 3 - 2)
                }
            }
            KeyEvent.VK_RIGHT -> moveCaretForward()
            else -> {
                if (!e.isControlDown && !e.isAltDown) {
                    val x = Character.digit(e.keyChar, 16)
                    if (x >= 0) {
                        editContent(x)
                        moveCaretForward()
                    }
                }
            }
        }
    }

    private inline fun getMaxColumns(row: Int): Int {
        return if (row == maxRows - 1) {
            val x = count % ROW_BYTES
            if (x == 0) {
                ROW_BYTES
            } else {
                x
            }
        } else {
            ROW_BYTES
        }
    }

    /**
     * @param x 0~15
     */
    private fun editContent(x: Int) {
        if (count > 0) {
            val oldValue = data[caretAddress].toInt()
            val newValue = if (caretCol % 3 == 1) { // 低位
                oldValue and 0xf0 or x
            } else { // 高位
                x shl 4 or (oldValue and 0x0f)
            }.toByte()

            data[caretAddress] = newValue

            undoMan.addEdit(ContentEdit(caretRow, caretCol, caretAddress, oldValue, newValue))
        }
    }

    private fun moveCaretForward() {
        var col = caretCol + 1
        val cols = getMaxColumns(caretRow) * 3 - 2
        if (caretCol < cols) {
            if (col % 3 == 2) {
                ++col
            }
            setCaretPosition(caretRow, col)
        } else if (caretRow < maxRows - 1) {
            setCaretPosition(caretRow + 1, 0)
        }
    }

    fun gotoAddress(addr: Int) {
        if (addr !in offset until (offset + count)) {
            return
        }

        setCaretPosition((addr - offset) / ROW_BYTES, (addr - offset) % ROW_BYTES * 3)
    }

    /**
     * row和col必须是合法的位置
     */
    fun setCaretPosition(row: Int, col: Int) {
        if (count > 0) {
            val changed = row != caretRow || col != caretCol
            caretRow = row
            caretCol = col
            caretAddress = offset + row * ROW_BYTES + col / 3
            if (changed) {
                fireCaretChangeEvent()
                repaint()
            }
        }
    }

    fun findBytes(bytes: ByteArray, next: Boolean) {
        if (next) {
            val start = caretAddress + 1
            if (count + offset - start >= bytes.size) {
                outer@ for (i in start..(offset + count - bytes.size)) {
                    for (j in 0 until bytes.size) {
                        if (data[i + j] != bytes[j]) {
                            continue@outer
                        }
                    }
                    gotoAddress(i)
                    fireFindStatusEvent(FindStatusEvent(this, true))
                    return
                }
            }
            fireFindStatusEvent(FindStatusEvent(this, false))
        } else {
            var start = caretAddress - 1
            if (offset + count - start < bytes.size) {
                start = offset + count - bytes.size
            }

            if (start >= offset) {
                outer@ for (i in start downTo offset) {
                    for (j in 0 until bytes.size) {
                        if (data[i + j] != bytes[j]) {
                            continue@outer
                        }
                    }
                    gotoAddress(i)
                    fireFindStatusEvent(FindStatusEvent(this, true))
                    return
                }
            }
            fireFindStatusEvent(FindStatusEvent(this, false))
        }
    }

    fun addFindStatusListener(l: FindStatusListener) {
        findListeners -= l
        findListeners += l
    }

    fun removeFindStatusListener(l: FindStatusListener) {
        findListeners -= l
    }

    private fun fireFindStatusEvent(e: FindStatusEvent) {
        findListeners.forEach {
            it.findStatusChanged(e)
        }
    }

    override fun keyReleased(e: KeyEvent) { }

    override fun keyTyped(e: KeyEvent) { }

    private inner class MouseListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            val row = (e.y - topMargin) / lineHeight
            var col = (e.x - leftMargin) / charWidth
            if (row <= lastDisplayedRow) {
                val cols = getMaxColumns(row) * 3 - 1
                if (col % 3 == 2) {
                    --col
                }
                if (col in 0..cols) {
                    setCaretPosition(row, col)
                }
            }
        }
    }

    private inner class ContentEdit(private val caretRow: Int,
                                    private val caretCol: Int,
                                    private val caretAddr: Int,
                                    private val oldValue: Int,
                                    private val newValue: Byte)
        : AbstractUndoableEdit() {

        override fun undo() {
            super.undo()

            data[caretAddr] = oldValue.toByte()
            setCaretPosition(caretRow, caretCol)
            repaint()
        }

        override fun redo() {
            super.redo()

            data[caretAddr] = newValue
            setCaretPosition(caretRow, caretCol)
            repaint()
        }
    }

}

