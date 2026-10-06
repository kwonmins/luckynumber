package com.example.unum.presentation.result

import com.example.unum.presentation.*

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.ui.components.AppTopBar
import com.example.unum.ui.components.EmptyStateView
import com.example.unum.ui.components.KeywordPills
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.DeepNavy
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun ResultScreen(
    viewModel: AppViewModel,
    onOpenInput: () -> Unit,
    onOpenPremium: () -> Unit
) {
    val bundle = viewModel.uiState.collectAsStateWithLifecycle().value.latestBundle
    val context = LocalContext.current

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        if (bundle == null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    title = "아직 읽은 수리가 없어요",
                    description = "생년월일을 입력하면 핵심 숫자와 나의 기본 성향을 바로 확인할 수 있어요.",
                    actionText = "생년월일 입력하기",
                    onActionClick = onOpenInput,
                    icon = Icons.Rounded.Insights
                )
            }
            return@MysticBackground
        }

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppTopBar(
                    title = "나의 수리 결과",
                    subtitle = "${bundle.displayInput.year}년 ${bundle.displayInput.month}월 ${bundle.displayInput.day}일의 결",
                    action = {
                        IconButton(
                            onClick = { shareResult(context, bundle) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "결과 공유하기", tint = DeepNavy)
                        }
                    }
                )
            }
            item { ResultHeroCard(bundle) }
            item { LifeFlowRow(bundle) }
            item { LifeStageNarrativeCard(bundle) }
            item { ResultNarrativeCard(bundle) }
            item { TopicPatternCard(bundle) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReadingNote(
                        index = "강",
                        title = "나의 강점",
                        body = bundle.freeReading?.strength ?: bundle.content.destinyProfile.strength,
                        tint = Mint,
                        modifier = Modifier.weight(1f)
                    )
                    ReadingNote(
                        index = "유",
                        title = "마음에 둘 것",
                        body = bundle.freeReading?.caution ?: bundle.content.destinyProfile.caution,
                        tint = Rose,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                PremiumReadingCta(onClick = onOpenPremium)
            }
        }
    }
}

@Composable
private fun ResultHeroCard(bundle: NumerologyResultBundle) {
    val profile=bundle.content.destinyProfile
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Surface).border(1.dp,Border,RoundedCornerShape(22.dp))) {
        com.example.unum.ui.components.MoonGarden(Modifier.fillMaxWidth().height(110.dp))
        Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("나의 수리 · ${bundle.numbers.destiny}",color=Accent,style=MaterialTheme.typography.labelMedium)
            Text(profile.resultTitle,color=DeepNavy,style=MaterialTheme.typography.titleLarge)
            Text(bundle.freeReading?.opening ?: profile.summary,color=TextSecondary,style=MaterialTheme.typography.bodyLarge)
            KeywordPills(profile.coreKeywords,Modifier.fillMaxWidth())
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                com.example.unum.ui.components.PastelSuri(8,Modifier.size(80.dp))
                Text("지금의 흐름을 천천히 읽어보세요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LifeFlowRow(bundle: NumerologyResultBundle) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("인생의 숫자 흐름", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text("초기 · 중기 · 후기", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowNumber("초기", bundle.numbers.early, Blue, Modifier.weight(1f))
            FlowNumber("중기", bundle.numbers.middle, Accent, Modifier.weight(1f))
            FlowNumber("후기", bundle.numbers.late, Gold, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FlowNumber(label: String, value: Int, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(20.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        Text(value.toString(), color = tint, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun LifeStageNarrativeCard(bundle: NumerologyResultBundle) {
    val reading = bundle.freeReading ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(26.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("숫자 조합이 만드는 차이", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        LifeStageReadingRow("초기", bundle.numbers.early, reading.early, Blue)
        LifeStageReadingRow("중기", bundle.numbers.middle, reading.middle, Accent)
        LifeStageReadingRow("후기", bundle.numbers.late, reading.late, Gold)
    }
}

@Composable
private fun LifeStageReadingRow(label: String, number: Int, body: String, tint: Color) {
    if (body.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(tint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(number.toString(), color = tint, style = MaterialTheme.typography.labelMedium)
            }
            Text("$label 해석", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
        }
        Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TopicPatternCard(bundle: NumerologyResultBundle) {
    val reading = bundle.freeReading ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(26.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("분야별로 반복되는 장면", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        TopicPatternRow("관계", reading.relationship, Rose)
        TopicPatternRow("일", reading.work, Blue)
        TopicPatternRow("돈", reading.money, Gold)
    }
}

@Composable
private fun TopicPatternRow(label: String, body: String, tint: Color) {
    if (body.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = tint, style = MaterialTheme.typography.labelLarge)
        Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ResultNarrativeCard(bundle: NumerologyResultBundle) {
    val profile = bundle.content.destinyProfile
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(26.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("당신을 읽는 문장", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Surface2)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("수리 ${bundle.numbers.destiny}", color = Accent, style = MaterialTheme.typography.labelMedium)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
        Text(
            bundle.freeReading?.core ?: profile.summary,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            "“${bundle.freeReading?.action?.ifBlank { profile.actionGuide } ?: profile.actionGuide}”",
            color = Accent,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun ReadingNote(
    index: String,
    title: String,
    body: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(tint.copy(alpha = 0.09f))
            .border(1.dp, tint.copy(alpha = 0.24f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(tint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(index, color = Surface, style = MaterialTheme.typography.labelMedium)
        }
        Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 6)
    }
}

@Composable
private fun PremiumReadingCta(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Accent)
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("이 결과를 더 깊게 읽어볼까요?", color = Surface, style = MaterialTheme.typography.titleMedium)
            Text("지금의 고민을 담은 나만의 운세노트", color = Surface.copy(alpha = 0.74f), style = MaterialTheme.typography.bodySmall)
        }
        Box(
            modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Surface)
        }
    }
}

private fun shareResult(context: android.content.Context, bundle: NumerologyResultBundle) {
    val profile = bundle.content.destinyProfile
    val body = buildString {
        append("나의 핵심 번호는 ${bundle.numbers.destiny}번, ${profile.title}입니다.\n")
        append(bundle.freeReading?.opening ?: profile.resultTitle)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "수리운세 성향 결과")
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(intent, "결과 공유하기"))
}
