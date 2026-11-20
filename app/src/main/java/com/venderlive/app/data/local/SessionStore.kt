package com.venderlive.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.venderlive.app.data.model.UserProfile
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("session")

/** What we remember about the signed-in user between launches. */
data class CachedProfile(
    val nickname: String? = null,
    val gender: String? = null,
    val publicId: String? = null,
    val bio: String? = null,
    val country: String? = null,
    val avatar: String? = null,
    val completed: Boolean = false,
)

class SessionStore(private val context: Context) {

    private val keyToken = stringPreferencesKey("access_token")
    private val keyRefresh = stringPreferencesKey("refresh_token")
    private val keyUserId = stringPreferencesKey("user_id")
    private val keyNickname = stringPreferencesKey("nickname")
    private val keyGender = stringPreferencesKey("gender")
    private val keyPublicId = stringPreferencesKey("public_id")
    private val keyProfileDone = booleanPreferencesKey("profile_done")
    private val keyBio = stringPreferencesKey("bio")
    private val keyCountry = stringPreferencesKey("country")
    private val keyAvatar = stringPreferencesKey("avatar")

    suspend fun accessToken(): String? =
        context.dataStore.data.first()[keyToken]

    suspend fun save(accessToken: String, refreshToken: String?, userId: String) {
        context.dataStore.edit {
            it[keyToken] = accessToken
            it[keyUserId] = userId
            if (refreshToken != null) it[keyRefresh] = refreshToken
        }
    }

    /**
     * The user finished the profile screen: name and gender are now both cached and
     * the setup screen must not be shown again on this device.
     */
    suspend fun saveProfile(nickname: String, gender: String) {
        context.dataStore.edit {
            it[keyNickname] = nickname
            it[keyGender] = gender
            it[keyProfileDone] = true
        }
    }

    /**
     * The server already knows this user (returning login). Cache whatever it sent so
     * the home screen can show the name offline, and keep the setup screen away.
     */
    suspend fun cacheServerProfile(user: UserProfile) {
        context.dataStore.edit {
            if (!user.nickname.isNullOrBlank()) it[keyNickname] = user.nickname
            if (!user.gender.isNullOrBlank()) it[keyGender] = user.gender
            if (!user.publicId.isNullOrBlank()) it[keyPublicId] = user.publicId
            it[keyBio] = user.bio.orEmpty()
            it[keyCountry] = user.country.orEmpty()
            it[keyAvatar] = user.avatar.orEmpty()
            // A brand new account still has to pick a name and gender, even when the
            // server hands out a placeholder nickname.
            // The server says is_new until a name and gender were saved, so an account
            // that was left half-finished is asked again, and a finished one never is.
            it[keyProfileDone] = !user.isNew && !user.nickname.isNullOrBlank()
        }
    }

    suspend fun profile(): CachedProfile {
        val prefs = context.dataStore.data.first()
        return CachedProfile(
            nickname = prefs[keyNickname],
            gender = prefs[keyGender],
            publicId = prefs[keyPublicId],
            bio = prefs[keyBio]?.takeIf { it.isNotBlank() },
            country = prefs[keyCountry]?.takeIf { it.isNotBlank() },
            avatar = prefs[keyAvatar]?.takeIf { it.isNotBlank() },
            completed = prefs[keyProfileDone] ?: false,
        )
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
