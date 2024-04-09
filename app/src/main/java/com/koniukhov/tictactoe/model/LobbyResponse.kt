package com.koniukhov.tictactoe.model

class LobbyResponse{
    enum class Status {
        WAITING,
        FOUND
    }

    var status: Status
        private set
    var playerName: String = ""
        private set
    var enemyName: String = ""
        private set
    lateinit var preGameResponse: PreGameResponse
        private set

    constructor(status: Status){
        this.status = status
    }

    constructor(status: Status, playerName: String, enemyName: String, preGameResponse: PreGameResponse){
        this.status = status
        this.playerName = playerName
        this.enemyName = enemyName
        this.preGameResponse = preGameResponse
    }
}