package com.example.unum

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.example.unum.navigation.UnumAppNavigation
import com.example.unum.presentation.AppViewModel
import com.example.unum.presentation.OnboardingScreen
import com.example.unum.ui.theme.UnumTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnumTheme {
                var showIntro by rememberSaveable { mutableStateOf(true) }
                var showOnboarding by rememberSaveable { mutableStateOf(true) }

                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    AnimatedVisibility(
                        visible = !showIntro && !showOnboarding,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        UnumAppNavigation(viewModel = viewModel)
                    }
                    AnimatedVisibility(
                        visible = !showIntro && showOnboarding,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        OnboardingScreen(onFinished = { showOnboarding = false })
                    }
                    AnimatedVisibility(
                        visible = showIntro,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IntroVideoScreen(onFinished = { showIntro = false })
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroVideoScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(8_000)
        onFinished()
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onFinished() },
        factory = { context ->
            VideoView(context).apply {
                val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.intro_video}")
                setVideoURI(uri)
                setOnPreparedListener { player ->
                    player.isLooping = false
                    player.start()
                }
                setOnCompletionListener { onFinished() }
                setOnErrorListener { _, _, _ ->
                    onFinished()
                    true
                }
            }
        }
    )
}
