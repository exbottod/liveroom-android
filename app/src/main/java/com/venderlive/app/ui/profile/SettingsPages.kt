package com.venderlive.app.ui.profile

import android.app.Activity
import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venderlive.app.data.GoogleToken
import com.venderlive.app.data.model.BlockedUser
import com.venderlive.app.data.repository.AuthRepository
import com.venderlive.app.ui.login.InAppWebScreen
import kotlinx.coroutines.launch

private val Ink = Color(0xFF1C1F2A)
private val Muted = Color(0xFF8B90A0)
private val Page = Color(0xFFF7F7F8)
private val Teal = Color(0xFF1CDBBA)
private val Danger = Color(0xFFE5484D)
private val Blue = Color(0xFF3B6FE0)

private const val TERMS_URL = "https://exbottod.github.io/liveroom-android/agreement.html"
private const val PRIVACY_URL = "https://exbottod.github.io/liveroom-android/privacy.html"
private const val CHILD_SAFETY_URL = "https://exbottod.github.io/liveroom-android/child-safety.html"

/** The page opened from the Settings list. */
private enum class SettingsPage { ACCOUNT, LANGUAGE, BLOCKED, ABOUT }

@Composable
private fun Page(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    BackHandler(onBack = onBack)
    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Ink)
            }
            Text(
                title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        content()
    }
}

@Composable
private fun Row1(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    valueColor: Color = Muted,
    titleColor: Color = Ink,
    arrow: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = titleColor, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (value != null) Text(value, color = valueColor, fontSize = 15.sp)
        if (arrow) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFFB5B8C2))
        }
    }
}

@Composable
private fun Gap() = Box(Modifier.fillMaxWidth().height(10.dp).background(Page))

/** Pengaturan: the settings list, and the pages opened from it. */
@Composable
fun SettingsHome(
    repo: AuthRepository,
    nickname: String,
    displayId: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var page by remember { mutableStateOf<SettingsPage?>(null) }
    var notifications by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var cacheBytes by remember { mutableStateOf(cacheSize(context)) }
    var language by remember { mutableStateOf(prefs.getString("language", "id") ?: "id") }
    var confirmCache by remember { mutableStateOf(false) }

    when (page) {
        SettingsPage.ACCOUNT -> { AccountSecurityPage(repo, nickname, displayId, onBack = { page = null }, onDeleted = onLogout); return }
        SettingsPage.LANGUAGE -> {
            LanguagePage(selected = language, onBack = { page = null }) {
                language = it
                prefs.edit().putString("language", it).apply()
                page = null
            }
            return
        }
        SettingsPage.BLOCKED -> { BlockedPage(repo, onBack = { page = null }); return }
        SettingsPage.ABOUT -> { AboutPage(onBack = { page = null }); return }
        null -> Unit
    }

    Page("Pengaturan", onBack) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row1("Akun & Keamanan", onClick = { page = SettingsPage.ACCOUNT })
            Gap()
            Row1("Bahasa", onClick = { page = SettingsPage.LANGUAGE })
            Row1("Daftar blokir", onClick = { page = SettingsPage.BLOCKED })
            Gap()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Pemberitahuan", color = Ink, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = notifications,
                    onCheckedChange = { notifications = it; prefs.edit().putBoolean("notifications", it).apply() },
                    colors = SwitchDefaults.colors(checkedTrackColor = Teal, checkedThumbColor = Color.White),
                )
            }
            Row1("Hapus cache", value = formatSize(cacheBytes), valueColor = Ink, onClick = { confirmCache = true })
            Row1("Tentang", onClick = { page = SettingsPage.ABOUT })

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 28.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Page)
                    .clickable(onClick = onLogout)
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Keluar", color = Danger, fontSize = 18.sp) }

            Text(
                "Versi saat ini ${versionName(context)}",
                color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp),
            )
        }
    }

    if (confirmCache) {
        AlertDialog(
            onDismissRequest = { confirmCache = false },
            title = { Text("Hapus cache?") },
            text = { Text("File sementara akan dihapus. Akun dan data kamu tidak ikut terhapus.") },
            confirmButton = {
                TextButton(onClick = {
                    clearCache(context); cacheBytes = cacheSize(context); confirmCache = false
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { confirmCache = false }) { Text("Batal") } },
        )
    }
}

// ---------------------------------------------------------------------------
// Akun & Keamanan
// ---------------------------------------------------------------------------

