package com.venderlive.app.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venderlive.app.data.model.OtherProfile
import com.venderlive.app.data.repository.AuthRepository
import com.venderlive.app.ui.home.EmptyState
import kotlinx.coroutines.launch

private val Ink = Color(0xFF1C1F2A)
private val Muted = Color(0xFF8B90A0)
private val Teal = Color(0xFF1CDBBA)
private val Purple = Color(0xFF8E7BFF)
private val Soft = Color(0xFFF4F6F8)
private val Danger = Color(0xFFE5484D)

private enum class ProfileTab(val label: String) { PROFILE("Profil"), HONOR("Kehormatan"), MOMENTS("Momen") }

/**
 * The profile page reached by tapping a nickname anywhere (a room seat, a list, the Aku
 * tab). [publicId] identifies whose profile it is; the pencil only appears on our own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    repo: AuthRepository,
    publicId: String,
    isMe: Boolean,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(ProfileTab.PROFILE) }
    var profile by remember { mutableStateOf<OtherProfile?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        profile = runCatching { repo.profile(publicId) }
            .onFailure { error = it.message }
            .getOrNull()
    }
    LaunchedEffect(publicId) { load() }

    BackHandler(onBack = onBack)

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        // Top bar: back, then gallery and pencil when it is our own profile.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Ink)
            }
            Spacer(Modifier.weight(1f))
            if (isMe) {
                IconButton(onClick = { message = "Galeri foto segera hadir" }) {
                    Icon(Icons.Filled.PhotoLibrary, "Galeri", tint = Ink)
                }
                IconButton(onClick = { editing = true }) {
                    Icon(Icons.Filled.Edit, "Ubah profil", tint = Ink)
                }
            }
        }

        val p = profile
        if (p == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "Memuat profil...", color = Muted, fontSize = 15.sp, textAlign = TextAlign.Center)
            }
            return@Column
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Avatar on the left, three counters on the right.
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(url = p.avatar, label = p.nickname.orEmpty(), size = 74.dp)
                Spacer(Modifier.width(16.dp))
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Counter(p.stats.friends, "Teman-teman")
                    Counter(p.stats.following, "Mengikuti")
                    Counter(p.stats.followers, "Pengikut")
                }
            }

            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                ProfileTab.entries.forEach { item ->
                    val on = item == tab
                    Column(
                        modifier = Modifier.weight(1f).clickable { tab = item }.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            item.label,
                            color = if (on) Ink else Muted,
                            fontSize = 16.sp,
                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .width(if (on) 26.dp else 0.dp)
                                .height(3.dp)
                                .background(if (on) Ink else Color.Transparent, RoundedCornerShape(3.dp)),
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEDEFF2)))

            when (tab) {
                ProfileTab.PROFILE -> ProfilePane(p, isMe) { message = "Undang teman segera hadir" }
                ProfileTab.HONOR -> EmptyState("Belum ada kehormatan", "Lencana dan medali akan muncul di sini.")
                ProfileTab.MOMENTS -> EmptyState("Belum ada momen", "Momen yang diunggah dari tab Momen akan muncul di sini.")
            }
            Spacer(Modifier.height(40.dp).navigationBarsPadding())
        }
    }

    message?.let { text ->
        AlertDialog(
            onDismissRequest = { message = null },
            confirmButton = { TextButton(onClick = { message = null }) { Text("Mengerti") } },
            text = { Text(text) },
        )
    }

    if (editing) {
        EditProfileScreen(
            repo = repo,
            profile = profile,
            onBack = { editing = false },
            onSaved = {
                editing = false
                scope.launch { load() }
            },
            onPhotoChanged = { scope.launch { load() } },
        )
        return
    }
}

@Composable
private fun Counter(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun ProfilePane(p: OtherProfile, isMe: Boolean, onInvite: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(14.dp))
        // Name, gender mark, flag, online dot
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                p.nickname ?: "Pengguna",
                color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            if (p.gender == "male" || p.gender == "female") {
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(20.dp).clip(CircleShape)
                        .background(if (p.gender == "male") Color(0xFF4C9AFF) else Color(0xFFFF7FB0)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (p.gender == "male") "♂" else "♀", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            val flag = flagOf(p.country)
            if (flag.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Text(flag, fontSize = 16.sp)
            }
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF22C55E)))
        }
        Spacer(Modifier.height(8.dp))

        // ID and level badges
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFEDEFF3))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) { Text("ID: ${p.publicId ?: p.id}", color = Ink, fontSize = 12.sp) }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.clip(RoundedCornerShape(6.dp))
                    .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFFA98BFF), Color(0xFF7C6BFF))))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) { Text("Lv.${p.level}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        }

        val countryName = countryNameOf(p.country)
        val bio = p.bio?.takeIf { it.isNotBlank() }
        if (bio != null || countryName != null) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    bio ?: "NEW USER",
                    color = Ink, fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
                if (countryName != null) {
                    Spacer(Modifier.width(6.dp))
                    Text(countryName, color = Muted, fontSize = 13.sp)
                    val flag = flagOf(p.country)
                    if (flag.isNotEmpty()) { Spacer(Modifier.width(4.dp)); Text(flag, fontSize = 13.sp) }
                }
            }
        }

        if (isMe) {
            Spacer(Modifier.height(10.dp))
            Text(
                if (p.bio.isNullOrBlank() || p.country.isNullOrBlank())
                    "Ketuk ikon pensil untuk melengkapi foto, nama, bio, dan bendera."
                else "Ketuk ikon pensil untuk mengubah profil.",
                color = Muted, fontSize = 12.sp,
            )
        }

        // CP (couple) slot
        Spacer(Modifier.height(22.dp))
        SectionTitle("CP")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Soft).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(url = p.avatar, label = p.nickname.orEmpty(), size = 56.dp)
                Spacer(Modifier.height(6.dp))
                Text(
                    (p.nickname ?: "").take(12), color = Ink, fontSize = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("❤ 〰️ ❤", color = Color(0xFFFF6B8A), fontSize = 18.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape).border(1.dp, Color(0xFFD8DDE5), CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) { Text("🐱", fontSize = 22.sp) }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(50)).background(Purple).clickable(onClick = onInvite)
                        .padding(horizontal = 18.dp, vertical = 5.dp),
                ) { Text("Undang", color = Color.White, fontSize = 12.sp) }
            }
        }

        // Hubungan slots
        Spacer(Modifier.height(22.dp))
        SectionTitle("Hubungan (0/9)")
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) {
                Box(
                    Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(12.dp))
                        .background(Soft).border(1.dp, Color(0xFFE4E8EE), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("+", color = Color(0xFFB5B8C2), fontSize = 26.sp) }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier.size(15.dp).clip(CircleShape).background(Color(0xFFE7EAF0)),
            contentAlignment = Alignment.Center,
        ) { Text("?", color = Muted, fontSize = 10.sp) }
    }
}
