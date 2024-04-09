package com.koniukhov.tictactoe.model

import com.koniukhov.tictactoe.util.Constants.ID_IS_MISSING


class RegistrationResponse {
    enum class Status {
        SUCCESS,
        ALREADY_EXISTS
    }

    var status: Status
        private set
    var message: String
        private set
    var playerId = ID_IS_MISSING
        private set

    constructor(status: Status, message: String) {
        this.status = status
        this.message = message
    }

    constructor(status: Status, message: String, playerId: Int) {
        this.status = status
        this.message = message
        this.playerId = playerId
    }
}