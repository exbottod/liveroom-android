package com.venderlive.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venderlive.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    val nickname: String = "",
    val gender: String? = null,
    val saving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
) {
    val nameValid: Boolean get() = nickname.trim().length >= 2
    val canConfirm: Boolean get() = nameValid && gender != null && !saving
}

/**
 * One screen, shown once after the first login, before the user reaches home.
 * The invite code is intentionally not part of this version.
 */
class ProfileSetupViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupUiState())
    val state: StateFlow<ProfileSetupUiState> = _state.asStateFlow()

    fun onNicknameChange(value: String) {
        val clean = value.replace("\n", " ").take(MAX_NAME)
        _state.value = _state.value.copy(nickname = clean, error = null)
    }

    fun onGenderChange(gender: String) {
        _state.value = _state.value.copy(gender = gender, error = null)
    }

    fun confirm() {
        val current = _state.value
        if (!current.canConfirm) return
        val gender = current.gender ?: return
        _state.value = current.copy(saving = true, error = null)

        viewModelScope.launch {
            try {
                repo.saveProfile(current.nickname, gender)
                _state.value = _state.value.copy(saving = false, done = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = e.message ?: "Gagal menyimpan profil",
                )
            }
        }
    }

    companion object {
        const val MAX_NAME = 30
    }
}
