package com.koniukhov.tictactoe.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.koniukhov.tictactoe.model.BaseRequest
import com.koniukhov.tictactoe.model.GameRequest
import com.koniukhov.tictactoe.model.GameResponse
import com.koniukhov.tictactoe.model.LobbyResponse
import com.koniukhov.tictactoe.model.RegistrationRequest
import com.koniukhov.tictactoe.model.RegistrationResponse
import com.koniukhov.tictactoe.model.StatisticsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.WebSocketConnectionException
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

private const val TAG = "WebsocketViewModelTag"

private const val WEBSOCKET_URL = "ws://10.0.2.2:8080/websocket"
private const val REGISTRATION_DESTINATION = "/app/registration"
private const val LOBBY_DESTINATION = "/app/lobby"
private const val STATISTICS_DESTINATION = "/app/statistics"
private const val GAME_DESTINATION = "/app/game"

private const val SUBSCRIPTION_REGISTRATION_DESTINATION = "/player/%s/queue/registration"
private const val SUBSCRIPTION_LOBBY_DESTINATION = "/player/%s/queue/lobby"
private const val SUBSCRIPTION_STATISTICS_DESTINATION = "/player/%s/queue/statistics"
private const val SUBSCRIPTION_GAME_DESTINATION = "/player/%s/queue/game"

class WebSocketViewModel : ViewModel(){
    private val client = StompClient(OkHttpWebSocketClient())
    private lateinit var session: StompSession

    private lateinit var registrationSubscription: Flow<String>
    private lateinit var lobbySubscription: Flow<String>
    private lateinit var statisticsSubscription: Flow<String>
    private lateinit var gameSubscription: Flow<String>

    private lateinit var sessionJob: Job
    private lateinit var registrationJob: Job
    private lateinit var lobbyJob: Job
    private lateinit var statisticsJob: Job
    private lateinit var gameJob: Job

    private val gson: Gson = Gson()

    private val _registrationResponse = MutableLiveData<RegistrationResponse?>()
    val registrationResponse: LiveData<RegistrationResponse?> = _registrationResponse

    private val _statisticsResponse = MutableLiveData<StatisticsResponse>()
    val statisticsResponse: LiveData<StatisticsResponse> = _statisticsResponse

    private val _lobbyResponse = MutableLiveData<LobbyResponse?>()
    val lobbyResponse: LiveData<LobbyResponse?> = _lobbyResponse

    private val _gameResponse = MutableLiveData<GameResponse?>()
    val gameResponse: LiveData<GameResponse?> = _gameResponse

    fun connect(){
        viewModelScope.launch(Dispatchers.IO) {
            sessionJob = launch {
                try {
                    session = client.connect(WEBSOCKET_URL)
                }catch (e: WebSocketConnectionException){
                    Log.d(TAG, e.toString())
                }
            }
        }
    }

    fun disconnect(){
        viewModelScope.launch(Dispatchers.IO) {
            session.disconnect()
        }
    }

    fun subscribeToRegistration(name: String){
        viewModelScope.launch(Dispatchers.IO) {
            sessionJob.join()
            registrationSubscription = session.subscribeText(SUBSCRIPTION_REGISTRATION_DESTINATION.format(name))

            registrationJob = launch{
                registrationSubscription.collect{
                    val response = gson.fromJson(it, RegistrationResponse::class.java)
                    _registrationResponse.postValue(response)
                }
            }
        }
    }

    fun subscribeToLobby(id: Int){
        viewModelScope.launch(Dispatchers.IO) {
            sessionJob.join()
            lobbySubscription = session.subscribeText(SUBSCRIPTION_LOBBY_DESTINATION.format(id.toString()))

            lobbyJob = launch{
                lobbySubscription.collect{
                    val response = gson.fromJson(it, LobbyResponse::class.java)
                    _lobbyResponse.postValue(response)
                }
            }
        }
    }

    fun subscribeToStatistics(id: Int){
        viewModelScope.launch(Dispatchers.IO) {
            sessionJob.join()
            statisticsSubscription = session.subscribeText(SUBSCRIPTION_STATISTICS_DESTINATION.format(
                id.toString()))

            viewModelScope.launch(Dispatchers.IO) {
                statisticsJob = launch{
                    statisticsSubscription.collect{
                        val response = gson.fromJson(it, StatisticsResponse::class.java)
                        _statisticsResponse.postValue(response)
                    }
                }
            }
        }
    }

    fun subscribeToGame(id: Int){
        viewModelScope.launch(Dispatchers.IO) {
            sessionJob.join()
            gameSubscription = session.subscribeText(SUBSCRIPTION_GAME_DESTINATION.format(id.toString()))

            gameJob = launch{
                gameSubscription.collect{
                    val response = gson.fromJson(it, GameResponse::class.java)
                    _gameResponse.postValue(response)
                }
            }
        }
    }

    fun unsubscribeRegistration(){
        registrationJob.cancel()
    }

    fun unsubscribeLobby(){
        lobbyJob.cancel()
    }

    fun unsubscribeStatistics(){
        statisticsJob.cancel()
    }

    fun unsubscribeGame(){
        gameJob.cancel()
    }

    private fun sendMsgToDestination(msg: String, destination: String){
        viewModelScope.launch(Dispatchers.IO) {
            sessionJob.join()
            session.sendText(destination, msg)
        }
    }

    fun sendMsgToRegistration(androidId: String, name: String){
        viewModelScope.launch(Dispatchers.IO) {
            val request = RegistrationRequest(androidId, name)
            sendMsgToDestination(gson.toJson(request), REGISTRATION_DESTINATION)
        }
    }

    fun sendMsgToLobby(id: Int){
        viewModelScope.launch(Dispatchers.IO) {
            val request = BaseRequest(id)
            sendMsgToDestination(gson.toJson(request), LOBBY_DESTINATION)
        }
    }

    fun sendMsgToStatistics(id: Int){
        viewModelScope.launch(Dispatchers.IO) {
            val request = BaseRequest(id)
            sendMsgToDestination(gson.toJson(request), STATISTICS_DESTINATION)
        }
    }

    fun sendMsgToGame(id: Int, cellIndex: Int){
        viewModelScope.launch(Dispatchers.IO) {
            val request = GameRequest(id, cellIndex)
            sendMsgToDestination(gson.toJson(request), GAME_DESTINATION)
        }
    }

    fun clearRegistrationResponse(){
        _registrationResponse.value = null
    }

    fun clearLobbyResponse() {
        _lobbyResponse.value = null
    }

    fun clearGameResponse(){
        _gameResponse.value = null
    }
}