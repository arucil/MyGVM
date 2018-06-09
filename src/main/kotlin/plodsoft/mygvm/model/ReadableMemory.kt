package plodsoft.mygvm.model

interface ReadableMemory {
    fun getByte(address: Int): Byte
}