package com.example.apputbid.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Top-level DataStore instance tied to Context
val Context.dataStore by preferencesDataStore(name = "user_prefs")

object BalanceStore {
    private val BALANCE_KEY = doublePreferencesKey("user_balance")
    const val DEFAULT_BALANCE = 1000.0

    fun balanceFlow(context: Context): Flow<Double> =
        context.dataStore.data.map { prefs ->
            prefs[BALANCE_KEY] ?: DEFAULT_BALANCE
        }

    suspend fun setBalance(context: Context, value: Double) {
        context.dataStore.edit { prefs ->
            prefs[BALANCE_KEY] = value
        }
    }
}
