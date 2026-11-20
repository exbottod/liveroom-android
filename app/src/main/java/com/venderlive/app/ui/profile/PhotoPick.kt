package com.venderlive.app.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/** Reads the picked photo, scales it down and returns a small JPEG ready to upload. */
internal fun readSquareJpeg(context: android.content.Context, uri: android.net.Uri): ByteArray? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        ?: return null
    ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
        bitmap.recycle()
        out.toByteArray()
    }
}.getOrNull()
