package com.example.apputbid.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apputbid.BuildConfig
import com.example.apputbid.data.AuthRepository
import com.example.apputbid.data.User
import com.example.apputbid.data.UserDbHelper
import com.example.apputbid.ui.main.BiddingDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val loading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val route: Route = Route.Login,
    val isAdmin: Boolean = false
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(UserDbHelper.get(app))

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        viewModelScope.launch {
            try {
                val allUsernames = repo.getAllUsernames()
                BiddingDatabase.seedUsers(allUsernames)

                // NEW: load banned users from DB into in-memory cache
                val banned = repo.getAllBannedUsernames()
                BiddingDatabase.seedBanned(banned)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }


    // --- navigation helpers ---
    fun goToAdminLogin() {
        _state.value = _state.value.copy(error = null, route = Route.AdminLogin)
    }
    fun backToLogin() {
        _state.value = _state.value.copy(error = null, route = Route.Login)
    }

    // --- user flows (unchanged) ---
    fun register(username: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val res = repo.register(username, password.toCharArray())
            _state.value = if (res.isSuccess) {
                BiddingDatabase.ensureUser(username) // <-- start at $20
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
                BiddingDatabase.ensureUser(username) // <-- makes sure they’re listed + $20 if new
                _state.value.copy(
                    loading = false,
                    currentUser = res.getOrNull(),
                    route = Route.Main,
                    isAdmin = false
                )
            } else {
                _state.value.copy(loading = false, error = res.exceptionOrNull()?.message)
            }
        }
    }

    fun logout() {
        _state.value = AuthState(route = Route.Login)
    }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    // --- admin auth (passcode only, no DB row needed) ---
    fun adminLogin(passcode: String) {
        val ok = passcode == BuildConfig.ADMIN_PASSCODE
        _state.value = if (ok) {
            _state.value.copy(
                error = null,
                // create a virtual admin user to reuse your UI if needed
                currentUser = User(id = -1L, username = "admin", saltB64 = "", hashB64 = ""),
                isAdmin = true,
                route = Route.Admin
            )
        } else {
            _state.value.copy(error = "Invalid admin passcode")
        }
    }


    fun updateFinalScore(gameId: Long, homeScore: Int, awayScore: Int, onDone: (Boolean, String?) -> Unit = {_,_->}) {
        viewModelScope.launch {
            try {
                // TODO: repo.updateFinalScore(gameId, homeScore, awayScore)
                onDone(true, null)
            } catch (t: Throwable) {
                onDone(false, t.message)
            }
        }
    }

    fun addTeam(sport: String, teamName: String, onDone: (Boolean, String?) -> Unit = {_,_->}) {
        viewModelScope.launch {
            try {
                // TODO: repo.addTeam(sport, teamName)
                onDone(true, null)
            } catch (t: Throwable) {
                onDone(false, t.message)
            }
        }
    }

    fun setUserBanned(username: String, banned: Boolean, onDone: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                repo.setBanned(username, banned)

                // keep in-memory cache in sync
                if (banned) {
                    BiddingDatabase.banUser(username)
                } else {
                    BiddingDatabase.unbanUser(username)
                }

                onDone(true, null)
            } catch (t: Throwable) {
                onDone(false, t.message)
            }
        }
    }

}

sealed class Route {
    data object Login : Route()
    data object AdminLogin : Route()
    data object Main : Route()
    data object Admin : Route()
}
