package com.koniukhov.tictactoe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.koniukhov.tictactoe.R
import com.koniukhov.tictactoe.data.PlayerPreferencesManager
import com.koniukhov.tictactoe.data.PlayerPreferencesManager.Companion.datastore
import com.koniukhov.tictactoe.databinding.FragmentHomeBinding
import com.koniukhov.tictactoe.model.LobbyResponse
import com.koniukhov.tictactoe.model.StatisticsResponse
import com.koniukhov.tictactoe.util.Constants.ID_IS_MISSING
import com.koniukhov.tictactoe.viewmodels.GameViewModel
import com.koniukhov.tictactoe.viewmodels.PreferencesViewModel
import com.koniukhov.tictactoe.viewmodels.WebSocketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val preferencesViewModel: PreferencesViewModel by viewModels {
        PreferencesViewModel.Factory(PlayerPreferencesManager(requireContext().datastore))
    }
    private val webSocketViewModel: WebSocketViewModel by activityViewModels()
    private val gameViewModel: GameViewModel by activityViewModels()
    private val id: MutableLiveData<Int> = MutableLiveData(ID_IS_MISSING)
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectPreferences()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        observeId()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStatisticsResponse()
        observeLobbyResponse()
        handlePlayBtn()
    }

    private fun observeStatisticsResponse() {
        webSocketViewModel.statisticsResponse.observe(viewLifecycleOwner) {
            setTextViews(it)
        }
    }

    private fun setTextViews(it: StatisticsResponse) {
        binding.numOfGames.text =
            String.format(getString(R.string.num_of_games), it.numberOfGames.toString())
        binding.numOfWins.text =
            String.format(getString(R.string.num_of_wins), it.numberOfWins.toString())
        binding.name.text = it.name
    }

    private fun observeLobbyResponse(){
        snackbar = Snackbar.make(requireActivity(), binding.content, getString(R.string.searching_lobby), Snackbar.LENGTH_INDEFINITE)
        webSocketViewModel.lobbyResponse.observe(viewLifecycleOwner){
            it?.let {
                when(it.status){
                    LobbyResponse.Status.WAITING -> {
                        snackbar.show()
                    }
                    LobbyResponse.Status.FOUND -> {
                        snackbar.dismiss()
                        setPreGameInfo(it)
                        findNavController().navigate(R.id.gameFragment)
                    }
                }
            }
        }
    }

    private fun setPreGameInfo(it: LobbyResponse) {
        gameViewModel.setPlayerName(it.playerName)
        gameViewModel.setEnemyName(it.enemyName)
        gameViewModel.setCurrentPlayer(it.preGameResponse.currentPlayer)
        gameViewModel.setCanMove(it.preGameResponse.canMove)
    }

    private fun collectPreferences(){
        lifecycleScope.launch(Dispatchers.IO) {
            preferencesViewModel.preferencesFlow.collect {
                id.postValue(it.id)
            }
        }
    }

    private fun observeId(){
        id.observe(viewLifecycleOwner){
            if (it != ID_IS_MISSING){
                subscribeToWebSocket(it)
            }
        }
    }

    private fun subscribeToWebSocket(id: Int) {
        webSocketViewModel.subscribeToLobby(id)
        webSocketViewModel.subscribeToStatistics(id)
        webSocketViewModel.sendMsgToStatistics(id)
    }

    private fun handlePlayBtn(){
        binding.playBtn.setOnClickListener{
            id.value?.let { _id -> webSocketViewModel.sendMsgToLobby(_id) }
        }
    }

    override fun onDestroyView() {
        if (id.value != ID_IS_MISSING){
            webSocketViewModel.unsubscribeLobby()
            webSocketViewModel.unsubscribeStatistics()
        }
        webSocketViewModel.clearLobbyResponse()
        super.onDestroyView()
    }
}