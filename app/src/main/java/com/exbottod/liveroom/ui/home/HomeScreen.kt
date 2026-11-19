package com.exbottod.liveroom.ui.home

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exbottod.liveroom.ui.theme.BrandAccent
import com.exbottod.liveroom.ui.theme.SurfaceCard

/**
 * Phase 1 skeleton: room list placeholder + bottom navigation.
 * Seat room (RoomAudioActivity equivalent) comes in phase 2.
 */
@Composable
fun HomeScreen(
    userId: String,
    onLogout: () -> Unit,
) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Room", "Pesan", "Saya")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when (tab) {
                0 -> RoomListPlaceholder()
                1 -> Placeholder("Chat & antrean pesan")
                else -> ProfilePlaceholder(userId, onLogout)
            }
        }

        NavigationBar(containerColor = SurfaceCard) {
            tabs.forEachIndexed { index, label ->
                NavigationBarItem(
                    selected = tab == index,
                    onClick = { tab = index },
                    icon = { Text(label.take(1), color = BrandAccent, fontWeight = FontWeight.Bold) },
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
private fun RoomListPlaceholder() {
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
