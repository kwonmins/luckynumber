package com.example.unum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.AuthState
import com.example.unum.data.model.UserSyncState
import com.example.unum.navigation.UnumAppNavigation
import com.example.unum.presentation.AppViewModel
import com.example.unum.presentation.OnboardingScreen
import com.example.unum.ui.theme.UnumTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnumTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var enteredAsGuest by rememberSaveable { mutableStateOf(false) }

                // Let new users experience the core report before asking for account login.
                LaunchedEffect(uiState.authState) {
                    if (uiState.authState is AuthState.SignedIn) {
                        enteredAsGuest = true
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    if (enteredAsGuest || uiState.authState is AuthState.SignedIn) {
                        UnumAppNavigation(viewModel = viewModel)
                    } else {
                        OnboardingScreen(
                            isSigningIn = uiState.userSyncState is UserSyncState.Syncing,
                            errorMessage = uiState.inputError,
                            onStartAsGuest = { enteredAsGuest = true },
                            onStartWithKakao = { viewModel.signInWithKakao(this@MainActivity) }
                        )
                    }
                }
            }
        }
    }
}
