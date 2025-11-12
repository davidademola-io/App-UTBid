package com.example.apputbid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.apputbid.ui.AuthViewModel
import com.example.apputbid.ui.auth.UniBiddingApp



// Color Scheme
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
fun LoginScreen(
    vm: AuthViewModel,   // <-- comes from your Activity / UniBiddingApp
) {
    val state by vm.state.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                onClick = { vm.login(username, password) },   // <-- uses SQLite via ViewModel
                enabled = !state.loading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
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
                onClick = { vm.register(username, password) },  // <-- SQLite registration
                enabled = !state.loading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
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
        }
    }
}



@Composable
fun MainScreen(username: String, onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var balance by remember { mutableStateOf(1000.0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home, "Home") },
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
                    icon = { Icon(if (selectedTab == 1) Icons.Filled.List else Icons.Outlined.List, "Bidding") },
                    label = { Text("Bidding") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
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
            0 -> HomeScreen(username, balance, Modifier.padding(paddingValues))
            1 -> BiddingScreen(username, balance, onBalanceChange = { balance = it }, Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun HomeScreen(username: String, balance: Double, modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("All") }

    // Get unique sports from games
    val sports = remember {
        listOf("All") + BiddingDatabase.games.map { it.sport }.distinct().sorted()
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with User Profile
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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search teams, sports, or status...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Main Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Balance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
                    StatCard("Active Bids", "0", Modifier.weight(1f))
                    StatCard("Total Won", "$0.00", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sports Filter Tabs
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

                Spacer(modifier = Modifier.height(16.dp))

                // Recent & Upcoming Games
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isBlank() && selectedSport == "All") "Recent & Upcoming Games" else "Search Results",
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

                if (filteredGames.isEmpty()) {
                    // No results message
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
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredGames) { game ->
                            GameCard(game)
                        }
                    }
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
fun GameCard(game: Game) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(BiddingDatabase.events) { event ->
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
fun EventCard(event: BiddingEvent, onBidClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(16.dp))

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
fun TeamBidButton(team: String, odds: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
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
    UniBiddingTheme {
        MainScreen(username = "BigBalla67", onLogout = {})
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DarkThemePreview() {
    UniBiddingTheme(darkTheme = true) {
        MainScreen(username = "BigBalla67", onLogout = {})
    }
}