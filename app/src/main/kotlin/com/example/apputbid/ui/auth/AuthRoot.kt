package com.example.apputbid.ui.auth

import androidx.compose.runtime.*
import com.example.apputbid.ui.MainScreen

@Composable
fun UniBiddingApp(vm: AuthViewModel) {
    val state by vm.state.collectAsState()

    when (state.currentUser) {
        null -> LoginScreen(vm = vm)
        else -> MainScreen(
            username = state.currentUser!!.username,
            onLogout = { vm.logout() }
        )
    }
}