package com.example.apputbid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.apputbid.ui.auth.UniBiddingApp
import com.example.apputbid.ui.theme.UniBiddingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniBiddingTheme {
                UniBiddingApp()
            }
        }
    }
}
