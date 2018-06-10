package plodsoft.mygvm.runtime

class RandomGen(var seed: Int) {
    /**
     * 产生一个随机数
     * @return 0~0x7fff
     */
    fun next(): Int {
        seed = seed * 22695477 + 1
        return seed ushr 16 and 0x7fff
    }
}