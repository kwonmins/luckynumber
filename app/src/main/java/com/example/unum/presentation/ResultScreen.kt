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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.KeywordPills
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.PremiumBadge
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.Surface3
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun ResultScreen(viewModel: AppViewModel, onOpenPremium: () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bundle = uiState.latestBundle

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        if (bundle == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Text(
                        "아직 리포트가 없어요. 입력 화면에서 생년월일을 먼저 넣어주세요.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(16.dp)) }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("무료 결과", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("공유", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                item { ResultHero(bundle, onOpenPremium) }
                item { FlowReport(bundle) }
                item { ActionPlan(bundle) }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}

@Composable
private fun ResultHero(bundle: NumerologyResultBundle, onOpenPremium: () -> Unit) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Color(0xFFFFFCF5), borderColor = Border, contentPadding = 22) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("당신의 핵심 번호", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Text(bundle.numbers.destiny.toString(), color = Accent, style = MaterialTheme.typography.displayLarge)
            Text(bundle.content.destinyProfile.title, color = Accent, style = MaterialTheme.typography.titleLarge)
            Text(
                bundle.content.destinyProfile.destinyText,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            KeywordPills(bundle.content.destinyProfile.coreKeywords)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoBox("강점", bundle.content.lifeRecord.keywords.take(3), Accent, Modifier.weight(1f))
                InfoBox("주의", bundle.content.lifeRecord.cautionKeywords.take(3), Rose, Modifier.weight(1f))
            }
            GradientButton("운세노트 만들기", onOpenPremium, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun InfoBox(title: String, items: List<String>, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.04f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, color = color, style = MaterialTheme.typography.labelLarge)
            items.forEach {
                Text("• $it", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FlowReport(bundle: NumerologyResultBundle) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, contentPadding = 18) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("인생 흐름 리포트", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("전체 흐름", color = Accent, style = MaterialTheme.typography.labelMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FlowTab("전체 흐름", true, Modifier.weight(1f))
                FlowTab("초기", false, Modifier.weight(1f))
                FlowTab("중기", false, Modifier.weight(1f))
                FlowTab("후기", false, Modifier.weight(1f))
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowBar("초기", bundle.numbers.early, Blue)
                FlowBar("중기", bundle.numbers.middle, Accent)
                FlowBar("후기", bundle.numbers.late, Mint)
            }
            StageCard("초기", "기반 잡기", bundle.content.lifeRecord.earlyText, bundle.numbers.early, Blue)
            StageCard("중기", "성장과 확장", bundle.content.lifeRecord.middleText, bundle.numbers.middle, Accent)
            StageCard("후기", "안정과 성숙", bundle.content.lifeRecord.lateText, bundle.numbers.late, Mint)
        }
    }
}

@Composable
private fun FlowTab(text: String, selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Accent.copy(alpha = 0.10f) else Surface2)
            .border(1.dp, if (selected) Accent.copy(alpha = 0.24f) else Border, RoundedCornerShape(999.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Accent else TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FlowBar(label: String, value: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(value.toString(), color = TextPrimary, style = MaterialTheme.typography.labelMedium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Surface3)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value.coerceIn(0, 9) + 1) / 10f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun StageCard(label: String, title: String, body: String, number: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), color = color, style = MaterialTheme.typography.labelLarge)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text("$label · $title", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ActionPlan(bundle: NumerologyResultBundle) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Rose.copy(alpha = 0.04f), borderColor = Rose.copy(alpha = 0.18f), contentPadding = 18) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Rose, CircleShape))
                Text("주의 & 액션 플랜", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
            Text("지금 조심할 것", color = Rose, style = MaterialTheme.typography.labelLarge)
            Text(bundle.content.lifeRecord.summaryText, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bundle.content.lifeRecord.cautionKeywords.take(3).forEach {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Rose.copy(alpha = 0.08f))
                            .border(1.dp, Rose.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(it, color = Rose, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            ChecklistItem("하루 10분 마음 상태 먼저 체크하기", true)
            ChecklistItem("중요하지 않은 일은 과감히 거절하기", true)
            ChecklistItem("반복되는 실수 패턴을 짧게 기록하기", false)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("TIP  작은 신호를 가볍게 넘기지 마세요. 커지기 전에 잡는 게 핵심입니다.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ChecklistItem(text: String, checked: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(17.dp)
                .clip(CircleShape)
                .background(if (checked) Mint else Surface)
                .border(1.dp, if (checked) Mint else Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Text("✓", color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
        Text(text, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
