package com.venderlive.app.ui.wallet

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF1C1F2A)
private val Muted = Color(0xFF8B90A0)
private val Teal = Color(0xFF1CDBBA)
private val Gold = Color(0xFFF7C948)
private val Hot = Color(0xFFFF5A3C)
private val Link = Color(0xFF2E7BF6)

/** Where top-up problems are reported; the owner's support address. */
private const val SUPPORT_EMAIL = "developerex100@gmail.com"

private data class CoinPack(val amount: Long, val bonus: Long?, val price: Long)

// Prices follow the screenshot's scale. The last row on the reference showed an
// inconsistent price for 200,000,000, so it is priced proportionally here.
private val CoinPacks = listOf(
    CoinPack(2_000_000, null, 18_000),
    CoinPack(10_000_000, 250_000, 91_000),
    CoinPack(50_000_000, 3_125_000, 459_000),
    CoinPack(200_000_000, 25_000_000, 1_790_000),
)

private val DiamondPacks = listOf(
    CoinPack(20_000, null, 18_000),
    CoinPack(100_000, 2_500, 91_000),
    CoinPack(500_000, 31_250, 459_000),
    CoinPack(2_000_000, 250_000, 1_790_000),
)

/** Where the developer panel publishes the balances. */
private const val CONFIG_URL = "https://exbottod.github.io/liveroom-android/wallet.json"

internal data class WalletConfig(
    val coins: Long = 0L,
    val diamonds: Long = 0L,
    /** How many diamonds buy [coinsPerTrade] coins. */
    val diamondsPerTrade: Long = 100L,
    val coinsPerTrade: Long = 33L,
)

internal suspend fun fetchWalletConfig(): WalletConfig? = withContext(Dispatchers.IO) {
    runCatching {
        val url = URL("$CONFIG_URL?t=" + System.currentTimeMillis())
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("User-Agent", "VenderLive")
        }
        if (connection.responseCode != 200) return@runCatching null
        val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        WalletConfig(
            coins = json.optLong("coins", 0L),
            diamonds = json.optLong("diamonds", 0L),
            diamondsPerTrade = json.optLong("diamondsPerTrade", 100L).takeIf { it > 0 } ?: 100L,
            coinsPerTrade = json.optLong("coinsPerTrade", 33L).takeIf { it > 0 } ?: 33L,
        )
    }.getOrNull()
}

/** Diamond amounts offered for exchange, straight from the reference screen. */
private val DiamondTrades = listOf(
    10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L,
)

private fun thousands(value: Long): String =
    "%,d".format(value).replace(',', '.')

/**
 * The Dompet tab from the reference screenshot: coin and diamond balances,
 * the ticket vault, and the top-up packages with their prices.
 */
