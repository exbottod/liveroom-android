package com.venderlive.app.data.remote

import com.venderlive.app.data.model.ApiEnvelope
import com.venderlive.app.data.model.AuthResult
import com.venderlive.app.data.model.FirebaseLoginRequest
import com.venderlive.app.data.model.OtpRequest
import com.venderlive.app.data.model.OtpVerifyRequest
import com.venderlive.app.data.model.ProfileUpdateRequest
import com.venderlive.app.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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

    // Phase 2 (room + seats) will add:
    // GET  v1/rooms                    -> room list
    // GET  v1/rooms/{id}               -> room detail + seat map
    // POST v1/rooms/{id}/seats/{index}/join
    // POST v1/rooms/{id}/seats/{index}/leave
    // POST v1/rooms/{id}/seats/{index}/approve   (host/admin)
    // POST v1/rooms/{id}/seats/{index}/mute
}
