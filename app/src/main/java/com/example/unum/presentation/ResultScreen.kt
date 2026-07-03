package com.example.unum.presentation

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.ui.components.AppTopBar
import com.example.unum.ui.components.EmptyStateView
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.KeywordPills
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToneInfoCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.AccentDark
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.CategoryLoveSurface
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.StatusSuccessSurface
import com.example.unum.ui.theme.Success
import com.example.unum.ui.theme.Surface
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
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AppTopBar(
                    title = "나의 성향 결과",
                    subtitle = "핵심 번호로 보는 기본 성향",
                    action = {
                        IconButton(
                            onClick = { shareResult(context, bundle) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "결과 공유하기", tint = TextPrimary)
                        }
                    }
                )
            }
            item { ResultHeroCard(bundle) }
            item {
                val free = bundle.freeReading
                val life = bundle.content.lifeRecord
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToneInfoCard(
                        title = "강점",
                        items = free?.strength?.let(::listOf) ?: life.keywords.take(3),
                        tint = Success,
                        background = StatusSuccessSurface,
                        modifier = Modifier.weight(1f)
                    )
                    ToneInfoCard(
                        title = "주의",
                        items = free?.caution?.let(::listOf) ?: life.cautionKeywords.take(3),
                        tint = Color(0xFFEA580C),
                        background = CategoryLoveSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("더 자세히 보고 싶다면", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "현재 고민을 입력하면 개인화된 프리미엄 운세노트를 만들 수 있어요.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                GradientButton("나만의 상세 리포트 만들기", onOpenPremium, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ResultHeroCard(bundle: NumerologyResultBundle) {
    val profile = bundle.content.destinyProfile
    val free = bundle.freeReading
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Color.Transparent, borderColor = Color.Transparent, contentPadding = 0) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Gold.copy(alpha = 0.18f), Color(0xFF0A0B1A), Color(0xFF07080F))
                    ),
                    RoundedCornerShape(22.dp)
                )
                .border(1.dp, Gold.copy(alpha = 0.30f), RoundedCornerShape(22.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text("핵심 수리", color = TextMuted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text(
                        bundle.numbers.destiny.toString(),
                        color = Gold,
                        fontSize = 94.sp,
                        lineHeight = 94.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(profile.title, color = TextPrimary, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                }
                Image(
                    painter = painterResource(MascotArt.Result),
                    contentDescription = "결과를 소개하는 수리",
                    modifier = Modifier.size(width = 146.dp, height = 172.dp),
                    contentScale = ContentScale.Fit
                )
            }
            KeywordPills(profile.coreKeywords, Modifier.fillMaxWidth())
            Text(
                free?.opening ?: profile.resultTitle,
                color = Gold,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Start
            )
            Text(
                free?.core ?: profile.summary,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
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
        putExtra(Intent.EXTRA_SUBJECT, "운세노트 성향 결과")
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(intent, "결과 공유하기"))
}
