package com.example.unum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.DestinyProfile
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.Surface3
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun DestinyCard(number: Int, profile: DestinyProfile, code: String, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 20,
        tonalColor = Surface
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Accent.copy(alpha = 0.10f))
                            .border(1.dp, Accent.copy(alpha = 0.18f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(number.toString(), color = Accent, style = MaterialTheme.typography.displayLarge)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("내 숫자 리포트", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text("${profile.title}형", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("code $code", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            KeywordPills(profile.coreKeywords)
            Text(profile.destinyText, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            SummaryBanner(
                summaryText = profile.oneLineAdvice,
                oneLineAdvice = "더 구체적인 고민은 수리의 운세노트에서 이어서 정리할 수 있어요."
            )
        }
    }
}

@Composable
fun LifeStageCards(early: Int, middle: Int, late: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        NumberPill("초년", early.toString(), Blue, Modifier.weight(1f))
        NumberPill("중년", middle.toString(), Accent, Modifier.weight(1f))
        NumberPill("말년", late.toString(), Mint, Modifier.weight(1f))
    }
}

@Composable
fun FlowGraphCard(early: Int, middle: Int, late: Int, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Surface,
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("흐름 스냅샷", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            StageProgressRow("초년", early, Blue)
            StageProgressRow("중년", middle, Accent)
            StageProgressRow("말년", late, Mint)
        }
    }
}

@Composable
private fun StageProgressRow(label: String, value: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Text(value.toString(), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface3, RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value.coerceIn(0, 9) + 1) / 10f)
                    .background(color, RoundedCornerShape(999.dp))
                    .padding(vertical = 5.dp)
            )
        }
    }
}

@Composable
fun InterpretationCard(
    title: String,
    badgeNumber: Int,
    accentColor: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Surface,
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.34f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(badgeNumber.toString(), color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                }
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
            Text(text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SummaryBanner(summaryText: String, oneLineAdvice: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Gold.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .border(1.dp, Gold.copy(alpha = 0.24f), RoundedCornerShape(8.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("핵심 요약", color = Gold, style = MaterialTheme.typography.labelLarge)
            Text(summaryText, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(oneLineAdvice, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PremiumUpgradeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Surface,
        borderColor = Accent.copy(alpha = 0.35f),
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PremiumBadge("운세노트")
            Text("지금 고민까지 담아 책자처럼 읽기", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text("연애, 일, 돈, 관계처럼 실제 고민을 넣으면 상황별 해석과 주의할 장면을 노트로 정리합니다.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            GradientButton("운세노트 열기", onClick, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun TodaySummaryCard(title: String, body: String, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Surface,
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
