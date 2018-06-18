package plodsoft.mygvm.gui.hexedit

import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class HexEditor(window: Window,
                private val data: ByteArray,
                private val offset: Int = 0,
                private val count: Int = data.size)
    : JComponent() {

    private val scrollPane: JScrollPane
    private val contentArea: ContentArea
    private val contentDetail: ContentDetailPanel

    init {
        layout = BorderLayout()

        contentArea = ContentArea(window, data, offset, count)
        scrollPane = JScrollPane(contentArea)

        add(scrollPane)

        val size = Dimension(contentArea.preferredSize.width,
                contentArea.lineHeight + ContentArea.BORDER_TOP + ContentArea.BORDER_BOTTOM)

        val topPanel = ContentOffsetPanel(contentArea.leftMargin + scrollPane.insets.left, contentArea.charAscent)
        topPanel.preferredSize = size
        add(topPanel, BorderLayout.NORTH)

        contentDetail = ContentDetailPanel()
        contentDetail.preferredSize = size
        add(contentDetail, BorderLayout.SOUTH)

        contentArea.addCaretChangeListener(object : CaretChangeListener {
            override fun caretMoved(e: CaretChangeEvent) {
                scrollCaretToViewport()

                contentDetail.repaint()
            }
        })

        contentArea.addFindStatusListener(object : FindStatusListener {
            override fun findStatusChanged(e: FindStatusEvent) {
                contentDetail.findStatus = if (e.isFound) "" else "没有找到"
                contentDetail.repaint()
            }
        })

        isFocusable = true
        addKeyListener(contentArea)
    }

    private fun scrollCaretToViewport() {
        val y = contentArea.caretRow * contentArea.lineHeight + contentArea.topMargin
        val rect = scrollPane.viewport.viewRect

        if (y !in rect.y until (rect.y + rect.height)) {
            scrollPane.viewport.viewPosition = Point(0, y)
        }
    }

    private class ContentOffsetPanel(private val leftMargin: Int, private val textY: Int) : JPanel() {
        init {
            border = BorderFactory.createEmptyBorder(ContentArea.BORDER_TOP, 0, ContentArea.BORDER_BOTTOM, 0)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)

            g.font = ContentArea.TextFont
            g.color = Color.BLACK

            g.drawString("00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F", leftMargin, ContentArea.BORDER_TOP + textY)
        }
    }

    private inner class ContentDetailPanel : JPanel() {
        var findStatus: String = ""

        init {
            border = BorderFactory.createEmptyBorder(ContentArea.BORDER_TOP, ContentArea.BORDER_LEFT, ContentArea.BORDER_BOTTOM, ContentArea.BORDER_RIGHT)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.drawString(String.format("地址: 0x%04X / %d", contentArea.caretAddress, contentArea.caretAddress),
                    ContentArea.BORDER_LEFT, ContentArea.BORDER_TOP + contentArea.charAscent)

            if (!findStatus.isEmpty()) {
                val fm = g.fontMetrics
                val rect = fm.getStringBounds(findStatus, g)
                g.color = Color.RED
                g.drawString(findStatus, width - insets.right - rect.width.toInt(), ContentArea.BORDER_TOP + contentArea.charAscent)
            }
        }
    }
}
