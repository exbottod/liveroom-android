package com.venderlive.app.data.repository

import com.venderlive.app.data.local.CachedProfile
import com.venderlive.app.data.local.SessionStore
import com.venderlive.app.data.model.AuthResult
import com.venderlive.app.data.model.FirebaseLoginRequest
import com.venderlive.app.data.model.OtpRequest
import com.venderlive.app.data.model.OtpVerifyRequest
import com.venderlive.app.data.model.ProfileUpdateRequest
import com.venderlive.app.data.remote.ApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val sessionStore: SessionStore,
    private val api: ApiService,
) {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    fun currentUserId(): String? = auth.currentUser?.uid

    /** Cached name/gender so the home screen can greet the user without a request. */
    fun cachedProfile(): CachedProfile = runBlocking { sessionStore.profile() }

    /**
     * True while the signed-in user has not finished the profile screen yet. Read once
     * at startup: it decides whether the app opens on the profile screen or on home.
     */
    fun needsProfileSetup(): Boolean = !cachedProfile().completed

    /** Step 1: ask Firebase to send the OTP to the formatted phone number. */
    suspend fun requestOtp(
        phoneE164: String,
        activity: android.app.Activity,
        onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onAutoVerified: (idToken: String) -> Unit,
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        result.user?.getIdToken(false)?.addOnSuccessListener { token ->
                            onAutoVerified(token.token ?: "")
                        }
                    }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                // surfaced by the caller through the state below
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                onCodeSent(id, token)
            }
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneE164,
            60,
            java.util.concurrent.TimeUnit.SECONDS,
            activity,
            callbacks,
        )
    }

    /** Step 2: confirm the SMS code with Firebase, then trade the Firebase ID token
     *  for our own backend session (our VPS issues the access token). */
    suspend fun verifyOtpAndLogin(verificationId: String, smsCode: String): AuthResult {
        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
        val user = auth.signInWithCredential(credential).await().user
            ?: error("Firebase tidak mengembalikan user")

        val idToken = user.getIdToken(false).await().token
            ?: error("Gagal mengambil ID token")

        return exchange(idToken)
    }


    /**
     * Retrofit throws on any 4xx/5xx and drops the server's own message, which is why the
     * screen only said "HTTP 401". Pull the backend's `message` out of the error body instead.
     */
    private suspend fun <T> readingServerMessage(call: suspend () -> T): T {
        try {
            return call()
        } catch (e: retrofit2.HttpException) {
            val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            val message = runCatching {
                com.google.gson.JsonParser.parseString(raw).asJsonObject.get("message")?.asString
            }.getOrNull()
            throw IllegalStateException(message ?: "Server menolak permintaan (HTTP ${e.code()})")
        } catch (e: java.io.IOException) {
            throw IllegalStateException("Tidak bisa terhubung ke server, cek internet")
        }
    }

    /** Ask our VPS to send a 6-digit code over WhatsApp. */
    suspend fun requestWhatsAppOtp(phoneE164: String) {
        val response = readingServerMessage { api.otpRequest(OtpRequest(phoneE164)) }
        if (response.code != 0) error(response.message ?: "Gagal mengirim kode")
    }

    /** Confirm the WhatsApp code and take our own session. */
    suspend fun verifyWhatsAppOtp(phoneE164: String, code: String): AuthResult {
        val response = readingServerMessage { api.otpVerify(OtpVerifyRequest(phoneE164, code)) }
        val result = response.data ?: error(response.message ?: "Kode salah")
        sessionStore.save(result.accessToken, result.refreshToken, result.user.id)
        sessionStore.cacheServerProfile(result.user)
        return result
    }

    /** Sign in with a Google ID token from Credential Manager, then trade the
     *  Firebase ID token for our own backend session. */
    suspend fun signInWithGoogleIdToken(googleIdToken: String): AuthResult {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(googleIdToken, null)
        val user = auth.signInWithCredential(credential).await().user
            ?: error("Firebase tidak mengembalikan user")

        val idToken = user.getIdToken(false).await().token
            ?: error("Gagal mengambil ID token")

        return exchange(idToken)
    }

    /** Trade an already-obtained Firebase ID token for our backend session. */
    suspend fun exchange(idToken: String): AuthResult {
        val response = readingServerMessage { api.firebaseLogin(FirebaseLoginRequest(idToken)) }
        val result = response.data ?: error(response.message ?: "Login ditolak server")

        sessionStore.save(result.accessToken, result.refreshToken, result.user.id)
        sessionStore.cacheServerProfile(result.user)
        return result
    }

    /**
     * Save the name and gender picked on the profile screen.
     *
     * The local cache is written first, so the user can enter the app even when the
     * network is slow. Returns true when the server also stored it; false means the
     * values only live on this device for now and must be pushed again later.
     */
    suspend fun saveProfile(nickname: String, gender: String): Boolean {
        val clean = nickname.trim()
        require(clean.isNotEmpty()) { "Nama tidak boleh kosong" }
        sessionStore.saveProfile(clean, gender)
        val synced = runCatching { api.updateProfile(ProfileUpdateRequest(clean, gender)) }
            .getOrNull()
            ?.let { it.code == 0 }
            ?: false
        return synced
    }

    suspend fun logout() {
        auth.signOut()
        sessionStore.clear()
    }
}
