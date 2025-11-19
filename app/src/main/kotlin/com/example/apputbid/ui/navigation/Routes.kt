package com.example.apputbid.ui.navigation

class Routes {

    sealed class Screen(val route: String) {
        data object Login : Screen("login")
        data object Main : Screen("main")
        data object AdminLogin : Screen("admin_login")
    }
}