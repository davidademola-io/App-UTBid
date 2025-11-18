package com.example.apputbid.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apputbid.ui.AuthViewModel
import com.example.apputbid.ui.Route
import com.example.apputbid.ui.admin.AdminDashboard
import com.example.apputbid.ui.admin.AdminLoginScreen
import com.example.apputbid.ui.main.MainScreen

@Composable
fun UniBiddingApp(vm: AuthViewModel) {
    val state by vm.state.collectAsState()

    when (state.route) {
        is Route.Login -> LoginScreen(vm = vm)

        is Route.AdminLogin -> AdminLoginScreen(
            onBack = { vm.backToLogin() },
            onAdminAuthed = { pass -> vm.adminLogin(pass) } // uses BuildConfig.ADMIN_PASSCODE
        )

        is Route.Main -> MainScreen(
            username = state.currentUser?.username.orEmpty(),
            onLogout = { vm.logout() }
        )

        is Route.Admin -> AdminDashboard(
            vm = vm,
            onLogout = { vm.logout() }
        )

        Route.Admin -> TODO()
        Route.AdminLogin -> TODO()
        Route.Login -> TODO()
        Route.Main -> TODO()
    }
}

@Composable
fun LoginScreen(vm: AuthViewModel) {
    val state by vm.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showAdmin by remember { mutableStateOf(false) }
    var adminPass by remember { mutableStateOf("") }

    val errorMessage = state.error.orEmpty()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "UTBid",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Place Your Bets",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { vm.login(username, password) },
                enabled = !state.loading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (state.loading) "Signing in..." else "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { vm.register(username, password) },
                enabled = !state.loading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    if (state.loading) "Creating..." else "Register",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // —— Admin login button ——
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { vm.goToAdminLogin() },   // << was dialog before; now navigates
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Admin login")
            }
        }
    }

    // —— Admin passcode dialog ——
    if (showAdmin) {
        AlertDialog(
            onDismissRequest = { showAdmin = false },
            title = { Text("Admin Login") },
            text = {
                OutlinedTextField(
                    value = adminPass,
                    onValueChange = { adminPass = it },
                    label = { Text("Admin passcode") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Requires vm.adminLogin(passcode). See VM snippet below.
                        vm.adminLogin(adminPass)
                        adminPass = ""
                        showAdmin = false
                    }
                ) { Text("Enter") }
            },
            dismissButton = {
                TextButton(onClick = { showAdmin = false }) { Text("Cancel") }
            }
        )
    }
}
