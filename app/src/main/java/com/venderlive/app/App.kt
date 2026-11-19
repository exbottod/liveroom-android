package com.venderlive.app

import android.app.Application
import com.venderlive.app.data.local.SessionStore
import com.venderlive.app.data.remote.ApiClient
import com.venderlive.app.data.repository.AuthRepository
import com.venderlive.app.debug.CrashLog

/**
 * Kept as small as possible, like the viza app: onCreate only installs the crash
 * recorder. Storage and networking are created on first use, so a problem there
 * cannot stop the app from starting.
 */
class App : Application() {

    val sessionStore: SessionStore by lazy { SessionStore(this) }

    val authRepository: AuthRepository by lazy {
        ApiClient.init(sessionStore)
        AuthRepository(sessionStore, ApiClient.service)
    }

    override fun onCreate() {
        super.onCreate()
        CrashLog.install(this)
        CrashLog.rotateSteps(this)
        CrashLog.step(this, "App.onCreate: mulai Firebase")
        runCatching { com.google.firebase.FirebaseApp.initializeApp(this) }
            .onSuccess { CrashLog.step(this, "Firebase siap") }
            .onFailure { CrashLog.step(this, "Firebase GAGAL: ${it.javaClass.name}: ${it.message}") }
        CrashLog.step(this, "App.onCreate selesai")
    }
}
