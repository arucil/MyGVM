package plodsoft.mygvm.runtime

import plodsoft.mygvm.keyboard.DefaultKeyboardModel
import plodsoft.mygvm.keyboard.KeyboardModel
import plodsoft.mygvm.memory.DefaultRamModel
import plodsoft.mygvm.memory.RamModel
import plodsoft.mygvm.memory.RamSegment
import plodsoft.mygvm.screen.DefaultScreenModel
import plodsoft.mygvm.screen.ScreenModel
import plodsoft.mygvm.util.readAll
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.experimental.xor
import kotlin.math.absoluteValue
import kotlin.math.max
import plodsoft.mygvm.screen.ScreenModel.DrawMode
import plodsoft.mygvm.text.DefaultTextModel
import plodsoft.mygvm.text.TextModel
import java.util.*


class Runtime(val ramModel: RamModel = DefaultRamModel(),
              val screenModel: ScreenModel = DefaultScreenModel(
                      RamSegment(ramModel, GRAPHICS_ADDRESS, ScreenModel.RAM_SIZE),
                      RamSegment(ramModel, GRAPHICS_BUFFER_ADDRESS, ScreenModel.RAM_SIZE)),
              private val textModel: TextModel = DefaultTextModel(
                      RamSegment(ramModel, TEXT_BUFFER_ADDRESS, DefaultTextModel.SMALL_FONT_ROWS * DefaultTextModel.SMALL_FONT_COLUMNS),
                      screenModel),
              val keyboardModel: KeyboardModel = DefaultKeyboardModel()
        ) {

    companion object {
        private const val CODE_INITIAL_OFFSET = 16
        private const val DATA_STACK_CAPACITY = 1024
        private const val FRAME_STACK_CAPACITY = 20 * 1024 // Lava 20K

        private const val ADDRESS_BYTES = 3

        private const val STRING_STACK_ADDRESS = 0x1000
        private const val STRING_STACK_CAPACITY = 1024

        const val TEXT_BUFFER_ADDRESS = 0x0

        const val GRAPHICS_ADDRESS = 0x100

        const val GRAPHICS_BUFFER_ADDRESS = 0x900

        private const val TRUE = -1
        private const val FALSE = 0


        /**
         * 获取从addr开始的以\0结尾的字符串的结尾(\0)地址
         */
        private inline fun RamModel.getStringEnd(addr: Int): Int {
            ///TODO: 检查字符串没有\0结尾
            var addr1 = addr
            while (getByte(addr1) != 0.toByte()) {
                ++addr1
            }
            return addr1
        }
    }


    private var code: ByteArray = ByteArray(0)
    private var pc: Int = 0
    private val dataStack = DataStack(DATA_STACK_CAPACITY)
    private var currentFrameBase: Int = 0
    private var currentFrameEnd: Int = 0 // 下一栈帧的起始地址
    private var initialFrameBase: Int = 0

    private var isOver = false // 程序是否执行结束

    /* 字符串栈 */
    private var stringStackPtr: Int = 0

    private var stringLiteralXorFactor: Byte = 0

    private val randGen = RandomGen(0)

    private val calendar = Calendar.getInstance()


    /**
     * 加载lav数据
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    fun loadCode(input: InputStream) {
        code = input.readAll()
        verifyCodeHeader()
    }

    private fun verifyCodeHeader() {
        if (code.size <= 16 || code[0] != 'L'.toByte() || code[1] != 'A'.toByte() || code[2] != 'V'.toByte()) {
            throw IllegalArgumentException("非法的LAV文件")
        }

        if (code[3] != 0x12.toByte()) {
            throw IllegalArgumentException("不支持的LAV文件版本: 0x" + code[3].toString(16))
        }

        if (!code.sliceArray(4 until CODE_INITIAL_OFFSET).all { it == 0.toByte() }) {
            throw IllegalArgumentException("非法的LAV文件")
        }
    }

    /**
     * 准备运行虚拟机
     */
    fun prepare() {
        isOver = false

        pc = CODE_INITIAL_OFFSET
        dataStack.clear()
        currentFrameBase = -1
        currentFrameEnd = -1
        initialFrameBase = -1

        stringStackPtr = STRING_STACK_ADDRESS
        stringLiteralXorFactor = 0
    }

    /**
     * 程序执行结束后调用, 清理相关资源
     */
    fun cleanUp() {
    }

    /**
     * 执行一条指令
     * @return 程序是否执行结束
     * @throws InterruptedException
     */
    fun runOneStep(): Boolean {
        if (isOver) {
            return true
        }

        when (code[pc++].toInt() and 0xff) {
            0x00 -> {}

            0x01 -> dataStack.push(fetchUint8())
            0x02 -> dataStack.push(fetchInt16())
            0x03 -> dataStack.push(fetchInt32())

            0x04 -> dataStack.push(ramModel.getUint8(fetchUint16()))
            0x05 -> dataStack.push(ramModel.getInt16(fetchUint16()))
            0x06 -> dataStack.push(ramModel.getInt32(fetchUint16()))

            0x07 -> dataStack.push(ramModel.getUint8((dataStack.pop() + fetchUint16()) and 0xffff))
            0x08 -> dataStack.push(ramModel.getInt16((dataStack.pop() + fetchUint16()) and 0xffff))
            0x09 -> dataStack.push(ramModel.getInt32((dataStack.pop() + fetchUint16()) and 0xffff))

            0x0a -> dataStack.push((dataStack.pop() + fetchUint16()) and 0xffff or 0x0001_0000)
            0x0b -> dataStack.push((dataStack.pop() + fetchUint16()) and 0xffff or 0x0002_0000)
            0x0c -> dataStack.push((dataStack.pop() + fetchUint16()) and 0xffff or 0x0004_0000)

            0x0d -> {
                dataStack.push(stringStackPtr) ///TODO: 0x00100000标记 ???
                do {
                    val c = code[pc++] xor stringLiteralXorFactor
                    ramModel.setByte(stringStackPtr++, c)
                } while (c != 0.toByte())
                if (stringStackPtr >= STRING_STACK_ADDRESS + STRING_STACK_CAPACITY * 3 / 4) {
                    stringStackPtr = STRING_STACK_ADDRESS
                }
            }

            0x0e -> dataStack.push(ramModel.getUint8(fetchUint16() + currentFrameBase))
            0x0f -> dataStack.push(ramModel.getInt16(fetchUint16() + currentFrameBase))
            0x10 -> dataStack.push(ramModel.getInt32(fetchUint16() + currentFrameBase))

            0x11 -> dataStack.push(ramModel.getUint8(fetchUint16() + dataStack.pop() + currentFrameBase and 0xffff))
            0x12 -> dataStack.push(ramModel.getInt16(fetchUint16() + dataStack.pop() + currentFrameBase and 0xffff))
            0x13 -> dataStack.push(ramModel.getInt32(fetchUint16() + dataStack.pop() + currentFrameBase and 0xffff))

            0x14 -> dataStack.push(fetchUint16() + dataStack.pop() + currentFrameBase and 0xffff or 0x0001_0000)
            0x15 -> dataStack.push(fetchUint16() + dataStack.pop() + currentFrameBase and 0xffff or 0x0002_0000)
            0x16 -> dataStack.push(fetchUint16() + dataStack.pop() + currentFrameBase and 0xffff or 0x0004_0000)

            0x17 -> dataStack.push(fetchUint16() + dataStack.pop() and 0xffff)
            0x18 -> dataStack.push(fetchUint16() + dataStack.pop() + currentFrameBase and 0xffff)
            0x19 -> dataStack.push(fetchUint16() + currentFrameBase and 0xffff)

            0x1a -> dataStack.push(TEXT_BUFFER_ADDRESS)
            0x1b -> dataStack.push(GRAPHICS_ADDRESS)

            0x1c -> dataStack.push(-dataStack.pop())

            0x1d, 0x1e, 0x1f, 0x20 -> {
                val ptr = dataStack.pop()
                val addr = (ptr and 0xffff) + if (ptr and 0x0080_0000 != 0) { // 局部变量指针
                    currentFrameBase
                } else { // 全局变量
                    0
                }
                val len = ptr ushr 16 and 0x7f

                var value = when (len) {
                    1 -> ramModel.getUint8(addr)
                    2 -> ramModel.getInt16(addr)
                    4 -> ramModel.getInt32(addr)
                    else -> throw IllegalStateException("unreachable: invalid ptr type: $len")
                }

                when (code[pc - 1].toInt()) {
                    0x1d -> dataStack.push(++value)
                    0x1e -> dataStack.push(--value)
                    0x1f -> dataStack.push(value++)
                    0x20 -> dataStack.push(value--)
                }
                when (len) {
                    1 -> ramModel.setUint8(addr, value)
                    2 -> ramModel.setInt16(addr, value)
                    4 -> ramModel.setInt32(addr, value)
                }
            }

            0x21 -> dataStack.push(dataStack.pop() + dataStack.pop())
            0x22 -> dataStack.pop().let { dataStack.push(dataStack.pop() - it) }
            0x23 -> dataStack.push(dataStack.pop() and dataStack.pop())
            0x24 -> dataStack.push(dataStack.pop() or dataStack.pop())
            0x25 -> dataStack.push(dataStack.pop().inv())
            0x26 -> dataStack.push(dataStack.pop() xor dataStack.pop())
            0x27 -> dataStack.pop().let { dataStack.push(dataStack.pop() != 0 && it != 0) }
            0x28 -> dataStack.pop().let { dataStack.push(dataStack.pop() != 0 || it != 0) }
            0x29 -> dataStack.push(dataStack.pop() == 0)
            0x2a -> dataStack.push(dataStack.pop() * dataStack.pop())
            0x2b -> dataStack.pop().let { dataStack.push(dataStack.pop() / it) }
            0x2c -> dataStack.pop().let { dataStack.push(dataStack.pop() % it) }
            0x2d -> dataStack.pop().let { dataStack.push(dataStack.pop() shl max(0, it)) }
            0x2e -> dataStack.pop().let { dataStack.push(dataStack.pop() ushr max(0, it)) }
            0x2f -> dataStack.push(dataStack.pop() == dataStack.pop())
            0x30 -> dataStack.push(dataStack.pop() != dataStack.pop())
            0x31 -> dataStack.pop().let { dataStack.push(dataStack.pop() <= it) }
            0x32 -> dataStack.pop().let { dataStack.push(dataStack.pop() >= it) }
            0x33 -> dataStack.pop().let { dataStack.push(dataStack.pop() > it) }
            0x34 -> dataStack.pop().let { dataStack.push(dataStack.pop() < it) }

            0x35 -> {
                val value = dataStack.pop()
                val ptr = dataStack.pop()
                val addr = (ptr and 0xffff) + if (ptr and 0x0080_0000 != 0) { // 局部 变量
                    currentFrameBase
                } else { // 全局变量
                    0
                }
                val len = ptr ushr 16 and 0x7f

                when (len) {
                    1 -> ramModel.setUint8(addr, value)
                    2 -> ramModel.setInt16(addr, value)
                    4 -> ramModel.setInt32(addr, value)
                    else -> throw IllegalStateException("unreachable: invalid ptr: $len")
                }

                dataStack.push(value)
            }

            0x36 -> dataStack.push(ramModel.getUint8(dataStack.pop() and 0xffff))
            0x37 -> dataStack.push(dataStack.pop() and 0xffff or 0x0001_0000)

            0x38 -> dataStack.pop()

            0x39 -> fetchUint24().let { newPC ->
                if (dataStack.peek(0) == 0) {
                    pc = newPC
                }
            }
            0x3a -> fetchUint24().let { newPC ->
                if (dataStack.peek(0) != 0) {
                    pc = newPC
                }
            }
            0x3b -> pc = fetchUint24()

            0x3c -> fetchUint16().let { frameBase ->
                initialFrameBase = frameBase
                currentFrameBase = frameBase
                currentFrameEnd = frameBase
            }
            0x3d -> fetchUint24().let { newPC ->
                ramModel.setUint24(currentFrameEnd, pc)
                pc = newPC
            }
            0x3e -> {
                ramModel.setUint16(currentFrameEnd + ADDRESS_BYTES, currentFrameBase)
                currentFrameBase = currentFrameEnd
                currentFrameEnd += fetchUint16()
                if (currentFrameBase >= initialFrameBase + FRAME_STACK_CAPACITY) {
                    throw VMException("frame stack overflow")
                }

                val argc = fetchUint8()
                dataStack.shrink(argc)

                var addr = currentFrameBase + ADDRESS_BYTES + 2
                for (i in 0 until argc) {
                    ramModel.setInt32(addr, dataStack.peek(i))
                    addr += 4
                }
            }
            0x3f -> {
                pc = ramModel.getUint24(currentFrameBase)
                currentFrameEnd = currentFrameBase
                currentFrameBase = ramModel.getUint16(currentFrameBase + ADDRESS_BYTES)
            }

            0x40 -> isOver = true

            0x41 -> {
                var addr = fetchUint16()
                val len = fetchUint16()
                for (i in 0 until len) {
                    ramModel.setByte(addr++, code[pc++])
                }
            }

            0x42 -> dataStack.push(GRAPHICS_BUFFER_ADDRESS)

            0x43 -> stringLiteralXorFactor = code[pc++]

            0x44 -> {} // #loadall

            0x45 -> dataStack.push(dataStack.pop() + fetchInt16())
            0x46 -> dataStack.push(dataStack.pop() - fetchInt16())
            0x47 -> dataStack.push(dataStack.pop() * fetchInt16())
            0x48 -> dataStack.push(dataStack.pop() / fetchInt16())
            0x49 -> dataStack.push(dataStack.pop() % fetchInt16())
            0x4a -> dataStack.push(dataStack.pop() shl fetchInt16()) // 不检查 rhs 是否小于0
            0x4b -> dataStack.push(dataStack.pop() ushr fetchInt16()) // 不检查 rhs 是否小于0
            0x4c -> dataStack.push(dataStack.pop() == fetchInt16())
            0x4d -> dataStack.push(dataStack.pop() != fetchInt16())
            0x4e -> dataStack.push(dataStack.pop() > fetchInt16())
            0x4f -> dataStack.push(dataStack.pop() < fetchInt16())
            0x50 -> dataStack.push(dataStack.pop() >= fetchInt16())
            0x51 -> dataStack.push(dataStack.pop() <= fetchInt16())

            /* 系统函数 */

            // putchar
            0x80 -> {
                textModel.addByte(dataStack.pop().toByte())
                textModel.renderToScreen(0)
            }

            // getchar
            0x81 -> dataStack.push(keyboardModel.getLastKey(true))

            // printf
            0x82 -> {
                val argc = dataStack.pop()
                dataStack.shrink(argc)
                textModel.addBytes(formatString())
                textModel.renderToScreen(0)
            }

            // strcpy
            0x83 -> {
                var srcAddr = dataStack.pop() and 0xffff
                var destAddr = dataStack.pop() and 0xffff

                do {
                    val b = ramModel.getByte(srcAddr++)
                    ramModel.setByte(destAddr++, b)
                } while (b != 0.toByte())
            }

            // strlen
            0x84 -> {
                val addr = dataStack.pop() and 0xffff

                dataStack.push(ramModel.getStringEnd(addr) - addr)
            }

            // SetScreen(uint8)
            0x85 -> {
                textModel.textMode =
                    if (dataStack.pop() == 0)
                        TextModel.TextMode.LARGE_FONT
                    else
                        TextModel.TextMode.SMALL_FONT
                textModel.clear()
            }

            // UpdateLCD(char)
            0x86 -> textModel.renderToScreen(dataStack.pop() and 0xff)

            // Delay(int16)
            0x87 -> Thread.sleep((dataStack.pop() and 0x7fff).toLong())

            // WriteBlock
            0x88 -> {
                dataStack.shrink(6)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val width = dataStack.peek(2)
                val height = dataStack.peek(3)
                val mode = dataStack.peek(4)
                val addr = dataStack.peek(5) and 0xffff

                screenModel.drawData(x, y, width, height, ramModel, addr, mode)
            }

            // Refresh
            0x89 -> screenModel.renderBufferToGraphics()

            // TextOut
            0x8a -> {
                dataStack.shrink(4)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val addr = dataStack.peek(2)
                val mode = dataStack.peek(3)

                var addr1 = ramModel.getStringEnd(addr)

                screenModel.drawString(x, y, ramModel, addr, addr1 - addr,
                        if ((mode and 0x80) != 0) TextModel.TextMode.SMALL_FONT
                        else TextModel.TextMode.LARGE_FONT,
                        mode)
            }

            // Block
            0x8b -> {
                dataStack.shrink(5)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val x1 = dataStack.peek(2)
                val y1 = dataStack.peek(3)
                val mode = dataStack.peek(4) xor DrawMode.GRAPHICS_DRAW_MASK
                screenModel.drawRect(x, y, x1, y1, true, mode)
            }

            // Rectangle
            0x8c -> {
                dataStack.shrink(5)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val x1 = dataStack.peek(2)
                val y1 = dataStack.peek(3)
                val mode = dataStack.peek(4) xor DrawMode.GRAPHICS_DRAW_MASK
                screenModel.drawRect(x, y, x1, y1, false, mode)
            }

            // exit
            0x8d -> isOver = true

            // ClearScreen
            0x8e -> screenModel.clearBuffer()

            // abs
            0x8f -> dataStack.push(dataStack.pop().absoluteValue)

            // rand
            0x90 -> dataStack.push(randGen.next())

            // srand
            0x91 -> randGen.seed = dataStack.pop()

            // Locate
            0x92 -> {
                dataStack.shrink(2)
                val row = dataStack.peek(0)
                val col = dataStack.peek(1)
                textModel.setLocation(row, col)
            }

            // Inkey
            0x93 -> dataStack.push(keyboardModel.getLastKey(false))

            // Point
            0x94 -> {
                dataStack.shrink(3)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val mode = dataStack.peek(2) xor DrawMode.GRAPHICS_DRAW_MASK
                screenModel.drawPoint(x, y, mode)
            }

            // GetPoint
            0x95 -> {
                dataStack.shrink(2)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                dataStack.push(screenModel.testPoint(x, y))
            }

            // Line
            0x96 -> {
                dataStack.shrink(5)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val x1 = dataStack.peek(2)
                val y1 = dataStack.peek(3)
                val mode = dataStack.peek(4) xor DrawMode.GRAPHICS_DRAW_MASK
                screenModel.drawLine(x, y, x1, y1, mode)
            }

            // Box
            0x97 -> {
                dataStack.shrink(6)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val x1 = dataStack.peek(2)
                val y1 = dataStack.peek(3)
                val fill = dataStack.peek(4) != 0
                val mode = dataStack.peek(5) xor DrawMode.GRAPHICS_DRAW_MASK
                screenModel.drawRect(x, y, x1, y1, fill, mode)
            }

            // Circle
            0x98 -> {
                dataStack.shrink(5)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val r = dataStack.peek(2)
                val fill = dataStack.peek(3) != 0
                val mode = dataStack.peek(4) xor DrawMode.GRAPHICS_DRAW_MASK
                screenModel.drawOval(x, y, r, r, fill, mode)
            }

            // Ellipse
            0x99 -> {
                dataStack.shrink(6)
                val x = dataStack.peek(0)
                val y = dataStack.peek(1)
                val rx = dataStack.peek(2)
                val ry = dataStack.peek(3)
                val fill = dataStack.peek(4) != 0
                val mode = dataStack.peek(5) xor DrawMode.GRAPHICS_DRAW_MASK
                screenModel.drawOval(x, y, rx, ry, fill, mode)
            }

            // Beep
            0x9a -> {}

            // isalnum
            0x9b -> testUint8 { it in '0'.toInt()..'9'.toInt() || it in 'a'.toInt()..'z'.toInt() || it in 'A'.toInt()..'Z'.toInt() }

            // isalpha
            0x9c -> testUint8 { it in 'a'.toInt()..'z'.toInt() || it in 'A'.toInt()..'Z'.toInt() }

            // iscntrl
            0x9d -> testUint8 { it in 0..0x1f || it == 0x7f }

            // isdigit
            0x9e -> testUint8 { it in '0'.toInt()..'9'.toInt() }

            // isgraph
            0x9f -> testUint8 { it in 0x21..0x7e }

            // islower
            0xa0 -> testUint8 { it in 'a'.toInt()..'z'.toInt() }

            // isprint
            0xa1 -> testUint8 { it in 0x20..0x7e }

            // ispunct
            0xa2 -> testUint8 { it in 0x21..0x2f || it in 0x3a..0x40 || it in 0x5b..0x60 || it in 0x7b..0x7e }

            // isspace
            0xa3 -> testUint8 { it in 9..13 || it == 0x20 }

            // isupper
            0xa4 -> testUint8 { it in 'A'.toInt()..'Z'.toInt() }

            // isxdigit
            0xa5 -> testUint8 { it in '0'.toInt()..'9'.toInt() || it in 'A'.toInt()..'F'.toInt() || it in 'a'.toInt()..'f'.toInt() }

            // strcat
            0xa6 -> {
                ///TODO: 检查字符串没有\0结尾

                var srcAddr = dataStack.pop() and 0xffff
                val destAddr = dataStack.pop() and 0xffff

                var destAddr1 = ramModel.getStringEnd(destAddr)

                do {
                    val b = ramModel.getByte(srcAddr++)
                    ramModel.setByte(destAddr1++, b)
                } while (b != 0.toByte())
            }

            // strchr
            0xa7 -> {
                ///TODO: 检查字符串没有\0结尾

                val c = dataStack.pop().toByte()
                var addr = dataStack.pop() and 0xffff

                while (true) {
                    val b = ramModel.getByte(addr)
                    if (b == c) {
                        dataStack.push(addr)
                        break
                    }
                    if (b == 0.toByte()) {
                        dataStack.push(0)
                        break
                    }
                    ++addr
                }
            }

            // strcmp
            0xa8 -> {
                ///TODO: 检查字符串没有\0结尾

                var addr2 = dataStack.pop() and 0xffff
                var addr1 = dataStack.pop() and 0xffff

                while (true) {
                    val a = ramModel.getUint8(addr1++)
                    val r = a - ramModel.getUint8(addr2++)
                    if (r != 0 || a == 0) {
                        dataStack.push(r)
                        break
                    }
                }
            }

            // strstr
            0xa9 -> {
                ///TODO: 检查字符串没有\0结尾

                val subAddr = dataStack.pop() and 0xffff
                var addr = dataStack.pop() and 0xffff
                var result = 0

                outer@while (true) {
                    var subAddr1 = subAddr
                    var addr1 = addr

                    while (true) {
                        val c2 = ramModel.getByte(subAddr1)
                        if (c2 == 0.toByte()) {
                            result = addr
                            break@outer
                        }

                        val c1 = ramModel.getByte(addr1)
                        if (c1 == 0.toByte()) {
                            break@outer
                        }

                        if (c1 != c2) {
                            break
                        }
                        ++subAddr1
                        ++addr1
                    }
                    ++addr
                }

                dataStack.push(result)
            }

            // tolower
            0xaa -> (dataStack.pop() and 0xff).let { c ->
                dataStack.push(if (c in 'A'.toInt()..'Z'.toInt()) c + 32 else c)
            }

            // toupper
            0xab -> (dataStack.pop() and 0xff).let { c ->
                dataStack.push(if (c in 'a'.toInt()..'z'.toInt()) c - 32 else c)
            }

            // memset
            0xac -> {
                dataStack.shrink(3)
                val addr = dataStack.peek(0) and 0xffff
                val b = dataStack.peek(1).toByte()
                val count = dataStack.peek(2) and 0xffff

                ramModel.fill(addr, count, b)
            }

            // memcpy
            0xad -> {
                dataStack.shrink(3)
                var destAddr = dataStack.peek(0) and 0xffff
                var srcAddr = dataStack.peek(1) and 0xffff
                val count = dataStack.peek(2) and 0xffff

                repeat(count) {
                    ramModel.setByte(destAddr++, ramModel.getByte(srcAddr++))
                }
            }

            // sprintf
            0xb8 -> {
                val count = dataStack.pop() - 1
                dataStack.shrink(count)
                val bytes = formatString()
                var addr = dataStack.pop() and 0xffff

                bytes.forEach { ramModel.setByte(addr++, it) }
                ramModel.setByte(addr, 0)
            }

            // Getms
            0xbb -> dataStack.push((System.currentTimeMillis() % 1000 * 256 / 1000).toInt())

            // CheckKey
            0xbc -> {
                val key = dataStack.pop()
                if ((key and 0xffffff80.toInt()) != 0) {
                    dataStack.push(keyboardModel.getPressedKey())
                } else {
                    dataStack.push(keyboardModel.isKeyPressed(key and 0x7f))
                }
            }

            // memmove
            0xbd -> {
                dataStack.shrink(3)
                val destAddr = dataStack.peek(0) and 0xffff
                val srcAddr = dataStack.peek(1) and 0xffff
                val count = dataStack.peek(2) and 0xffff

                ramModel.copy(destAddr, srcAddr, count)
            }

            // Crc16
            0xbe -> {
                val count = dataStack.pop() and 0xffff
                val addr = dataStack.pop() and 0xffff
                dataStack.push(ramModel.getCrc16(addr, count))
            }

            // Secret
            0xbf -> {
                dataStack.shrink(3)
                val addr = dataStack.peek(0) and 0xffff
                val count = dataStack.peek(1) and 0xffff
                val pwdAddr = dataStack.peek(2) and 0xffff
                val pwd = ramModel.getString(pwdAddr)

                ramModel.xorEncrypt(addr, count, pwd)
            }

            // GetTime
            0xc2 -> {
                calendar.time = Date()
                val addr = dataStack.pop() and 0xffff
                ramModel.setInt16(addr, calendar.get(Calendar.YEAR))
                ramModel.setUint8(addr + 2, calendar.get(Calendar.MONTH))
                ramModel.setUint8(addr + 3, calendar.get(Calendar.DAY_OF_MONTH))
                ramModel.setUint8(addr + 4, calendar.get(Calendar.HOUR_OF_DAY))
                ramModel.setUint8(addr + 5, calendar.get(Calendar.MINUTE))
                ramModel.setUint8(addr + 6, calendar.get(Calendar.SECOND))
                ramModel.setUint8(addr + 7, calendar.get(Calendar.DAY_OF_WEEK))
            }

            // SetTime
            0xc3 -> dataStack.pop() // 忽略

            // GetWord
            0xc4 -> {
                val mode = dataStack.pop()
                dataStack.push(keyboardModel.getLastKey(true))
            }

            // XDraw
            0xc5 -> {
                when (dataStack.pop()) {
                    0 -> screenModel.scroll(ScreenModel.ScrollDirection.Left)
                    1 -> screenModel.scroll(ScreenModel.ScrollDirection.Right)
                    4 -> screenModel.mirror(ScreenModel.MirrorDirection.Horizontal)
                    5 -> screenModel.mirror(ScreenModel.MirrorDirection.Vertical)
                }
            }

            // ReleaseKey
            0xc6 -> {
                val key = dataStack.pop()
                if ((key and 0xffffff80.toInt()) != 0) {
                    keyboardModel.revalidateAllKeys()
                } else {
                    keyboardModel.revalidateKey(key and 0x7f)
                }
            }

            // GetBlock
            0xc7 -> {
                dataStack.shrink(6)
                val x = dataStack.peek(0) and 0xffff
                val y = dataStack.peek(1) and 0xffff
                val width = dataStack.peek(2) and 0xffff
                val height = dataStack.peek(3) and 0xffff
                val mode = dataStack.peek(4)
                val addr = dataStack.peek(5)

                screenModel.saveData(x, y, width, height, (mode and DrawMode.GRAPHICS_DRAW_MASK) != 0, ramModel, addr)
            }

            // Sin
            0xc8 -> dataStack.push(sin(dataStack.pop()))

            // Cos
            0xc9 -> dataStack.push(cos(dataStack.pop()))

            // FillArea
            0xca -> {
                dataStack.shrink(3)
            }

            else -> throw VMException("非法指令: 0x${(code[pc - 1].toInt() and 0xff).toString(16)}")
        }
        return isOver
    }

    private inline fun DataStack.push(value: Boolean) {
        push(if (value) TRUE else FALSE)
    }

    /**
     * 读取一字节无符号整数, pc++
     */
    private fun fetchUint8(): Int = code[pc++].toInt() and 0xff

    private fun fetchUint16(): Int =
        ((code[pc].toInt() and 0xff) or
        (code[pc + 1].toInt() and 0xff shl 8)).also { pc += 2 }

    private fun fetchInt16(): Int =
        ((code[pc].toInt() and 0xff) or
        (code[pc + 1].toInt() shl 8)).toShort().toInt().also { pc += 2 }

    private fun fetchUint24(): Int =
        ((code[pc].toInt() and 0xff) or
        (code[pc + 1].toInt() and 0xff shl 8) or
        (code[pc + 2].toInt() and 0xff shl 16)).also { pc += 3 }

    private fun fetchInt32(): Int =
        ((code[pc].toInt() and 0xff) or
        (code[pc + 1].toInt() and 0xff shl 8) or
        (code[pc + 2].toInt() and 0xff shl 16) or
        (code[pc + 3].toInt() shl 24)).also { pc += 4 }


    /**
     * format字符串地址在dataStack.peek(0)
     */
    private fun formatString(): ByteArray =
        with (ByteArrayOutputStream()) {
            var formatAddr = dataStack.peek(0)
            var argPtr = 0

            outer@while (true) {
                val c = ramModel.getUint8(formatAddr++)
                if (c == 0) {
                    break
                }
                when (c) {
                    '%'.toInt() -> {
                        val c1 = ramModel.getUint8(formatAddr++)
                        when (c1) {
                            0 -> break@outer
                            'd'.toInt() -> write(dataStack.peek(++argPtr).toString().toByteArray())
                            'c'.toInt() -> write(dataStack.peek(++argPtr) and 0xff)

                            ///TODO: 检测%s没有\0结尾
                            's'.toInt() -> {
                                var addr = dataStack.peek(++argPtr) and 0xffff
                                while (true) {
                                    val b = ramModel.getByte(addr++)
                                    if (b == 0.toByte()) {
                                        break
                                    }
                                    write(b.toInt())
                                }
                            }

                            else -> write(c1)
                        }
                    }
                    else -> write(c)
                }
            }

            toByteArray()
        }

    private inline fun testUint8(predicate: (Int) -> Boolean) {
        dataStack.push(predicate(dataStack.pop() and 0xff))
    }
}