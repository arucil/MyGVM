package plodsoft.mygvm.model

interface WritableMemory {
    fun setByte(address: Int, value: Byte)

    fun fill(address: Int, count: Int, value: Byte) {
        for (i in address until (address + count)) {
            setByte(i, value)
        }
    }
}