package com.example.unum.presentation.premium

import com.example.unum.presentation.*

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unum.data.model.PremiumMode
import com.example.unum.data.model.PremiumTopic
import com.example.unum.presentation.spec.FeatureSpecs
import com.example.unum.ui.components.premiumTopicMascot
import com.example.unum.ui.components.FortuneIllustration
import com.example.unum.ui.components.fortuneArt

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun PremiumFormScreen(
    uiState: AppUiState,
    viewModel: AppViewModel,
    onStart: () -> Unit
) {
    val isCompatibility = uiState.premiumMode == PremiumMode.COMPATIBILITY
    val partner = uiState.compatibilityForm.partner
    var personalStep by remember { mutableStateOf(1) }
    val compatibilityReady = uiState.latestBundle != null &&
        partner.year.length == 4 &&
        partner.month.isNotBlank() &&
        partner.day.isNotBlank() &&
        uiState.compatibilityConcern.trim().length >= 6
    val canStart = if (isCompatibility) {
        compatibilityReady
    } else {
        uiState.latestBundle != null && uiState.premiumConcern.trim().length >= 6
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PremiumEntryHeader()
        PremiumEntryModeSelector(
            selected = uiState.premiumMode,
            onSelected = { mode ->
                personalStep = 1
                viewModel.setPremiumMode(mode)
            }
        )

        if (isCompatibility) {
            PremiumCompatibilityEntry(
                uiState = uiState,
                viewModel = viewModel,
                canStart = canStart,
                onStart = onStart
            )
        } else if (personalStep == 1) {
            PremiumPersonalIntro(
                uiState = uiState,
                onTopicSelected = viewModel::selectPremiumTopic,
                onContinue = { personalStep = 2 }
            )
        } else {
            PremiumPersonalDetails(
                uiState = uiState,
                onConcernChange = viewModel::updatePremiumConcern,
                onBack = { personalStep = 1 },
                canStart = canStart,
                onStart = onStart
            )
        }

        uiState.inputError?.let {
            Text(it, color = Color(0xFFFDA4AF), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
    }
}

internal val PremiumEntryNavy = Color(0xFF030409)
internal val PremiumEntryBlue = Color(0xFF5B8AF5)
internal val PremiumEntryIndigo = Color(0xFF7B9AF8)
internal val PremiumEntryPink = Color(0xFFFF4D6D)
internal val PremiumEntryViolet = Color(0xFFA78BFA)
internal val PremiumEntryGold = Color(0xFFF0A84A)

@Composable
internal fun PremiumEntryBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PremiumEntryNavy, Color(0xFF0A0B1A), Color(0xFF060710))
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PremiumEntryGold.copy(alpha = 0.19f), Color.Transparent),
                    center = Offset(size.width * 0.04f, size.height * 0.02f),
                    radius = size.width * 0.58f
                ),
                radius = size.width * 0.58f,
                center = Offset(size.width * 0.04f, size.height * 0.02f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PremiumEntryViolet.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.98f, size.height * 0.25f),
                    radius = size.width * 0.46f
                ),
                radius = size.width * 0.46f,
                center = Offset(size.width * 0.98f, size.height * 0.25f)
            )
        }
        content()
    }
}

@Composable
internal fun PremiumEntryHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("♛", color = PremiumEntryGold, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text(
                "PREMIUM",
                color = PremiumEntryGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        Text(
            "나만의 노트 제작",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
        )
    }
}

