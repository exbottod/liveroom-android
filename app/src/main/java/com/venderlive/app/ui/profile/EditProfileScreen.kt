package com.venderlive.app.ui.profile

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.venderlive.app.data.model.OtherProfile
import com.venderlive.app.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

private val Soft = Color(0xFFF4F6F8)
private val Danger = Color(0xFFE5484D)

/**
 * The Edit profile page behind the pencil: photo from the gallery, name, birthday,
 * bio, and the location flag picked from every country.
 */
@Composable
fun EditProfileScreen(
    repo: AuthRepository,
    profile: OtherProfile?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onPhotoChanged: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(profile?.nickname.orEmpty()) }
    var bio by remember { mutableStateOf(profile?.bio.orEmpty()) }
    var birthDate by remember { mutableStateOf(profile?.birthDate) }
    var country by remember { mutableStateOf(profile?.country) }
    var avatar by remember { mutableStateOf(profile?.avatar) }
    var picking by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onBack)

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        val parts = birthDate?.split("-")
        if (parts != null && parts.size == 3) {
            parts[0].toIntOrNull()?.let { cal.set(Calendar.YEAR, it) }
            parts[1].toIntOrNull()?.let { cal.set(Calendar.MONTH, it - 1) }
            parts[2].toIntOrNull()?.let { cal.set(Calendar.DAY_OF_MONTH, it) }
        } else {
            cal.set(Calendar.YEAR, 2000)
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                // Same order the reference shows: 2002-12-7.
                birthDate = "$year-${month + 1}-$day"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        uploading = true
        error = null
        scope.launch {
            val jpeg = withContext(Dispatchers.IO) { readSquareJpeg(context, uri) }
            if (jpeg == null) {
                uploading = false
                error = "Foto tidak bisa dibaca"
                return@launch
            }
            try {
                repo.uploadAvatar(jpeg)
                // Show the new photo straight away by re-reading the profile.
                avatar = runCatching { repo.profile(profile?.publicId.orEmpty()) }.getOrNull()?.avatar
                    ?: avatar
                onPhotoChanged()
            } catch (e: Exception) {
                error = e.message ?: "Gagal mengunggah foto"
            } finally {
                uploading = false
            }
        }
    }

    if (picking) {
        CountryPickerDialog(
            selected = country,
            onDismiss = { picking = false },
            onPick = { country = it; picking = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        // Top bar: back, title, Save.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = InkText)
            }
            Text(
                "Edit profil", color = InkText, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            TextButton(
                enabled = !saving && !uploading && name.trim().length >= 2,
                onClick = {
                    saving = true
                    error = null
                    scope.launch {
                        try {
                            repo.editProfile(
                                name.trim(),
                                bio.trim(),
                                country,
                                birthDate?.takeIf { it.isNotBlank() },
                            )
                            onSaved()
                        } catch (e: Exception) {
                            error = e.message ?: "Gagal menyimpan"
                        } finally {
                            saving = false
                        }
                    }
                },
            ) {
                Text(if (saving) "Menyimpan..." else "Simpan", color = FlagTeal, fontSize = 16.sp)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).imePadding(),
        ) {
            // Photo with the Edit button on its bottom edge.
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                Box {
                    Avatar(url = avatar, label = name, size = 104.dp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(50))
                            .background(FlagTeal)
                            .clickable(enabled = !uploading) {
                                picker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (uploading) "..." else "Edit", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 30) name = it },
                label = { Text("Nama") },
                singleLine = true,
                supportingText = {
                    Text("${name.length}/30", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            // Birthday
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Soft)
                    .clickable { openDatePicker() }.padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    birthDate?.takeIf { it.isNotBlank() } ?: "Pilih tanggal lahir",
                    color = if (birthDate.isNullOrBlank()) MutedText else InkText,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFFB5B8C2))
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 100) bio = it },
                placeholder = { Text("Perkenalan") },
                minLines = 3,
                supportingText = {
                    Text("${bio.length}/100", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                },
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            // Location: label on the left, flag and country, pencil on the right.
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Soft)
                    .clickable { picking = true }.padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Lokasi", color = InkText, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                if (country != null) {
                    Text(flagOf(country), fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(countryNameOf(country) ?: country!!, color = InkText, fontSize = 15.sp)
                } else {
                    Text("Pilih negara", color = MutedText, fontSize = 15.sp)
                }
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Filled.Edit, "Ubah lokasi", tint = MutedText, modifier = Modifier.size(18.dp))
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = Danger, fontSize = 13.sp)
            }
            Spacer(Modifier.height(30.dp).navigationBarsPadding())
        }
    }
}
