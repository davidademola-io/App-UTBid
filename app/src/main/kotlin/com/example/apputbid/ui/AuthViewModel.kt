package com.example.apputbid.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apputbid.BuildConfig
import com.example.apputbid.data.AuthRepository
import com.example.apputbid.data.BalanceStore
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

    // Expose the repository to the UI layer (UniBiddingApp, MainScreen, etc.)
    val authRepository: AuthRepository
        get() = repo

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        viewModelScope.launch {
            try {
                val allUsernames = repo.getAllUsernames()
                BiddingDatabase.seedUsers(allUsernames)

                val banned = repo.getAllBannedUsernames()
                BiddingDatabase.seedBanned(banned)

                // NEW: load and apply any saved game results
                val overrides = repo.getAllGameResults()
                BiddingDatabase.applyResultOverrides(overrides)

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
            // 1) Check if the user is banned **before** trying to authenticate
            val isBanned = try {
                repo.isBanned(username)
            } catch (t: Throwable) {
                t.printStackTrace()
                false  // if something goes wrong, don't block login just from this
            }

            if (isBanned) {
                // Show message in the same way other auth errors show on LoginScreen
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Your account has been locked. Please contact customer service."
                )
                return@launch
            }

            // 2) Proceed with normal login flow if not banned
            val res = repo.login(username, password.toCharArray())
            _state.value = if (res.isSuccess) {
                BiddingDatabase.ensureUser(username) // still ensures they appear in Admin + $20 if new
                _state.value.copy(
                    loading = false,
                    currentUser = res.getOrNull(),
                    route = Route.Main,
                    isAdmin = false
                )
            } else {
                _state.value.copy(
                    loading = false,
                    error = res.exceptionOrNull()?.message
                )
            }
        }
    }


    fun logout() {
        _state.value = AuthState(route = Route.Login)
    }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    // --- admin auth (passcode only, no DB row needed) ---
    fun adminLogin() {
        // ✅ Credentials have already been validated in AdminLoginScreen.
        // Just mark this session as admin and route to the Admin dashboard.
        _state.value = _state.value.copy(
            error = null,
            currentUser = User(
                id = -1L,
                username = "admin",
                saltB64 = "",
                hashB64 = ""
            ),
            isAdmin = true,
            route = Route.Admin
        )
    }



    fun updateFinalScore(
        gameId: Int,
        homeScore: Int,
        awayScore: Int,
        onDone: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                // 1) Persist result in SQLite
                repo.saveGameResult(
                    gameId = gameId,
                    home = homeScore,
                    away = awayScore,
                    status = "completed"
                )

                // 2) Update in-memory games + resolve bets -> payouts
                val payouts: List<BiddingDatabase.BetPayout> =
                    BiddingDatabase.updateGameResult(
                        gameId = gameId,
                        homeScore = homeScore,
                        awayScore = awayScore
                    )

                // 3) Apply payouts to each user's wallet
                if (payouts.isNotEmpty()) {
                    val ctx = getApplication<Application>().applicationContext

                    // Update balances as before
                    payouts
                        .groupBy { it.username }
                        .forEach { (username, userPayouts) ->
                            val total = userPayouts.sumOf { it.amount }
                            BalanceStore.adjustBalance(ctx, username, total)
                        }

                    // 4) 🔹 NEW: persist each bet result into bet_history table
                    payouts.forEach { p ->
                        val resultString = when (p.result) {
                            BiddingDatabase.BetResult.WIN -> "WIN"
                            BiddingDatabase.BetResult.LOSS -> "LOSS"
                            BiddingDatabase.BetResult.PUSH -> "PUSH"
                        }

                        repo.updateBetHistoryResult(
                            username = p.username,
                            gameId = p.gameId,
                            pick = p.pick,
                            result = resultString,
                            payout = p.amount
                        )
                    }

                }

                onDone(true, null)
            } catch (t: Throwable) {
                t.printStackTrace()
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

    fun setUserBanned(
        username: String,
        banned: Boolean,
        onDone: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                // write to SQLite
                repo.setBanned(username, banned)

                // keep in-memory DB in sync for the rest of the app
                if (banned) {
                    BiddingDatabase.banUser(username)
                } else {
                    BiddingDatabase.unbanUser(username)
                }

                onDone(true, null)
            } catch (t: Throwable) {
                t.printStackTrace()
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
