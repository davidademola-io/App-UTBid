//package com.example.apputbid.ui.auth
//
//import android.content.res.Configuration
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AdminPanelSettings
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.apputbid.ui.AuthViewModel
//import com.example.apputbid.ui.theme.UniBiddingTheme
//
//
//@Composable
//fun LoginRoute(vm: AuthViewModel) {
//    val ui by vm.state.collectAsState()
//    var username by remember { mutableStateOf("") }   // <-- VM expects "username"
//    var password by remember { mutableStateOf("") }
//    var showAdmin by remember { mutableStateOf(false) }
//
//    Surface(Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text("Welcome", style = MaterialTheme.typography.headlineMedium)
//            Spacer(Modifier.height(24.dp))
//
//            OutlinedTextField(
//                value = username,
//                onValueChange = { username = it },
//                label = { Text("Username") },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(Modifier.height(12.dp))
//
//            OutlinedTextField(
//                value = password,
//                onValueChange = { password = it },
//                label = { Text("Password") },
//                visualTransformation = PasswordVisualTransformation(),
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            if (ui.error != null) {
//                Spacer(Modifier.height(12.dp))
//                Text(ui.error!!, color = MaterialTheme.colorScheme.error)
//            }
//
//            Spacer(Modifier.height(16.dp))
//            Button(
//                onClick = { vm.login(username, password) }, // <-- EXACT match
//                enabled = !ui.loading,
//                modifier = Modifier.fillMaxWidth()
//            ) { Text(if (ui.loading) "Logging in..." else "Log in") }
//
//            Spacer(Modifier.height(12.dp))
//            OutlinedButton(
//                onClick = { showAdmin = true },
//                enabled = !ui.loading,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
//                Spacer(Modifier.width(8.dp))
//                Text("Admin login")
//            }
//        }
//    }
//
//    if (showAdmin) {
//        AdminLoginDialog(
//            onDismiss = { showAdmin = false },
//            onSubmit = { pass ->
//                vm.adminLogin(pass)  // see VM method below
//                showAdmin = false
//            }
//        )
//    }
//}
//
//@Composable
//private fun AdminLoginDialog(
//    onDismiss: () -> Unit,
//    onSubmit: (String) -> Unit
//) {
//    var key by remember { mutableStateOf("") }
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Admin Login") },
//        text = {
//            OutlinedTextField(
//                value = key,
//                onValueChange = { key = it },
//                label = { Text("Admin passcode") },
//                singleLine = true,
//                visualTransformation = PasswordVisualTransformation(),
//                modifier = Modifier.fillMaxWidth()
//            )
//        },
//        confirmButton = { TextButton(onClick = { onSubmit(key) }) { Text("Enter") } },
//        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun LoginScreenPreview() {
//    UniBiddingTheme(darkTheme = false) {
//        LoginScreen(vm = viewModel())
//    }
//}
//
//@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Composable
//fun LoginScreenDarkPreview() {
//    UniBiddingTheme(darkTheme = true) {
//        LoginScreen(vm = viewModel())
//    }
//}