package plodsoft.mygvm.model

interface WritableMemory {
    fun setByte(address: Int, value: Byte)
}