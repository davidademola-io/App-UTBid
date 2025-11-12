package com.example.apputbid.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apputbid.data.AuthRepository
import com.example.apputbid.data.User
import com.example.apputbid.data.UserDbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val loading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(UserDbHelper.get(app))

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun register(username: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val res = repo.register(username, password.toCharArray())
            _state.value = if (res.isSuccess) {
                _state.value.copy(loading = false)
            } else {
                _state.value.copy(loading = false, error = res.exceptionOrNull()?.message)
            }
        }
    }

    fun login(username: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val res = repo.login(username, password.toCharArray())
            _state.value = if (res.isSuccess) {
                _state.value.copy(loading = false, currentUser = res.getOrNull())
            } else {
                _state.value.copy(loading = false, error = res.exceptionOrNull()?.message)
            }
        }
    }

    fun logout() { _state.value = AuthState() }
    fun clearError() { _state.value = _state.value.copy(error = null) }
}
