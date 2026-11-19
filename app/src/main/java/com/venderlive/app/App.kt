package com.venderlive.app

import android.app.Application
import com.venderlive.app.data.local.SessionStore
import com.venderlive.app.data.remote.ApiClient
import com.venderlive.app.data.repository.AuthRepository
import com.venderlive.app.debug.CrashLog

class App : Application() {

    lateinit var sessionStore: SessionStore
        private set
    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        CrashLog.install(this)
        CrashLog.rotateSteps(this)
        CrashLog.step(this, "App.onCreate mulai")
        sessionStore = SessionStore(this)
        ApiClient.init(sessionStore)
        authRepository = AuthRepository(sessionStore, ApiClient.service)
        CrashLog.step(this, "App.onCreate selesai")
    }
}
