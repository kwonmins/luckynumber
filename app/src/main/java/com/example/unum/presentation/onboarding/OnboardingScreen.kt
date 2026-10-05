package com.example.unum.presentation.onboarding

import com.example.unum.presentation.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.DeepNavy
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Surface
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
    MysticBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(38.dp).background(Accent, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("數", color = Surface, style = MaterialTheme.typography.titleMedium)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("수리운세", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("UNUM ALMANAC", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Text("오늘의 나를 읽는 법", color = Accent, style = MaterialTheme.typography.bodySmall)
                }
            }
            item { OnboardingHero() }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OnboardingStat("01", "핵심 수리", Modifier.weight(1f))
                    OnboardingStat("05", "오늘의 흐름", Modifier.weight(1f))
                    OnboardingStat("∞", "나의 기록", Modifier.weight(1f))
                }
            }
            item { AppExplanationPanel() }
            if (!errorMessage.isNullOrBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = Accent,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    GradientButton(
                        text = "로그인 없이 나의 수리 읽기  →",
                        onClick = onStartAsGuest,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SecondaryButton(
                        text = if (isSigningIn) "카카오 로그인 중..." else "카카오로 기록 이어가기",
                        onClick = onStartWithKakao,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSigningIn
                    )
                    Text(
                        "먼저 체험하고, 저장이 필요할 때 로그인해도 괜찮아요.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(DeepNavy)
            .padding(24.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.035f),
                radius = size.width * 0.56f,
                center = Offset(size.width * 0.98f, size.height * 0.10f)
            )
            repeat(3) { index ->
                drawCircle(
                    color = Gold.copy(alpha = 0.10f + index * 0.045f),
                    radius = size.width * (0.18f + index * 0.08f),
                    center = Offset(size.width * 0.84f, size.height * 0.18f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("BORN WITH A NUMBER", color = Gold, style = MaterialTheme.typography.labelMedium)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "오늘의 나를\n숫자로 읽다",
                    color = Surface,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp, lineHeight = 50.sp)
                )
                Text(
                    "생년월일에 담긴 나의 기질부터\n오늘 필요한 방향까지 차분하게.",
                    color = Surface.copy(alpha = 0.67f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 34.dp, end = 2.dp)
                .size(112.dp)
                .background(Accent, CircleShape)
                .border(8.dp, Color.White.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("7", color = Surface, fontSize = 68.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun OnboardingStat(number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(20.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(number, color = Accent, style = MaterialTheme.typography.titleLarge)
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AppExplanationPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Surface2)
            .padding(19.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("수리운세에서 만나는 것", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        OnboardingDescriptionLine("생년월일로 읽는 핵심 숫자와 기본 성향")
        OnboardingDescriptionLine("연애, 일, 돈, 배움, 마음을 담은 오늘의 흐름")
        OnboardingDescriptionLine("기억하고 싶은 해석을 한 권의 운세노트로 보관")
    }
}

@Composable
private fun OnboardingDescriptionLine(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(Accent)
        )
        Text(text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}
