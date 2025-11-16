package com.example.apputbid.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.apputbid.ui.auth.AuthViewModel
import com.example.apputbid.ui.auth.LoginScreen
import com.example.apputbid.ui.main.PlayerStats
import com.example.apputbid.ui.main.ProfileDrawer
import com.example.apputbid.ui.theme.UniBiddingTheme
import com.example.apputbid.ui.wallet.WalletScreen

// Bidding data classes
data class BiddingEvent(
    val id: Int,
    val title: String,
    val team1: String,
    val team2: String,
    val odds1: Double,
    val odds2: Double,
    val category: String
)

data class Game(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val date: String,
    val status: String,
    val sport: String
)

data class Team(
    val name: String,
    val wins: Int,
    val losses: Int,
    val sport: String
)

data class Bid(
    val eventId: Int,
    val eventTitle: String,
    val team: String,
    val amount: Double,
    val odds: Double
)

object BiddingDatabase {
    val events = listOf(
        BiddingEvent(1, "Men's Soccer", "Blue Ballers", "Kinfolk", 1.8, 2.1, "Sports"),
        BiddingEvent(1, "Men's Soccer", "Calmation", "DDD FC", 1.5, 2.5, "Sports"),
        BiddingEvent(2, "Women's Soccer", "Oval Gladiators", "Heavy Flow", 1.9, 1.9, "Sports"),
        BiddingEvent(2, "Women's Soccer", "Ball Handlers", "The SockHers", 2.0, 1.7, "Sports"),
    )

    val teams = listOf(
        Team("Blue Ballers", 12, 3, "Men's Soccer"),
        Team("Kinfolk", 10, 5, "Men's Soccer"),
        Team("Oval Gladiators", 8, 7, "Women's Soccer"),
        Team("Heavy Flow", 11, 4, "Women's Soccer"),
        Team("DDD FC", 9, 6, "Men's Soccer"),
        Team("Calmation", 7, 8, "Men's Soccer"),
        Team("Ball Handlers", 13, 2, "Women's Soccer"),
        Team("The SockHers", 6, 9, "Women's Soccer")
    )

    val games = listOf(
        Game(1, "Blue Ballers", "Kinfolk", 4, 1, "Today, 3:00 PM", "completed", "Men's Soccer"),
        Game(2, "Oval Gladiators", "Heavy Flow", 2, 3, "Today, 6:30 PM", "completed", "Women's Soccer"),
        Game(3, "Calmation", "DDD FC", null, null, "Tomorrow, 4:00 PM", "upcoming", "Men's Soccer"),
        Game(4, "Ball Handlers", "The SockHers", 3, 2, "Yesterday", "completed", "Women's Soccer"),
    )

    private val userBids = mutableMapOf<String, MutableList<Bid>>()

    fun placeBid(username: String, bid: Bid) {
        if (!userBids.containsKey(username)) {
            userBids[username] = mutableListOf()
        }
        userBids[username]?.add(bid)
    }

    fun getUserBids(username: String): List<Bid> {
        return userBids[username] ?: emptyList()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setContent {
            UniBiddingTheme {
                UniBiddingApp(authViewModel)
            }
        }
    }
}

@Composable
fun UniBiddingApp(vm: AuthViewModel) {
    val state by vm.state.collectAsState()

    when (state.currentUser) {
        null -> LoginScreen(vm = vm)
        else -> MainScreen(
            username = state.currentUser!!.username,
            onLogout = { vm.logout() }
        )
    }
}

