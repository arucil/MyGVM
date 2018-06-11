package plodsoft.mygvm.model

import java.util.*

class DefaultRamModel : RamModel {
    private val data = ByteArray(RamModel.SIZE)

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

    override fun getByte(address: Int): Byte = data[address]

    override fun setByte(address: Int, value: Byte) {
        data[address] = value
    }

    override fun fill(address: Int, count: Int, value: Byte) {
        Arrays.fill(data, address, address + count, value)
    }

    override fun copy(destAddress: Int, srcAddress: Int, count: Int) {
        System.arraycopy(data, srcAddress, data, destAddress, count)
    }
}