@Composable
private fun AccountSecurityPage(
    repo: AuthRepository,
    nickname: String,
    displayId: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var links by remember { mutableStateOf<Map<String, Boolean>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var comingSoon by remember { mutableStateOf<String?>(null) }

    suspend fun refresh() {
        links = runCatching { repo.accountLinks() }.getOrElse { message = it.message; emptyMap() }
    }
    LaunchedEffect(Unit) { refresh() }

    comingSoon?.let { name ->
        ComingSoonScreen(title = name, onBack = { comingSoon = null })
        return
    }

    fun statusRow(provider: String, label: String, supported: Boolean): @Composable () -> Unit = {
        val linked = links?.get(provider) == true
        Row1(
            title = label,
            value = if (linked) "Tertaut" else "Tidak Tertaut",
            valueColor = if (linked) Blue else Danger,
            onClick = {
                if (!supported) { comingSoon = label; return@Row1 }
                if (busy) return@Row1
                scope.launch {
                    busy = true
                    message = null
                    try {
                        if (linked) {
                            repo.unlink(provider)
                        } else if (provider == "google") {
                            val activity = context as? Activity ?: error("Layar ini tidak bisa membuka Google")
                            val token = GoogleToken.pick(activity)
                            if (token != null) repo.linkGoogle(token)
                        } else {
                            comingSoon = label
                        }
                        refresh()
                    } catch (e: Exception) {
                        message = e.message ?: "Gagal memproses"
                    } finally {
                        busy = false
                    }
                }
            },
        )
    }

    Page("Akun & Keamanan", onBack) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Teal),
                    contentAlignment = Alignment.Center,
                ) { Text(nickname.take(1).uppercase(), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(nickname, color = Ink, fontSize = 22.sp)
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.clip(RoundedCornerShape(50)).background(Teal).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("ID: $displayId", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
            Box(Modifier.fillMaxWidth().background(Page).padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text("Pengikatan akun", color = Ink, fontSize = 18.sp)
            }
            statusRow("google", "Google", true)()
            statusRow("facebook", "Facebook", false)()
            statusRow("telegram", "Telegram", false)()
            statusRow("phone", "Telepon", false)()
            message?.let {
                Text(it, color = Danger, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }
            Gap()
            Row1("Hapus Akun", titleColor = Danger, arrow = false, onClick = { confirmDelete = true })
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Hapus akun?") },
            text = { Text("Akun dan ID $displayId akan dihapus permanen dan tidak bisa dikembalikan.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch {
                        try { repo.deleteAccount(); onDeleted() } catch (e: Exception) { message = e.message }
                    }
                }) { Text("Hapus", color = Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Batal") } },
        )
    }
}

// ---------------------------------------------------------------------------
// Bahasa
// ---------------------------------------------------------------------------

private val Languages = listOf(
    "en" to "English", "hi" to "हिंदी", "bn" to "বাংলা", "id" to "Bahasa Indonesia",
    "fil" to "Filipino", "th" to "แบบไทย", "vi" to "Tiếng Việt", "tr" to "Türkçe",
    "ar" to "العربية", "ur" to "اردو", "es" to "Español", "pt" to "Português",
)

@Composable
private fun LanguagePage(selected: String, onBack: () -> Unit, onPick: (String) -> Unit) {
    Page("Bahasa", onBack) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Languages.forEach { (code, name) ->
                val on = code == selected
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onPick(code) }
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(name, color = if (on) Teal else Ink, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    if (on) {
                        Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF12D66A)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Text(
                "Terjemahan antarmuka lainnya segera hadir. Pilihanmu sudah tersimpan.",
                color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Daftar blokir
// ---------------------------------------------------------------------------

@Composable
private fun BlockedPage(repo: AuthRepository, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<BlockedUser>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        users = runCatching { repo.blockedUsers() }.getOrElse { error = it.message; emptyList() }
    }
    LaunchedEffect(Unit) { load() }

    Page("Daftar blokir", onBack) {
        val list = users
        when {
            list == null -> Box(Modifier.fillMaxSize())
            list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error ?: "Belum ada hasil", color = Muted, fontSize = 20.sp)
                }
            }
            else -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                list.forEach { u ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(u.nickname ?: "Pengguna", color = Ink, fontSize = 16.sp)
                            Text("ID: ${u.publicId ?: u.id}", color = Muted, fontSize = 12.sp)
                        }
                        TextButton(onClick = { scope.launch { runCatching { repo.unblock(u.id) }; load() } }) {
                            Text("Buka blokir", color = Teal)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tentang
// ---------------------------------------------------------------------------

@Composable
private fun AboutPage(onBack: () -> Unit) {
    var web by remember { mutableStateOf<Pair<String, String>?>(null) }
    web?.let { (title, url) ->
        InAppWebScreen(title = title, url = url, onClose = { web = null })
        return
    }
    Page("Tentang", onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(190.dp).background(Color(0xFFF1FBFA)),
                contentAlignment = Alignment.Center,
            ) {
                Text("VenderLive", color = Teal, fontSize = 36.sp, fontWeight = FontWeight.Black)
            }
            Row1("Perjanjian Pengguna", arrow = false, onClick = { web = "Perjanjian Pengguna" to TERMS_URL })
            Row1("Kebijakan privasi", arrow = false, onClick = { web = "Kebijakan Privasi" to PRIVACY_URL })
            Row1("Kebijakan Keselamatan Anak", arrow = false, onClick = { web = "Kebijakan Keselamatan Anak" to CHILD_SAFETY_URL })
        }
    }
}

// ---------------------------------------------------------------------------
// helpers
// ---------------------------------------------------------------------------

private fun versionName(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "-"

private fun cacheSize(context: Context): Long =
    (context.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }) +
        (context.externalCacheDir?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L)

private fun clearCache(context: Context) {
    runCatching { context.cacheDir.listFiles()?.forEach { it.deleteRecursively() } }
    runCatching { context.externalCacheDir?.listFiles()?.forEach { it.deleteRecursively() } }
}

private fun formatSize(bytes: Long): String =
    if (bytes < 1024 * 1024) String.format(java.util.Locale.US, "%.2fKB", bytes / 1024.0)
    else String.format(java.util.Locale.US, "%.2fMB", bytes / 1024.0 / 1024.0)
