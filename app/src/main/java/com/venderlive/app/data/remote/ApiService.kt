package com.venderlive.app.data.remote

import com.venderlive.app.data.model.ApiEnvelope
import com.venderlive.app.data.model.AuthResult
import com.venderlive.app.data.model.BlockedUser
import com.venderlive.app.data.model.LinkRequest
import com.venderlive.app.data.model.AvatarRequest
import com.venderlive.app.data.model.LinkState
import com.venderlive.app.data.model.OtherProfile
import com.venderlive.app.data.model.ProfileEditRequest
import com.venderlive.app.data.model.FirebaseLoginRequest
import com.venderlive.app.data.model.OtpRequest
import com.venderlive.app.data.model.OtpVerifyRequest
import com.venderlive.app.data.model.ProfileUpdateRequest
import com.venderlive.app.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path

/**
 * Contract with our own backend on our own VPS.
 * Firebase is only used to prove phone ownership; the server issues the session.
 */
interface ApiService {

    @POST("v1/auth/firebase-login")
    suspend fun firebaseLogin(@Body body: FirebaseLoginRequest): ApiEnvelope<AuthResult>

    @POST("v1/auth/otp/request")
    suspend fun otpRequest(@Body body: OtpRequest): ApiEnvelope<Map<String, Any>>

    @POST("v1/auth/otp/verify")
    suspend fun otpVerify(@Body body: OtpVerifyRequest): ApiEnvelope<AuthResult>

    @GET("v1/me")
    suspend fun me(): ApiEnvelope<UserProfile>

    /** First-run profile: the name and gender the user picks before entering the app. */
    @POST("v1/profile")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): ApiEnvelope<UserProfile>

    /** Public profile opened from a room seat or from the Aku tab. */
    @GET("v1/users/{publicId}")
    suspend fun profile(@Path("publicId") publicId: String): ApiEnvelope<OtherProfile>

    /** Pencil edits: name, bio, country flag. */
    @PATCH("v1/profile")
    suspend fun editProfile(@Body body: ProfileEditRequest): ApiEnvelope<UserProfile>

    @POST("v1/profile/avatar")
    suspend fun uploadAvatar(@Body body: AvatarRequest): ApiEnvelope<UserProfile>

    /** Which sign-in methods are tied to this account. */
    @GET("v1/account/links")
    suspend fun accountLinks(): ApiEnvelope<List<LinkState>>

    @POST("v1/account/links")
    suspend fun linkAccount(@Body body: LinkRequest): ApiEnvelope<LinkState>

    @DELETE("v1/account/links/{provider}")
    suspend fun unlinkAccount(@Path("provider") provider: String): ApiEnvelope<LinkState>

    @GET("v1/account/blocked")
    suspend fun blockedUsers(): ApiEnvelope<List<BlockedUser>>

    @DELETE("v1/account/blocked/{id}")
    suspend fun unblock(@Path("id") id: String): ApiEnvelope<Map<String, Any>>

    /** Removes the account for good. */
    @DELETE("v1/account")
    suspend fun deleteAccount(): ApiEnvelope<Map<String, Any>>

    // Phase 2 (room + seats) will add:
    // GET  v1/rooms                    -> room list
    // GET  v1/rooms/{id}               -> room detail + seat map
    // POST v1/rooms/{id}/seats/{index}/join
    // POST v1/rooms/{id}/seats/{index}/leave
    // POST v1/rooms/{id}/seats/{index}/approve   (host/admin)
    // POST v1/rooms/{id}/seats/{index}/mute
}
