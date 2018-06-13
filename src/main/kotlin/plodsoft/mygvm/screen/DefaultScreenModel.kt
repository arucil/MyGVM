package plodsoft.mygvm.screen

import plodsoft.mygvm.memory.DefaultRamModel
import plodsoft.mygvm.memory.RamSegment
import plodsoft.mygvm.memory.ReadableMemory
import plodsoft.mygvm.memory.WritableMemory
import plodsoft.mygvm.text.DefaultTextModel
import plodsoft.mygvm.text.Font
import plodsoft.mygvm.text.TextModel
import plodsoft.mygvm.util.Rect
import kotlin.math.absoluteValue

class DefaultScreenModel(private val graphicsRam: RamSegment, private val bufferRam: RamSegment) : ScreenModel {
    companion object {
        private val BIT_MASK = intArrayOf(0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01)

        private val ByteMirrorTable = byteArrayOf(
                0x00.toByte(), 0x80.toByte(), 0x40.toByte(), 0xc0.toByte(), 0x20.toByte(), 0xa0.toByte(), 0x60.toByte(), 0xe0.toByte(),
                0x10.toByte(), 0x90.toByte(), 0x50.toByte(), 0xd0.toByte(), 0x30.toByte(), 0xb0.toByte(), 0x70.toByte(), 0xf0.toByte(),
                0x08.toByte(), 0x88.toByte(), 0x48.toByte(), 0xc8.toByte(), 0x28.toByte(), 0xa8.toByte(), 0x68.toByte(), 0xe8.toByte(),
                0x18.toByte(), 0x98.toByte(), 0x58.toByte(), 0xd8.toByte(), 0x38.toByte(), 0xb8.toByte(), 0x78.toByte(), 0xf8.toByte(),
                0x04.toByte(), 0x84.toByte(), 0x44.toByte(), 0xc4.toByte(), 0x24.toByte(), 0xa4.toByte(), 0x64.toByte(), 0xe4.toByte(),
                0x14.toByte(), 0x94.toByte(), 0x54.toByte(), 0xd4.toByte(), 0x34.toByte(), 0xb4.toByte(), 0x74.toByte(), 0xf4.toByte(),
                0x0c.toByte(), 0x8c.toByte(), 0x4c.toByte(), 0xcc.toByte(), 0x2c.toByte(), 0xac.toByte(), 0x6c.toByte(), 0xec.toByte(),
                0x1c.toByte(), 0x9c.toByte(), 0x5c.toByte(), 0xdc.toByte(), 0x3c.toByte(), 0xbc.toByte(), 0x7c.toByte(), 0xfc.toByte(),
                0x02.toByte(), 0x82.toByte(), 0x42.toByte(), 0xc2.toByte(), 0x22.toByte(), 0xa2.toByte(), 0x62.toByte(), 0xe2.toByte(),
                0x12.toByte(), 0x92.toByte(), 0x52.toByte(), 0xd2.toByte(), 0x32.toByte(), 0xb2.toByte(), 0x72.toByte(), 0xf2.toByte(),
                0x0a.toByte(), 0x8a.toByte(), 0x4a.toByte(), 0xca.toByte(), 0x2a.toByte(), 0xaa.toByte(), 0x6a.toByte(), 0xea.toByte(),
                0x1a.toByte(), 0x9a.toByte(), 0x5a.toByte(), 0xda.toByte(), 0x3a.toByte(), 0xba.toByte(), 0x7a.toByte(), 0xfa.toByte(),
                0x06.toByte(), 0x86.toByte(), 0x46.toByte(), 0xc6.toByte(), 0x26.toByte(), 0xa6.toByte(), 0x66.toByte(), 0xe6.toByte(),
                0x16.toByte(), 0x96.toByte(), 0x56.toByte(), 0xd6.toByte(), 0x36.toByte(), 0xb6.toByte(), 0x76.toByte(), 0xf6.toByte(),
                0x0e.toByte(), 0x8e.toByte(), 0x4e.toByte(), 0xce.toByte(), 0x2e.toByte(), 0xae.toByte(), 0x6e.toByte(), 0xee.toByte(),
                0x1e.toByte(), 0x9e.toByte(), 0x5e.toByte(), 0xde.toByte(), 0x3e.toByte(), 0xbe.toByte(), 0x7e.toByte(), 0xfe.toByte(),
                0x01.toByte(), 0x81.toByte(), 0x41.toByte(), 0xc1.toByte(), 0x21.toByte(), 0xa1.toByte(), 0x61.toByte(), 0xe1.toByte(),
                0x11.toByte(), 0x91.toByte(), 0x51.toByte(), 0xd1.toByte(), 0x31.toByte(), 0xb1.toByte(), 0x71.toByte(), 0xf1.toByte(),
                0x09.toByte(), 0x89.toByte(), 0x49.toByte(), 0xc9.toByte(), 0x29.toByte(), 0xa9.toByte(), 0x69.toByte(), 0xe9.toByte(),
                0x19.toByte(), 0x99.toByte(), 0x59.toByte(), 0xd9.toByte(), 0x39.toByte(), 0xb9.toByte(), 0x79.toByte(), 0xf9.toByte(),
                0x05.toByte(), 0x85.toByte(), 0x45.toByte(), 0xc5.toByte(), 0x25.toByte(), 0xa5.toByte(), 0x65.toByte(), 0xe5.toByte(),
                0x15.toByte(), 0x95.toByte(), 0x55.toByte(), 0xd5.toByte(), 0x35.toByte(), 0xb5.toByte(), 0x75.toByte(), 0xf5.toByte(),
                0x0d.toByte(), 0x8d.toByte(), 0x4d.toByte(), 0xcd.toByte(), 0x2d.toByte(), 0xad.toByte(), 0x6d.toByte(), 0xed.toByte(),
                0x1d.toByte(), 0x9d.toByte(), 0x5d.toByte(), 0xdd.toByte(), 0x3d.toByte(), 0xbd.toByte(), 0x7d.toByte(), 0xfd.toByte(),
                0x03.toByte(), 0x83.toByte(), 0x43.toByte(), 0xc3.toByte(), 0x23.toByte(), 0xa3.toByte(), 0x63.toByte(), 0xe3.toByte(),
                0x13.toByte(), 0x93.toByte(), 0x53.toByte(), 0xd3.toByte(), 0x33.toByte(), 0xb3.toByte(), 0x73.toByte(), 0xf3.toByte(),
                0x0b.toByte(), 0x8b.toByte(), 0x4b.toByte(), 0xcb.toByte(), 0x2b.toByte(), 0xab.toByte(), 0x6b.toByte(), 0xeb.toByte(),
                0x1b.toByte(), 0x9b.toByte(), 0x5b.toByte(), 0xdb.toByte(), 0x3b.toByte(), 0xbb.toByte(), 0x7b.toByte(), 0xfb.toByte(),
                0x07.toByte(), 0x87.toByte(), 0x47.toByte(), 0xc7.toByte(), 0x27.toByte(), 0xa7.toByte(), 0x67.toByte(), 0xe7.toByte(),
                0x17.toByte(), 0x97.toByte(), 0x57.toByte(), 0xd7.toByte(), 0x37.toByte(), 0xb7.toByte(), 0x77.toByte(), 0xf7.toByte(),
                0x0f.toByte(), 0x8f.toByte(), 0x4f.toByte(), 0xcf.toByte(), 0x2f.toByte(), 0xaf.toByte(), 0x6f.toByte(), 0xef.toByte(),
                0x1f.toByte(), 0x9f.toByte(), 0x5f.toByte(), 0xdf.toByte(), 0x3f.toByte(), 0xbf.toByte(), 0x7f.toByte(), 0xff.toByte())

        private val HigherBitsMask = intArrayOf(0x00, 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe)

        private val LowerBitsMask = intArrayOf(0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f)

        private fun RamSegment.fastCopy(destRam: RamSegment) {
            System.arraycopy((backingRam as DefaultRamModel).data, startingAddress,
                    (destRam.backingRam as DefaultRamModel).data, destRam.startingAddress,
                    size)
        }

        /**
         * 将数组中offset起始处的count个字节向左或向右移动bits个bit
         * @param offset 偏移地址
         * @param count 长度
         * @param nBits -7到7之间(包含). 为正则向左移动,负则向右移动
         */
        private fun ByteArray.shift(offset: Int, count: Int, nBits: Int) {
            if (nBits > 0) {
                val restBits = 8 - nBits
                var bits = 0
                for (i in (offset + count - 1) downTo offset) {
                    val b = this[i].toInt()
                    this[i] = (b shl nBits or bits).toByte()
                    bits = b and HigherBitsMask[nBits] ushr restBits
                }
            } else if (nBits < 0) {
                val nBits = -nBits
                val restBits = 8 - nBits
                var bits = 0
                for (i in offset until (offset + count)) {
                    val b = this[i].toInt()
                    this[i] = (b and 0xff ushr nBits or bits).toByte()
                    bits = b and LowerBitsMask[nBits] shl restBits
                }
            }
        }
    }

