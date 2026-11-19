package com.venderlive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.venderlive.app.debug.Beacon
import com.venderlive.app.debug.BootGateView
import com.venderlive.app.debug.CrashLog
import com.venderlive.app.navigation.AppNav
import com.venderlive.app.ui.theme.VenderLiveTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLog.step(this, "MainActivity.onCreate")
        Beacon.sendAsync("ACTIVITY v${BuildConfig.VERSION_NAME}", Beacon.deviceLine(this) + "\nMainActivity.onCreate jalan")
        // Open straight into the app, the way viza and rezo do: no gate screen.
        runCatching { openCompose() }.onFailure { error ->
            CrashLog.report(this, error)
            // Last resort: a plain text view with the error, no Compose involved.
            setContentView(
                BootGateView(
                    context = this,
                    report = "Gagal menampilkan layar awal.\n\n${error.javaClass.name}: ${error.message}",
                    canOpenApp = false,
                    onOpen = {},
                    onClear = {},
                ),
            )
        }
    }

    /** Plain-view gate: always renders, and reports what the last run did. */
    private fun showGate() {
        val trace = CrashLog.read(this)
        val prevSteps = CrashLog.previousSteps(this)
        val currentSteps = CrashLog.steps(this)
        val diedLastTime = prevSteps.isNotBlank() && !prevSteps.contains("startup sukses")

        val report = buildString {
            appendLine("Versi: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Perangkat: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}, Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
            appendLine("API: ${BuildConfig.API_BASE_URL}")
            appendLine()
            when {
                trace != null -> {
                    appendLine("== LAPORAN CRASH TERAKHIR ==")
                    appendLine(trace)
                }
                diedLastTime -> {
                    appendLine("== STARTUP TERAKHIR TIDAK SELESAI ==")
                    appendLine(prevSteps)
                }
                else -> {
                    appendLine("Tidak ada crash tercatat. Tekan Buka aplikasi untuk menampilkan VenderLive.")
                }
            }
            appendLine()
            appendLine("== LANGKAH SEKARANG ==")
            appendLine(currentSteps)
        }

        setContentView(
            BootGateView(
                context = this,
                report = report,
                canOpenApp = true,
                onOpen = { openCompose() },
                onClear = { recreate() },
            ),
        )
    }

    private fun openCompose() {
        Beacon.sendAsync("OPEN v${BuildConfig.VERSION_NAME}", "Menampilkan Compose")
        enableEdgeToEdge()
        CrashLog.step(this, "setContent mulai")
        setContent {
            VenderLiveTheme {
                AppNav()
            }
        }
        CrashLog.step(this, "setContent dipanggil")
        window.decorView.post {
            CrashLog.step(this, "startup sukses")
            Beacon.sendAsync("OK v${BuildConfig.VERSION_NAME}", "Compose tampil tanpa error")
        }
    }
}
