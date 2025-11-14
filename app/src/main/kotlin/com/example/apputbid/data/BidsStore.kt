package com.example.apputbid.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.apputbid.ui.main.Bid
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object BidsStore {
    private val gson = Gson()

    private fun keyForUser(username: String) =
        stringPreferencesKey("bids_$username")

    fun bidsFlow(context: Context, username: String): Flow<List<Bid>> =
        context.dataStore.data.map { prefs ->
            val key = keyForUser(username)
            val json = prefs[key] ?: return@map emptyList<Bid>()

            val type = object : TypeToken<List<Bid>>() {}.type
            gson.fromJson<List<Bid>>(json, type) ?: emptyList()
        }

    suspend fun addBid(context: Context, username: String, bid: Bid) {
        context.dataStore.edit { prefs ->
            val key = keyForUser(username)
            val currentJson = prefs[key]
            val type = object : TypeToken<MutableList<Bid>>() {}.type

            val currentList: MutableList<Bid> =
                if (currentJson != null) {
                    gson.fromJson<MutableList<Bid>>(currentJson, type) ?: mutableListOf()
                } else {
                    mutableListOf()
                }

            currentList.add(bid)
            prefs[key] = gson.toJson(currentList)
        }
    }
}
