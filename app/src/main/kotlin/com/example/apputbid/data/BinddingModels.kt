package com.example.apputbid.data

// Simple in-memory user "database"
object UserDatabase {
    private val users = mutableMapOf<String, String>()

    fun register(username: String, password: String): Boolean {
        return if (!users.containsKey(username)) {
            users[username] = password
            true
        } else {
            false
        }
    }

    fun login(username: String, password: String): Boolean {
        return users[username] == password
    }
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
        BiddingEvent(1, "Soccer", "Blue Ballers", "Kinfolk", 1.8, 2.1, "Sports"),
        BiddingEvent(2, "Hackathon Winner", "Team Alpha", "Team Beta", 1.5, 2.5, "Academic"),
        BiddingEvent(3, "Debate Competition", "Law Society", "Business Club", 1.9, 1.9, "Academic"),
        BiddingEvent(4, "Football Finals", "Wildcats", "Panthers", 2.0, 1.7, "Sports"),
        BiddingEvent(5, "Chess Tournament", "Knights Club", "Rooks Society", 2.2, 1.6, "Games"),
    )

    val teams = listOf(
        Team("Tigers", 12, 3, "Basketball"),
        Team("Eagles", 10, 5, "Basketball"),
        Team("Wildcats", 8, 7, "Football"),
        Team("Panthers", 11, 4, "Football"),
        Team("Blue Ballers", 9, 6, "Soccer"),
        Team("Kinfolk", 7, 8, "Soccer"),
        Team("Sharks", 13, 2, "Hockey"),
        Team("Bears", 6, 9, "Hockey")
    )

    val games = listOf(
        Game(1, "Blue Ballers", "Kinfolk", 4, 1, "Today, 3:00 PM", "completed", "Soccer"),
        Game(2, "Wildcats", "Panthers", 24, 21, "Today, 6:30 PM", "completed", "Football"),
        Game(3, "Dragons", "Phoenix", null, null, "Tomorrow, 4:00 PM", "upcoming", "Soccer"),
        Game(4, "Sharks", "Bears", 3, 2, "Yesterday", "completed", "Hockey"),
        Game(5, "Eagles", "Tigers", null, null, "Nov 15, 7:00 PM", "upcoming", "Basketball"),
        Game(6, "Panthers", "Wildcats", null, null, "Nov 16, 5:30 PM", "upcoming", "Football")
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