    private var activeRam = graphicsRam


    override var target: ScreenModel.Target
        get() = if (activeRam === graphicsRam) ScreenModel.Target.Graphics else ScreenModel.Target.Buffer
        set(value) {
            activeRam = when (value) {
                ScreenModel.Target.Graphics -> graphicsRam
                ScreenModel.Target.Buffer -> bufferRam
            }
        }


    override val dirtyRegion: Rect
        get() = TODO("not implemented")

    override fun clear() {
        activeRam.zero()
    }

    override fun drawData(_x: Int, _y: Int, _width: Int, _height: Int, mem: ReadableMemory, _addr: Int,
                          _mode: ScreenModel.DataDrawMode, horizontalMirrored: Boolean, inverse: Boolean) {
        var x = _x
        var y = _y
        var addr = _addr
        var width = _width
        var height = _height
        var mode = _mode

        if (width <= 0 || height <= 0) {
            return
        }
        if (x >= ScreenModel.WIDTH || y >= ScreenModel.HEIGHT || x + width < 0 || y + height < 0) {
            return
        }

        //每行数据占用byte数
        val bytePerLine = (width + 7) ushr 3
        //每行开始第一个数据前无用的bit数(<=7)
        var unuseDataBits = 0
        if (x < 0) {
            addr += -x / 8
            unuseDataBits = (-x) % 8
            width += x
            x = 0
        }
        if (y < 0) {
            addr += -bytePerLine * y
            height += y
            y = 0
        }
        if (x + width > ScreenModel.WIDTH) {
            width -= x + width - ScreenModel.WIDTH
        }
        if (y + height > ScreenModel.HEIGHT) {
            height -= y + height - ScreenModel.HEIGHT
        }

        //绘制处前无用的bit数(<=7)
        val unuseScreenBits = x % 8
        //绘制开始地址
        var offset = ScreenModel.BYTE_WIDTH * y + x / 8
        //实际每行用到数据的byte数
        val count = (unuseDataBits + width + 7) / 8
        //实际绘制影响到的byte数
        val size = (unuseScreenBits + width + 7) / 8
        //绘制结尾剩下的bit数
        val remain = size * 8 - unuseScreenBits - width
        //用于存储图像数据
        val mapData = ByteArray(count + 1)
        while (height-- > 0) {
            for (i in 0 until count) {
                mapData[i] = mem.getByte(addr + i)
            }
            addr += bytePerLine
            mapData.shift(0, mapData.size, unuseDataBits - unuseScreenBits)
            for (index in 0 until size) {
                var s = mapData[index].toInt()
                val d = activeRam.getByte(offset + index).toInt()

                var mask = 0
                if (index == 0) {
                    mask = mask or HigherBitsMask[unuseScreenBits]
                } else
                if (index == size - 1) {
                    mask = mask or LowerBitsMask[remain]
                }
                val old = d and mask

                if (inverse) {
                    s = s xor 0xff
                    if (mode == ScreenModel.DataDrawMode.Not) {
                        mode = ScreenModel.DataDrawMode.Copy
                    }
                }

                activeRam.setByte(offset + index,
                        (old or (when (mode) {
                            ScreenModel.DataDrawMode.Not -> s xor mask.inv()
                            ScreenModel.DataDrawMode.Or ->  s or d
                            ScreenModel.DataDrawMode.And -> s and d
                            ScreenModel.DataDrawMode.Xor -> s xor d
                            else -> s
                        } and mask.inv())).toByte())
            }

            offset += ScreenModel.BYTE_WIDTH
        }
    }


