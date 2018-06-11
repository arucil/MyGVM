package plodsoft.mygvm.model

class RamSegment(val backingRam: RamModel, val startingAddress: Int, val size: Int) : ReadableMemory, WritableMemory {
    override fun getByte(address: Int): Byte = backingRam.getByte(address + startingAddress)

    override fun setByte(address: Int, value: Byte) {
        backingRam.setByte(address + startingAddress, value)
    }

    override fun fill(address: Int, count: Int, value: Byte) {
        backingRam.fill(startingAddress + address, count, value)
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
    fun copy(destAddr: Int, srcAddr: Int, count: Int) {
        backingRam.copy(destAddr + startingAddress, srcAddr + startingAddress, count)
    }
}