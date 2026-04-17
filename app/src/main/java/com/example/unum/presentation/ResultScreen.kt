package com.example.unum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.domain.NumerologyCalculator
import com.example.unum.ui.components.DestinyCard
import com.example.unum.ui.components.FlowGraphCard
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.InterpretationCard
import com.example.unum.ui.components.LifeStageCards
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SummaryBanner
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.TextSecondary

@Composable
fun ResultScreen(viewModel: AppViewModel, onOpenPremium: () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bundle = uiState.latestBundle

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        if (bundle == null) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.Center) {
                item { Text("먼저 홈에서 생년월일을 입력해 주세요.", color = TextSecondary, style = MaterialTheme.typography.bodyLarge) }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val displayInput = bundle.displayInput
                item { Spacer(Modifier.height(18.dp)) }
                item { SectionTitle("결과 보기") }
                item {
                    Text(
                        "${NumerologyCalculator.formatDate(displayInput.year, displayInput.month, displayInput.day)} · ${displayInput.gender.label} · code ${bundle.numbers.code}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item { DestinyCard(bundle.numbers.destiny, bundle.content.destinyProfile) }
                item { LifeStageCards(bundle.numbers.early, bundle.numbers.middle, bundle.numbers.late) }
                item { FlowGraphCard(bundle.numbers.early, bundle.numbers.middle, bundle.numbers.late) }
                item { InterpretationCard("초년 해석", bundle.content.lifeRecord.early, Blue, bundle.content.lifeRecord.earlyText) }
                item { InterpretationCard("중년 해석", bundle.content.lifeRecord.middle, Rose, bundle.content.lifeRecord.middleText) }
                item { InterpretationCard("말년 해석", bundle.content.lifeRecord.late, Mint, bundle.content.lifeRecord.lateText) }
                item { InterpretationCard("삶 전체 해석", bundle.content.lifeRecord.destiny, Accent, bundle.content.lifeRecord.lifeText) }
                item { SummaryBanner(bundle.content.lifeRecord.summaryText, bundle.content.lifeRecord.oneLineAdvice) }
                item { GradientButton("✦ AI 프리미엄 상담 받기", onOpenPremium, Modifier.fillMaxWidth()) }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}
