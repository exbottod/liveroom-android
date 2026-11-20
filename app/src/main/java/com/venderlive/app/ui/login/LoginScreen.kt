package com.venderlive.app.ui.login

import android.app.Activity
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.venderlive.app.R
import com.venderlive.app.data.model.AuthResult
import com.venderlive.app.ui.theme.BrandAccent
import com.venderlive.app.ui.theme.BrandPrimary
import com.venderlive.app.ui.theme.SurfaceDark

private const val TERMS_URL = "https://exbottod.github.io/liveroom-android/agreement.html"
private const val PRIVACY_URL = "https://exbottod.github.io/liveroom-android/privacy.html"

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: (AuthResult) -> Unit,
    onGuest: () -> Unit = {},
) {
    var phoneMode by remember { mutableStateOf(false) }
    val st by viewModel.state.collectAsState()
    val screenContext = LocalContext.current
    // Any successful sign-in (Google or phone) moves on; previously only the phone
    // screen listened for it, so a Google login succeeded and stayed on this screen.
    androidx.compose.runtime.LaunchedEffect(st.loggedIn) {
        st.loggedIn?.let(onLoggedIn)
    }
    androidx.compose.runtime.LaunchedEffect(st.error) {
        st.error?.let {
            android.widget.Toast.makeText(screenContext, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    if (phoneMode || st.stepOtp) {
        PhoneLoginScreen(viewModel, onLoggedIn, onBack = { phoneMode = false })
    } else {
        val context = LocalContext.current
        // Agreement and privacy pages open inside the app, not in Chrome.
        var webPage by remember { mutableStateOf<Pair<String, String>?>(null) }
        val host = context as? android.app.Activity
        Box(modifier = Modifier.fillMaxSize()) {
            LoginHome(
                viewModel = viewModel,
                onLoggedIn = onLoggedIn,
                onPhone = { phoneMode = true },
                onGoogle = {
                    val activity = host
                    if (activity == null) {
                        android.widget.Toast
                            .makeText(screenContext, "Google tidak bisa dibuka di layar ini", android.widget.Toast.LENGTH_LONG)
                            .show()
                    } else {
                        viewModel.signInWithGoogle(activity)
                    }
                },
                onGuest = onGuest,
                onOpenTerms = { webPage = "Perjanjian Pengguna" to TERMS_URL },
                onOpenPrivacy = { webPage = "Kebijakan Privasi" to PRIVACY_URL },
            )
            // A failed Google sign-in is shown on the screen itself, not only as a
            // toast, so a failure can never look like nothing happened.
            st.error?.let { message ->
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(start = 18.dp, end = 18.dp, bottom = 92.dp)
                        .background(Color(0xFFD93636), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
        webPage?.let { (title, url) ->
            InAppWebScreen(title = title, url = url, onClose = { webPage = null })
        }
    }
}

@Composable
private fun PhoneLoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: (AuthResult) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current as? Activity

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            androidx.compose.ui.graphics.Color(0xFFF1FBFA),
                            androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark.copy(alpha = 0.55f), BrandPrimary.copy(alpha = 0.25f)))),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                text = "VenderLive",
                style = MaterialTheme.typography.headlineLarge,
                color = BrandAccent,
                modifier = Modifier.clickable(onClick = onBack),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (state.stepOtp) "Masukkan kode verifikasi" else "Masuk untuk mulai ngobrol",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            if (!state.stepOtp) {
                PhoneStep(state, viewModel, activity)
            } else {
                OtpStep(state, viewModel)
            }

            state.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(32.dp))
            Text(
                text = "Login berarti setuju dengan Perjanjian Pengguna dan Kebijakan Privasi.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PhoneStep(
    state: LoginUiState,
    viewModel: LoginViewModel,
    activity: Activity?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var pickerOpen by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .clickable { pickerOpen = true }
                .padding(start = 12.dp, end = 8.dp, top = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(state.country.flag, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                state.country.dial,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Pilih negara",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }

        if (pickerOpen) {
            CountryPickerSheet(
                selected = state.country,
                onDismiss = { pickerOpen = false },
                onPick = {
                    viewModel.onCountryChange(it)
                    pickerOpen = false
                },
            )
        }
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = state.phone,
            onValueChange = viewModel::onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nomor WhatsApp") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandAccent,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            ),
        )
    }

    Spacer(Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = state.agreeTerms,
            onCheckedChange = viewModel::onAgreeChange,
            colors = CheckboxDefaults.colors(checkedColor = BrandAccent),
        )
        Text(
            text = "Saya setuju dengan syarat dan ketentuan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        )
    }

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = { activity?.let(viewModel::sendOtp) },
        enabled = state.canSendOtp,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text("Kirim Kode OTP", fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryPickerSheet(
    selected: Country,
    onDismiss: () -> Unit,
    onPick: (Country) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val results = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            AllCountries
        } else {
            AllCountries.filter {
                it.name.lowercase().contains(q) ||
                    it.iso.lowercase() == q ||
                    it.dial.contains(q.removePrefix("+"))
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Pilih negara",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Tutup")
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            placeholder = { Text("Cari negara atau kode") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandAccent,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            ),
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(results, key = { it.iso }) { country ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(country) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(country.flag, fontSize = 22.sp)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = country.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = country.dial,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                    if (country.iso == selected.iso) {
                        Spacer(Modifier.width(8.dp))
                        Text("✓", color = BrandAccent, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
        }
    }
}

@Composable
private fun OtpStep(
    state: LoginUiState,
    viewModel: LoginViewModel,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = viewModel::onBackToPhone) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = BrandAccent)
        }
        Text(
            text = state.fullPhone,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }

    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = state.otp,
        onValueChange = viewModel::onOtpChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("6 digit kode") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandAccent,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
        ),
    )

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        if (state.secondsLeft > 0) {
            Text(
                "Kirim ulang dalam ${state.secondsLeft}s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        } else {
            TextButton(onClick = viewModel::onBackToPhone) {
                Text("Kirim ulang kode", color = BrandAccent, fontSize = 13.sp)
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = viewModel::verifyOtp,
        enabled = state.canVerify,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text("Masuk", fontWeight = FontWeight.SemiBold)
        }
    }
}
