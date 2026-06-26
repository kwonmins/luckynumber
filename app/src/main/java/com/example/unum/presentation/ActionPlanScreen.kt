package com.example.unum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun ActionPlanScreen(
    viewModel: AppViewModel,
    onOpenPremium: () -> Unit
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
                Text("리포트 생성 후 주의 신호를 확인할 수 있어요.", color = TextSecondary)
            }
            return@MysticBackground
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }
            item { Text("주의 신호", color = TextPrimary, style = MaterialTheme.typography.titleLarge) }
            item { WarningCard(bundle) }
            item { CautionTags(bundle) }
            item { GradientButton("프리미엄 리포트로 더 깊게 보기", onOpenPremium, Modifier.fillMaxWidth()) }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun WarningCard(bundle: NumerologyResultBundle) {
    val cautions = bundle.content.lifeRecord.cautionKeywords
    val cautionText = bundle.freeReading?.caution
        ?: "${cautions.take(2).joinToString(", ")} 흐름이 반복되면 좋은 에너지가 쉽게 소모될 수 있어요. 단정적인 예언이 아니라, 오늘의 선택을 조금 더 부드럽게 조율하기 위한 생활 관리 신호로 봐주세요."
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Rose.copy(alpha = 0.07f),
        borderColor = Rose.copy(alpha = 0.22f),
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Rose)
                Text("지금 조심할 것", color = Rose, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                cautionText,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CautionTags(bundle: NumerologyResultBundle) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("주의 신호", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (bundle.content.lifeRecord.cautionKeywords.take(4) + "소통 회피").distinct().forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Rose.copy(alpha = 0.08f))
                        .border(1.dp, Rose.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(tag, color = Rose, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
