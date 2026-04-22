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
import com.example.unum.ui.components.AppHeader
import com.example.unum.ui.components.DestinyCard
import com.example.unum.ui.components.FlowGraphCard
import com.example.unum.ui.components.InterpretationCard
import com.example.unum.ui.components.LifeStageCards
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.PremiumUpgradeCard
import com.example.unum.ui.components.SectionCaption
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Text(
                        "아직 무료 결과가 없어요. 홈에서 생년월일을 입력하면 바로 확인할 수 있어요.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            val displayInput = bundle.displayInput
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(18.dp)) }
                item {
                    AppHeader(
                        title = "무료 운세 결과",
                        subtitle = "${NumerologyCalculator.formatDate(displayInput.year, displayInput.month, displayInput.day)} · ${displayInput.gender.label}",
                        eyebrow = "무료 결과"
                    )
                }
                item {
                    DestinyCard(
                        number = bundle.numbers.destiny,
                        profile = bundle.content.destinyProfile,
                        code = bundle.numbers.code
                    )
                }
                item { SectionTitle("시기별 숫자") }
                item { SectionCaption("초년, 중년, 말년의 흐름을 먼저 가볍게 읽을 수 있어요.") }
                item { LifeStageCards(bundle.numbers.early, bundle.numbers.middle, bundle.numbers.late) }
                item { FlowGraphCard(bundle.numbers.early, bundle.numbers.middle, bundle.numbers.late) }
                item {
                    SummaryBanner(
                        summaryText = bundle.content.lifeRecord.summaryText,
                        oneLineAdvice = bundle.content.lifeRecord.oneLineAdvice
                    )
                }
                item { SectionTitle("자세히 읽기") }
                item {
                    InterpretationCard(
                        title = "초년의 흐름",
                        badgeNumber = bundle.content.lifeRecord.early,
                        accentColor = Blue,
                        text = bundle.content.lifeRecord.earlyText
                    )
                }
                item {
                    InterpretationCard(
                        title = "중년의 흐름",
                        badgeNumber = bundle.content.lifeRecord.middle,
                        accentColor = Accent,
                        text = bundle.content.lifeRecord.middleText
                    )
                }
                item {
                    InterpretationCard(
                        title = "말년의 흐름",
                        badgeNumber = bundle.content.lifeRecord.late,
                        accentColor = Mint,
                        text = bundle.content.lifeRecord.lateText
                    )
                }
                item {
                    InterpretationCard(
                        title = "전체 흐름",
                        badgeNumber = bundle.content.lifeRecord.destiny,
                        accentColor = Rose,
                        text = bundle.content.lifeRecord.lifeText
                    )
                }
                item {
                    MascotGuideCard(
                        title = "다음 단계",
                        message = "무료 결과는 요약 중심으로 보여드렸어요. 고민이 있다면 AI 프리미엄 운세에서 더 깊게 이어서 읽을 수 있어요.",
                        imageRes = MascotArt.Result
                    )
                }
                item { PremiumUpgradeCard(onOpenPremium) }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}
