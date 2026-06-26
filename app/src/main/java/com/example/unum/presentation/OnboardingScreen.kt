package com.example.unum.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Background
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    isSigningIn: Boolean,
    errorMessage: String?,
    onStartAsGuest: () -> Unit,
    onStartWithKakao: () -> Unit
) {
    MysticBackground(modifier = Modifier.fillMaxSize(), animatedWaves = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(236.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF3B82F6), Accent, Color(0xFF1E3A8A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(alpha = 0.10f), radius = size.minDimension * 0.20f, center = Offset(size.width * 0.18f, size.height * 0.26f))
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = size.minDimension * 0.28f, center = Offset(size.width * 0.86f, size.height * 0.18f))
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = size.minDimension * 0.34f, center = Offset(size.width * 0.74f, size.height * 0.82f))
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("7", color = Color.White, style = MaterialTheme.typography.displayMedium)
                    }
                    Text(
                        text = "운세노트",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "생년월일로 오늘의 핵심수와\n나의 성향 흐름을 먼저 확인해보세요.",
                        color = Color.White.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AppExplanationPanel()
                WaveArtwork(modifier = Modifier.fillMaxWidth())
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "저장, 공유, 프리미엄 리포트가 필요할 때 로그인할 수 있어요.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = Accent,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    GradientButton(
                        text = "생년월일 입력하고 체험하기",
                        onClick = onStartAsGuest,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SecondaryButton(
                        text = if (isSigningIn) "카카오 로그인 중..." else "카카오로 로그인하고 시작하기",
                        onClick = onStartWithKakao,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSigningIn
                    )
                }
            }
        }
    }
}

@Composable
private fun AppExplanationPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface2.copy(alpha = 0.78f))
            .border(1.dp, Border.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("무엇을 해주나요?", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        OnboardingDescriptionLine("생년월일로 성향, 선택 리듬, 시기별 흐름을 정리해요.")
        OnboardingDescriptionLine("오늘의 연애, 일, 돈, 배움, 자기관리 운세를 짧게 확인해요.")
        OnboardingDescriptionLine("숫자는 높고 낮음이 아니라 각자 다른 역할과 기질로 읽어요.")
    }
}

@Composable
private fun OnboardingDescriptionLine(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Accent.copy(alpha = 0.84f))
        )
        Text(text, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WaveArtwork(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .height(220.dp)
            .padding(horizontal = 8.dp)
    ) {
        fun drawWave(offsetY: Float, color: androidx.compose.ui.graphics.Color, alpha: Float, width: Float) {
            val path = Path().apply {
                moveTo(0f, center.y + offsetY)
                cubicTo(size.width * 0.20f, center.y - 80f + offsetY, size.width * 0.30f, center.y + 80f + offsetY, size.width * 0.50f, center.y + offsetY)
                cubicTo(size.width * 0.70f, center.y - 80f + offsetY, size.width * 0.80f, center.y + 80f + offsetY, size.width, center.y + offsetY)
            }
            drawPath(path, color.copy(alpha = alpha), style = Stroke(width = width, cap = StrokeCap.Round))
        }
        drawCircle(Accent.copy(alpha = 0.07f), radius = size.minDimension * 0.44f, center = Offset(size.width * 0.82f, size.height * 0.68f))
        drawCircle(Accent.copy(alpha = 0.05f), radius = size.minDimension * 0.38f, center = Offset(size.width * 0.16f, size.height * 0.72f))
        drawWave(0f, Accent, 0.92f, 4.dp.toPx())
        drawWave(24.dp.toPx(), androidx.compose.ui.graphics.Color(0xFFA855F7), 0.45f, 2.dp.toPx())
        drawWave((-22).dp.toPx(), androidx.compose.ui.graphics.Color(0xFF818CF8), 0.42f, 3.dp.toPx())
    }
}
