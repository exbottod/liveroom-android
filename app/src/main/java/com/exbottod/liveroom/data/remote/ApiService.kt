package com.exbottod.liveroom.data.remote

import com.exbottod.liveroom.data.model.ApiEnvelope
import com.exbottod.liveroom.data.model.AuthResult
import com.exbottod.liveroom.data.model.FirebaseLoginRequest
import com.exbottod.liveroom.data.model.UserProfile
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

    @GET("v1/me")
    suspend fun me(): ApiEnvelope<UserProfile>

    // Phase 2 (room + seats) will add:
    // GET  v1/rooms                    -> room list
    // GET  v1/rooms/{id}               -> room detail + seat map
    // POST v1/rooms/{id}/seats/{index}/join
    // POST v1/rooms/{id}/seats/{index}/leave
    // POST v1/rooms/{id}/seats/{index}/approve   (host/admin)
    // POST v1/rooms/{id}/seats/{index}/mute
}
