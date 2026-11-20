package com.venderlive.app.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.venderlive.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** The newest release published on GitHub, with the APK we should download. */
data class AvailableUpdate(
    val version: String,
    val notes: String,
    val apkUrl: String,
    val sizeBytes: Long,
)

/**
 * Checks the public release feed, so people who installed the app learn about a new
 * build from inside the app instead of hearing about it from someone else.
 */
object UpdateChecker {

    private const val LATEST =
        "https://api.github.com/repos/exbottod/liveroom-android/releases/latest"

    suspend fun check(): AvailableUpdate? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(LATEST).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "VenderLive")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.optString("tag_name").trim()
            val version = tag.removePrefix("v").removePrefix("V")
            val notes = json.optString("body").trim()

            // The small universal APK is uploaded next to each release.
            var apkUrl = ""
            var size = 0L
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name") == "venderlive.apk") {
                        apkUrl = asset.optString("browser_download_url")
                        size = asset.optLong("size")
                        break
                    }
                }
            }
            if (version.isBlank() || apkUrl.isBlank()) return@runCatching null
            if (!isNewer(version, BuildConfig.VERSION_NAME)) return@runCatching null
            AvailableUpdate(version, notes, apkUrl, size)
        }.getOrNull()
    }

    /** True when [remote] is a later version than the installed [local]. */
    private fun isNewer(remote: String, local: String): Boolean {
        val a = remote.split(".").mapNotNull { it.toIntOrNull() }
        val b = local.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    /** Downloads the new APK and opens the system installer when it is ready. */
    fun downloadAndInstall(context: Context, update: AvailableUpdate) {
        // Android 8+ needs explicit permission to install an app from this one.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
            return
        }

        val dir = File(context.getExternalFilesDir(null), "updates").apply { mkdirs() }
        val target = File(dir, "venderlive-${update.version}.apk")
        if (target.exists()) target.delete()

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(update.apkUrl))
            .setTitle("VenderLive ${update.version}")
            .setDescription("Mengunduh pembaruan aplikasi")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(context, null, "updates/${target.name}")
            .setMimeType("application/vnd.android.package-archive")
        val downloadId = manager.enqueue(request)

        // When the download finishes, hand the file to the installer.
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) != downloadId) return
                runCatching { ctx.unregisterReceiver(this) }
                if (!target.exists()) return
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    ctx, "${ctx.packageName}.updates", target,
                )
                val install = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runCatching { ctx.startActivity(install) }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED,
            )
        } else {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }
}
