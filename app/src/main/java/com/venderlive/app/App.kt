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
        runCatching {
            sessionStore = SessionStore(this)
            CrashLog.step(this, "SessionStore siap")
            ApiClient.init(sessionStore)
            CrashLog.step(this, "ApiClient siap")
            authRepository = AuthRepository(sessionStore, ApiClient.service)
            CrashLog.step(this, "App.onCreate selesai")
        }.onFailure {
            CrashLog.step(this, "App.onCreate GAGAL: ${it.javaClass.name}: ${it.message}")
        }
    }
}
