package plodsoft.mygvm.file

import plodsoft.mygvm.util.readAll
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DefaultFileSystem(initialRoot: String) : FileSystem {
    private val openFiles = ArrayList<FileImpl>()

    override var root: String = initialRoot
        set(value) {
            val file = File(value)
            if (!file.isDirectory) {
                throw IllegalArgumentException("Not a directory")
            }
            field = value
        }

    override fun openFile(path: String, mode: Int): FileSystem.File {
        if (!path.startsWith("/")) {
            throw IllegalArgumentException("Path not starting with '/': $path")
        }

        val file = getNativeFile(path)

        if ((mode and FileSystem.CREATE) != 0) {
            file.createNewFile()
        }

        if (openFiles.find { it.nativeFile == file } !== null) {
            throw IOException("File already open: $path")
        }

        val canRead = (mode and FileSystem.READ) != 0
        val canWrite = (mode and FileSystem.WRITE) != 0

        if (!canRead && !canWrite) {
            throw IllegalArgumentException("At least one of READ and WRITE must be specified")
        }

        val f = FileImpl(file, file.readAll(), canRead, canWrite)
        openFiles += f
        return f
    }

    /* */
    override fun isValidDirectory(path: String): Boolean {
        return try {
            getNativeFile(path).isDirectory
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * 获取系统File对象. 若试图访问root路径外的文件, 抛出IllegalArgumentException异常
     *
     * @throws IllegalArgumentException 非法路径
     */
    private fun getNativeFile(path: String): File {
        val f = File(root + path)
        val s = f.canonicalPath
        if (!s.startsWith(root)) {
            throw IllegalArgumentException("Path outside Root folder")
        }
        return File(s)
    }

    override fun createDirectory(path: String): Boolean =
        getNativeFile(path).mkdir()

    override fun deleteFile(path: String): Boolean {
        val f = getNativeFile(path)
        if (openFiles.find { it.nativeFile == f } !== null) {
            return false
        }
        return f.delete()
    }

    override fun listFiles(path: String): Array<String> {
        return getNativeFile(path).list()
    }

    /* */
    private inner class FileImpl(val nativeFile: File,
                           private var data: ByteArray,
                           private val canRead: Boolean,
                           private val canWrite: Boolean)
        : FileSystem.File {

        /**
         * 文件是否修改过. 如果true则在关闭文件时要将文件内容写入系统
         */
        private var isDirty = false

        override val size: Int
            get() = data.size

        override var offset: Int = 0
            /**
             * @throws IllegalArgumentException
             */
            set(value) {
                if (value !in 0..size) {
                    throw IllegalArgumentException("Offset out of bounds: $value, size=$size")
                }
                field = value
            }

        override fun read(count: Int): ByteArray {
            val count1 = if (offset + count > size) size - offset else count
            offset += count1
            return data.copyOfRange(offset - count1, offset)
        }

        override fun write(bytes: ByteArray): Int {
            if (bytes.isEmpty()) {
                return 0
            }

            isDirty = true

            if (offset + bytes.size > size) {
                data = data.copyOf(offset + bytes.size)
            }

            System.arraycopy(bytes, 0, data, offset, bytes.size)

            offset += bytes.size

            return bytes.size
        }

        override fun close() {
            openFiles.remove(this)

            if (isDirty) {
                BufferedOutputStream(FileOutputStream(nativeFile)).use {
                    it.write(data)
                }
            }
        }

        override fun truncate() {
            data = ByteArray(0)
            offset = 0

            isDirty = true
        }
    }
}