    override fun saveData(x: Int, y: Int, width: Int, height: Int, mem: WritableMemory, addr: Int) {
        val bw = width ushr 3
        if (bw == 0 || height == 0) {
            return
        }

        val bx = x ushr 3
        if (x and 0xfff8 >= ScreenModel.WIDTH || y >= ScreenModel.HEIGHT) {
            // 屏幕外是空白
            mem.fill(addr, height * bw, 0)
            return
        }

        var addr1 = addr
        for (offset in (y * ScreenModel.BYTE_WIDTH) until ((y + height) * ScreenModel.BYTE_WIDTH) step ScreenModel.BYTE_WIDTH) {
            for (i in bx until (bx + bw)) {
                mem.setByte(addr1++, activeRam.getByte(offset + i))
            }
        }
    }

    override fun drawString(x: Int, y: Int, mem: ReadableMemory, addr: Int, len: Int, font: TextModel.TextMode,
                            mode: ScreenModel.DataDrawMode, horizontalMirrored: Boolean, inverse: Boolean) {
        var len = len
        var addr = addr
        var x = x
        val charWidth =
                if (font == TextModel.TextMode.LARGE_FONT) DefaultTextModel.LARGE_FONT_WIDTH
                else DefaultTextModel.SMALL_FONT_WIDTH
        val charHeight =
                if (font == TextModel.TextMode.LARGE_FONT) DefaultTextModel.LARGE_FONT_HEIGHT
                else DefaultTextModel.SMALL_FONT_HEIGHT

        while (--len >= 0) {
            val b = mem.getByte(addr++).toInt()
            x += if (b < 0 && len > 0) {
                drawData(x, y, charWidth * 2, charHeight, Font.fontMem, Font.getFontDataOffset(b and 0xff or (mem.getByte(addr++).toInt() and 0xff shl 8), font),
                        mode, horizontalMirrored, inverse)
                --len
                charWidth * 2
            } else {
                drawData(x, y, charWidth, charHeight, Font.fontMem, Font.getFontDataOffset(b and 0xff, font),
                        mode, horizontalMirrored, inverse)
                charWidth
            }
        }
    }

