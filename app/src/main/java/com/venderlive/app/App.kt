package com.venderlive.app

import android.app.Application
import com.venderlive.app.data.local.SessionStore
import com.venderlive.app.data.remote.ApiClient
import com.venderlive.app.data.repository.AuthRepository

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
