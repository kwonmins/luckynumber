package com.example.unum

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.unum.presentation.discovery.TarotSpreadPicker
import com.example.unum.ui.theme.UnumTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TarotSpreadUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectingDoesNotDrawAndUpwardDragCommitsOnlyOnce() {
        var draws = 0
        compose.setContent {
            var enabled by remember { mutableStateOf(true) }
            UnumTheme {
                Box(Modifier.width(280.dp)) {
                    TarotSpreadPicker(enabled) { draws++; enabled = false }
                }
            }
        }
        compose.onNodeWithTag("tarot-confirm").assertIsNotEnabled()
        compose.onNodeWithTag("tarot-spread").performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(0, draws) }
        compose.onNodeWithTag("tarot-confirm").assertIsEnabled()
        compose.onNodeWithTag("tarot-spread").performTouchInput { swipeLeft() }
        compose.runOnIdle { assertEquals(0, draws) }
        compose.onNodeWithTag("tarot-spread").performTouchInput {
            swipe(Offset(center.x, height * .65f), Offset(center.x, height * .15f), 600)
        }
        compose.runOnIdle { assertEquals(1, draws) }
        compose.onNodeWithTag("tarot-confirm").assertIsNotEnabled()
        compose.onNodeWithTag("tarot-spread").performTouchInput { swipeUp() }
        compose.runOnIdle { assertEquals(1, draws) }
    }
}
