package plodsoft.mygvm.keyboard

class DefaultKeyboardModel : KeyboardModel {
    private val keyStates = BooleanArray(128)

    /**
     * 0不是合法的键值, 用0表示没有按键
     */
    private var lastKey = 0

    private val lock = Object()

    /**
     * 接收按键事件
     *
     * @param key lava键值
     */
    fun keyPressed(key: Int) {
        lastKey = key
        keyStates[key] = true

        lock.notify()
    }

    /**
     * 接收释放按键事件
     *
     * @param key lava键值
     */
    fun keyReleased(key: Int) {
        if (lastKey == key) {
            lastKey = 0
        }

        keyStates[key] = false
    }

    override fun getLastKey(wait: Boolean): Int {
        if (wait) {
            while (true) {
                val key = lastKey
                if (key != 0) {
                    lastKey = 0
                    return key
                } else {
                    lock.wait()
                }
            }
        } else {
            return lastKey.also { lastKey = 0 }
        }
    }

    override fun isKeyPressed(key: Int): Boolean {
        return keyStates[key]
    }

    override fun getPressedKey(): Int {
        for (i in 1 until keyStates.size) {
            if (keyStates[i]) {
                return i
            }
        }
        return 0
    }

    override fun revalidateKey(key: Int) {
        if (keyStates[key]) {
            lastKey = key
        }
    }

    override fun revalidateAllKeys() {
        for (i in 0 until keyStates.size) {
            if (keyStates[i]) {
                lastKey = i
                break
            }
        }
    }
}