package plodsoft.mygvm.memory

interface WritableMemory {
    fun setByte(offset: Int, value: Byte)

    fun fill(offset: Int, count: Int, value: Byte) {
        for (i in offset until (offset + count)) {
            setByte(i, value)
        }
    }
}
