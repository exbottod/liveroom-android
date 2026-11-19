package com.venderlive.app.debug

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sends short startup/crash notes to a public relay so the developer can read
 * them even when the app dies before drawing anything. No user data is sent:
 * only the version, the device model, the startup steps and the error text.
 */
object Beacon {

    private const val TOPIC = "venderlive-diag-6aff0f1e"

    fun sendAsync(tag: String, body: String) {
        runCatching { Thread { post(tag, body) }.start() }
    }

    /** Used by the crash handler: blocks briefly so the note leaves the device. */
    fun sendBlocking(tag: String, body: String) {
        runCatching { post(tag, body) }
    }

    private fun post(tag: String, body: String) {
        runCatching {
            val connection = URL("https://ntfy.sh/$TOPIC").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("Title", tag)
            connection.setRequestProperty("Priority", "high")
            connection.outputStream.use { it.write(body.toByteArray()) }
            connection.responseCode
            connection.disconnect()
        }
    }

    fun deviceLine(context: Context): String = buildString {
        appendLine("Versi: ${com.venderlive.app.BuildConfig.VERSION_NAME} (${com.venderlive.app.BuildConfig.VERSION_CODE})")
        appendLine("Perangkat: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}, Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
        appendLine("ABI: ${android.os.Build.SUPPORTED_ABIS.joinToString()}")
        appendLine("Aplikasi: ${context.packageName}")
    }
}
