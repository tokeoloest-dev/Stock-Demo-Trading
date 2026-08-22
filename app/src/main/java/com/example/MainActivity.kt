package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.model.AppScreen
import com.example.data.repository.TradingRepository
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainTradingScreen
import com.example.ui.theme.StockTradingTheme
import com.example.ui.viewmodel.TradingViewModel
import com.example.ui.viewmodel.TradingViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: TradingViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TradingRepository(database)
        TradingViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val loadingProgress by viewModel.loadingProgress.collectAsState()
            val loadingStatusText by viewModel.loadingStatusText.collectAsState()

            StockTradingTheme(
                appTheme = appTheme,
                darkTheme = isDarkMode
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        AppScreen.LOADING -> {
                            LoadingScreen(
                                progress = loadingProgress,
                                statusText = loadingStatusText
                            )
                        }
                        AppScreen.LOGIN -> {
                            LoginScreen(
                                onGoogleSignIn = { viewModel.login("Google User (Trader)", "Google") },
                                onFacebookSignIn = { viewModel.login("Facebook User (Trader)", "Facebook") },
                                onGuestSignIn = { viewModel.login("Demo Trader Pro", "Guest Demo") }
                            )
                        }
                        else -> {
                            MainTradingScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
