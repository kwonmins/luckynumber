package com.example.unum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
                var showOnboarding by rememberSaveable { mutableStateOf(true) }

                Box(Modifier.fillMaxSize()) {
                    if (showOnboarding) {
                        OnboardingScreen(onFinished = { showOnboarding = false })
                    } else {
                        UnumAppNavigation(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