    override fun drawRect(_x1: Int, _y1: Int, _x2: Int, _y2: Int, fill: Boolean, mode: ScreenModel.ShapeDrawMode) {
        var x1 = _x1
        var x2 = _x2
        if (x1 > x2) {
            x1 = _x2
            x2 = _x1
        }

        var y1 = _y1
        var y2 = _y2
        if (y1 > y2) {
            y1 = _y2
            y2 = _y1
        }

        if (x2 < 0 || x1 >= ScreenModel.WIDTH || y2 < 0 || y1 >= ScreenModel.HEIGHT) {
            return
        }

        if (x1 < 0) {
            x1 = 0
        }
        if (x2 >= ScreenModel.WIDTH) {
            x2 = ScreenModel.WIDTH - 1
        }

        if (y1 < 0) {
            y1 = 0
        }
        if (y2 >= ScreenModel.HEIGHT) {
            y2 = ScreenModel.HEIGHT - 1
        }

        if (fill) {
            for (y in y1..y2) {
                hLine(x1, x2, y, mode)
            }
        } else {
            hLine(x1, x2, y1, mode)
            if (y2 > y1) {
                hLine(x1, x2, y2, mode)
                vLine(x1, y1 + 1, y2 - 1, mode)
                if (x2 > x1) {
                    vLine(x2, y1 + 1, y2 - 1, mode)
                }
            }
        }
    }

    /**
     * 画竖线. 要求 y1 <= y2 且坐标位于屏幕范围内
     */
    private inline fun vLine(x: Int, y1: Int, y2: Int, mode: ScreenModel.ShapeDrawMode) {
        for (y in y1..y2) {
            point(x, y, mode)
        }
    }

    /**
     * 画横线. 要求 x1 <= x2 且坐标位于屏幕范围内
     */
    private inline fun hLine(x1: Int, x2: Int, y: Int, mode: ScreenModel.ShapeDrawMode) {
        for (x in x1..x2) {
            point(x, y, mode)
        }
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, mode: ScreenModel.ShapeDrawMode) {
        if ((y2 - y1).absoluteValue <= (x2 - x1).absoluteValue) {
            if (x1 <= x2) {
                drawLineAux(x1, y1, x2, y2) { x, y -> checkedPoint(x, y, mode) }
            } else {
                drawLineAux(x2, y2, x1, y1) { x, y -> checkedPoint(x, y, mode) }
            }
        } else {
            if (y1 <= y2) {
                drawLineAux(y1, x1, y2, x2) { y, x -> checkedPoint(x, y, mode) }
            } else {
                drawLineAux(y2, x2, y1, x1) { y, x -> checkedPoint(x, y, mode) }
            }
        }
    }

