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
import androidx.compose.ui.graphics.Color
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
                        Text("시기별 역할 지도", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "높고 낮음을 비교하는 그래프가 아니라, 각 시기에 어떤 역할의 결이 강해지는지 보여줘요.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        LifeRoleMap(
                            bundle = bundle,
                            selectedStage = selectedStage,
                            onSelect = { selectedStage = it }
                        )
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
    val labels = listOf("모든 시기", "초년", "중년", "말년")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = selectedStage == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
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
private fun LifeRoleMap(
    bundle: NumerologyResultBundle,
    selectedStage: Int,
    onSelect: (Int) -> Unit
) {
    val phases = listOf(
        FlowPhase("초년", "20~30세", bundle.numbers.early, "기초를 배우는 결", "관계, 습관, 기준을 처음 세우는 시기", Accent, 1),
        FlowPhase("중년", "31~60세", bundle.numbers.middle, "확장과 조율의 결", "일, 책임, 선택의 폭이 넓어지는 시기", Mint, 2),
        FlowPhase("말년", "61세 이후", bundle.numbers.late, "정리와 전승의 결", "내가 쌓은 방식을 삶의 언어로 남기는 시기", Rose, 3)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        phases.forEach { phase ->
            FlowRoleTile(
                phase = phase,
                selected = selectedStage == 0 || selectedStage == phase.index,
                onClick = { onSelect(phase.index) }
            )
        }
    }
}

@Composable
private fun FlowRoleTile(phase: FlowPhase, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) phase.color.copy(alpha = 0.12f) else Surface2.copy(alpha = 0.72f))
            .border(1.dp, if (selected) phase.color.copy(alpha = 0.34f) else Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(phase.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("결 ${phase.number}", color = phase.color, style = MaterialTheme.typography.bodySmall)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("${phase.title} · ${phase.range}", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(phase.role, color = phase.color, style = MaterialTheme.typography.bodySmall)
            Text(phase.caption, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private data class FlowPhase(
    val title: String,
    val range: String,
    val number: Int,
    val role: String,
    val caption: String,
    val color: Color,
    val index: Int
)

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
            Text("결 $number", color = Accent, style = MaterialTheme.typography.bodySmall)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("$title ($range) · $subtitle", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
