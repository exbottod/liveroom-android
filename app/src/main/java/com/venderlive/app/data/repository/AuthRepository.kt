package com.venderlive.app.data.repository

import com.venderlive.app.data.local.SessionStore
import com.venderlive.app.data.model.AuthResult
import com.venderlive.app.data.model.FirebaseLoginRequest
import com.venderlive.app.data.remote.ApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val sessionStore: SessionStore,
    private val api: ApiService,
) {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    fun currentUserId(): String? = auth.currentUser?.uid

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
        val response = api.firebaseLogin(FirebaseLoginRequest(idToken))
        val result = response.data ?: error(response.message ?: "Login ditolak server")

        sessionStore.save(result.accessToken, result.refreshToken, result.user.id)
        return result
    }

    suspend fun logout() {
        auth.signOut()
        sessionStore.clear()
    }
}
