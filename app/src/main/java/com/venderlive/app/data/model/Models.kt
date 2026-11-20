package com.venderlive.app.data.model

import com.google.gson.annotations.SerializedName

data class FirebaseLoginRequest(
    @SerializedName("id_token") val idToken: String,
)

data class UserProfile(
    val id: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val phone: String? = null,
    val level: Int = 0,
    /** "male" or "female", null while the user has not finished the profile screen. */
    val gender: String? = null,
    @SerializedName("is_new") val isNew: Boolean = false,
)

data class AuthResult(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    val user: UserProfile,
)

/** Sent once, right after the first login, before the user reaches the home screen. */
data class ProfileUpdateRequest(
    val nickname: String,
    val gender: String,
)

data class ApiEnvelope<T>(
    val code: Int = 0,
    val message: String? = null,
    val data: T? = null,
)
