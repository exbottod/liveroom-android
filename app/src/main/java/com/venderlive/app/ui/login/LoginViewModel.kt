package com.venderlive.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venderlive.app.data.model.AuthResult
import com.venderlive.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val phone: String = "",
    val otp: String = "",
    val verificationId: String? = null,
    val agreeTerms: Boolean = false,
    val loading: Boolean = false,
    val secondsLeft: Int = 0,
    val error: String? = null,
    val loggedIn: AuthResult? = null,
) {
    val stepOtp: Boolean get() = verificationId != null
    val phoneValid: Boolean get() = phone.length in 9..13
    val canSendOtp: Boolean get() = phoneValid && agreeTerms && !loading
    val canVerify: Boolean get() = otp.length == 6 && !loading
}

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phone = value.filter { it.isDigit() }.take(13), error = null)
    }

    fun onOtpChange(value: String) {
        _state.value = _state.value.copy(otp = value.filter { it.isDigit() }.take(6), error = null)
    }

    fun onAgreeChange(value: Boolean) {
        _state.value = _state.value.copy(agreeTerms = value)
    }

    fun onBackToPhone() {
        _state.value = _state.value.copy(verificationId = null, otp = "", secondsLeft = 0)
    }

    fun sendOtp(activity: android.app.Activity) {
        val current = _state.value
        if (!current.canSendOtp) return
        _state.value = current.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                repo.requestWhatsAppOtp("+62${current.phone}")
                _state.value = _state.value.copy(
                    verificationId = "wa",
                    loading = false,
                    secondsLeft = 60,
                )
                startCountdown()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Gagal mengirim kode WhatsApp",
                )
            }
        }
    }

    fun verifyOtp() {
        val current = _state.value
        if (!current.canVerify) return
        _state.value = current.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                val result = repo.verifyWhatsAppOtp("+62${current.phone}", current.otp)
                _state.value = _state.value.copy(loading = false, loggedIn = result)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Kode salah")
            }
        }
    }

    fun signInWithGoogle(activity: android.app.Activity) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val webClientId = googleWebClientId(activity)
                if (webClientId.isBlank()) {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "Google belum siap: web client ID belum ada",
                    )
                    return@launch
                }
                val manager = androidx.credentials.CredentialManager.create(activity)
                val request = androidx.credentials.GetCredentialRequest.Builder()
                    .addCredentialOption(
                        com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
                            .Builder(webClientId)
                            .build(),
                    )
                    .build()
                val credential = manager.getCredential(activity, request).credential
                val googleIdToken = if (
                    credential is androidx.credentials.CustomCredential &&
                    credential.type ==
                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                        .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                        .createFrom(credential.data).idToken
                } else {
                    error("Kredensial Google tidak dikenali")
                }
                val result = repo.signInWithGoogleIdToken(googleIdToken)
                _state.value = _state.value.copy(loading = false, loggedIn = result)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Login Google gagal",
                )
            }
        }
    }

    private fun googleWebClientId(context: android.content.Context): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) return runCatching { context.getString(resId) }.getOrDefault("")
        return runCatching { com.venderlive.app.BuildConfig.GOOGLE_WEB_CLIENT_ID }.getOrDefault("")
    }

    private fun loginWithToken(idToken: String) {
        viewModelScope.launch {
            try {
                val result = repo.exchange(idToken)
                _state.value = _state.value.copy(loggedIn = result)
            } catch (_: Exception) {
                _state.value = _state.value.copy(error = "Auto-verify gagal, coba manual")
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (_state.value.secondsLeft > 0) {
                kotlinx.coroutines.delay(1000)
                _state.value = _state.value.copy(secondsLeft = _state.value.secondsLeft - 1)
            }
        }
    }
}
