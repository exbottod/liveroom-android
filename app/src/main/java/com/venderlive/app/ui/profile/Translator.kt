package com.venderlive.app.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Languages the translator offers, keyed the way the service expects them. */
internal val TranslatorLanguages = listOf(
    "id" to "Indonesia", "en" to "English", "ms" to "Melayu", "zh-CN" to "中文",
    "ar" to "العربية", "hi" to "हिंदी", "bn" to "বাংলা", "th" to "ไทย",
    "vi" to "Tiếng Việt", "fil" to "Filipino", "tr" to "Türkçe", "es" to "Español",
    "pt" to "Português", "fr" to "Français", "de" to "Deutsch", "ja" to "日本語",
    "ko" to "한국어", "ru" to "Русский",
)

/**
 * Translates text for the people in a room who do not share a language yet.
 * The public service needs no key, so it works without any setup from the owner.
 */
internal suspend fun translateText(text: String, from: String, to: String): String =
    withContext(Dispatchers.IO) {
        val url = "https://api.mymemory.translated.net/get?q=" +
            URLEncoder.encode(text, "UTF-8") +
            "&langpair=" + URLEncoder.encode("$from|$to", "UTF-8")
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            setRequestProperty("User-Agent", "VenderLive")
        }
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        val response = json.optJSONObject("responseData")
        val translated = response?.optString("translatedText").orEmpty().trim()
        if (translated.isBlank()) {
            throw IllegalStateException(json.optString("responseDetails").ifBlank { "Terjemahan kosong" })
        }
        translated
    }

/** Pick the language to translate from or to. */
@Composable
private fun LanguageListDialog(
    title: String,
    selected: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                TranslatorLanguages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPick(code) }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(name, color = if (code == selected) Teal else Ink, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        if (code == selected) Text("✓", color = Teal, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } },
    )
}

/** The translator page reached from Bahasa > Penerjemah. */
@Composable
internal fun TranslatorPage(appLanguage: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Start from the app language to Indonesian, and flip if the app already is Indonesian.
    var from by remember { mutableStateOf(if (appLanguage.equals("id", true)) "en" else appLanguage) }
    var to by remember { mutableStateOf(if (appLanguage.equals("id", true)) "en" else "id") }
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var picking by remember { mutableStateOf<Int?>(null) }
    var copied by remember { mutableStateOf(false) }

    fun nameOf(code: String) = TranslatorLanguages.firstOrNull { it.first == code }?.second ?: code

    picking?.let { which ->
        LanguageListDialog(
            title = if (which == 0) "Terjemahkan dari" else "Terjemahkan ke",
            selected = if (which == 0) from else to,
            onDismiss = { picking = null },
            onPick = { code ->
                if (which == 0) from = code else to = code
                picking = null
            },
        )
    }

    Page("Penerjemah", onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Terjemahkan pesan atau nama untuk teman di room yang bahasanya berbeda.",
                color = Muted, fontSize = 13.sp,
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LanguageChip(nameOf(from), Modifier.weight(1f)) { picking = 0 }
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp).size(34.dp)
                        .clip(RoundedCornerShape(50)).background(Color(0xFFF0F3F6)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.ArrowDownward, "Tukar bahasa",
                        tint = Ink, modifier = Modifier.size(18.dp),
                    )
                }
                LanguageChip(nameOf(to), Modifier.weight(1f)) { picking = 1 }
            }

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.length <= 800) input = it },
                label = { Text("Teks (${nameOf(from)})") },
                minLines = 3,
                supportingText = { Text("${input.length}/800") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(if (input.isBlank() || busy) Color(0xFFBFC6D2) else Teal)
                    .clickable(enabled = input.isNotBlank() && !busy) {
                        busy = true
                        error = null
                        copied = false
                        scope.launch {
                            try {
                                output = translateText(input.trim(), from, to)
                            } catch (e: Exception) {
                                error = e.message ?: "Gagal menerjemahkan"
                            } finally {
                                busy = false
                            }
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (busy) "Menerjemahkan..." else "Terjemahkan", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Color(0xFFE5484D), fontSize = 13.sp)
            }

            if (output.isNotBlank()) {
                Spacer(Modifier.height(18.dp))
                Text("Hasil", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF4F6F8))
                        .padding(14.dp),
                ) {
                    Text(output, color = Ink, fontSize = 15.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFEDEFF3))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Terjemahan", output))
                                copied = true
                            }
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                    ) { Text(if (copied) "Tersalin" else "Salin", color = Ink, fontSize = 13.sp) }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFEDEFF3))
                            .clickable {
                                // Hand the two languages over and clear the result.
                                val oldFrom = from
                                from = to
                                to = oldFrom
                                input = output
                                output = ""
                            }
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                    ) { Text("Tukar arah", color = Ink, fontSize = 13.sp) }
                }
            }
            Spacer(Modifier.height(30.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun LanguageChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFF4F6F8))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = Ink, fontSize = 14.sp) }
}
