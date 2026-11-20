package com.venderlive.app.ui.svip

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val Ink = Color(0xFF1C1F2A)
private val Muted = Color(0xFF8B90A0)
private val Teal = Color(0xFF1CDBBA)
private val Gold = Color(0xFFF3C24B)
private const val SVIP_URL = "https://exbottod.github.io/liveroom-android/svip.json"

/** One SVIP tier as published by the developer panel. */
internal data class SvipLevel(
    val level: Int,
    val price: Long,
    val score: Long,
    val weeklyCoins: Long,
    val medalUrl: String,
    val labelUrl: String,
    val frameUrl: String,
)

private const val FIRESTORE_URL =
    "https://firestore.googleapis.com/v1/projects/test-app-94b28/databases/(default)/documents/pub/svip"

private fun parseFirestoreLevels(root: JSONObject): List<SvipLevel> {
    // Firestore wraps every value: {"integerValue":"5"}, {"stringValue":"x"}, arrays under values.
    fun num(field: JSONObject?, key: String): Long {
        val value = field?.optJSONObject("mapValue")?.optJSONObject("fields")?.optJSONObject(key) ?: return 0L
        return value.optString("integerValue").toLongOrNull()
            ?: value.optDouble("doubleValue", 0.0).toLong()
    }
    fun text(field: JSONObject?, key: String): String =
        field?.optJSONObject("mapValue")?.optJSONObject("fields")?.optJSONObject(key)?.optString("stringValue").orEmpty()

    val values = root.optJSONObject("fields")?.optJSONObject("levels")
        ?.optJSONObject("arrayValue")?.optJSONArray("values") ?: return emptyList()
    return (0 until values.length()).mapNotNull { index ->
        val item = values.optJSONObject(index) ?: return@mapNotNull null
        SvipLevel(
            level = num(item, "level").toInt().takeIf { it > 0 } ?: (index + 1),
            price = num(item, "price"),
            score = num(item, "score"),
            weeklyCoins = num(item, "weeklyCoins"),
            medalUrl = text(item, "medalUrl"),
            labelUrl = text(item, "labelUrl"),
            frameUrl = text(item, "frameUrl"),
        )
    }
}

private fun getText(url: String): String? = runCatching {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 10000
        readTimeout = 10000
        setRequestProperty("User-Agent", "VenderLive")
    }
    if (connection.responseCode != 200) null else connection.inputStream.bufferedReader().use { it.readText() }
}.getOrNull()

/** The panel writes to Firestore; the old svip.json stays as a fallback. */
internal suspend fun fetchSvipLevels(): List<SvipLevel> = withContext(Dispatchers.IO) {
    val fromDatabase = getText(FIRESTORE_URL)
        ?.let { runCatching { parseFirestoreLevels(JSONObject(it)) }.getOrDefault(emptyList()) }
        .orEmpty()
    if (fromDatabase.isNotEmpty()) return@withContext fromDatabase

    runCatching {
        val body = getText("$SVIP_URL?t=" + System.currentTimeMillis()) ?: return@runCatching emptyList()
        val list = JSONObject(body).optJSONArray("levels") ?: return@runCatching emptyList()
        (0 until list.length()).mapNotNull { index ->
            val item = list.optJSONObject(index) ?: return@mapNotNull null
            SvipLevel(
                level = item.optInt("level", index + 1),
                price = item.optLong("price", 0L),
                score = item.optLong("score", 0L),
                weeklyCoins = item.optLong("weeklyCoins", 0L),
                medalUrl = item.optString("medalUrl").orEmpty(),
                labelUrl = item.optString("labelUrl").orEmpty(),
                frameUrl = item.optString("frameUrl").orEmpty(),
            )
        }
    }.getOrDefault(emptyList())
}

/** Downloads a picture once and keeps it for the life of the screen. */
@Composable
private fun RemotePicture(url: String, size: Int) {
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(url) {
        if (url.isBlank()) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("User-Agent", "VenderLive")
                }
                BitmapFactory.decodeStream(connection.inputStream)
            }.getOrNull()
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(), contentDescription = null,
            contentScale = ContentScale.Fit, modifier = Modifier.size(size.dp),
        )
    }
}

private fun thousands(value: Long): String = "%,d".format(value).replace(',', '.')

private data class Privilege(val icon: String, val title: String)

