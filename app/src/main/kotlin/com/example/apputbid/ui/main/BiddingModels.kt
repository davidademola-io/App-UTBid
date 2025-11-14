package com.example.apputbid.ui.main

// One place for all the bidding-related models

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
        BiddingEvent(2, "Men's Soccer", "Calmation", "DDD FC", 1.5, 2.5, "Sports"),
        BiddingEvent(3, "Women's Soccer", "Oval Gladiators", "Heavy Flow", 1.9, 1.9, "Sports"),
        BiddingEvent(4, "Women's Soccer", "Ball Handlers", "The SockHers", 2.0, 1.7, "Sports"),
    )

    val teams = listOf(
        Team("Blue Ballers", 12, 3, "Men's Soccer"),
        Team("Kinfolk", 10, 5, "Men's Soccer"),
        Team("Calmation", 7, 8, "Men's Soccer"),
        Team("DDD FC", 9, 6, "Men's Soccer"),
        Team("Oval Gladiators", 8, 7, "Women's Soccer"),
        Team("Heavy Flow", 11, 4, "Women's Soccer"),
        Team("Ball Handlers", 13, 2, "Women's Soccer"),
        Team("The SockHers", 6, 9, "Women's Soccer")
    )

    val games = listOf(
        Game(1, "Blue Ballers", "Kinfolk", 4, 1, "Today, 3:00 PM", "completed", "Men's Soccer"),
        Game(2, "Oval Gladiators", "Heavy Flow", 2, 3, "Today, 6:30 PM", "completed", "Women's Soccer"),
        Game(3, "Calmation", "DDD FC", null, null, "Tomorrow, 4:00 PM", "upcoming", "Men's Soccer"),
        Game(4, "Ball Handlers", "The SockHers", 3, 2, "Yesterday", "completed", "Women's Soccer"),
    )

    // ------------------------------
    //  Bids + Balances per user
    // ------------------------------

    private val userBids = mutableMapOf<String, MutableList<Bid>>()

    private const val DEFAULT_BALANCE = 1000.0
    private val userBalances = mutableMapOf<String, Double>()

    fun placeBid(username: String, bid: Bid) {
        val list = userBids.getOrPut(username) { mutableListOf() }
        list.add(bid)
    }

    fun getUserBalance(username: String): Double {
        // If user has no stored balance yet, we treat it as default
        return userBalances[username] ?: DEFAULT_BALANCE
    }

    fun setUserBalance(username: String, newBalance: Double) {
        userBalances[username] = newBalance
    }

}
