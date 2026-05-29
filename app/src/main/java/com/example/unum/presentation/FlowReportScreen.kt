package com.example.unum.presentation

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun FlowReportScreen(
    viewModel: AppViewModel,
    onOpenActionPlan: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bundle = uiState.latestBundle

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        if (bundle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("먼저 생년월일을 입력해 리포트를 만들어주세요.", color = TextSecondary)
            }
            return@MysticBackground
        }

        var selectedStage by remember { mutableIntStateOf(0) }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }
            item { Text("인생 흐름 리포트", color = TextPrimary, style = MaterialTheme.typography.titleLarge) }
            item { FlowTabs(selectedStage = selectedStage, onSelect = { selectedStage = it }) }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 18) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("인생 흐름 한눈에 보기", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        FlowLegend()
                        LifeCurveChart(bundle = bundle, selectedStage = selectedStage)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("0세", "20세", "40세", "60세", "80세").forEach {
                                Text(it, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            item {
                StageCard(
                    title = "초년",
                    range = "20~30세",
                    subtitle = "기반을 다지는 시기",
                    body = bundle.content.lifeRecord.earlyText,
                    number = bundle.numbers.early,
                    selected = selectedStage == 1,
                    onClick = { selectedStage = 1 }
                )
            }
            item {
                StageCard(
                    title = "중년",
                    range = "31~60세",
                    subtitle = "성장과 확장의 시기",
                    body = bundle.content.lifeRecord.middleText,
                    number = bundle.numbers.middle,
                    selected = selectedStage == 2,
                    onClick = { selectedStage = 2 }
                )
            }
            item {
                StageCard(
                    title = "말년",
                    range = "61세 이후",
                    subtitle = "완결과 성숙의 시기",
                    body = bundle.content.lifeRecord.lateText,
                    number = bundle.numbers.late,
                    selected = selectedStage == 3,
                    onClick = { selectedStage = 3 }
                )
            }
            item { GradientButton("주의 & 액션 플랜 보기", onOpenActionPlan, Modifier.fillMaxWidth()) }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun FlowTabs(selectedStage: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("전체 흐름", "초년", "중년", "말년")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = selectedStage == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) Accent else Surface2)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected) androidx.compose.ui.graphics.Color.White else TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun FlowLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendDot("종합", Accent)
        LegendDot("안정", Mint)
        LegendDot("주의", Rose)
    }
}

@Composable
private fun LegendDot(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LifeCurveChart(bundle: NumerologyResultBundle, selectedStage: Int) {
    val values = listOf(bundle.numbers.early, bundle.numbers.middle, bundle.numbers.late)
    val accent = Accent
    val muted = Border
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
    ) {
        val top = 12.dp.toPx()
        val bottom = size.height - 18.dp.toPx()
        val chartHeight = bottom - top
        repeat(4) { index ->
            val y = top + chartHeight * index / 3f
            drawLine(muted, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        val points = values.mapIndexed { index, value ->
            val x = when (index) {
                0 -> size.width * 0.08f
                1 -> size.width * 0.50f
                else -> size.width * 0.92f
            }
            val y = bottom - (((value.coerceIn(0, 9) + 1) / 10f) * chartHeight)
            Offset(x, y)
        }
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            cubicTo(size.width * 0.26f, top, size.width * 0.33f, bottom, points[1].x, points[1].y)
            cubicTo(size.width * 0.67f, top, size.width * 0.74f, bottom, points[2].x, points[2].y)
        }
        drawPath(path = path, color = accent, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        points.forEachIndexed { index, point ->
            val selected = selectedStage == 0 || selectedStage == index + 1
            drawCircle(
                color = if (selected) accent else TextMuted,
                radius = if (selected) 5.dp.toPx() else 3.5.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun StageCard(
    title: String,
    range: String,
    subtitle: String,
    body: String,
    number: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .border(1.dp, if (selected) Accent else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Accent.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), color = Accent, style = MaterialTheme.typography.titleMedium)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("$title ($range) · $subtitle", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
