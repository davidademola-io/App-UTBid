package com.example.apputbid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.apputbid.ui.AuthViewModel
import com.example.apputbid.ui.Route
import com.example.apputbid.ui.admin.AdminDashboard
import com.example.apputbid.ui.admin.AdminLoginScreen
import com.example.apputbid.ui.auth.LoginScreen
import com.example.apputbid.ui.main.MainScreen

// Colors
private val Orange = Color(0xFFFF6B35)
private val LightOrange = Color(0xFFFF8C61)
private val Blue = Color(0xFF004E89)
private val LightBlue = Color(0xFF1A759F)
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    secondary = Blue,
    tertiary = LightBlue,
    background = Color(0xFFFFFBF5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = LightOrange,
    secondary = LightBlue,
    tertiary = Blue,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun UniBiddingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

