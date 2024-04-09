package com.koniukhov.tictactoe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.koniukhov.tictactoe.R
import com.koniukhov.tictactoe.data.PlayerPreferencesManager
import com.koniukhov.tictactoe.data.PlayerPreferencesManager.Companion.datastore
import com.koniukhov.tictactoe.databinding.FragmentRegistrationBinding
import com.koniukhov.tictactoe.model.RegistrationResponse
import com.koniukhov.tictactoe.util.Constants.ID_IS_MISSING
import com.koniukhov.tictactoe.util.Util
import com.koniukhov.tictactoe.viewmodels.PreferencesViewModel
import com.koniukhov.tictactoe.viewmodels.WebSocketViewModel

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val  binding get() = _binding!!

    private val webSocketViewModel: WebSocketViewModel by activityViewModels()
    private val preferencesViewModel: PreferencesViewModel by viewModels {
        PreferencesViewModel.Factory(PlayerPreferencesManager(requireContext().datastore))
    }
    private var androidId: String = ID_IS_MISSING.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidId = Util.getAndroidId(requireContext())
        webSocketViewModel.subscribeToRegistration(androidId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleSignUpBtn()
        observeRegistrationResponse()
    }

    private fun handleSignUpBtn(){
        binding.signUpBtn.setOnClickListener{
            it.isEnabled = false
            val name = binding.editText.text.toString()
            name.trim()
            if (name.isEmpty()){
                Toast.makeText(requireContext(), getString(R.string.name_mustnt_be_empty), Toast.LENGTH_SHORT).show()
            }else{
                webSocketViewModel.sendMsgToRegistration(androidId,name)
            }
        }
    }

    private fun observeRegistrationResponse(){
        webSocketViewModel.registrationResponse.observe(viewLifecycleOwner){response ->
            response?.let {
                when(response.status){
                    RegistrationResponse.Status.SUCCESS -> {
                        preferencesViewModel.updatePreferences(response.playerId)
                        findNavController().navigate(R.id.action_registrationFragment_to_homeFragment)
                    }

                    RegistrationResponse.Status.ALREADY_EXISTS -> {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                        binding.signUpBtn.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        webSocketViewModel.clearRegistrationResponse()
        webSocketViewModel.unsubscribeRegistration()
        super.onDestroyView()
    }
}