    /**
     * bresenham algorithm
     *
     * 要求 x1 <= x2
     */
    private inline fun drawLineAux(x1: Int, y1: Int, x2: Int, y2: Int, dot: (Int, Int) -> Unit) {
        val dx = x2 - x1
        var dy = y2 - y1
        var incy = 1
        if (dy < 0) {
            dy = -dy
            incy = -1
        }
        val acc1 = 2 * dy
        val acc2 = 2 * (dy - dx)

        var d = 2 * dy - dx

        var y = y1
        for (x in x1..x2) {
            dot(x, y)
            d += if (d < 0) {
                acc1
            } else {
                y += incy
                acc2
            }
        }
    }

    override fun drawOval(cx: Int, cy: Int, a: Int, b: Int, fill: Boolean, mode: ScreenModel.ShapeDrawMode) {
        if (cx - a >= ScreenModel.WIDTH || cx + a < 0 || cy - b >= ScreenModel.HEIGHT || cy + b < 0) {
            return
        }

        val `a^2` = a * a
        val `b^2` = b * b
        val `2*a^2` = `a^2` * 2
        val `2*b^2` = `b^2` * 2
        var x = 0
        var y = b
        var px = 0
        var py = `2*a^2` * y
        var p = `b^2` - `a^2` * b + ((`a^2` + 2) ushr 2)
        while (px < py) {
            x++
            px += `2*b^2`
            if (p < 0) {
                p += `b^2` + px
            } else {
                if (fill) {
                    ovalHLine(cx - x + 1, cx + x - 1, cy + y, mode)
                    ovalHLine(cx - x + 1, cx + x - 1, cy - y, mode)
                }
                y--
                py -= `2*a^2`
                p += `b^2` + px - py
            }
            if (!fill) {
                checkedPoint(cx - x, cy - y, mode)
                checkedPoint(cx - x, cy + y, mode)
                checkedPoint(cx + x, cy - y, mode)
                checkedPoint(cx + x, cy + y, mode)
            }

        }
        if (fill) {
            ovalHLine(cx - x, cx + x, cy + y, mode)
            ovalHLine(cx - x, cx + x, cy - y, mode)
        }
        p = `b^2` * x * x + `b^2` * x + `a^2` * (y - 1) * (y - 1) - `a^2` * `b^2` + ((`b^2` + 2) ushr 2)
        while (--y > 0) {
            py -= `2*a^2`
            if (p > 0) {
                p += `a^2` - py
            } else {
                x++
                px += `2*b^2`
                p += `a^2` - py + px
            }
            if (fill) {
                ovalHLine(cx - x, cx + x, cy + y, mode)
                ovalHLine(cx - x, cx + x, cy - y, mode)
            } else {
                checkedPoint(cx - x, cy - y, mode)
                checkedPoint(cx - x, cy + y, mode)
                checkedPoint(cx + x, cy - y, mode)
                checkedPoint(cx + x, cy + y, mode)
            }
        }
        if (fill) {
            ovalHLine(cx - a, cx + a, cy, mode)
        } else {
            checkedPoint(cx, cy + b, mode)
            checkedPoint(cx, cy - b, mode)
            checkedPoint(cx + a, cy, mode)
            checkedPoint(cx - a, cy, mode)
        }
    }

    // 要求 x1 <= x2, 不要求位于屏幕范围内
    private inline fun ovalHLine(_x1: Int, _x2: Int, y: Int, mode: ScreenModel.ShapeDrawMode) {
        if (y in 0 until ScreenModel.HEIGHT && _x2 >= 0 && _x1 < ScreenModel.WIDTH) {
            val x1 = if (_x1 < 0) 0 else _x1
            val x2 = if (_x2 >= ScreenModel.WIDTH) ScreenModel.WIDTH - 1 else _x2
            hLine(x1, x2, y, mode)
        }
    }