@Composable
internal fun PremiumEntryModeSelector(selected: PremiumMode, onSelected: (PremiumMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(PremiumMode.PERSONAL to "🔮 운세노트", PremiumMode.COMPATIBILITY to "💑 궁합노트").forEach { (mode, label) ->
            val isSelected = selected == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.11f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) Color.White.copy(alpha = 0.14f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelected(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.42f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
internal fun PremiumPersonalIntro(
    uiState: AppUiState,
    onTopicSelected: (PremiumTopic) -> Unit,
    onContinue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PremiumPersonalPreview(topic = uiState.premiumTopic)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "어떤 고민이 있으신가요?",
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            PremiumTopicGrid(selected = uiState.premiumTopic, onSelected = onTopicSelected)
        }
        PremiumIncludedCard()
        if (uiState.latestBundle == null) {
            PremiumEntryHint("생년월일 분석을 먼저 완료하면 맞춤 운세노트를 제작할 수 있어요.")
        }
        PremiumPrimaryButton(
            text = "✦  맞춤 비책 제작하기",
            onClick = onContinue,
            colors = listOf(PremiumEntryGold, Color(0xFFD4884A)),
            contentColor = PremiumEntryNavy
        )
    }
}

@Composable
internal fun PremiumPersonalPreview(topic: PremiumTopic) {
    val topicLabel = topic.bookLabel()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(PremiumEntryBlue.copy(alpha = 0.34f), PremiumEntryViolet.copy(alpha = 0.34f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(18.dp))
    ) {
        Image(
            painter = painterResource(premiumTopicMascot(topic)),
            contentDescription = "운세노트를 쓰는 수리",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(118.dp)
                .fillMaxHeight()
                .padding(top = 8.dp, end = 4.dp),
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.72f)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "미리보기 · $topicLabel 리포트",
                    color = Color(0xFF93C5FD),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "“숫자 속 흐름을 읽어,\n지금 필요한 답을 찾아드려요”",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("에너지 분석", "타이밍", "비책 3가지").forEach { label ->
                    Text(
                        label,
                        color = Color.White.copy(alpha = 0.68f),
                        fontSize = 9.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun PremiumTopicGrid(selected: PremiumTopic, onSelected: (PremiumTopic) -> Unit) {
    val topics = FeatureSpecs.premiumTopicPlans.map { it.topic }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        topics.chunked(2).forEach { rowTopics ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                rowTopics.forEach { topic ->
                    val isSelected = selected == topic
                    val accent = topicAccent(topic)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (isSelected) accent.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.055f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) accent.copy(alpha = 0.62f)
                                else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(13.dp)
                            )
                            .clickable { onSelected(topic) }
                            .padding(horizontal = 5.dp, vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FortuneIllustration(topic.fortuneArt(), Modifier.size(26.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            topic.bookLabel(),
                            color = if (isSelected) accent else Color.White.copy(alpha = 0.50f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                repeat(2 - rowTopics.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

internal fun topicAccent(topic: PremiumTopic): Color = when (topic) {
    PremiumTopic.ROMANCE -> PremiumEntryPink
    PremiumTopic.CAREER -> PremiumEntryBlue
    PremiumTopic.MONEY -> Color(0xFF2DD4BF)
    PremiumTopic.STUDY -> PremiumEntryViolet
    PremiumTopic.HEALTH -> Color(0xFF2DD4BF)
    PremiumTopic.BUSINESS -> PremiumEntryGold
    PremiumTopic.GENERAL -> PremiumEntryViolet
    PremiumTopic.SELF_ESTEEM -> PremiumEntryGold
    PremiumTopic.RELATIONSHIP -> PremiumEntryPink
}

@Composable
internal fun PremiumIncludedCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PremiumEntryGold.copy(alpha = 0.09f))
            .border(1.dp, PremiumEntryGold.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            "이 리포트에 포함돼요",
            color = Color.White.copy(alpha = 0.74f),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        listOf(
            "수비학 기반 에너지 심층 분석 (20p+)",
            "분야별 타이밍 캘린더",
            "지금 당장 쓸 수 있는 비책 3가지",
            "피해야 할 패턴 경고 카드"
        ).forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.Top) {
                Text("✦", color = PremiumEntryGold, fontSize = 11.sp)
                Text(item, color = Color.White.copy(alpha = 0.60f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun PremiumPersonalDetails(
    uiState: AppUiState,
    onConcernChange: (String) -> Unit,
    onBack: () -> Unit,
    canStart: Boolean,
    onStart: () -> Unit
) {
    var selectedTone by remember { mutableStateOf("전략적") }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 5.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = Color.White.copy(alpha = 0.55f), fontSize = 24.sp)
            Text(uiState.premiumTopic.bookLabel(), color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(15.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("구체적인 고민을 알려주세요", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            PremiumDarkConcernField(
                value = uiState.premiumConcern,
                onValueChange = onConcernChange,
                placeholder = "예) 오래 만난 연인과 결혼을 고민 중인데, 지금이 좋은 시기인지 알고 싶어요..."
            )
            Text("상세할수록 더 정확한 비책이 나와요 · 6자 이상", color = Color.White.copy(alpha = 0.30f), fontSize = 10.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("리포트 톤 선택", color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("⚡" to "직설적", "🌸" to "부드럽게", "🎯" to "전략적").forEach { (emoji, label) ->
                    val selected = selectedTone == label
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (selected) PremiumEntryBlue.copy(alpha = 0.31f) else Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                if (selected) PremiumEntryIndigo.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.07f),
                                RoundedCornerShape(13.dp)
                            )
                            .clickable { selectedTone = label }
                            .padding(vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 18.sp)
                        Text(
                            label,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.42f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        if (uiState.latestBundle == null) {
            PremiumEntryHint("입력 화면에서 생년월일을 저장한 뒤 제작할 수 있어요.")
        }
        PremiumPrimaryButton(
            text = "♛  프리미엄 비책 생성하기",
            onClick = onStart,
            enabled = canStart,
            colors = listOf(PremiumEntryGold, Color(0xFFD4884A)),
            contentColor = PremiumEntryNavy
        )
        PremiumTrustLine("🔒  안전하게 제작 · 평균 22페이지")
    }
}

