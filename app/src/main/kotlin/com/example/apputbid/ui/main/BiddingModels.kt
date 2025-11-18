package com.example.apputbid.ui.main

import com.example.apputbid.data.AuthRepository

// ===== Models =====

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
    val status: String,   // "upcoming" | "completed" (or "final")
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

/** Active/open bet placed by a user (resolved when the game completes). */
data class Bet(
    val id: Long,
    val username: String,
    val gameId: Int,      // align with Game.id (was Long before)
    val pick: String,     // "home" or "away"
    val stake: Double,    // amount wagered
    val odds: Double      // decimal odds (e.g., 1.8)
)


// ===== In-memory DB used by UI/Admin =====

object BiddingDatabase {

    // --- constants ---
    private const val DEFAULT_START_BALANCE = 20.0
    private const val ESCROW_STAKE = false // set true if you debit stake at placeBet

    // ---- Users & moderation ----
    private val registeredUsers = linkedSetOf<String>()     // filled via ensureUser/seedUsers
    private val bannedUsers = mutableSetOf<String>()

    // ---- Balances ----
    private val balances = mutableMapOf<String, Double>()   // username -> balance

    // ---- Bets (active/open) ----
    private val activeBets = mutableListOf<Bet>()
    private var betSeq = 1L

    // ---- Seeds ----
    private val _events = mutableListOf(
        BiddingEvent(1, "Men's Soccer", "Blue Ballers", "Kinfolk", 1.8, 2.1, "Sports"),
        BiddingEvent(2, "Men's Soccer", "Calmation", "DDD FC", 1.5, 2.5, "Sports"),
        BiddingEvent(3, "Women's Soccer", "Oval Gladiators", "Heavy Flow", 1.9, 1.9, "Sports"),
        BiddingEvent(4, "Women's Soccer", "Ball Handlers", "The SockHers", 2.0, 1.7, "Sports"),
    )
    val events: List<BiddingEvent> get() = _events

    private val _teams = listOf(
        Team("Blue Ballers", 12, 3, "Men's Soccer"),
        Team("Kinfolk", 10, 5, "Men's Soccer"),
        Team("Calmation", 7, 8, "Men's Soccer"),
        Team("DDD FC", 9, 6, "Men's Soccer"),
        Team("Oval Gladiators", 8, 7, "Women's Soccer"),
        Team("Heavy Flow", 11, 4, "Women's Soccer"),
        Team("Ball Handlers", 13, 2, "Women's Soccer"),
        Team("The SockHers", 6, 9, "Women's Soccer")
    )
    val teams: List<Team> get() = _teams

    private val _games = mutableListOf(
        Game(1, "Blue Ballers", "Kinfolk", 4, 1, "Today, 3:00 PM", "upcoming", "Men's Soccer"),
        Game(2, "Oval Gladiators", "Heavy Flow", 2, 3, "Today, 6:30 PM", "upcoming", "Women's Soccer"),
        Game(3, "Calmation", "DDD FC", null, null, "Tomorrow, 4:00 PM", "upcoming", "Men's Soccer"),
        Game(4, "Ball Handlers", "The SockHers", 3, 2, "Yesterday", "upcoming", "Women's Soccer"),
    )
    val games: List<Game> get() = _games


    fun ensureUser(username: String) {
        if (username.isBlank()) return
        registeredUsers += username
        // initialize wallet only once
        balances.putIfAbsent(username, DEFAULT_START_BALANCE)
    }

    /** Seed multiple users at once (useful if you ever preload from DB). */
    fun seedUsers(usernames: List<String>) {
        usernames.forEach { ensureUser(it) }
    }

    fun getAllUsers(): List<String> =
    (registeredUsers + balances.keys + activeBets.map { it.username })
    .filter { it.isNotBlank() && it != "admin" }
    .distinct()
    .sorted()

    fun isBanned(username: String): Boolean = username in bannedUsers

    fun banUser(username: String) {
        bannedUsers += username
    }


    fun unbanUser(username: String) {
        bannedUsers -= username
    }

    fun addFunds(username: String, amount: Double) {
        if (amount <= 0.0) return
        ensureUser(username)
        balances[username] = getBalance(username) + amount
    }

    fun getBalance(username: String): Double = balances[username] ?: 0.0


    private fun credit(username: String, amount: Double) = addFunds(username, amount)


