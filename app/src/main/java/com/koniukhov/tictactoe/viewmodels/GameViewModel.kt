package com.koniukhov.tictactoe.viewmodels

import androidx.lifecycle.ViewModel
import com.koniukhov.tictactoe.model.GameResponse

class GameViewModel : ViewModel() {
    var playerName = String()
        private set
    var enemyName = String()
        private set
    var currentPlayer = String()
        private set
    var canMove: Boolean = false
        private set

    fun setPlayerName(playerName: String){
        this.playerName = playerName
    }

    fun setEnemyName(enemyName: String){
        this.enemyName = enemyName
    }

    fun setCurrentPlayer(currentPlayer: String){
        this.currentPlayer = currentPlayer
    }

    fun setCanMove(canMove: Boolean){
        this.canMove = canMove
    }

    fun updateValues(gameResponse: GameResponse){
        currentPlayer = gameResponse.currentPlayer
        canMove = gameResponse.canMove
    }
}