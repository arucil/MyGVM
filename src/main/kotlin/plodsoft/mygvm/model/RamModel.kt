package plodsoft.mygvm.model


interface RamModel : ReadableMemory, WritableMemory {
    companion object {
        val SIZE = 65536
    }

    fun getUint8(address: Int): Int = getByte(address).toInt() and 0xff
    fun getUint16(address: Int): Int
    fun getInt16(address: Int): Int
    fun getUint24(address: Int): Int
    fun getInt32(address: Int): Int

    fun setUint8(address: Int, value: Int) = setByte(address, value.toByte())
    fun setUint16(address: Int, value: Int) = setInt16(address, value)
    fun setInt16(address: Int, value: Int)
    fun setUint24(address: Int, value: Int)
    fun setInt32(address: Int, value: Int)
}