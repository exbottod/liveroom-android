package com.exbottod.liveroom.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("session")

class SessionStore(private val context: Context) {

    private val keyToken = stringPreferencesKey("access_token")
    private val keyRefresh = stringPreferencesKey("refresh_token")
    private val keyUserId = stringPreferencesKey("user_id")

    suspend fun accessToken(): String? =
        context.dataStore.data.first()[keyToken]

    suspend fun save(accessToken: String, refreshToken: String?, userId: String) {
        context.dataStore.edit {
            it[keyToken] = accessToken
            it[keyUserId] = userId
            if (refreshToken != null) it[keyRefresh] = refreshToken
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