@Composable
fun WalletScreen(
    coinBalance: Long = 0L,
    diamondBalance: Long = 0L,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var diamonds by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf<String?>(null) }
    // Balances stay empty until the developer panel publishes them.
    var coins by remember { mutableStateOf(coinBalance) }
    var gems by remember { mutableStateOf(diamondBalance) }
    var config by remember { mutableStateOf(WalletConfig(coinBalance, diamondBalance)) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fetchWalletConfig()?.let {
            config = it
            coins = it.coins
            gems = it.diamonds
        }
    }

    BackHandler(onBack = onBack)

    message?.let {
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("Pertukaran") },
            text = { Text(it) },
            confirmButton = { TextButton(onClick = { message = null }) { Text("Oke") } },
        )
    }

    busy?.let {
        AlertDialog(
            onDismissRequest = { busy = null },
            title = { Text("Pengisian ulang") },
            text = { Text("Pembelian $it akan aktif setelah pembayaran terhubung. Untuk sekarang saldo belum bertambah.") },
            confirmButton = { TextButton(onClick = { busy = null }) { Text("Mengerti") } },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        // Top bar: back, title, Detail.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Ink)
            }
            Text(
                "Dompet", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            Text(
                "Detail", color = Muted, fontSize = 14.sp,
                modifier = Modifier.padding(end = 16.dp).clickable { busy = "riwayat transaksi" },
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // Koin / Berlian
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                CurrencyTab("Koin", !diamonds) { diamonds = false }
                Spacer(Modifier.width(26.dp))
                CurrencyTab("Berlian", diamonds) { diamonds = true }
            }
            Spacer(Modifier.height(18.dp))

            Text("Saldo saya", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))

            // Yellow balance banner.
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(
                        if (diamonds) {
                            Brush.horizontalGradient(listOf(Color(0xFF8E6BFF), Color(0xFF5B33E0)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFFFFE9A3), Color(0xFFFFD24D)))
                        },
                    )
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (diamonds) "Berlian" else "Koin",
                        color = if (diamonds) Color(0xFFEDE6FF) else Color(0xFF8A6A00),
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        thousands(if (diamonds) gems else coins),
                        color = if (diamonds) Color.White else Color(0xFF4A3700),
                        fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    )
                }
                Text(if (diamonds) "◆" else "★", color = Color(0xFFFFF6D8), fontSize = 40.sp)
            }
            Spacer(Modifier.height(14.dp))

            if (diamonds) {
                // Exchange diamonds for coins, as on the reference screen.
                Text("Tukar Berlian ke Koin", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${thousands(config.diamondsPerTrade)} Berlian untuk ${thousands(config.coinsPerTrade)} Koin",
                    color = Muted, fontSize = 12.sp,
                )
                Spacer(Modifier.height(12.dp))
                DiamondTrades.forEach { cost ->
                    val gain = cost * config.coinsPerTrade / config.diamondsPerTrade
                    TradeRow(cost = cost, gain = gain) {
                        if (gems < cost) {
                            message = "Berlianmu belum cukup. Butuh ${thousands(cost)} berlian."
                        } else {
                            gems -= cost
                            coins += gain
                            message = "Berhasil ditukar: ${thousands(cost)} berlian menjadi ${thousands(gain)} koin."
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Tukar Berlian ke Koin", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
            }

            // Ticket vault promo.
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF5F0FF))
                    .clickable { busy = "Brankas Tiket" }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE4D7FF)),
                    contentAlignment = Alignment.Center,
                ) { Text("🎁", fontSize = 18.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Brankas Tiket", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("Tiket dan hadiah untuk room", color = Muted, fontSize = 12.sp)
                }
                Box(
                    Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF7C4DFF))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) { Text("Buka", color = Color.White, fontSize = 12.sp) }
            }
            Spacer(Modifier.height(18.dp))

            (if (diamonds) DiamondPacks else CoinPacks).forEach { pack ->
                PackRow(
                    pack = pack,
                    diamond = diamonds,
                    onBuy = { busy = "${thousands(pack.amount)} ${if (diamonds) "berlian" else "koin"}" },
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(16.dp))
            // Support line, with the contact phrase clickable.
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    Modifier.size(18.dp).clip(CircleShape).background(Color(0xFFD9DDE6)),
                    contentAlignment = Alignment.Center,
                ) { Text("?", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Jika Anda mengalami masalah pengisian ulang, silakan", color = Muted, fontSize = 12.sp)
                    Text(
                        "hubungi layanan pelanggan",
                        color = Link, fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            val intent = Intent(
                                Intent.ACTION_SENDTO,
                                Uri.parse("mailto:$SUPPORT_EMAIL?subject=Masalah%20pengisian%20ulang%20VenderLive"),
                            )
                            runCatching { context.startActivity(intent) }
                        },
                    )
                }
            }
            Spacer(Modifier.height(24.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun CurrencyTab(label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            label,
            color = if (active) Ink else Muted,
            fontSize = if (active) 18.sp else 16.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.width(if (active) 26.dp else 0.dp).height(3.dp)
                .clip(RoundedCornerShape(2.dp)).background(if (active) Teal else Color.Transparent),
        )
    }
}

@Composable
private fun PackRow(pack: CoinPack, diamond: Boolean, onBuy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F8FA)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            Box(
                Modifier.size(52.dp).clip(CircleShape)
                    .background(if (diamond) Color(0xFFE7F3FF) else Color(0xFFFFF4D6)),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (diamond) "◆" else "★", color = if (diamond) Color(0xFF4EA8FF) else Gold, fontSize = 26.sp)
            }
            Box(
                Modifier.clip(RoundedCornerShape(topStart = 10.dp, bottomEnd = 8.dp))
                    .background(Hot).padding(horizontal = 4.dp, vertical = 1.dp),
            ) { Text("HOT!", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(thousands(pack.amount), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            pack.bonus?.let {
                Text("+${thousands(it)}", color = Teal, fontSize = 12.sp)
            }
        }
        Box(
            Modifier.clip(RoundedCornerShape(50)).background(Teal)
                .clickable(onClick = onBuy).padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text("IDR ${thousands(pack.price)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** One exchange row: the coins received and the diamond price button. */
@Composable
private fun TradeRow(cost: Long, gain: Long, onTrade: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F8FA)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFF4D6)),
            contentAlignment = Alignment.Center,
        ) { Text("★", color = Gold, fontSize = 24.sp) }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(thousands(gain), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Koin", color = Muted, fontSize = 12.sp)
        }
        Box(
            Modifier.clip(RoundedCornerShape(50)).background(Teal)
                .clickable(onClick = onTrade).padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text("Berlian ${thousands(cost)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