private val Privileges = listOf(
    Privilege("🏷️", "Label SVIP"),
    Privilege("🏅", "Medali SVIP"),
    Privilege("🖼️", "Bingkai SVIP"),
    Privilege("🪙", "Hadiah Koin Mingguan"),
    Privilege("🎁", "Keuntungan Game"),
    Privilege("📅", "Hadiah Check-in Ganda"),
    Privilege("✨", "Avatar Animasi"),
    Privilege("🛡️", "Perlindungan Larangan Chat"),
    Privilege("🔗", "Slot Hubungan Gratis"),
    Privilege("👻", "Sembunyikan Kehadiran di Room"),
    Privilege("🔇", "Abaikan Pesan"),
    Privilege("🎙️", "Perlindungan dari Pengeluaran Mic"),
    Privilege("🎧", "Super Mic"),
    Privilege("💬", "Gambar Chat Publik"),
    Privilege("📌", "Penyematan Chat Publik"),
    Privilege("🖌️", "Sampul Room Animasi"),
    Privilege("🔕", "Abaikan Suara"),
    Privilege("🚪", "Perlindungan Pengusiran"),
    Privilege("🎬", "Tema Room Animasi"),
    Privilege("🔤", "ID Huruf Kustom"),
    Privilege("🐉", "Efek Masuk Mic"),
    Privilege("👏", "Efek Sambutan Mic"),
)

/** The SVIP tab: tiers across the top, the emblem, then the privilege grid. */
@Composable
fun SvipScreen(onBack: () -> Unit, onOpenWallet: () -> Unit = {}) {
    var levels by remember { mutableStateOf<List<SvipLevel>>(emptyList()) }
    var selected by remember { mutableStateOf(1) }
    var note by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        levels = fetchSvipLevels()
        levels.firstOrNull()?.let { selected = it.level }
    }

    BackHandler(onBack = onBack)

    note?.let {
        AlertDialog(
            onDismissRequest = { note = null },
            title = { Text("SVIP") },
            text = { Text(it) },
            confirmButton = { TextButton(onClick = { note = null }) { Text("Oke") } },
        )
    }

    val current = levels.firstOrNull { it.level == selected }
        ?: SvipLevel(selected, 0L, 0L, 0L, "", "", "")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF15171F)).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
            }
            Text(
                "SVIP", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { note = "Pengaturan SVIP segera hadir." }) {
                Icon(Icons.Filled.Settings, "Pengaturan", tint = Color(0xFFD7B66A))
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Tier tabs, scrollable when the panel publishes many levels.
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                (if (levels.isEmpty()) listOf(SvipLevel(1, 0, 0, 0, "", "", "")) else levels).forEach { item ->
                    val active = item.level == selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selected = item.level },
                    ) {
                        Text(
                            "SVIP${item.level}",
                            color = if (active) Color(0xFFFFE3A3) else Color(0xFF8B90A0),
                            fontSize = if (active) 17.sp else 15.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.width(if (active) 30.dp else 0.dp).height(3.dp)
                                .clip(RoundedCornerShape(2.dp)).background(if (active) Gold else Color.Transparent),
                        )
                    }
                }
            }

            // The emblem with the tier label the panel uploaded.
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(150.dp).clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF3A2E18), Color(0xFF15171F))),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (current.medalUrl.isNotBlank()) {
                        RemotePicture(current.medalUrl, 130)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🦌", fontSize = 54.sp)
                            Text("SVIP", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (current.labelUrl.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    RemotePicture(current.labelUrl, 96)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Membership status box.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Color(0xFF232633))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF343A4B)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (current.frameUrl.isNotBlank()) {
                        RemotePicture(current.frameUrl, 42)
                    } else {
                        Text("👤", fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tidak Ada SVIP", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Riwayat Musim >", color = Color(0xFF9AA0B0), fontSize = 12.sp,
                        modifier = Modifier.clickable { note = "Riwayat musim muncul setelah ada pembelian SVIP." },
                    )
                }
                Box(
                    Modifier.clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFF7D07A), Color(0xFFD9A23A))))
                        .clickable {
                            note = "Isi Ulang membuka Dompet. Pembayaran SVIP belum terhubung."
                            onOpenWallet()
                        }
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                ) { Text("Isi Ulang", color = Color(0xFF3A2A05), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(10.dp))

            // Progress towards the next tier.
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                Text(
                    "${thousands(current.score)} poin lagi untuk naik ke SVIP${current.level}",
                    color = Color(0xFFE7C97F), fontSize = 13.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text("Musim depan: Tidak Ada SVIP", color = Color(0xFF8B90A0), fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2C3040)),
                ) {
                    Box(
                        Modifier.fillMaxWidth(0.02f).height(6.dp).clip(RoundedCornerShape(3.dp))
                            .background(Gold),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Harga SVIP${current.level}: IDR ${thousands(current.price)}",
                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                )
                if (current.weeklyCoins > 0) {
                    Text(
                        "Hadiah koin mingguan ${thousands(current.weeklyCoins)}",
                        color = Teal, fontSize = 12.sp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Hak istimewa SVIP${current.level} (${Privileges.size}/48)",
                color = Color(0xFFFFE3A3), fontSize = 14.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            // Privilege grid, three across like the reference.
            Privileges.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItems.forEach { privilege ->
                        Column(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF232633)).padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(privilege.icon, fontSize = 22.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                privilege.title, color = Color(0xFFC9CEDA), fontSize = 10.sp,
                                textAlign = TextAlign.Center, maxLines = 2,
                            )
                        }
                    }
                    repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Spacer(Modifier.height(24.dp).navigationBarsPadding())
        }
    }
}
