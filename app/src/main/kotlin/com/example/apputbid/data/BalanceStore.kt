package com.example.apputbid.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.balanceDataStore by preferencesDataStore("balances")

object BalanceStore {
    const val DEFAULT_BALANCE = 1000.0

    private fun keyFor(username: String) =
        doublePreferencesKey("balance_$username")

    fun balanceFlow(context: Context, username: String): Flow<Double> =
        context.balanceDataStore.data.map { prefs ->
            prefs[keyFor(username)] ?: DEFAULT_BALANCE
        }

    suspend fun setBalance(context: Context, username: String, value: Double) {
        context.balanceDataStore.edit { prefs ->
            prefs[keyFor(username)] = value
        }
    }

    suspend fun adjustBalance(context: Context, username: String, delta: Double) {
        context.balanceDataStore.edit { prefs ->
            val key = keyFor(username)
            val current = prefs[key] ?: DEFAULT_BALANCE
            prefs[key] = current + delta
        }
    }
}
