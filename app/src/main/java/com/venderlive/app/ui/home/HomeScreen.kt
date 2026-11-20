package com.venderlive.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.venderlive.app.R
import com.venderlive.app.ui.profile.ComingSoonScreen
import com.venderlive.app.ui.profile.ProfileScreen
import com.venderlive.app.ui.profile.ProfileStats
import com.venderlive.app.ui.theme.BrandAccent
import com.venderlive.app.ui.theme.SurfaceCard

/**
 * Phase 1 skeleton: room list placeholder + bottom navigation.
 * Seat room (RoomAudioActivity equivalent) comes in phase 2.
 */
@Composable
fun HomeScreen(
    userId: String,
    nickname: String? = null,
    publicId: String? = null,
    gender: String? = null,
    authRepository: com.venderlive.app.data.repository.AuthRepository,
    onLogout: () -> Unit,
    onOpenRoom: (String) -> Unit = {},
    onOpenProfile: (String) -> Unit = {},
) {
    var tab by remember { mutableStateOf(0) }
    // Page opened from the Aku tab: null = none, "Pengaturan" = settings, else coming soon.
    var openPage by remember { mutableStateOf<String?>(null) }
    // The daily check-in rewards are not backed by the server yet, so the popup stays off.
    var showCheckIn by remember { mutableStateOf(false) }
    // A newer release published on GitHub turns into this dialog.
    var update by remember { mutableStateOf<com.venderlive.app.update.AvailableUpdate?>(null) }
    // Size of the small difference file, shown instead of the full APK size.
    var smallSize by remember { mutableStateOf<Long?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        // Asking once per launch is enough; a failed check is silent.
        val found = com.venderlive.app.update.UpdateChecker.check()
        update = found
        found?.let { smallSize = com.venderlive.app.update.PatchUpdate.smallSize(context, it.version) }
    }

    val tabs = listOf(
        "Beranda" to R.drawable.home_ic_tab_home,
        "Permainan" to R.drawable.home_ic_tab_game,
        "Momen" to R.drawable.home_ic_tab_moment,
        "Pesan" to R.drawable.home_ic_tab_msg,
        "Aku" to R.drawable.home_ic_tab_mine,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when (tab) {
                0 -> HomeFeed(onOpenRoom = onOpenRoom)
                1 -> Placeholder("Permainan segera hadir")
                2 -> MomentScreen()
                3 -> MessagesScreen()
                else -> {
                    val page = openPage
                    when (page) {
                        null -> ProfileScreen(
                            nickname = nickname?.takeIf { it.isNotBlank() }
                                ?: "user_" + userId.takeLast(6),
                            displayId = publicId?.takeIf { it.isNotBlank() } ?: userId,
                            gender = gender,
                            stats = ProfileStats(),
                            onMenuClick = { openPage = it },
                            onOpenProfile = {
                                val id = publicId?.takeIf { it.isNotBlank() } ?: return@ProfileScreen
                                onOpenProfile(id)
                            },
                        )
                        "SVIP" -> com.venderlive.app.ui.svip.SvipScreen(
                            onBack = { openPage = null },
                            onOpenWallet = { openPage = "Dompet" },
                        )
                        "Dompet" -> com.venderlive.app.ui.wallet.WalletScreen(
                            onBack = { openPage = null },
                        )
                        "Pengaturan" -> com.venderlive.app.ui.profile.SettingsHome(
                            repo = authRepository,
                            nickname = nickname?.takeIf { it.isNotBlank() } ?: "user_" + userId.takeLast(6),
                            displayId = publicId?.takeIf { it.isNotBlank() } ?: userId,
                            onBack = { openPage = null },
                            onLogout = onLogout,
                        )
                        else -> ComingSoonScreen(title = page, onBack = { openPage = null })
                    }
                }
            }
        }

        update?.let { new ->
            AlertDialog(
                onDismissRequest = { update = null },
                title = { Text("Versi ${new.version} sudah tersedia") },
                text = {
                    Column {
                        Text("Versi di HP kamu ${com.venderlive.app.BuildConfig.VERSION_NAME}.")
                        if (new.sizeBytes > 0) {
                            Spacer(Modifier.height(6.dp))
                            val bytes = smallSize ?: new.sizeBytes
                            Text(
                                (if (smallSize != null) "Unduhan kecil sekitar " else "Unduhan sekitar ") +
                                    "${"%.1f".format(bytes / 1024.0 / 1024.0)} MB.",
                            )
                        }
                        if (new.notes.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(new.notes.take(220), fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        com.venderlive.app.update.PatchUpdate.downloadAndInstall(context, new.version)
                        update = null
                    }) { Text("Perbarui sekarang") }
                },
                dismissButton = { TextButton(onClick = { update = null }) { Text("Nanti") } },
            )
        }

        if (showCheckIn) {
            DailyCheckInDialog(
                onClaim = { showCheckIn = false },
                onDismiss = { showCheckIn = false },
            )
        }

        NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
            tabs.forEachIndexed { index, (label, icon) ->
                NavigationBarItem(
                    selected = tab == index,
                    onClick = { tab = index; openPage = null },
                    icon = {
                        Box {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = label,
                                modifier = Modifier.size(24.dp),
                            )
                            if (index == 3 && unreadTotal() > 0) {
                                UnreadDot(
                                    unreadTotal(),
                                    Modifier.align(Alignment.TopEnd).padding(start = 14.dp).size(16.dp),
                                )
                            }
                        }
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = androidx.compose.ui.graphics.Color(0xFF2FD3B4),
                        selectedTextColor = androidx.compose.ui.graphics.Color(0xFF2A2A33),
                        unselectedIconColor = androidx.compose.ui.graphics.Color(0xFFC9C9D2),
                        unselectedTextColor = androidx.compose.ui.graphics.Color(0xFFC9C9D2),
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun RoomListPlaceholder(onOpenRoom: (String) -> Unit) {
    val demoRooms = listOf(
        "Room Santai Malam" to "12/8 seat",
        "Ngobrol Bareng" to "5/8 seat",
        "Music Corner" to "3/8 seat",
    )
    Column(modifier = Modifier.padding(20.dp)) {
        Text("Room Populer", style = MaterialTheme.typography.headlineLarge, color = BrandAccent)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(demoRooms) { (name, info) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .clickable { onOpenRoom(name) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text(info, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Daftar room akan diambil dari API VPS Anda: GET /v1/rooms",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun ProfilePlaceholder(userId: String, onLogout: () -> Unit) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text("Akun Saya", style = MaterialTheme.typography.headlineLarge, color = BrandAccent)
        Spacer(Modifier.height(12.dp))
        Text("User ID: $userId", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onLogout) { Text("Keluar", color = BrandAccent) }
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    }
}
