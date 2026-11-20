package com.venderlive.app.ui.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venderlive.app.R

private val Teal = Color(0xFF1FD8B8)
private val TealSoft = Color(0xFFE6FAF5)
private val Ink = Color(0xFF1C1F2A)
private val Muted = Color(0xFF8B90A0)

data class ProfileStats(
    val friends: Int = 0,
    val following: Int = 0,
    val followers: Int = 0,
    val visitors: Int = 0,
)

private data class MenuItem(val icon: Int, val title: String, val trailing: String? = null, val dot: Boolean = false)

@Composable
fun ProfileScreen(
    nickname: String,
    displayId: String,
    gender: String? = null,
    stats: ProfileStats = ProfileStats(),
    onMenuClick: (String) -> Unit = {},
    onOpenProfile: () -> Unit = {},
) {
    val primaryMenu = listOf(
        MenuItem(R.drawable.ic_wallet, "Dompet"),
        MenuItem(R.drawable.ic_svip, "SVIP", "Buka kunci", dot = true),
        MenuItem(R.drawable.ic_noble, "Bangsawan", "Buka kunci"),
        MenuItem(R.drawable.ic_level, "Level"),
        MenuItem(R.drawable.ic_medal, "Medali"),
        MenuItem(R.drawable.ic_cp, "CP"),
        MenuItem(R.drawable.ic_relationship, "Hubungan"),
        MenuItem(R.drawable.ic_family, "Keluarga", "Bergabunglah sekarang"),
        MenuItem(R.drawable.ic_guard, "Penjaga"),
    )

    val inviteMenu = listOf(
        MenuItem(R.drawable.ic_invite, "Undang Teman", "Earn Coins"),
        MenuItem(R.drawable.ic_shop, "Toko"),
        MenuItem(R.drawable.ic_items, "Item Saya"),
    )
    val systemMenu = listOf(
        MenuItem(R.drawable.ic_feedback, "Umpan Balik"),
        MenuItem(R.drawable.ic_settings, "Pengaturan"),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F8)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Brush.verticalGradient(listOf(Teal, Color(0xFF7FE8D3), Color(0xFFF4F6F8)))),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            Spacer(Modifier.height(48.dp))
            ProfileHeader(nickname = nickname, displayId = displayId, gender = gender, onClick = onOpenProfile)
            Spacer(Modifier.height(18.dp))
            StatsRow(stats)
            Spacer(Modifier.height(18.dp))

            MenuCard(items = primaryMenu, onClick = onMenuClick)

            Spacer(Modifier.height(12.dp))
            MenuCard(items = inviteMenu, onClick = onMenuClick)
            Spacer(Modifier.height(12.dp))
            MenuCard(items = systemMenu, onClick = onMenuClick)
        }
    }
}

@Composable
private fun ProfileHeader(nickname: String, displayId: String, gender: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFFFF4D8D)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = nickname.take(1).uppercase(),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = nickname, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                if (gender == "male" || gender == "female") {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (gender == "male") Color(0xFF5AA9FF) else Color(0xFFFF7FB0)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (gender == "male") "♂" else "♀", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.28f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(text = "ID: $displayId", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatsRow(stats: ProfileStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatItem(stats.friends, "Teman-teman")
        StatItem(stats.following, "Mengikuti")
        StatItem(stats.followers, "Pengikut")
        StatItem(stats.visitors, "Pengunjung")
    }
}

@Composable
private fun StatItem(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$value", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(text = label, color = Ink.copy(alpha = 0.75f), fontSize = 12.sp)
    }
}

@Composable
private fun MenuCard(items: List<MenuItem>, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(item.title) }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(Modifier.width(16.dp))
                Text(text = item.title, color = Ink, fontSize = 15.sp, modifier = Modifier.weight(1f))

                if (item.dot) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF4D4F)),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (item.trailing != null) {
                    Text(text = item.trailing, color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                }
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp),
                )
            }
            if (index < items.lastIndex) {
                Box(
                    modifier = Modifier
                        .padding(start = 58.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF0F2F5)),
                )
            }
        }
    }
}
