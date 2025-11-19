package com.example.apputbid.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.apputbid.data.BalanceStore
import com.example.apputbid.data.BidsStore
import com.example.apputbid.data.AuthRepository
import com.example.apputbid.ui.settings.SettingsScreen
import com.example.apputbid.ui.wallet.WalletScreen

/* =========================
   MAIN SCREEN + NAV BAR
   ========================= */

@Composable
fun MainScreen(
    username: String,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    authRepository: AuthRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ Per-user balance
    val balance by BalanceStore
        .balanceFlow(context, username)
        .collectAsState(initial = BalanceStore.DEFAULT_BALANCE)

    var selectedTab by remember { mutableStateOf(0) }

    val updateBalance: (Double) -> Unit = { newBalance ->
        scope.launch {
            BalanceStore.setBalance(context, username, newBalance)
        }
    }

    // Drawer + settings
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var playerStats by remember { mutableStateOf(PlayerStats(wins = 0, losses = 0)) }

    LaunchedEffect(username) {
        // Load stats from SQLite via AuthRepository
        val stats = authRepository.getUserBetStats(username)
        playerStats = PlayerStats(
            wins = stats.wins,
            losses = stats.losses
        )
    }
    var showSettings by remember { mutableStateOf(false) }

    ProfileDrawer(
        drawerState = drawerState,
        username = username,
        playerStats = playerStats,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onLogout = onLogout,
        onNavigateToSettings = {
            scope.launch { drawerState.close() }
            showSettings = true
        }
    ) {
        if (showSettings) {
            // 🔹 Full-screen SettingsScreen
            SettingsScreen(
                username = username,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onBack = { showSettings = false },
                onLogout = onLogout
            )
        } else {
            // 🔹 Normal tabbed main UI
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        // 0 – Home
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home") },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )

                        // 1 – Bidding
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selectedTab == 1) Icons.Filled.List else Icons.Outlined.List,
                                    contentDescription = "Bidding"
                                )
                            },
                            label = { Text("Bidding") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )

                        // 2 – Wallet
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    Icons.Filled.AccountBalanceWallet,
                                    contentDescription = "Wallet"
                                )
                            },
                            label = { Text("Wallet") },
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )

                        // 3 – History
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.History, contentDescription = "History") },
                            label = { Text("History") },
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            ) { paddingValues ->
                when (selectedTab) {
                    0 -> HomeScreen(
                        username = username,
                        balance = balance,
                        authRepository = authRepository,
                        modifier = Modifier.padding(paddingValues),
                        onProfileClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                    1 -> BiddingScreen(
                        username = username,
                        balance = balance,
                        onBalanceChange = updateBalance,
                        modifier = Modifier.padding(paddingValues)
                    )
                    2 -> WalletScreen(
                        username = username,
                        balance = balance,
                        onBalanceChange = updateBalance,
                        modifier = Modifier.padding(paddingValues)
                    )
                    3 -> HistoryScreen(
                        username = username,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}


/* =========================
   HOME SCREEN + STATS
   ========================= */

@Composable
fun HomeScreen(
    username: String,
    balance: Double,
    authRepository: AuthRepository,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("All") }
    var showActiveBidsDialog by remember { mutableStateOf(false) }

    val userBids by BidsStore
        .bidsFlow(context, username)
        .collectAsState(initial = emptyList())

    // Only open bets
    val activeBets = BiddingDatabase.getActiveBetsForUser(username)
    val activeDisplayBids = activeBets.map { bet ->
        val game = BiddingDatabase.games.find { it.id == bet.gameId }
        val eventTitle = game?.sport ?: "Game #${bet.gameId}"
        val teamName = when (bet.pick) {
            "home" -> game?.homeTeam ?: "Home"
            "away" -> game?.awayTeam ?: "Away"
            else -> "Unknown"
        }
        Bid(
            eventId = bet.gameId,
            eventTitle = eventTitle,
            team = teamName,
            amount = bet.stake,
            odds = bet.odds
        )
    }
    val activeBidsCount = activeDisplayBids.size

    // 🔹 Total Won from SQLite via AuthRepository
    var totalWon by remember { mutableStateOf(0.0) }
    LaunchedEffect(username) {
        totalWon = authRepository.getTotalWon(username)
    }

    val sports = remember {
        listOf("All") + BiddingDatabase.games.map { it.sport }.distinct().sorted()
    }

    val filteredGames = remember(searchQuery, selectedSport) {
        var games = BiddingDatabase.games
        if (selectedSport != "All") games = games.filter { it.sport == selectedSport }
        if (searchQuery.isNotBlank()) {
            games = games.filter { game ->
                game.homeTeam.contains(searchQuery, ignoreCase = true) ||
                        game.awayTeam.contains(searchQuery, ignoreCase = true) ||
                        game.sport.contains(searchQuery, ignoreCase = true) ||
                        game.status.contains(searchQuery, ignoreCase = true)
            }
        }
        games
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {

            /* ===== Top bar (with small balance that never scrolls away) ===== */
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary,
                tonalElevation = 4.dp
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
                            text = "UTBid",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            text = "Welcome back!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        // Always-visible compact balance line
                        Text(
                            text = "Balance: $${"%.2f".format(balance)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = username,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onProfileClick() },
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }

            // Search (fixed above scrolling content)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search teams, sports, or status...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Everything below scrolls together
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Big balance card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Current Balance",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "$${"%.2f".format(balance)}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Quick Stats
                item {
                    Text(
                        text = "Quick Stats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Active Bids",
                            value = activeBidsCount.toString(),
                            modifier = Modifier.weight(1f),
                            onClick = { if (activeBidsCount > 0) showActiveBidsDialog = true }
                        )

                        // 🔹 Color logic for Total Won card
                        val (bgColor, txtColor) = when {
                            totalWon > 0.0 -> Pair(
                                Color(0xFF4CAF50),   // Green
                                Color.White
                            )
                            totalWon < 0.0 -> Pair(
                                Color(0xFFF44336),   // Red
                                Color.White
                            )
                            else -> Pair(
                                MaterialTheme.colorScheme.surfaceVariant,   // Neutral
                                MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StatCard(
                            title = "Total Won",
                            value = "$${"%.2f".format(totalWon)}",
                            modifier = Modifier.weight(1f),
                            backgroundColor = bgColor,
                            contentColor = txtColor
                        )
                    }
                }

                // Sport filter tabs
                item {
                    Spacer(Modifier.height(8.dp))
                    ScrollableTabRow(
                        selectedTabIndex = sports.indexOf(selectedSport),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sports.forEach { sport ->
                            Tab(
                                selected = selectedSport == sport,
                                onClick = { selectedSport = sport },
                                text = {
                                    Text(
                                        text = sport,
                                        fontWeight = if (selectedSport == sport) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                // List header
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                if (searchQuery.isBlank() && selectedSport == "All") "Recent & Upcoming Games"
                                else "Search Results",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (searchQuery.isNotBlank() || selectedSport != "All") {
                            Text(
                                text = "${filteredGames.size} found",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Games or empty state
                if (filteredGames.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No games found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Try searching for a different team or sport",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredGames) { game ->
                        GameCard(game)
                    }
                }
            }
        }

        if (showActiveBidsDialog) {
            ActiveBidsDialog(
                bids = activeDisplayBids,
                onDismiss = { showActiveBidsDialog = false }
            )
        }
    }
}

/* =========================
   BIDDING SCREEN
   ========================= */

@Composable
fun BiddingScreen(
    username: String,
    balance: Double,
    onBalanceChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedEvent by remember { mutableStateOf<BiddingEvent?>(null) }
    var showBidDialog by remember { mutableStateOf(false) }
    var selectedTeam by remember { mutableStateOf("") }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Available Events",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Balance: $${"%.2f".format(balance)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Only show events for games that are still "upcoming"
            val upcomingEvents = BiddingDatabase.events.filter { event ->
                val game = BiddingDatabase.games.find { it.id == event.id }
                game?.status == "upcoming"
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (upcomingEvents.isEmpty()) {
                    item {
                        Text(
                            text = "No upcoming events available for bidding.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(upcomingEvents) { event ->
                        EventCard(
                            event = event,
                            onBidClick = { team ->
                                selectedEvent = event
                                selectedTeam = team
                                showBidDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showBidDialog && selectedEvent != null) {
        BidDialog(
            event = selectedEvent!!,
            team = selectedTeam,
            balance = balance,
            onDismiss = { showBidDialog = false },
            onConfirm = { amount ->
                if (amount <= balance) {
                    val event = selectedEvent!!
                    val odds = if (selectedTeam == event.team1)
                        event.odds1 else event.odds2

                    // Persist bid history (for History tab)
                    scope.launch {
                        BidsStore.addBid(
                            context = context,
                            username = username,
                            bid = Bid(
                                eventId = event.id,
                                eventTitle = event.title,
                                team = selectedTeam,
                                amount = amount,
                                odds = odds
                            )
                        )
                    }

                    // Register active bet so it can be settled later
                    val pick = if (selectedTeam == event.team1) "home" else "away"
                    BiddingDatabase.placeBet(
                        username = username,
                        gameId = event.id,
                        pick = pick,
                        stake = amount,
                        odds = odds
                    )

                    // Update balance immediately (stake removed)
                    onBalanceChange(balance - amount)
                }
                showBidDialog = false
            }
        )
    }
}

/* =========================
   HISTORY
   ========================= */

@Composable
fun HistoryScreen(
    username: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bids by BidsStore
        .bidsFlow(context, username)
        .collectAsState(initial = emptyList())

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (bids.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No bids yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Place a bid to see your history here.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bids) { bid -> HistoryBidCard(bid = bid) }
            }
        }
    }
}

/* =========================
   CARDS + DIALOGS
   ========================= */

@Composable
fun EventCard(
    event: BiddingEvent,
    onBidClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = event.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeamBidButton(
                    team = event.team1,
                    odds = event.odds1,
                    onClick = { onBidClick(event.team1) },
                    modifier = Modifier.weight(1f)
                )
                TeamBidButton(
                    team = event.team2,
                    odds = event.odds2,
                    onClick = { onBidClick(event.team2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TeamBidButton(
    team: String,
    odds: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = team,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${odds}x",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BidDialog(
    event: BiddingEvent,
    team: String,
    balance: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var bidAmount by remember { mutableStateOf("") }
    val odds = if (team == event.team1) event.odds1 else event.odds2
    val potentialWin = bidAmount.toDoubleOrNull()?.let { it * odds } ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Place Bid", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Event: ${event.title}")
                Text(
                    "Team: $team",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Odds: ${odds}x")
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = bidAmount,
                    onValueChange = { input ->
                        bidAmount = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Bid Amount") },
                    prefix = { Text("$") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "Potential Win: $${"%.2f".format(potentialWin)}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Available Balance: $${"%.2f".format(balance)}",
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = bidAmount.toDoubleOrNull()
                    if (amount != null && amount > 0 && amount <= balance) onConfirm(amount)
                },
                enabled = bidAmount.toDoubleOrNull()?.let { it > 0 && it <= balance } ?: false
            ) {
                Text("Confirm Bid")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun HistoryBidCard(
    bid: Bid,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(bid.eventTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Team: ${bid.team}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Amount: $${"%.2f".format(bid.amount)}",
                fontWeight = FontWeight.Medium
            )
            Text("Odds: ${bid.odds}x")
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Potential win: $${"%.2f".format(bid.amount * bid.odds)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color? = null,
    contentColor: Color? = null
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    val containerColor = backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant
    val textColor = contentColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = textColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = textColor
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun ActiveBidsDialog(
    bids: List<Bid>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Active Bids",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (bids.isEmpty()) {
                Text("You have no active bids.")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bids) { bid ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = bid.eventTitle,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${bid.team}  •  $${"%.2f".format(bid.amount)} @ ${bid.odds}x"
                                )
                                Text(
                                    text = "Potential win: $${"%.2f".format(bid.amount * bid.odds)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun GameCard(
    game: Game,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: sport + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = game.sport,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = game.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Teams + optional score
            val homeScoreText = game.homeScore?.toString() ?: "-"
            val awayScoreText = game.awayScore?.toString() ?: "-"
            Text(
                text = "${game.homeTeam} $homeScoreText vs $awayScoreText ${game.awayTeam}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))
            // Status (completed / upcoming etc.)
            Text(
                text = game.status.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when (game.status.lowercase()) {
                    "completed" -> MaterialTheme.colorScheme.secondary
                    "upcoming" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
