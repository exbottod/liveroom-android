package com.venderlive.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shown on launch when the previous run crashed, with a copy button so the
 *  trace can be sent straight from the phone. */
@Composable
fun CrashScreen(text: String, onClear: () -> Unit) {
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(16.dp),
    ) {
        Text(
            text = "Laporan terakhir",
            color = Color.White,
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Aplikasi berhenti di percobaan sebelumnya. Salin teks ini lalu kirim ke pengembang.",
            color = Color(0xFF9FB3C8),
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF111C2E))
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = text,
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { clipboard.setText(AnnotatedString(text)) },
            ) { Text("Salin") }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    clipboard.setText(AnnotatedString(text))
                    onClear()
                },
            ) { Text("Salin & mulai ulang") }
        }
    }
}
