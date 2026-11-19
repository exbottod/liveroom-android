package com.venderlive.app.debug

import android.content.Context
import com.venderlive.app.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Catches the last crash and keeps its stack trace on disk, so the crash can be
 * read from inside the app on the next launch (no adb or logcat needed).
 */
object CrashLog {

    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { write(app, thread, error) }
            previous?.uncaughtException(thread, error)
        }
    }

    private fun write(context: Context, thread: Thread, error: Throwable) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val text = buildString {
            appendLine("Waktu: $stamp")
            appendLine("Thread: ${thread.name}")
            appendLine(
                "Perangkat: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}, " +
                    "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
            )
            appendLine("Versi: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("API: ${BuildConfig.API_BASE_URL}")
            appendLine()
            appendLine(error.stackTraceToString())
        }
        runCatching { File(context.filesDir, FILE_NAME).writeText(text) }
    }

    fun read(context: Context): String? =
        runCatching {
            File(context.filesDir, FILE_NAME)
                .takeIf { it.exists() }
                ?.readText()
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()

    fun clear(context: Context) {
        runCatching { File(context.filesDir, FILE_NAME).delete() }
    }
}
