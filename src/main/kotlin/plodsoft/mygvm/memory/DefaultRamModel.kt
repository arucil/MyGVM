package plodsoft.mygvm.memory

import java.util.*

class DefaultRamModel : RamModel {
    val data = ByteArray(RamModel.SIZE)

    override fun getUint16(address: Int): Int = getUint8(address) or (getUint8(address + 1) shl 8)

    override fun getInt16(address: Int): Int = (getUint8(address) or (getUint8(address + 1) shl 8)).toShort().toInt()

    override fun getUint24(address: Int): Int = getUint8(address) or (getUint8(address + 1) shl 8) or (getUint8(address + 2) shl 16)

    override fun getInt32(address: Int): Int = getUint8(address) or (getUint8(address + 1) shl 8) or (getUint8(address + 2) shl 16) or (getUint8(address + 3) shl 24)

    override fun setInt16(address: Int, value: Int) {
        setUint8(address, value)
        setUint8(address + 1, value ushr 8)
    }

    override fun setUint24(address: Int, value: Int) {
        setUint8(address, value)
        setUint8(address + 1, value ushr 8)
        setUint8(address + 2, value ushr 16)
    }

    override fun setInt32(address: Int, value: Int) {
        setUint8(address, value)
        setUint8(address + 1, value ushr 8)
        setUint8(address + 2, value ushr 16)
        setUint8(address + 3, value ushr 24)
    }

    override fun getByte(offset: Int): Byte = data[offset]

    override fun setByte(offset: Int, value: Byte) {
        data[offset] = value
    }

    override fun fill(offset: Int, count: Int, value: Byte) {
        Arrays.fill(data, offset, offset + count, value)
    }

    override fun copy(destAddress: Int, srcAddress: Int, count: Int) {
        System.arraycopy(data, srcAddress, data, destAddress, count)
    }
}
