package com.venderlive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.venderlive.app.debug.CrashLog
import com.venderlive.app.debug.CrashScreenView
import com.venderlive.app.navigation.AppNav
import com.venderlive.app.ui.theme.VenderLiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLog.step(this, "MainActivity.onCreate")

        // Plain-view crash report: works even when Compose is what crashed.
        val trace = CrashLog.read(this)
        if (trace != null) {
            CrashLog.step(this, "menampilkan laporan crash")
            setContentView(
                CrashScreenView(
                    context = this,
                    text = trace,
                    onCleared = { recreate() },
                ),
            )
            return
        }

        // A run that ended without a Java crash (killed by the system) still leaves steps.
        val prev = CrashLog.previousSteps(this)
        if (prev.isNotBlank() && !prev.contains("startup sukses")) {
            setContentView(
                CrashScreenView(
                    context = this,
                    text = "Startup terakhir tidak selesai.\n\nJejak langkah:\n" + prev,
                    onCleared = { recreate() },
                ),
            )
            return
        }

        enableEdgeToEdge()
        CrashLog.step(this, "setContent mulai")
        setContent {
            VenderLiveTheme {
                AppNav()
            }
        }
        CrashLog.step(this, "setContent dipanggil")
        window.decorView.post { CrashLog.step(this, "startup sukses") }
    }
}
