package com.exbottod.liveroom

import android.app.Application
import com.exbottod.liveroom.data.local.SessionStore
import com.exbottod.liveroom.data.remote.ApiClient
import com.exbottod.liveroom.data.repository.AuthRepository

class App : Application() {

    lateinit var sessionStore: SessionStore
        private set
    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)
        ApiClient.init(sessionStore)
        authRepository = AuthRepository(sessionStore, ApiClient.service)
    }
}
