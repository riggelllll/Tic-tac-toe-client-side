package com.koniukhov.tictactoe.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.koniukhov.tictactoe.data.PlayerPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferencesViewModel(private val  preferencesManager: PlayerPreferencesManager) : ViewModel() {

    var preferencesFlow = preferencesManager.preferencesFlow
        private set

    fun updatePreferences(id: Int){
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.updatePlayerPreferences(id)
        }
    }

    class Factory(
        private val preferencesManager: PlayerPreferencesManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PreferencesViewModel(preferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}