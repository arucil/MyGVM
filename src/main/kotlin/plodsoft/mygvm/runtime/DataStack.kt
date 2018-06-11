package plodsoft.mygvm.runtime

/**
 * lava虚拟机数据站. 操作数据的单位是int32
 */
class DataStack(val capacity: Int) {
    private val data = IntArray(capacity)
    private var top = 0

    fun push(value: Int) {
        if (top == capacity) {
            throw IllegalStateException("stack overflow")
        }
        data[top++] = value
    }

    fun pop(): Int {
        if (top == 0) {
            throw IllegalStateException("stack underflow")
        }
        return data[--top]
    }

    /**
     * 缩小count个元素
     */
    fun shrink(count: Int) {
        if (count > top) {
            throw IllegalArgumentException("count exceed size")
        }
        top -= count
    }

    fun peek(offset: Int = -1): Int {
        if (top + offset >= capacity || top + offset < 0) {
            throw IllegalArgumentException("Invalid offset")
        }
        return data[top + offset]
    }

    fun clear() {
        top = 0
    }

    val size
        get() = top
}