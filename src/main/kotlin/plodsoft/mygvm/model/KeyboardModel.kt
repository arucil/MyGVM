package plodsoft.mygvm.model

interface KeyboardModel {
    /**
     * 等待按键
     * @return lava键值
     */
    fun waitForKey(): Int
}