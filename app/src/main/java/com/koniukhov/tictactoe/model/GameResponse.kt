package com.koniukhov.tictactoe.model

class GameResponse {
    enum class Status{
        IN_PROGRESS, FINISHED
    }

    var status: Status
        private set
    var cellIndex: Int = -1
        private set
    var symbol: Char = Char.MIN_VALUE
        private set
    var currentPlayer: String = ""
        private set
    var canMove: Boolean = false
        private set
    var winnerMsg: String = ""
        private set

    constructor(status: Status, cellIndex: Int, symbol: Char, currentPlayer: String, canMove: Boolean){
        this.status = status
        this.cellIndex = cellIndex
        this.symbol = symbol
        this.canMove = canMove
        this.currentPlayer = currentPlayer
    }

    constructor(status: Status, cellIndex: Int, symbol: Char, winnerMsg: String){
        this.status = status
        this.cellIndex = cellIndex
        this.symbol = symbol
        this.winnerMsg = winnerMsg
    }
}