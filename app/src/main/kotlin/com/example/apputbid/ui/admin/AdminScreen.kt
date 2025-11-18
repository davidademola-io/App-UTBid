package com.example.apputbid.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apputbid.ui.AuthViewModel
import com.example.apputbid.ui.main.BiddingDatabase
import com.example.apputbid.ui.main.Game

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onBack: () -> Unit,
    onAdminAuthed: (String) -> Unit, // <-- pass key out
) {
    var key by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Login") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Admin passcode") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onAdminAuthed(key) }, // <-- send key to caller
                modifier = Modifier.fillMaxWidth(),
                enabled = key.isNotBlank()
            ) { Text("Enter Admin") }
        }
    }
}

@Composable
fun BannedUserScreen(onLogout: () -> Unit) {
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
                text = "Account Banned",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Your account has been suspended by an administrator.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                )
            ) {
                Text("Return to Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UsersSection(vm: AuthViewModel) {
    val users = BiddingDatabase.getAllUsers()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "User Management",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4169E1),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (users.isEmpty()) {
            item {
                Text(
                    "No users registered yet",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(users) { username ->

            // 🔑 Local state per user row
            var isBanned by remember(username) {
                mutableStateOf(BiddingDatabase.isBanned(username))
            }

            // 🔁 Side effect: whenever isBanned changes, persist it via ViewModel
            LaunchedEffect(isBanned) {
                // fire-and-forget; if it throws, UI still works
                vm.setUserBanned(username, isBanned)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBanned)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            username,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isBanned) {
                            Text(
                                "BANNED",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val newValue = !isBanned

                            // ✅ Same behavior as the original working version:
                            // update BiddingDatabase *and* local state.
                            if (newValue) {
                                BiddingDatabase.banUser(username)
                            } else {
                                BiddingDatabase.unbanUser(username)
                            }
                            isBanned = newValue
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBanned)
                                Color(0xFF4CAF50)  // green when banned → "Unban"
                            else
                                MaterialTheme.colorScheme.error // red when not banned → "Ban"
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isBanned) "Unban" else "Ban")
                    }
                }
            }
        }
    }
}





