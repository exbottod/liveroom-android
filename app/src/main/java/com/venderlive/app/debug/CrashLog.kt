package com.venderlive.app.debug

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
    private const val STEPS_FILE = "startup_steps.txt"
    private const val PREV_STEPS_FILE = "startup_steps_prev.txt"

    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { write(app, thread, error) }
            // The screen may never draw, but a notification survives the crash.
            runCatching { notify(app, error) }
            previous?.uncaughtException(thread, error)
        }
    }

    /** Reports an error that was caught in code, the same way a crash is reported. */
    fun report(context: Context, error: Throwable) {
        val app = context.applicationContext
        runCatching { write(app, Thread.currentThread(), error) }
        runCatching { notify(app, error) }
    }

    private fun notify(context: Context, error: Throwable) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "venderlive_crash"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Laporan kesalahan", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        val body = buildString {
            appendLine("${error.javaClass.name}: ${error.message}")
            appendLine()
            appendLine(steps(context))
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(context, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
        val notification = builder
            .setContentTitle("VenderLive berhenti (v${BuildConfig.VERSION_NAME})")
            .setContentText("${error.javaClass.simpleName}: ${error.message}".take(120))
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
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
            appendLine()
            appendLine("Jejak startup:")
            appendLine(steps(context))
        }
        runCatching { File(context.filesDir, FILE_NAME).writeText(text) }
    }

    /** Records how far startup got, so a crash (even a native one) leaves a trail. */
    fun step(context: Context, label: String) {
        val stamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        runCatching {
            File(context.filesDir, STEPS_FILE).appendText("$stamp $label\n")
        }
    }

    fun steps(context: Context): String =
        runCatching {
            File(context.filesDir, STEPS_FILE).takeIf { it.exists() }?.readText().orEmpty()
        }.getOrDefault("")

    fun clearSteps(context: Context) {
        runCatching { File(context.filesDir, STEPS_FILE).delete() }
        runCatching { File(context.filesDir, PREV_STEPS_FILE).delete() }
    }

    /** Keeps the previous run's steps so a run that died without a Java crash is visible. */
    fun rotateSteps(context: Context) {
        runCatching {
            val current = File(context.filesDir, STEPS_FILE)
            if (current.exists() && current.length() > 0) {
                current.copyTo(File(context.filesDir, PREV_STEPS_FILE), overwrite = true)
            }
            current.delete()
        }
    }

    fun previousSteps(context: Context): String =
        runCatching {
            File(context.filesDir, PREV_STEPS_FILE).takeIf { it.exists() }?.readText().orEmpty()
        }.getOrDefault("")

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