    private fun debit(username: String, amount: Double) {
        if (amount <= 0.0) return
        ensureUser(username)
        balances[username] = getBalance(username) - amount
    }


    /** Place (or stage) an active bet; call from your normal user flow. */
    fun placeBet(username: String, gameId: Int, pick: String, stake: Double, odds: Double) {
        if (username.isBlank() || stake <= 0.0) return
        if (isBanned(username)) return
        ensureUser(username)

        activeBets += Bet(
            id = betSeq++,
            username = username,
            gameId = gameId,
            pick = pick.lowercase(), // "home" / "away"
            stake = stake,
            odds = odds
        )

        if (ESCROW_STAKE) debit(username, stake) // hold stake up front if desired
    }

    fun withdraw(username: String, amount: Double) {
        if (amount <= 0.0) return
        ensureUser(username)
        val current = getBalance(username)
        if (current >= amount) {
            // simple subtract, no overdraft
            val balancesField = javaClass.getDeclaredField("balances")
            balancesField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val balances = balancesField.get(this) as MutableMap<String, Double>
            balances[username] = current - amount
        }
    }


    fun getActiveBetsForGame(gameId: Int): List<Bet> =
        activeBets.filter { it.gameId == gameId }

    private fun removeActiveBet(id: Long) {
        activeBets.removeAll { it.id == id }
    }

    // ---- Admin: add game + matching event ----
    fun addGameAndEvent(
        sport: String,
        homeTeam: String,
        awayTeam: String,
        date: String,
        homeOdds: Double,
        awayOdds: Double
    ) {
        val nextId = ((_games.maxOfOrNull { it.id } ?: 0) + 1)
        _games += Game(
            id = nextId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeScore = null,
            awayScore = null,
            date = date,
            status = "upcoming",
            sport = sport
        )
        _events += BiddingEvent(
            id = nextId,
            title = sport,
            team1 = homeTeam,
            team2 = awayTeam,
            odds1 = homeOdds,
            odds2 = awayOdds,
            category = "Sports"
        )
    }

    // ---- Set result + settle bets (called by SetGameResultScreen) ----
    fun updateGameResult(gameId: Int, homeScore: Int, awayScore: Int) {
        val idx = _games.indexOfFirst { it.id == gameId }
        if (idx < 0) return

        val g = _games[idx]
        val finalGame = g.copy(
            homeScore = homeScore,
            awayScore = awayScore,
            status = "completed"
        )
        _games[idx] = finalGame
        resolveBetsForGame(finalGame)
    }

    private fun resolveBetsForGame(game: Game) {
        val h = game.homeScore ?: 0
        val a = game.awayScore ?: 0
        val homeWon = h > a
        val awayWon = a > h
        val isTie = !homeWon && !awayWon

        val toResolve = activeBets.filter { it.gameId == game.id }.toList()
        toResolve.forEach { bet ->
            when {
                isTie -> {
                    // push: if you didn't escrow, refund stake now
                    if (!ESCROW_STAKE) credit(bet.username, bet.stake)
                    // if escrowed, just return stake by adding bet.stake
                    if (ESCROW_STAKE) credit(bet.username, bet.stake)
                    removeActiveBet(bet.id)
                }
                homeWon && bet.pick == "home" || awayWon && bet.pick == "away" -> {
                    // payout:
                    // If you DID NOT escrow the stake, credit only PROFIT = stake*(odds-1)
                    // If you DID escrow, credit full RETURN = stake*odds
                    val payout = if (ESCROW_STAKE) bet.stake * bet.odds else bet.stake * (bet.odds - 1.0)
                    credit(bet.username, payout)
                    removeActiveBet(bet.id)
                }
                else -> {
                    // losing side: if escrowed, nothing (stake already deducted); if not escrowed, no debit.
                    removeActiveBet(bet.id)
                }
            }
        }
    }

    fun seedBanned(usernames: List<String>) {
        bannedUsers.clear()
        bannedUsers.addAll(usernames)
    }

    fun applyResultOverrides(overrides: List<AuthRepository.GameResultOverride>) {
        overrides.forEach { o ->
            val idx = _games.indexOfFirst { it.id == o.gameId }
            if (idx >= 0) {
                val g = _games[idx]
                _games[idx] = g.copy(
                    homeScore = o.homeScore,
                    awayScore = o.awayScore,
                    status = o.status
                )
            }
        }

        data class BetPayout(
            val username: String,
            val amount: Double
        )

    }

}