    override fun drawPoint(x: Int, y: Int, mode: ScreenModel.ShapeDrawMode) {
        checkedPoint(x, y, mode)
    }

    /**
     * 检查坐标是否位于屏幕范围内
     */
    private inline fun checkedPoint(x: Int, y: Int, mode: ScreenModel.ShapeDrawMode) {
        if (x in 0 until ScreenModel.WIDTH && y in 0 until ScreenModel.HEIGHT) {
            point(x, y, mode)
        }
    }

    /**
     * 不检查坐标是否位于屏幕范围内
     */
    private inline fun point(x: Int, y: Int, mode: ScreenModel.ShapeDrawMode) {
        val offset = y * ScreenModel.BYTE_WIDTH + (x ushr 3)
        val b = activeRam.getByte(offset).toInt()
        activeRam.setByte(offset,
                when (mode) {
                    ScreenModel.ShapeDrawMode.Clear -> b and BIT_MASK[x and 0x7].inv()
                    ScreenModel.ShapeDrawMode.Normal -> b or BIT_MASK[x and 0x7]
                    ScreenModel.ShapeDrawMode.Invert -> b xor BIT_MASK[x and 0x7]
                }.toByte())
    }

    override fun scroll(dir: ScreenModel.ScrollDirection) {
        when (dir) {
            ScreenModel.ScrollDirection.Left -> {
                for (offset in 0 until ScreenModel.RAM_SIZE step ScreenModel.BYTE_WIDTH) {
                    var bit = false
                    for (i in (ScreenModel.BYTE_WIDTH - 1) downTo 0) {
                        var b = activeRam.getByte(offset + i).toInt() shl 1
                        if (bit) {
                            b = b or 1
                        }
                        activeRam.setByte(offset + i, b.toByte())
                        bit = b < 0
                    }
                }
            }
            ScreenModel.ScrollDirection.Right -> {
                for (offset in 0 until ScreenModel.RAM_SIZE step ScreenModel.BYTE_WIDTH) {
                    var bit = false
                    for (i in 0 until (ScreenModel.BYTE_WIDTH - 1)) {
                        val b = activeRam.getByte(offset + i).toInt()
                        if (bit) {
                            activeRam.setByte(offset + i, (b ushr 1 or 0x80).toByte())
                        } else {
                            activeRam.setByte(offset + i, (b ushr 1 and 0x7f).toByte())
                        }
                        bit = (b and 1) != 0
                    }
                }
            }
        }
    }

    override fun mirror(dir: ScreenModel.MirrorDirection) {
        when (dir) {
            ScreenModel.MirrorDirection.Horizontal -> {
                for (offset in 0 until ScreenModel.RAM_SIZE step ScreenModel.BYTE_WIDTH) {
                    for (i in 0 until (ScreenModel.BYTE_WIDTH - 1)) {
                        val tmp = activeRam.getByte(offset + i)
                        activeRam.setByte(offset + i, ByteMirrorTable[activeRam.getByte(offset + (ScreenModel.BYTE_WIDTH - 1 - i)).toInt() and 0xff])
                        activeRam.setByte(offset + (ScreenModel.BYTE_WIDTH - 1 - i), ByteMirrorTable[tmp.toInt() and 0xff])
                    }
                }
            }
            ScreenModel.MirrorDirection.Vertical -> {
                for (y in 0 until (ScreenModel.HEIGHT / 2)) {
                    val offsetTop = y * ScreenModel.BYTE_WIDTH
                    val offsetBottom = (ScreenModel.HEIGHT - 1 - y) * ScreenModel.BYTE_WIDTH
                    for (x in 0 until ScreenModel.WIDTH) {
                        val tmp = activeRam.getByte(offsetTop + x)
                        activeRam.setByte(offsetTop + x, activeRam.getByte(offsetBottom + x))
                        activeRam.setByte(offsetBottom + x, tmp)
                    }
                }
            }
        }
    }

    override fun testPoint(x: Int, y: Int): Int =
            if (x in 0 until ScreenModel.WIDTH && y in 0 until ScreenModel.HEIGHT) {
                activeRam.getByte(y * ScreenModel.BYTE_WIDTH + (x ushr 3)).toInt() and BIT_MASK[y and 0x7]
            } else
                0

    override fun renderBufferToGraphics() {
        bufferRam.fastCopy(graphicsRam)
    }
}