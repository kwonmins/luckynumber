package com.example.unum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun NotificationOnboardingScreen(
    initialEnabled: Boolean,
    onComplete: (Boolean) -> Unit
) {
    var enabled by rememberSaveable { mutableStateOf(initialEnabled) }
    var selectedTime by rememberSaveable { mutableStateOf("08:00") }

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(12.dp, RoundedCornerShape(28.dp), clip = false)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF60A5FA), Accent)),
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                }
                Text(
                    "매일 오늘의 수리를\n알려드릴게요",
                    color = TextPrimary,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    "하루를 시작하기 전, 오늘의 핵심 에너지를\n확인하고 준비해보세요",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
                    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Accent.copy(alpha = 0.10f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Accent, modifier = Modifier.size(19.dp))
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("오늘의 수리 알림", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                                    Text("매일 아침 핵심 에너지 전달", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            ToggleKnob(enabled = enabled, onClick = { enabled = !enabled })
                        }
                        if (enabled) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("알림 시간", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf("07:00", "08:00", "09:00").forEach { time ->
                                        TimeChip(
                                            text = time,
                                            selected = selectedTime == time,
                                            onClick = { selectedTime = time },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("알림 미리보기", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface2, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("운세노트 · 오전 $selectedTime", color = Accent, style = MaterialTheme.typography.labelMedium)
                                Text("오늘의 핵심수는 7이에요", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                                Text("내면 탐구의 에너지가 강한 하루예요", color = Accent, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton("알림 받기", onClick = { onComplete(true) }, modifier = Modifier.fillMaxWidth())
                SecondaryButton("나중에 설정할게요", onClick = { onComplete(false) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ToggleKnob(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 28.dp)
            .background(if (enabled) Accent else TextMuted.copy(alpha = 0.24f), CircleShape)
            .clickable(onClick = onClick)
            .padding(3.dp),
        contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(Modifier.size(22.dp).background(Color.White, CircleShape))
    }
}

@Composable
private fun TimeChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(if (selected) Accent else Surface, RoundedCornerShape(999.dp))
            .border(1.dp, if (selected) Accent else Border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else TextSecondary, style = MaterialTheme.typography.labelMedium)
    }
}
