package plodsoft.mygvm.memory

interface WritableMemory {
    fun setByte(address: Int, value: Byte)

    fun fill(address: Int, count: Int, value: Byte) {
        for (i in address until (address + count)) {
            setByte(i, value)
        }
    }
}
