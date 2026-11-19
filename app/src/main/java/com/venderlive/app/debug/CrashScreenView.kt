package com.venderlive.app.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * Crash report screen built with plain Android views, not Compose, so it still
 * renders when the crash is inside Compose itself.
 */
class CrashScreenView(
    private val context: Context,
    private val text: String,
    private val onCleared: () -> Unit,
) : LinearLayout(context) {

    private val report: String = text

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#0B1220"))
        val pad = (16 * resources.displayMetrics.density).toInt()
        setPadding(pad, pad, pad, pad)

        addView(TextView(context).apply {
            text = "Laporan terakhir"
            setTextColor(Color.WHITE)
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
        })

        addView(TextView(context).apply {
            text = "Aplikasi berhenti di percobaan sebelumnya. Ketuk Salin, lalu kirim teksnya ke pengembang."
            setTextColor(Color.parseColor("#9FB3C8"))
            textSize = 13f
            setPadding(0, pad / 3, 0, pad / 2)
        })

        val scroller = ScrollView(context)
        scroller.addView(TextView(context).apply {
            text = report
            setTextColor(Color.parseColor("#E2E8F0"))
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
            setPadding(pad / 2, pad / 2, pad / 2, pad / 2)
            setBackgroundColor(Color.parseColor("#111C2E"))
            inputType = InputType.TYPE_NULL
        })
        addView(
            scroller,
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                topMargin = pad / 2
            },
        )

        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val copy = Button(context).apply {
            text = "Salin"
            setOnClickListener {
                copyToClipboard(report)
                Toast.makeText(context, "Laporan disalin", Toast.LENGTH_SHORT).show()
            }
        }
        val restart = Button(context).apply {
            text = "Salin & mulai ulang"
            setOnClickListener {
                copyToClipboard(report)
                CrashLog.clear(context)
                CrashLog.clearSteps(context)
                onCleared()
            }
        }
        row.addView(copy, LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(restart, LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(row, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = pad / 2
        })
    }

    private fun copyToClipboard(value: String) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("VenderLive crash", value))
    }
}
