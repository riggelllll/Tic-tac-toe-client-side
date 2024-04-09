package com.koniukhov.tictactoe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.koniukhov.tictactoe.R
import com.koniukhov.tictactoe.data.PlayerPreferencesManager
import com.koniukhov.tictactoe.data.PlayerPreferencesManager.Companion.datastore
import com.koniukhov.tictactoe.databinding.FragmentGameBinding
import com.koniukhov.tictactoe.model.GameResponse
import com.koniukhov.tictactoe.util.Constants.ID_IS_MISSING
import com.koniukhov.tictactoe.util.Constants.SYMBOL_X
import com.koniukhov.tictactoe.viewmodels.GameViewModel
import com.koniukhov.tictactoe.viewmodels.PreferencesViewModel
import com.koniukhov.tictactoe.viewmodels.WebSocketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameFragment : Fragment() {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val preferencesViewModel: PreferencesViewModel by viewModels {
        PreferencesViewModel.Factory(PlayerPreferencesManager(requireContext().datastore))
    }
    private val webSocketViewModel: WebSocketViewModel by activityViewModels()
    private val gameViewModel: GameViewModel by activityViewModels()
    private val id: MutableLiveData<Int> = MutableLiveData(ID_IS_MISSING)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectPreferences()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container,false)
        observeId()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextViews()
        setListenersForCells()
        observeGameResponse()
    }

    private fun setTextViews() {
        binding.names.text =
            getString(R.string.names_of_players, gameViewModel.playerName, gameViewModel.enemyName)
        binding.currentMove.text = getString(R.string.current_move, gameViewModel.currentPlayer)
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
                webSocketViewModel.subscribeToGame(it)
            }
        }
    }

    private fun observeGameResponse(){
        webSocketViewModel.gameResponse.observe(viewLifecycleOwner){
            it?.let {
                if (it.status == GameResponse.Status.IN_PROGRESS){
                    updateUI(it)
                    gameViewModel.updateValues(it)
                }else{
                    gameViewModel.updateValues(it)
                    updateUI(it)
                    showWinDialog(it.winnerMsg)
                }
            }
        }
    }

    private fun setListenersForCells(){
        val cellIndexes = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8")
        cellIndexes.forEach {
            binding.root.findViewWithTag<ImageView>(it).setOnClickListener{_ ->
                sendMsg(it.toInt())
            }
        }
    }

    private fun sendMsg(cellIndex: Int){
        if (gameViewModel.canMove){
            webSocketViewModel.sendMsgToGame(id.value!!, cellIndex)
        }
    }

    private fun updateUI(gameResponse: GameResponse){
        binding.currentMove.text = getString(R.string.current_move, gameResponse.currentPlayer)
        val cell = binding.root.findViewWithTag<ImageView>(gameResponse.cellIndex.toString())
        if (gameResponse.symbol == SYMBOL_X){
            cell.setImageResource(R.drawable.x_cell)
        }else{
            cell.setImageResource(R.drawable.o_cell)
        }
    }

    private fun showWinDialog(winnerMsg: String){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.dialog_title))
            .setMessage(winnerMsg)
            .setPositiveButton(resources.getString(R.string.close)) { _, _ ->
                view?.post{
                    findNavController().navigate(R.id.action_gameFragment_to_homeFragment)
                }
            }
            .setCancelable(false)
            .show()

    }

    override fun onDestroyView() {
        webSocketViewModel.clearGameResponse()
        webSocketViewModel.unsubscribeGame()
        super.onDestroyView()
    }
}