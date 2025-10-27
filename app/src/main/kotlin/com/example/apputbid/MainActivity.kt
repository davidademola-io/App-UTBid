package com.example.apputbid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppGreeting()
        }
    }
}

@Composable
fun AppGreeting() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Text(text = "Hello Kotlin Android!")
    }
}
