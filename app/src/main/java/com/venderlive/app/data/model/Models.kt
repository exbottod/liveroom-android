package com.venderlive.app.data.model

import com.google.gson.annotations.SerializedName

data class FirebaseLoginRequest(
    @SerializedName("id_token") val idToken: String,
)

data class UserProfile(
    val id: String,
    /** Random six-digit ID the user shows to friends, e.g. "255800". */
    @SerializedName("public_id") val publicId: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val phone: String? = null,
    val level: Int = 0,
    /** "male" or "female", null while the user has not finished the profile screen. */
    val gender: String? = null,
    /** Short self-introduction shown on the profile page. */
    val bio: String? = null,
    /** Two-letter country code for the flag next to the nickname, e.g. "ID". */
    val country: String? = null,
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

/** One sign-in method and whether it is tied to this account. */
data class LinkState(
    val provider: String,
    val linked: Boolean = false,
)

data class LinkRequest(
    val provider: String,
    @SerializedName("id_token") val idToken: String,
)

data class BlockedUser(
    val id: String,
    @SerializedName("public_id") val publicId: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
)

/** Someone else's public profile, or our own when opened from the Aku tab. */
data class OtherProfile(
    val id: String,
    @SerializedName("public_id") val publicId: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val gender: String? = null,
    val level: Int = 0,
    val bio: String? = null,
    val country: String? = null,
    val stats: ProfileCounts = ProfileCounts(),
)

data class ProfileCounts(
    val friends: Int = 0,
    val following: Int = 0,
    val followers: Int = 0,
)

/** Pencil-button edits; every field is optional so one call can change one thing. */
data class ProfileEditRequest(
    val nickname: String? = null,
    val bio: String? = null,
    val country: String? = null,
)

data class AvatarRequest(
    @SerializedName("image_base64") val imageBase64: String,
)
