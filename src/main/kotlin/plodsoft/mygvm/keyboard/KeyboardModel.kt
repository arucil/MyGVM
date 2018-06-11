package plodsoft.mygvm.keyboard

/**
 * 键盘model
 *
 * lava键值为0~127之间
 */
interface KeyboardModel {
    /**
     * 获取按键
     * @param wait 是否等待用户按键
     * @return lava键值. 如果不等待按键, 并且用户没有按键, 则返回0
     */
    fun getLastKey(wait: Boolean): Int

    /**
     * 检测按键是否按下
     * @param key lava键值
     */
    fun isKeyPressed(key: Int): Boolean

    /**
     * 获取当前按下的键的键值
     * @return 如果没有键按下, 则返回0. 否则返回键值. 如果有多个键同时按下, 则返回其中一个键值
     */
    fun getPressedKey(): Int

    /**
     * 使指定按键变成释放状态, 即使该键处于按下状态.
     *
     * getLastKey()对于持续按下的按键只产生一次键值, 使用该函数可以产生连续按键
     * @param key lava键值
     */
    fun revalidateKey(key: Int)

    /**
     * 释放所有按键
     */
    fun revalidateAllKeys()
}