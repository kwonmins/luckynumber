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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
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
    onOpenFlow: () -> Unit
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
                Text(
                    "아직 리포트가 없어요. 입력 화면에서 생년월일을 먼저 넣어주세요.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
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
                    Icon(Icons.Rounded.Share, contentDescription = "공유", tint = TextSecondary)
                }
            }
            item { ResultNumberCard(bundle = bundle, onOpenFlow = onOpenFlow) }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun ResultNumberCard(bundle: NumerologyResultBundle, onOpenFlow: () -> Unit) {
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
                freeReading?.opening ?: life.summaryText.ifBlank { profile.destinyText },
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent.copy(alpha = 0.06f))
                    .border(1.dp, Accent.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                    .padding(13.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("오늘의 행동", color = Accent, style = MaterialTheme.typography.labelLarge)
                    Text(
                        freeReading?.action ?: life.oneLineAdvice,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            GradientButton(
                text = "전체 리포트 보기",
                onClick = onOpenFlow,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("흐름 리포트와 액션 플랜으로 이어집니다", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = TextMuted)
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
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.06f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(title, color = color, style = MaterialTheme.typography.labelLarge)
        items.forEach { item ->
            Text("• $item", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
