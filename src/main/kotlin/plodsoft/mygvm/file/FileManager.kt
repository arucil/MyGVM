package plodsoft.mygvm.file

import java.io.IOException
import java.util.*

/**
 * 管理文件操作：切换工作目录、创建目录、删除文件、打开文件、获取文件列表
 */
class FileManager(private val fileSystem: FileSystem) {
    companion object {
        const val MAX_OPEN_FILES = 3

        private const val FILE_HANDLE_MARKER = 0x80
        private const val FILE_HANDLE_MASK = 0x7f
    }

    /**
     * 当前工作目录. 总是以/结尾
     */
    var workingDir = "/"
        /**
         * @throws IllegalArgumentException
         */
        set(value) {
            val path = getAbsolutePath(value)
            if (!fileSystem.isValidDirectory(path)) {
                throw IllegalArgumentException("Not a valid directory")
            }
            field = path
            if (!field.endsWith('/')) {
                field += '/'
            }
        }

    private val openFiles = Array<FileSystem.File?>(MAX_OPEN_FILES) { null }

    /**
     * 获取虚拟机绝对路径
     */
    private fun getAbsolutePath(path: String) = if (path.startsWith('/')) path else workingDir + path

    /**
     * 关闭所有打开的文件
     */
    fun closeAllFiles() {
        openFiles.forEach {
            it?.close()
        }
        Arrays.fill(openFiles, null)
    }

    /**
     * 打开文件
     *
     * @param mode r, rb, r+, rb+, w, wb, w+, wb+, a, ab, a+, ab+
     *
     * @return file handle, 0x80 ~ 0x82
     *
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    fun openFile(path: String, mode: String): Int {
        var index = -1
        for (i in 0 until MAX_OPEN_FILES) {
            if (openFiles[i] === null) {
                index = i
                break
            }
        }
        if (index == -1) {
            throw IOException("No available file handle")
        }

        var truncate = false
        var append = false
        val _mode = when (mode) {
            "r", "rb" -> FileSystem.READ
            "r+", "rb+" -> FileSystem.READ or FileSystem.WRITE
            "w", "wb" -> {
                truncate = true
                FileSystem.WRITE or FileSystem.CREATE
            }
            "w+", "wb+" -> {
                truncate = true
                FileSystem.READ or FileSystem.WRITE or FileSystem.CREATE
            }
            "a", "ab" -> {
                append = true
                FileSystem.WRITE or FileSystem.CREATE
            }
            "a+", "ab+" -> {
                append = true
                FileSystem.READ or FileSystem.WRITE or FileSystem.CREATE
            }
            else -> throw IllegalArgumentException("Invalid mode: $mode")
        }

        val _path = getAbsolutePath(path)

        val file = fileSystem.openFile(_path, _mode)

        if (truncate) {
            file.truncate()
        }

        if (append) {
            file.offset = file.size
        }

        openFiles[index] = file
        return index or FILE_HANDLE_MARKER
    }

    /**
     * 关闭文件
     *
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    fun closeFile(fileHandle: Int) {
        val file = getFile(fileHandle)
        file.close()

        for (i in 0 until MAX_OPEN_FILES) {
            if (openFiles[i] === file) {
                openFiles[i] = null
                break
            }
        }
    }

    /**
     * 设置文件指针
     *
     * @throws IllegalArgumentException
     */
    fun setFileOffset(fileHandle: Int, offset: Int) {
        getFile(fileHandle).offset = offset
    }

    /**
     * 获取文件指着
     *
     * @throws IllegalArgumentException
     */
    fun getFileOffset(fileHandle: Int): Int =
        getFile(fileHandle).offset

    /**
     * 获取文件大小
     *
     * @throws IllegalArgumentException
     */
    fun getFileSize(fileHandle: Int): Int =
        getFile(fileHandle).size

    /**
     * 检查文件指针是否EOF
     *
     * @throws IllegalArgumentException
     */
    fun isEOF(fileHandle: Int): Boolean {
        val file = getFile(fileHandle)
        return file.offset >= file.size
    }

    /**
     * 读取文件
     *
     * @param count 要读取的字节数
     *
     * @throws IllegalArgumentException
     */
    fun readFile(fileHandle: Int, count: Int): ByteArray =
        getFile(fileHandle).read(count)

    /**
     * 写入文件
     *
     * @return 写入的字节数
     *
     * @throws IllegalArgumentException
     */
    fun writeFile(fileHandle: Int, data: ByteArray): Int =
        getFile(fileHandle).write(data)

    /**
     * 检查文件handle是合法的且文件是打开的. 返回File对象
     *
     * @throws IllegalArgumentException 非法路径或文件已关闭
     */
    private fun getFile(fileHandle: Int): FileSystem.File {
        if ((fileHandle and FILE_HANDLE_MARKER) == 0 || (fileHandle and FILE_HANDLE_MASK) !in 0 until MAX_OPEN_FILES) {
            throw IllegalArgumentException("Illegal file handle: $fileHandle")
        }

        val index = fileHandle and FILE_HANDLE_MASK
        val file = openFiles[index]
        if (file === null) {
            throw IllegalArgumentException("File is closed: $fileHandle")
        }

        return file
    }

    /**
     * 创建目录
     *
     * @return 是否创建成功
     *
     * @throws IllegalArgumentException 非法路径
     */
    fun createDirectory(path: String): Boolean {
        return fileSystem.createDirectory(getAbsolutePath(path))
    }

    /**
     * 删除文件
     *
     * @return 是否删除成功
     *
     * @throws IllegalArgumentException 非法路径
     */
    fun deleteFile(path: String): Boolean {
        return fileSystem.deleteFile(getAbsolutePath(path))
    }

    /**
     * 列出当前工作目录下的所有文件/目录
     */
    fun listFiles(): Array<String> {
        return fileSystem.listFiles(workingDir)
    }
}