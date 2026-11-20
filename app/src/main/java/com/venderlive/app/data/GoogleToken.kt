package com.venderlive.app.data

import android.app.Activity

/** Opens the Google account chooser and returns a Google ID token. */
object GoogleToken {

    private fun webClientId(context: android.content.Context): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) return runCatching { context.getString(resId) }.getOrDefault("")
        return runCatching { com.venderlive.app.BuildConfig.GOOGLE_WEB_CLIENT_ID }.getOrDefault("")
    }

    /** Returns null when the user closed the chooser without picking an account. */
    suspend fun pick(activity: Activity): String? {
        val clientId = webClientId(activity)
        if (clientId.isBlank()) error("Google belum siap: web client ID belum ada")
        val manager = androidx.credentials.CredentialManager.create(activity)
        val request = androidx.credentials.GetCredentialRequest.Builder()
            .addCredentialOption(
                com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
                    .Builder(clientId)
                    .build(),
            )
            .build()
        return try {
            val credential = manager.getCredential(activity, request).credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type ==
                com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                    .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                    .createFrom(credential.data).idToken
            } else {
                error("Kredensial Google tidak dikenali")
            }
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            null
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            error("Belum ada akun Google di HP ini, atau SHA-1 aplikasi belum didaftarkan di Firebase")
        }
    }
}
