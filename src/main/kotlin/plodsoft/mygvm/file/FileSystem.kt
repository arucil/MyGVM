package plodsoft.mygvm.file

interface FileSystem {
    companion object {
        const val READ = 1
        const val WRITE = 2

        /**
         * 如果文件不存在则创建文件
         */
        const val CREATE = 4
    }

    /**
     * 虚拟机根目录对应的系统路径
     *
     * 必须是唯一路径(File.getCanonicalPath()得到的路径)
     */
    var root: String

    /**
     * 打开文件
     *
     * @param path 虚拟机路径. 必须以/开头
     * @param mode READ和WRITE至少要有一项
     *
     * @throws java.io.IOException 文件打开失败
     * @throws IllegalArgumentException 参数错误
     */
    fun openFile(path: String, mode: Int): File

    /**
     * 检查path是否合法的目录.
     *
     * 若path试图访问root之外的目录, 或目录不存在, 则返回false
     */
    fun isValidDirectory(path: String): Boolean

    /**
     * 创建目录
     *
     * @return 是否创建成功
     *
     * @throws IllegalArgumentException 非法的目录路径
     */
    fun createDirectory(path: String): Boolean

    /**
     * 删除文件
     *
     * @throws IllegalArgumentException 非法的文件路径
     */
    fun deleteFile(path: String): Boolean

    /**
     * 列出path目录下的文件
     *
     * @throws IllegalArgumentException
     */
    fun listFiles(path: String): Array<String>

    /**
     * 文件接口
     */
    interface File {
        /**
         * 文件大小
         */
        val size: Int

        /**
         * 文件指针
         */
        var offset: Int

        /**
         * 写入文件
         *
         * @return 写入的字节数
         */
        fun write(bytes: ByteArray): Int

        /**
         * 读取文件
         *
         * @param count 要读取的字节数
         */
        fun read(count: Int): ByteArray

        /**
         * 关闭文件
         */
        fun close()

        /**
         * 清空文件内容
         */
        fun truncate()
    }
}