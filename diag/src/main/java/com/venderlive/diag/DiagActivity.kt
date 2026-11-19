package com.venderlive.diag

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest

private val Context.diagStore by preferencesDataStore("diag")

/**
 * Minimal test app: no Compose in the shell, every subsystem checked separately
 * and the result printed on screen. Built to find out why the main app closes
 * on a vivo V15 (Android 9).
 */
class DiagActivity : Activity() {

    private val lines = StringBuilder()
    private lateinit var output: TextView
    private val main = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        output = TextView(this).apply {
            setTextColor(Color.parseColor("#E2E8F0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = android.graphics.Typeface.MONOSPACE
            setTextIsSelectable(true)
            setPadding(24, 24, 24, 24)
        }

        val scroller = ScrollView(this).apply { addView(output) }
        val copy = Button(this)
        copy.text = "Salin hasil"
        copy.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("VenderLive uji", lines.toString()))
            copy.text = "Tersalin"
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0B1220"))
            addView(scroller, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(copy, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            })
        }
        setContentView(root)

        add("Activity tampil: OK (inti aplikasi jalan)")
        add("Perangkat: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        add("ABI: ${Build.SUPPORTED_ABIS.joinToString()}")

        checkSignature()
        checkDataStore()
        checkCompose()
        checkFirebase()
    }

    private fun add(line: String) {
        lines.appendLine(line)
        main.post { output.text = lines.toString() }
    }

    private fun checkSignature() {
        runCatching {
            val pm = packageManager
            val info = if (Build.VERSION.SDK_INT >= 28) {
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }
            val bytes = if (Build.VERSION.SDK_INT >= 28) {
                info.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
            } else {
                @Suppress("DEPRECATION")
                info.signatures?.firstOrNull()?.toByteArray()
            }
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes ?: ByteArray(0))
            add("Tanda tangan: " + digest.joinToString(":") { "%02X".format(it) }.take(29) + "...")
        }.onFailure { add("Tanda tangan: GAGAL ${it.javaClass.simpleName}: ${it.message}") }
    }

    private fun checkDataStore() {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                val key = stringPreferencesKey("ping")
                applicationContext.diagStore.edit { it[key] = "ok" }
                applicationContext.diagStore.data.first()[key]
            }.onSuccess { add("Penyimpanan (DataStore): OK -> $it") }
                .onFailure { add("Penyimpanan (DataStore): GAGAL ${it.javaClass.name}: ${it.message}") }
        }
    }

    private fun checkCompose() {
        runCatching {
            val view = ComposeView(this).apply {
                setContent { Box { Text("ok") } }
            }
            val host = LinearLayout(this)
            host.addView(view, ViewGroup.LayoutParams(1, 1))
            // Composing happens on measure, so force one small pass off screen.
            host.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(1, android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(1, android.view.View.MeasureSpec.EXACTLY),
            )
            host.layout(0, 0, 1, 1)
            add("Compose: OK")
        }.onFailure {
            add("Compose: GAGAL ${it.javaClass.name}: ${it.message}")
        }
    }

    private fun checkFirebase() {
        runCatching {
            val app = FirebaseApp.getApps(this).firstOrNull()
                ?: FirebaseApp.initializeApp(
                    this,
                    FirebaseOptions.Builder()
                        .setApplicationId("1:383929756819:android:2a6cc966ba67730bf4f166")
                        .setApiKey("AIzaSyAn5YG1LeeICQurRn_qyQtkSgPaup_Azd8")
                        .setProjectId("test-app-94b28")
                        .setStorageBucket("test-app-94b28.firebasestorage.app")
                        .build(),
                )
            add("Firebase: OK (${app?.name})")
            runCatching {
                val auth = FirebaseAuth.getInstance(app)
                add("Firebase Auth: OK (uid=${auth.currentUser?.uid ?: "belum login"})")
            }.onFailure {
                add("Firebase Auth: GAGAL ${it.javaClass.name}: ${it.message}")
            }
        }.onFailure {
            add("Firebase: GAGAL ${it.javaClass.name}: ${it.message}")
        }
    }
}
