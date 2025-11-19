package com.example.apputbid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.apputbid.ui.AuthViewModel
import com.example.apputbid.ui.auth.UniBiddingApp
import com.example.apputbid.ui.theme.UniBiddingTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // ✅ we are inside a composable context here
            val systemDark = isSystemInDarkTheme()

            // ✅ rememberSaveable holds the theme across config changes / process death
            var darkTheme by rememberSaveable {
                mutableStateOf(systemDark)
            }

            UniBiddingTheme(darkTheme = darkTheme) {
                UniBiddingApp(
                    vm = authViewModel,
                    isDarkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}
