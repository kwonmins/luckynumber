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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
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
import com.example.unum.ui.components.EmptyStateView
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
fun ResultScreen(
    viewModel: AppViewModel,
    onOpenInput: () -> Unit
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
                EmptyStateView(
                    title = "아직 리포트가 없어요",
                    description = "생년월일을 입력하면 오늘의 핵심수와 성향 리포트를 바로 볼 수 있어요.",
                    actionText = "생년월일 입력하기",
                    onActionClick = onOpenInput,
                    icon = Icons.Rounded.Insights
                )
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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("나의 성향 결과", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                }
            }
            item { ResultNumberCard(bundle = bundle) }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun ResultNumberCard(bundle: NumerologyResultBundle) {
    val destiny = bundle.numbers.destiny
    val profile = bundle.content.destinyProfile
    val life = bundle.content.lifeRecord
    val freeReading = bundle.freeReading

    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface,
        borderColor = Border,
        contentPadding = 22
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text("당신의 핵심 번호", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Text(destiny.toString(), color = Accent, style = MaterialTheme.typography.displayLarge)
            Text(profile.title, color = Accent, style = MaterialTheme.typography.titleLarge)
            Text(
                freeReading?.opening ?: profile.resultTitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                freeReading?.core ?: life.lifeText.ifBlank { life.summaryText },
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultInfoBox(
                    title = "강점",
                    items = freeReading?.strength?.let(::listOf) ?: life.keywords.take(3),
                    color = Accent,
                    modifier = Modifier.weight(1f)
                )
                ResultInfoBox(
                    title = "주의",
                    items = freeReading?.caution?.let(::listOf) ?: life.cautionKeywords.take(3),
                    color = Rose,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ResultInfoBox(
    title: String,
    items: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.06f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(title, color = color, style = MaterialTheme.typography.labelLarge)
        items.forEach { item ->
            Text("• $item", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
