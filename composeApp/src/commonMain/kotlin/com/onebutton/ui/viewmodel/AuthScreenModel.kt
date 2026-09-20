package com.onebutton.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.onebutton.model.Profile
import com.onebutton.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthScreenModel(private val authRepository: AuthRepository) : ScreenModel {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        screenModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.login(email, pass)
                val profile = authRepository.getCurrentProfile()
                if (profile != null) {
                    _authState.value = AuthState.Authenticated(profile)
                } else {
                    _authState.value = AuthState.Error("Profile not found")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val profile: Profile) : AuthState()
    data class Error(val message: String) : AuthState()
}
