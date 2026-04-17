package com.example.unum.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.DestinyProfile
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun DestinyCard(number: Int, profile: DestinyProfile, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 18) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("운명수", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Text(number.toString(), color = Accent, style = MaterialTheme.typography.displayLarge)
            Text(profile.title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            KeywordPills(profile.coreKeywords)
            Text(profile.destinyText, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun LifeStageCards(early: Int, middle: Int, late: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StageMiniCard("초년수", early, Blue, Modifier.weight(1f))
        StageMiniCard("중년수", middle, Accent, Modifier.weight(1f))
        StageMiniCard("말년수", late, Mint, Modifier.weight(1f))
    }
}

@Composable
private fun StageMiniCard(title: String, number: Int, color: Color, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier, contentPadding = 14) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Text(number.toString(), color = color, style = MaterialTheme.typography.displayMedium)
            Text("숫자 $number", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun FlowGraphCard(early: Int, middle: Int, late: Int, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 18) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("흐름 그래프", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    val width = size.width
                    val height = size.height
                    val points = listOf(
                        Offset(width * 0.15f, height * (0.92f - early / 12f)),
                        Offset(width * 0.50f, height * (0.92f - middle / 12f)),
                        Offset(width * 0.85f, height * (0.92f - late / 12f))
                    )
                    repeat(4) { i ->
                        val y = height * (0.2f + i * 0.18f)
                        drawLine(Border, Offset(0f, y), Offset(width, y), 1.dp.toPx())
                    }
                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        cubicTo(width * 0.28f, points[0].y, width * 0.36f, points[1].y, points[1].x, points[1].y)
                        cubicTo(width * 0.64f, points[1].y, width * 0.72f, points[2].y, points[2].x, points[2].y)
                    }
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Blue, Accent, Mint)
                        ),
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                    listOf(Blue, Accent, Mint).forEachIndexed { index, color ->
                        drawCircle(color.copy(alpha = 0.24f), 12.dp.toPx(), points[index])
                        drawCircle(color, 6.dp.toPx(), points[index])
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GraphLabel("초년", Blue)
                GraphLabel("중년", Accent)
                GraphLabel("말년", Mint)
            }
        }
    }
}

@Composable
private fun GraphLabel(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(text, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun InterpretationCard(title: String, badgeNumber: Int, accentColor: Color, text: String, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 18) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp)).border(1.dp, accentColor.copy(alpha = 0.22f), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(badgeNumber.toString(), color = accentColor, style = MaterialTheme.typography.labelMedium)
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
        modifier = modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Accent.copy(alpha = 0.16f), Mint.copy(alpha = 0.10f), Surface2)), RoundedCornerShape(20.dp)).border(1.dp, Border, RoundedCornerShape(20.dp)).padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("전체 흐름 요약", color = Gold, style = MaterialTheme.typography.labelLarge)
            Text(summaryText, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(oneLineAdvice, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
