package com.example.unum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.BorderStrong
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        "생년월일 속 숫자 흐름을 조용히 열어 오늘의 선택을 도와드려요.",
        "무료 결과에서는 핵심 성향과 조심해야 할 흐름을 먼저 보여드려요.",
        "더 깊은 고민은 수리의 운세노트로 상황별 조언까지 확인할 수 있어요."
    )

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "수리의\n운세노트",
                        color = TextPrimary,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = pages[page],
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
                InsightArtwork()
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    pages.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(width = if (index == page) 18.dp else 6.dp, height = 6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (index == page) Accent else Surface2)
                        )
                    }
                }
                GradientButton(
                    text = if (page == pages.lastIndex) "시작하기" else "다음",
                    onClick = {
                        if (page == pages.lastIndex) onFinished() else page += 1
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun InsightArtwork() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 172.dp, height = 222.dp)
                .offset(x = 9.dp, y = 9.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFECE0D0))
                .border(1.dp, Color(0xFFDCCBB7), RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .size(width = 154.dp, height = 210.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Surface)
                .border(1.dp, BorderStrong, RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(width = 8.dp, height = 172.dp)
                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .background(Accent.copy(alpha = 0.86f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 18.dp)
                    .size(width = 18.dp, height = 64.dp)
                    .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                    .background(Accent.copy(alpha = 0.86f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, top = 22.dp, end = 22.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        "수리의\n운세노트",
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("생년월일 숫자 흐름", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(1.dp)
                            .background(Border)
                    )
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFFAEF))
                            .border(1.dp, Accent.copy(alpha = 0.38f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("7", color = Accent, style = MaterialTheme.typography.displayMedium)
                    }
                    Text("오늘의 선택을 도와드려요", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.End
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(width = (16 + index * 4).dp, height = 2.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Border.copy(alpha = 0.72f))
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-45).dp, y = (-10).dp)
                .size(width = 26.dp, height = 78.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Accent.copy(alpha = 0.86f))
                .border(1.dp, Accent.copy(alpha = 0.22f), RoundedCornerShape(7.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 24.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(Surface)
                .border(1.dp, Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("수", color = Accent, style = MaterialTheme.typography.labelLarge)
        }
    }
}
