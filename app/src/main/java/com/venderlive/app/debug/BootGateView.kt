package com.venderlive.app.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * First screen, built with plain Android views only (no Compose, no Firebase),
 * so it always appears. It shows what the last run recorded and opens the real
 * app on demand. If the real app crashes, the next launch lands back here with
 * the trace on screen.
 */
class BootGateView(
    private val context: Context,
    private val report: String,
    private val canOpenApp: Boolean,
    private val onOpen: () -> Unit,
    private val onClear: () -> Unit,
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#0B1220"))
        val pad = (16 * resources.displayMetrics.density).toInt()
        setPadding(pad, pad, pad, pad)

        addView(TextView(context).apply {
            text = "VenderLive"
            setTextColor(Color.WHITE)
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
        })

        addView(TextView(context).apply {
            text = "Pemeriksaan awal. Salin teks di bawah lalu kirim ke pengembang."
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
        })
        addView(scroller, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
            topMargin = pad / 2
        })

        val buttons = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        buttons.addView(Button(context).apply {
            text = "Salin laporan"
            setOnClickListener {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("VenderLive", report))
                Toast.makeText(context, "Laporan disalin", Toast.LENGTH_SHORT).show()
            }
        }, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        if (canOpenApp) {
            buttons.addView(Button(context).apply {
                text = "Buka aplikasi"
                setOnClickListener {
                    CrashLog.clear(context)
                    CrashLog.clearSteps(context)
                    onClear()
                    onOpen()
                }
            }, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        addView(buttons, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = pad / 2
        })
    }
}