@Composable
fun AddGameSection() {
    var sport by remember { mutableStateOf("") }
    var homeTeam by remember { mutableStateOf("") }
    var awayTeam by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var homeOdds by remember { mutableStateOf("") }
    var awayOdds by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Add New Game",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4169E1)
            )
        }

        item {
            OutlinedTextField(
                value = sport,
                onValueChange = { sport = it },
                label = { Text("Sport") },
                placeholder = { Text("e.g., Men's Soccer") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4169E1),
                    focusedLabelColor = Color(0xFF4169E1)
                )
            )
        }

        item {
            OutlinedTextField(
                value = homeTeam,
                onValueChange = { homeTeam = it },
                label = { Text("Home Team") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4169E1),
                    focusedLabelColor = Color(0xFF4169E1)
                )
            )
        }

        item {
            OutlinedTextField(
                value = awayTeam,
                onValueChange = { awayTeam = it },
                label = { Text("Away Team") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4169E1),
                    focusedLabelColor = Color(0xFF4169E1)
                )
            )
        }

        item {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date/Time") },
                placeholder = { Text("e.g., Tomorrow, 4:00 PM") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4169E1),
                    focusedLabelColor = Color(0xFF4169E1)
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = homeOdds,
                    onValueChange = { homeOdds = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Home Odds") },
                    placeholder = { Text("e.g., 1.8") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4169E1),
                        focusedLabelColor = Color(0xFF4169E1)
                    )
                )
                OutlinedTextField(
                    value = awayOdds,
                    onValueChange = { awayOdds = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Away Odds") },
                    placeholder = { Text("e.g., 2.1") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4169E1),
                        focusedLabelColor = Color(0xFF4169E1)
                    )
                )
            }
        }

        if (successMessage.isNotEmpty()) {
            item {
                Text(
                    successMessage,
                    color = Color(0xFF4CAF50),  // Green
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Button(
                    onClick = {
                        val homeOddsDouble = homeOdds.toDoubleOrNull()
                        val awayOddsDouble = awayOdds.toDoubleOrNull()

                        if (sport.isNotBlank() && homeTeam.isNotBlank() &&
                            awayTeam.isNotBlank() && date.isNotBlank() &&
                            homeOddsDouble != null && awayOddsDouble != null
                        ) {
                            BiddingDatabase.addGameAndEvent(
                                sport = sport,
                                homeTeam = homeTeam,
                                awayTeam = awayTeam,
                                date = date,
                                homeOdds = homeOddsDouble,
                                awayOdds = awayOddsDouble
                            )

                            successMessage = "Game added successfully!"
                            sport = ""
                            homeTeam = ""
                            awayTeam = ""
                            date = ""
                            homeOdds = ""
                            awayOdds = ""
                        }
                    },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = sport.isNotBlank() && homeTeam.isNotBlank() &&
                        awayTeam.isNotBlank() && date.isNotBlank() &&
                        homeOdds.isNotBlank() && awayOdds.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                )
            ) {
                Text("Add Game", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SetResultsSection() {
    val upcomingGames = BiddingDatabase.games.filter { it.status == "upcoming" }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    if (selectedGame == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Select Game to Set Result",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4169E1),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (upcomingGames.isEmpty()) {
                item {
                    Text(
                        "No upcoming games",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(upcomingGames) { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGame = game }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            game.sport,
                            fontSize = 12.sp,
                            color = Color(0xFF4169E1),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${game.homeTeam} vs ${game.awayTeam}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            game.date,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else {
        SetGameResultScreen(
            game = selectedGame!!,
            onBack = {
                selectedGame = null
                refreshTrigger++
            }
        )
    }
}

@Composable
fun SetGameResultScreen(game: Game, onBack: () -> Unit) {
    var homeScore by remember { mutableStateOf("") }
    var awayScore by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to Games", color = Color(0xFF4169E1))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)  // Light blue
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    game.sport,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4169E1)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${game.homeTeam} vs ${game.awayTeam}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            "Enter Final Score",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4169E1)
        )

        OutlinedTextField(
            value = homeScore,
            onValueChange = { homeScore = it.filter { char -> char.isDigit() } },
            label = { Text("${game.homeTeam} Score") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4169E1),
                focusedLabelColor = Color(0xFF4169E1)
            )
        )

        OutlinedTextField(
            value = awayScore,
            onValueChange = { awayScore = it.filter { char -> char.isDigit() } },
            label = { Text("${game.awayTeam} Score") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4169E1),
                focusedLabelColor = Color(0xFF4169E1)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val home = homeScore.toIntOrNull()
                val away = awayScore.toIntOrNull()

                if (home != null && away != null) {
                    BiddingDatabase.updateGameResult(game.id, home, away)
                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = homeScore.toIntOrNull() != null && awayScore.toIntOrNull() != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4169E1)
            )
        ) {
            Text("Set Result & Resolve Bets", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    vm: AuthViewModel,
    onLogout: () -> Unit) {
    var selectedSection by remember { mutableStateOf(0) }
    var showAccountSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4169E1),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Admin Dashboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = { showAccountSheet = true }) {
                        Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Text("A", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Color(0xFF4169E1)
            ) {
                Tab(selected = selectedSection == 0, onClick = { selectedSection = 0 }, text = { Text("Users") })
                Tab(selected = selectedSection == 1, onClick = { selectedSection = 1 }, text = { Text("Add Game") })
                Tab(selected = selectedSection == 2, onClick = { selectedSection = 2 }, text = { Text("Set Results") })
            }

            when (selectedSection) {
                0 -> UsersSection(vm = vm)
                1 -> AddGameSection()
                2 -> SetResultsSection()
            }
        }
    }

    if (showAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                ListItem(
                    headlineContent = { Text("Logout") },
                    modifier = Modifier.clickable {
                        showAccountSheet = false
                        onLogout()
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


