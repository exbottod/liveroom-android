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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
) {
    var phoneMode by remember { mutableStateOf(false) }
    val st by viewModel.state.collectAsState()
    if (phoneMode || st.stepOtp) {
        PhoneLoginScreen(viewModel, onLoggedIn, onBack = { phoneMode = false })
    } else {
        val context = LocalContext.current
        val openPage: (String) -> Unit = { url ->
            runCatching {
                context.startActivity(
                    android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)),
                )
            }.onFailure {
                android.widget.Toast
                    .makeText(context, "Tidak bisa membuka halaman", android.widget.Toast.LENGTH_SHORT)
                    .show()
            }
        }
        LoginHome(
            viewModel = viewModel,
            onLoggedIn = onLoggedIn,
            onPhone = { phoneMode = true },
            onOpenTerms = { openPage(TERMS_URL) },
            onOpenPrivacy = { openPage(PRIVACY_URL) },
        )
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

    LaunchedEffect(state.loggedIn) {
        state.loggedIn?.let(onLoggedIn)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_login),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
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
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 18.dp),
        ) {
            Text("+62", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = state.phone,
            onValueChange = viewModel::onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("812 3456 7890") },
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
            text = "+62${state.phone}",
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
