package plodsoft.mygvm.memory

class RamSegment(val backingRam: RamModel, val startingAddress: Int, val size: Int) : ReadableMemory, WritableMemory {
    override fun getByte(offset: Int): Byte = backingRam.getByte(offset + startingAddress)

    override fun setByte(offset: Int, value: Byte) {
        backingRam.setByte(offset + startingAddress, value)
    }

    override fun fill(offset: Int, count: Int, value: Byte) {
        backingRam.fill(startingAddress + offset, count, value)
    }

    /**
     * 把内存段清零
     */
    inline fun zero() {
        fill(0, size, 0)
    }

    /**
     * 两块内存区域可以重叠
     */
    fun copy(destOffset: Int, srcOffset: Int, count: Int) {
        backingRam.copy(destOffset + startingAddress, srcOffset + startingAddress, count)
    }
}