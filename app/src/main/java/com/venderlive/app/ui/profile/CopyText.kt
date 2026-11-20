package com.venderlive.app.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

/**
 * Copies a value to the phone clipboard and says so. Used for the user ID, which people
 * need to share but could not select on screen.
 */
fun copyToClipboard(context: Context, label: String, value: String) {
    if (value.isBlank()) return
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(context, "$label $value disalin", Toast.LENGTH_SHORT).show()
    }
}
