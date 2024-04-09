package com.koniukhov.tictactoe

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.koniukhov.tictactoe.data.PlayerPreferencesManager
import com.koniukhov.tictactoe.data.PlayerPreferencesManager.Companion.datastore
import com.koniukhov.tictactoe.util.Constants.ID_IS_MISSING
import com.koniukhov.tictactoe.viewmodels.PreferencesViewModel
import com.koniukhov.tictactoe.viewmodels.WebSocketViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val preferencesViewModel: PreferencesViewModel by viewModels {
        PreferencesViewModel.Factory(PlayerPreferencesManager(datastore))
    }
    private val webSocketViewModel: WebSocketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webSocketViewModel.connect()

        installSplashScreen().apply {
            setKeepOnScreenCondition { true }
            setContentView(R.layout.activity_main)
            val navController = initNavController()

            lifecycleScope.launch {
                preferencesViewModel.preferencesFlow.collect{
                    if (it.id == ID_IS_MISSING) {
                        navController.navigate(R.id.action_homeFragment_to_registrationFragment)
                    } else {
                        navController.navigate(R.id.homeFragment)
                    }
                    setKeepOnScreenCondition { false }
                }
            }
        }
    }

    private fun initNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    override fun onDestroy() {
        webSocketViewModel.disconnect()
        super.onDestroy()
    }
}