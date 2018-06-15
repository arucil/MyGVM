package plodsoft.mygvm

import com.nikhaldimann.inieditor.IniEditor
import plodsoft.mygvm.util.ColorUtil
import java.io.IOException
import java.lang.NullPointerException

object Config {

    @JvmStatic
    var steps = 0; private set

    @JvmStatic
    var delay = 0; private set

    @JvmStatic
    var isDelayEnabled = false; private set

    @JvmStatic
    var backgroundColor = 0; private set

    @JvmStatic
    var foregroundColor = 0; private set

    @JvmStatic
    var pixelScale = 0; private set

    @JvmStatic
    var configLoadingException: Exception? = null; private set

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val ini = IniEditor()
        try {
            ini.load("mygvm.ini")
        } catch (e: IOException) {
            configLoadingException = e
            return
        }

        try {
            steps = Integer.parseInt(ini.get("Run", "steps"))
        } catch (e: NumberFormatException) {
            configLoadingException = e
            return
        } catch (e: NullPointerException) {
            configLoadingException = e
            return
        }

        try {
            delay = Integer.parseInt(ini.get("Run", "delay"))
        } catch (e: NullPointerException) {
            configLoadingException = e
            return
        } catch (e: NumberFormatException) {
            configLoadingException = e
            return
        }

        try {
            isDelayEnabled = java.lang.Boolean.parseBoolean(ini.get("Run", "delayEnabled"))
        } catch (e: NullPointerException) {
            configLoadingException = e
            return
        } catch (e: NumberFormatException) {
            configLoadingException = e
            return
        }

        try {
            backgroundColor = ColorUtil.parse(ini.get("UI", "backgroundColor"))
        } catch (e: NullPointerException) {
            configLoadingException = e
            return
        } catch (e: IllegalArgumentException) {
            configLoadingException = e
            return
        }

        try {
            foregroundColor = ColorUtil.parse(ini.get("UI", "foregroundColor"))
        } catch (e: NullPointerException) {
            configLoadingException = e
            return
        } catch (e: IllegalArgumentException) {
            configLoadingException = e
            return
        }

        try {
            pixelScale = Integer.parseInt(ini.get("UI", "pixelScale"))
        } catch (e: NullPointerException) {
            configLoadingException = e
        } catch (e: IllegalArgumentException) {
            configLoadingException = e
        }
    }
}