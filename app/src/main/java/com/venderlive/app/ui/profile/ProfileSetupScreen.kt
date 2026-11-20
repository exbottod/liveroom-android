package com.venderlive.app.ui.profile

import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Teal = Color(0xFF1FD8B3)
private val TealSoft = Color(0xFFE6FAF5)
private val Ink = Color(0xFF1C1F2A)
private val Muted = Color(0xFF8B90A0)

/**
 * "Siapkan Profil": the only thing between a first login and the home screen.
 * The arrow at the bottom right is the confirm action; there is no way back, because
 * the account exists already and logout lives in the Pengaturan tab inside the app.
 */
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onDone: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.done) {
        if (state.done) onDone()
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TealSoft, Color.White))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            IconButton(
                onClick = {
                    Toast
                        .makeText(context, "Selesaikan profil dulu untuk masuk", Toast.LENGTH_SHORT)
                        .show()
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Ink)
            }

            Spacer(Modifier.height(24.dp))
            Text("Siapkan Profil", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text(
                "Nama dan gender ini tampil di profil kamu. Bisa diubah nanti di Pengaturan.",
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )

            Spacer(Modifier.height(32.dp))
            FieldLabel("Nama")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.nickname,
                onValueChange = viewModel::onNicknameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Masukkan nama kamu", color = Muted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = {
                    Text(
                        "${state.nickname.length}/${ProfileSetupViewModel.MAX_NAME}",
                        color = Muted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = Color(0xFFDDE4E2),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            Spacer(Modifier.height(28.dp))
            FieldLabel("Gender")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GenderOption(
                    label = "Pria",
                    icon = Icons.Filled.Male,
                    selected = state.gender == GENDER_MALE,
                    onClick = { viewModel.onGenderChange(GENDER_MALE) },
                    modifier = Modifier.weight(1f),
                )
                GenderOption(
                    label = "Wanita",
                    icon = Icons.Filled.Female,
                    selected = state.gender == GENDER_FEMALE,
                    onClick = { viewModel.onGenderChange(GENDER_FEMALE) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Simpan", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(if (state.canConfirm) Teal else Teal.copy(alpha = 0.35f))
                        .clickable(enabled = state.canConfirm) { viewModel.confirm() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = "Simpan profil dan masuk",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun GenderOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) TealSoft else Color.White)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Teal else Color(0xFFDDE4E2),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) Teal else Muted,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = if (selected) Ink else Muted,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

private const val GENDER_MALE = "male"
private const val GENDER_FEMALE = "female"
