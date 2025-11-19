package com.example.apputbid.ui.admin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

import com.example.apputbid.BuildConfig
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onBack: () -> Unit,
    onAdminAuthed: () -> Unit  // ✅ success callback, no params
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Login",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "Back",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4169E1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                    text = "Admin Login",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800) // orange branding
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Management Dashboard",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF4169E1)
                    ),
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Admin Username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4169E1),
                        focusedLabelColor = Color(0xFF4169E1)
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Admin Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4169E1),
                        focusedLabelColor = Color(0xFF4169E1)
                    )
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val correctUser = BuildConfig.ADMIN_USERNAME
                        val correctPass = BuildConfig.ADMIN_PASSWORD

                        if (username == correctUser && password == correctPass) {
                            errorMessage = ""
                            onAdminAuthed()   // ✅ tell parent “login OK”
                        } else {
                            errorMessage = "Invalid admin credentials"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4169E1)
                    ),
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text(
                        "Login as Admin",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBack) {
                    Text(
                        "Back to User Login",
                        color = Color(0xFF4169E1),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameSection() {
    var sport by remember { mutableStateOf("") }
    var homeTeam by remember { mutableStateOf("") }
    var awayTeam by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }       // selected date text
    var time by remember { mutableStateOf("") }       // selected time text
    var homeOdds by remember { mutableStateOf("") }
    var awayOdds by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // 🔽 dropdown expanded states
    var dateExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }

    // ----- DATES: next 14 days -----
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEE, MMM d")   // e.g. "Fri, Nov 22"
    }
    val today = remember { LocalDate.now() }
    val dateOptions = remember {
        (0..13).map { offset ->
            today.plusDays(offset.toLong()).format(dateFormatter)
        }
    }

    // ----- TIMES: 24 hours, 30-min increments -----
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("h:mm a")       // e.g. "12:00 AM", "1:30 PM"
    }
    val timeOptions = remember {
        buildList {
            var current = LocalTime.of(7,0) // 00:00
            repeat(30 ) {
                add(current.format(timeFormatter))
                current = current.plusMinutes(30)
            }
        }
    }

    // ✅ Single datetime string we store in DB
    val dateTimeText = remember(date, time) {
        if (date.isNotBlank() && time.isNotBlank()) {
            "$date $time"          // e.g. "Fri, Nov 22 4:30 PM"
        } else {
            ""
        }
    }

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

        // 🔽 Date dropdown (next 14 days, scrollable)
        item {
            ExposedDropdownMenuBox(
                expanded = dateExpanded,
                onExpandedChange = { dateExpanded = !dateExpanded }
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { /* readOnly */ },
                    readOnly = true,
                    label = { Text("Date") },
                    placeholder = { Text("Select a date") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4169E1),
                        focusedLabelColor = Color(0xFF4169E1)
                    )
                )

                ExposedDropdownMenu(
                    expanded = dateExpanded,
                    onDismissRequest = { dateExpanded = false },
                    modifier = Modifier.heightIn(max = 300.dp) // makes it scrollable
                ) {
                    dateOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                date = option
                                dateExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 🔽 Time dropdown (24h x 30min, scrollable)
        item {
            ExposedDropdownMenuBox(
                expanded = timeExpanded,
                onExpandedChange = { timeExpanded = !timeExpanded }
            ) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { /* readOnly */ },
                    readOnly = true,
                    label = { Text("Time") },
                    placeholder = { Text("Select a time") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4169E1),
                        focusedLabelColor = Color(0xFF4169E1)
                    )
                )

                ExposedDropdownMenu(
                    expanded = timeExpanded,
                    onDismissRequest = { timeExpanded = false },
                    modifier = Modifier.heightIn(max = 300.dp) // scrollable list
                ) {
                    timeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                time = option
                                timeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = homeOdds,
                    onValueChange = {
                        homeOdds = it.filter { char -> char.isDigit() || char == '.' }
                    },
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
                    onValueChange = {
                        awayOdds = it.filter { char -> char.isDigit() || char == '.' }
                    },
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
                    color = Color(0xFF4CAF50),
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
                        awayTeam.isNotBlank() && dateTimeText.isNotBlank() &&
                        homeOddsDouble != null && awayOddsDouble != null
                    ) {
                        BiddingDatabase.addGameAndEvent(
                            sport = sport,
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            date = dateTimeText,  // ✅ full "date + time" string
                            homeOdds = homeOddsDouble,
                            awayOdds = awayOddsDouble
                        )

                        successMessage = "Game added successfully!"
                        sport = ""
                        homeTeam = ""
                        awayTeam = ""
                        date = ""
                        time = ""
                        homeOdds = ""
                        awayOdds = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = sport.isNotBlank() &&
                        homeTeam.isNotBlank() &&
                        awayTeam.isNotBlank() &&
                        dateTimeText.isNotBlank() &&
                        homeOdds.isNotBlank() &&
                        awayOdds.isNotBlank(),
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
fun SetResultsSection(vm: AuthViewModel) {
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
            vm = vm,
            onBack = { selectedGame = null
            refreshTrigger++
            }
        )
    }
}

@Composable
fun SetGameResultScreen(game: Game,
                        vm: AuthViewModel,
                        onBack: () -> Unit) {
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
                    vm.updateFinalScore(
                        gameId = game.id,
                        homeScore = home,
                        awayScore = away
                    ) { ok, err ->
                        // optionally handle error; for now just go back
                        onBack()
                    }
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
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    vm: AuthViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Admin Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text(
                            "Logout",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4169E1),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs for sections
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Color(0xFF4169E1)
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text("Users") }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text("Add Game") }
                )
                Tab(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    text = { Text("Set Results") }
                )
            }

            // Section content
            when (selectedSection) {
                0 -> UsersSection(vm = vm)
                1 -> AddGameSection()
                2 -> SetResultsSection(vm = vm)
            }
        }
    }
}



