package com.example.apputbid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.apputbid.ui.AuthViewModel
import com.example.apputbid.ui.auth.UniBiddingApp
import com.example.apputbid.ui.theme.UniBiddingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AndroidViewModel so default factory works:
        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setContent {
            UniBiddingTheme {
                UniBiddingApp(vm = authViewModel)
            }
        }
    }
}
