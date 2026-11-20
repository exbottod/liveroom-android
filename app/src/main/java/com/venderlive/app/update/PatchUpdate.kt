package com.venderlive.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Small updates: instead of the full ~15 MB APK the app downloads a bsdiff
 * difference file (a few hundred KB to a few MB), rebuilds the new APK from the
 * one installed on the phone, checks its fingerprint and installs it. When no
 * patch exists, it falls back to the full APK.
 */
object PatchUpdate {
    private const val BASE = "https://exbottod.github.io/liveroom-android/unduh"
    private const val BUILD_URL = "$BASE/build-info.json"
    private const val PATCH_INFO_URL = "$BASE/patch-info.json"
    private const val PATCH_DIR = "$BASE/patch"
    private const val FULL_APK =
        "https://github.com/exbottod/liveroom-android/releases/latest/download/venderlive.apk"

    /** One difference file: installed version -> published version. */
    data class Step(val file: String, val size: Long, val sha256: String)

    data class Info(val version: String, val notes: String, val step: Step?)

    private fun read(url: String): String? = runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 20000
            setRequestProperty("User-Agent", "VenderLive")
        }
        if (connection.responseCode != 200) return null
        connection.inputStream.bufferedReader().use { it.readText() }
    }.getOrNull()

    fun installedVersion(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0"
    }.getOrDefault("0")

    private fun isNewer(remote: String, local: String): Boolean {
        val left = remote.split(".").mapNotNull { it.toIntOrNull() }
        val right = local.split(".").mapNotNull { it.toIntOrNull() }
        for (index in 0 until maxOf(left.size, right.size)) {
            val a = left.getOrElse(index) { 0 }
            val b = right.getOrElse(index) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    private fun stepFor(target: String, installed: String): Step? {
        val body = read(PATCH_INFO_URL) ?: return null
        val list = runCatching { JSONObject(body).optJSONArray("patch") }.getOrNull() ?: return null
        for (index in 0 until list.length()) {
            val item = list.optJSONObject(index) ?: continue
            if (item.optString("dari") != installed || item.optString("ke") != target) continue
            val file = item.optString("file")
            if (file.isBlank()) continue
            return Step(file, item.optLong("ukuran", 0L), item.optString("sha256"))
        }
        return null
    }

    /** The published build, or null when the phone is already up to date. */
    fun check(context: Context): Info? {
        val body = read(BUILD_URL) ?: return null
        val json = runCatching { JSONObject(body) }.getOrNull() ?: return null
        val version = json.optString("versi")
        if (version.isBlank()) return null
        val installed = installedVersion(context)
        if (!isNewer(version, installed)) return null
        return Info(version, json.optString("catatan"), stepFor(version, installed))
    }

    /** Size of the small download in bytes, when the server has a patch for this phone. */
    fun smallSize(context: Context, target: String): Long? =
        stepFor(target, installedVersion(context))?.size?.takeIf { it > 0 }

    private fun sha256(file: File): String = runCatching {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(1 shl 16)
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrDefault("")

    private fun download(url: String, target: File): Boolean = runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 60000
            setRequestProperty("User-Agent", "VenderLive")
        }
        if (connection.responseCode != 200) return false
        target.parentFile?.mkdirs()
        connection.inputStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output, 1 shl 16) }
        }
        target.length() > 0
    }.getOrDefault(false)

    private fun updatesDir(context: Context): File =
        File(context.getExternalFilesDir(null) ?: context.filesDir, "updates").also { it.mkdirs() }

    private fun installedApk(context: Context): File? = runCatching {
        val source = File(context.applicationInfo.sourceDir)
        val copy = File(context.cacheDir, "installed.apk")
        if (!copy.exists() || copy.length() != source.length()) {
            source.inputStream().use { input -> copy.outputStream().use { output -> input.copyTo(output) } }
        }
        copy
    }.getOrNull()


    private val main = Handler(Looper.getMainLooper())

    /** Android only allows a Toast and an activity launch from the main thread. */
    private fun toast(context: Context, text: String, long: Boolean = false) {
        main.post {
            runCatching { Toast.makeText(context, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show() }
        }
    }

    private fun install(context: Context, apk: File) {
        main.post { startInstall(context, apk) }
    }

    private fun startInstall(context: Context, apk: File) {
        runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Tries the small patch first and only falls back to the full download when
     * there is no patch, the file is wrong, or rebuilding fails.
     */
    fun downloadAndInstall(context: Context, target: String) {
        val app = context.applicationContext
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            // A crash here used to close the whole app, so every step is guarded and
            // every failure ends with the full APK download instead.
            try {
                val step = stepFor(target, installedVersion(app))
                if (step != null) {
                    toast(app, "Mengunduh pembaruan kecil...")
                    val patch = File(app.cacheDir, "update.bsdiff")
                    val base = installedApk(app)
                    val downloaded = base != null && download("$PATCH_DIR/${step.file}", patch) &&
                        (step.sha256.isBlank() || sha256(patch).equals(step.sha256, ignoreCase = true))
                    if (downloaded && base != null) {
                        val rebuilt = File(updatesDir(app), "venderlive-$target.apk")
                        val result = runCatching { BsPatch.terapkan(base, patch, rebuilt) }.getOrNull()
                        if (result?.ok == true && rebuilt.exists() && rebuilt.length() > 2_000_000L) {
                            install(app, rebuilt)
                            return@launch
                        }
                        runCatching { rebuilt.delete() }
                    }
                    toast(app, "Pembaruan kecil gagal, mengunduh versi penuh...", long = true)
                }
                val full = File(updatesDir(app), "venderlive-$target.apk")
                if (download(FULL_APK, full)) {
                    install(app, full)
                } else {
                    toast(app, "Unduhan gagal. Periksa koneksi lalu coba lagi.", long = true)
                }
            } catch (error: Throwable) {
                // Never let a background failure reach the default handler.
                toast(app, "Pembaruan gagal: " + (error.message ?: error.javaClass.simpleName), long = true)
            }
        }
    }

}
