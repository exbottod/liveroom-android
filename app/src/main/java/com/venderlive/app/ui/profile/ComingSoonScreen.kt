package com.venderlive.app.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF1C1F2A)
private val Muted = Color(0xFF8B90A0)

/** A page header with a back arrow, shared by the pages opened from the Aku tab. */
@Composable
private fun PageScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    BackHandler(onBack = onBack)
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F6F8)).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Ink)
            }
            Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        content()
    }
}

/** Placeholder for a menu whose content is not built yet. */
@Composable
fun ComingSoonScreen(title: String, onBack: () -> Unit) {
    PageScaffold(title, onBack) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Segera hadir", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Halaman $title sedang disiapkan.", color = Muted, fontSize = 14.sp)
            }
        }
    }
}

/** Pengaturan: logout lives here; the other settings will be added later. */
@Composable
fun SettingsScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    PageScaffold("Pengaturan", onBack) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Pengaturan lainnya segera hadir.",
                color = Muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Keluar", color = Color(0xFFE5484D), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
