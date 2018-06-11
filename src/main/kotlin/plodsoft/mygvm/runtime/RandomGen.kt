package plodsoft.mygvm.runtime

/**
 * 简易随机数生成器
 */
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