@Composable
fun MainScreen(
    username: String,
    onLogout: () -> Unit,
    initialDarkTheme: Boolean = false  // Add parameter with default value
) {
    var selectedTab by remember { mutableStateOf(0) }
    var balance by remember { mutableStateOf(1000.0) }
    var isDarkTheme by remember { mutableStateOf(initialDarkTheme) }  // Use the parameter
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Mock player stats - you can connect this to real data later
    val playerStats = remember { PlayerStats(wins = 15, losses = 8) }

    UniBiddingTheme(darkTheme = isDarkTheme) {
        ProfileDrawer(
            drawerState = drawerState,
            username = username,
            playerStats = playerStats,
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme },
            onLogout = onLogout
        ) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    "Home"
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
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selectedTab == 1) Icons.Filled.List else Icons.Outlined.List,
                                    "Bidding"
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
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selectedTab == 2) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                    "Wallet"
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
                    }
                }
            ) { paddingValues ->
                when (selectedTab) {
                    0 -> HomeScreen(
                        username = username,
                        balance = balance,
                        modifier = Modifier.padding(paddingValues),
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )

                    1 -> BiddingScreen(
                        username,
                        balance,
                        onBalanceChange = { balance = it },
                        Modifier.padding(paddingValues)
                    )

                    2 -> WalletScreen(
                        balance = balance,
                        username = username,
                        onBalanceChange = { balance = it },
                        onBack = { selectedTab = 0 }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    username: String,
    balance: Double,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("All") }

    // Get unique sports from games
    val sports = remember {
        val sportsFromGames = BiddingDatabase.games.map { it.sport }.distinct()
        val allSports = (sportsFromGames + listOf("Basketball", "Volleyball", "Beach Volleyball")).distinct().sorted()
        listOf("All") + allSports
    }

    // Filter games based on search query and selected sport
    val filteredGames = remember(searchQuery, selectedSport) {
        var games = BiddingDatabase.games

        // Filter by sport
        if (selectedSport != "All") {
            games = games.filter { it.sport == selectedSport }
        }

        // Filter by search query
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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            item {
                // Top Bar
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = onMenuClick) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                            }
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
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = username,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Profile",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            item {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search teams, sports, or status...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            item {
                // Balance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                            fontSize = 16.sp,
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

            item {
                Text(
                    text = "Quick Stats",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                // Quick Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Active Bids", "0", Modifier.weight(1f))
                    StatCard("Total Won", "$0.00", Modifier.weight(1f))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Sports Tabs
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

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isBlank() && selectedSport == "All") "Recent & Upcoming Games" else "Search Results",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try searching for a different team or sport",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Game Cards
                items(filteredGames) { game ->
                    GameCard(
                        game = game,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GameCard(game: Game, modifier: Modifier = Modifier) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.sport,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = when (game.status) {
                        "completed" -> MaterialTheme.colorScheme.surfaceVariant
                        "live" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (game.status) {
                            "completed" -> "Final"
                            "live" -> "Live"
                            else -> "Upcoming"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (game.status) {
                            "completed" -> MaterialTheme.colorScheme.onSurfaceVariant
                            "live" -> Color.White
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.homeTeam,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = game.awayTeam,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (game.status == "completed") {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = game.homeScore.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (game.homeScore!! > game.awayScore!!)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = game.awayScore.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (game.awayScore!! > game.homeScore!!)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = game.date,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BiddingScreen(username: String, balance: Double, onBalanceChange: (Double) -> Unit, modifier: Modifier = Modifier) {
    var selectedEvent by remember { mutableStateOf<BiddingEvent?>(null) }
    var showBidDialog by remember { mutableStateOf(false) }
    var selectedTeam by remember { mutableStateOf("") }

    var groupedEvents = remember {
        BiddingDatabase.events.groupBy { it.title }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
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

            // Events List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedEvents.forEach { (sportTitle, events) ->
                    item {
                        SportGroupCard(
                            sportTitle = sportTitle,
                            events = events,
                            onBidClick = { event, team ->
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
                    val odds = if (selectedTeam == selectedEvent!!.team1) selectedEvent!!.odds1 else selectedEvent!!.odds2
                    BiddingDatabase.placeBid(
                        username,
                        Bid(selectedEvent!!.id, selectedEvent!!.title, selectedTeam, amount, odds)
                    )
                    onBalanceChange(balance - amount)
                }
                showBidDialog = false
            }
        )
    }
}

@Composable
fun SportGroupCard(
    sportTitle: String,
    events: List<BiddingEvent>,
    onBidClick: (BiddingEvent, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Sport Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sportTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${events.size} matches",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // List of matches for this sport
            events.forEachIndexed { index, event ->
                MatchRow(
                    event = event,
                    onBidClick = onBidClick
                )

                // Add divider between matches except for the last one
                if (index < events.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun MatchRow(
    event: BiddingEvent,
    onBidClick: (BiddingEvent, String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TeamBidButton(
            team = event.team1,
            odds = event.odds1,
            onClick = { onBidClick(event, event.team1) },
            modifier = Modifier.weight(1f)
        )

        TeamBidButton(
            team = event.team2,
            odds = event.odds2,
            onClick = { onBidClick(event, event.team2) },
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun TeamBidButton(team: String, odds: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3)  // Blue color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = team,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,  // White text for better contrast on blue
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${odds}x",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White  // White odds text
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
        title = {
            Text(
                text = "Place Bid",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Event: ${event.title}")
                Text("Team: $team", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Odds: ${odds}x")
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bidAmount,
                    onValueChange = { bidAmount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Bid Amount") },
                    prefix = { Text("$") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Potential Win: $${"%.2f".format(potentialWin)}", fontWeight = FontWeight.Bold)
                Text("Available Balance: $${"%.2f".format(balance)}", fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = bidAmount.toDoubleOrNull()
                    if (amount != null && amount > 0 && amount <= balance) {
                        onConfirm(amount)
                    }
                },
                enabled = bidAmount.toDoubleOrNull()?.let { it > 0 && it <= balance } ?: false
            ) {
                Text("Confirm Bid")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    UniBiddingTheme(darkTheme = false) {
        MainScreen(
            username = "BigBalla67",
            onLogout = {},
            initialDarkTheme = false  // Explicitly pass false for light theme
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DarkThemePreview() {
    UniBiddingTheme(darkTheme = true) {
        MainScreen(
            username = "BigBalla67",
            onLogout = {},
            initialDarkTheme = true  // Explicitly pass true for dark theme
        )
    }
}