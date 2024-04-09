package com.koniukhov.tictactoe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.koniukhov.tictactoe.data.PlayerPreferencesManager.Keys.PLAYER_ID
import com.koniukhov.tictactoe.model.PlayerPreferences
import com.koniukhov.tictactoe.util.Constants.ID_IS_MISSING
import com.koniukhov.tictactoe.util.Constants.PREFERENCES_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerPreferencesManager(private val dataStore: DataStore<Preferences>) {

    val preferencesFlow: Flow<PlayerPreferences> = dataStore.data.map { preferences ->
        val id = preferences[PLAYER_ID] ?: ID_IS_MISSING

        PlayerPreferences(id)
    }

    suspend fun updatePlayerPreferences(id: Int){
        dataStore.edit { preferences ->
            preferences[PLAYER_ID] = id
        }
    }

    companion object {
        val Context.datastore by preferencesDataStore(PREFERENCES_NAME)
    }

    private object Keys{
        val PLAYER_ID = intPreferencesKey("player_id")
    }
}