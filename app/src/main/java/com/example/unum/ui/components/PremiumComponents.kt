package com.example.unum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun MiniNumerologySummary(destiny: Int, early: Int, middle: Int, late: Int, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 16) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("현재 운명의 숫자", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Accent.copy(alpha = 0.12f))
                        .border(1.dp, Accent.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text("운명수 $destiny", color = Accent, style = MaterialTheme.typography.titleMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStagePill("초", early)
                    MiniStagePill("중", middle)
                    MiniStagePill("말", late)
                }
            }
        }
    }
}

@Composable
private fun MiniStagePill(prefix: String, number: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Surface2)
            .border(1.dp, Border, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text("$prefix $number", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicChipGroup(selected: PremiumTopic, onSelected: (PremiumTopic) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PremiumTopic.entries.forEach { topic ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected == topic) Accent.copy(alpha = 0.14f) else Surface)
                    .border(1.dp, if (selected == topic) Accent.copy(alpha = 0.30f) else Border, RoundedCornerShape(999.dp))
                    .clickable { onSelected(topic) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(topic.label, color = if (selected == topic) TextPrimary else TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun PremiumResultBox(result: PremiumConsultation, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 18) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PremiumSection("지금 고민의 핵심", result.core, Rose)
            PremiumSection("숫자의 흐름으로 본 해석", result.interpretation, Accent)
            MonthInsightRow(result)
            PremiumSection("주의할 점", result.caution, Rose)
            PremiumSection("앞으로의 방향", result.direction, Mint)
            PremiumSection("한 줄 조언", result.oneLineAdvice, Accent)
        }
    }
}

@Composable
private fun MonthInsightRow(result: PremiumConsultation) {
    if (result.bestMonth.isBlank() && result.riskyMonth.isBlank()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("월별 흐름", color = Accent, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MonthInsightCard(
                title = "추천",
                month = result.bestMonth,
                body = result.bestMonthReason,
                accentColor = Mint,
                modifier = Modifier.weight(1f)
            )
            MonthInsightCard(
                title = "주의",
                month = result.riskyMonth,
                body = result.riskyMonthReason,
                accentColor = Rose,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthInsightCard(
    title: String,
    month: String,
    body: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .border(1.dp, accentColor.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = accentColor, style = MaterialTheme.typography.labelMedium)
            Text(month.ifBlank { "-" }, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PremiumSection(title: String, body: String, accentColor: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = accentColor, style = MaterialTheme.typography.labelLarge)
        Text(body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}
