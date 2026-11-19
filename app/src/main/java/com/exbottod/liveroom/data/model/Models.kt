package com.exbottod.liveroom.data.model

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
    @SerializedName("is_new") val isNew: Boolean = false,
)

data class AuthResult(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    val user: UserProfile,
)

data class ApiEnvelope<T>(
    val code: Int = 0,
    val message: String? = null,
    val data: T? = null,
)
