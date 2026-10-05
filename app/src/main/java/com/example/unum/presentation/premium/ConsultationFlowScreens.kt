package com.example.unum.presentation.premium

import com.example.unum.presentation.*

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.PremiumMode
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.BookPaper
import com.example.unum.ui.theme.BookPaperEdge
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun QuestionConfirmScreen(
    topicLabel: String,
    question: String,
    originalConcern: String,
    onEdit: () -> Unit,
    onConfirm: () -> Unit
) {
    MysticBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("이 질문이 맞나요?", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(
                    "적어주신 고민을 AI가 명확하게 답할 수 있는 상담 질문으로 다듬었어요.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                SurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    tonalColor = Surface,
                    borderColor = Accent.copy(alpha = 0.28f),
                    contentPadding = 18
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("AI에 전달할 질문 · $topicLabel", color = Accent, style = MaterialTheme.typography.labelLarge)
                        Text(
                            question.ifBlank { originalConcern },
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                SurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    tonalColor = Surface2,
                    borderColor = Border,
                    contentPadding = 14
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("원문", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                        Text(originalConcern, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton("네, 이 질문이 맞아요", onConfirm, Modifier.fillMaxWidth())
                SecondaryButton("고민 다시 수정하기", onEdit, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
internal fun PremiumLoadingScreen(
    isLoading: Boolean,
    hasBook: Boolean,
    mode: PremiumMode,
    onDone: () -> Unit
) {
    val stages = remember(mode) {
        if (mode == PremiumMode.COMPATIBILITY) {
            listOf(
                "두 사람의 숫자를 비교 중",
                "궁합수를 계산하는 중",
                "프리미엄 궁합노트 제본 중"
            )
        } else {
            listOf(
                "수리가 숫자를 해석 중",
                "고민의 질문을 정리 중",
                "프리미엄 책자 제본 중"
            )
        }
    }
    var stageIndex by remember { mutableStateOf(0) }
    LaunchedEffect(isLoading) {
        while (isLoading) {
            delay(1_050)
            stageIndex = (stageIndex + 1) % stages.size
        }
        if (!isLoading && hasBook) {
            stageIndex = stages.lastIndex
        }
    }
    LaunchedEffect(isLoading, hasBook) {
        if (!isLoading && hasBook) {
            delay(2_500)
            onDone()
        }
    }
    MysticBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                PaperStackAnimation()
                ConsultingStageTicker(
                    stage = stages[stageIndex],
                    labels = stages,
                    caption = if (!isLoading && hasBook) {
                        "책자 준비가 끝났어요. 표지를 여는 중입니다."
                    } else if (mode == PremiumMode.COMPATIBILITY) {
                        "남자와 여자 각각의 흐름을 관계 문장과 책자 구조로 엮고 있어요."
                    } else {
                        "입력한 흐름을 상담 문장과 책자 구조로 엮고 있어요."
                    }
                )
                Text(
                    if (mode == PremiumMode.COMPATIBILITY) {
                        "수리가 두 사람 사이의 숫자 흐름과\n관계 질문을 조용히 풀고 있어요"
                    } else {
                        "수리가 당신의 숫자 흐름과\n적어주신 사연을 조용히 풀고 있어요"
                    },
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    if (mode == PremiumMode.COMPATIBILITY) {
                        "궁합수와 생활 흐름에 맞는 관계 비책을 제작 중입니다."
                    } else {
                        "고민 분야에 맞는 깊이 있는 비책을 제작 중입니다."
                    },
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = Accent,
                trackColor = Surface2
            )
        }
    }
}

@Composable
internal fun ConsultingStageTicker(stage: String, labels: List<String>, caption: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(initialScale = 0.96f)) togetherWith
                    (fadeOut(tween(160)) + scaleOut(targetScale = 1.02f))
            },
            label = "consultingStage"
        ) { text ->
            Text(
                text,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(caption, color = TextMuted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            labels.forEach { label ->
                val active = label == stage
                Box(
                    modifier = Modifier
                        .size(width = if (active) 22.dp else 7.dp, height = 7.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) Accent else Surface2)
                )
            }
        }
    }
}

@Composable
internal fun PaperStackAnimation() {
    val transition = rememberInfiniteTransition(label = "paperStack")
    val offset by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "offset"
    )
    Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        PaperSheet(rotation = -10f, y = offset - 2f)
        PaperSheet(rotation = 7f, y = -offset)
        PaperSheet(rotation = 0f, y = offset / 2f, active = true)
    }
}

@Composable
internal fun PaperSheet(rotation: Float, y: Float, active: Boolean = false) {
    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 78.dp)
            .graphicsLayer(rotationZ = rotation, translationY = y)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) BookPaper else Surface)
            .border(1.dp, if (active) BookPaperEdge else Border, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (active) Text("수", color = Gold, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
internal fun VoiceChoiceScreen(onRead: () -> Unit, onSkip: () -> Unit) {
    MysticBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Rounded.Headphones, contentDescription = null, tint = Accent, modifier = Modifier.size(48.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("수리가 직접 결과를 읽어줍니다", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(
                    "건너뛰기를 누르시면 바로 책자를 통해 결과를 보실 수 있어요.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton("수리가 읽어주세요", onRead, Modifier.fillMaxWidth())
                SecondaryButton("건너뛰기", onSkip, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
internal fun HanokReadingScreen(advice: String, onSkip: () -> Unit, onOpenBook: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF16120E))
    ) {
        HanokFallbackScene(Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.68f), Color.Transparent, Color.Black.copy(alpha = 0.82f))))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SURI'S SANCTUARY", color = Color(0xCCFDE68A), style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("건너뛰기", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            VoiceWave()
            SurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                tonalColor = Color.Black.copy(alpha = 0.56f),
                borderColor = Color(0x33F59E0B),
                contentPadding = 16
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0x33F59E0B))
                            .border(1.dp, Color(0x44FDE68A), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text("SURI'S WHISPER", color = Color(0xFFFDE68A), style = MaterialTheme.typography.labelMedium)
                    }
                    Text(
                        advice,
                        modifier = Modifier
                            .heightIn(max = 148.dp)
                            .verticalScroll(rememberScrollState()),
                        color = Color(0xFFF4EAE1),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("은은히 일렁이는 불빛 아래, 수리의 목소리에 조용히 귀 기울여 보세요.", color = Color(0xFFB6A99D), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(10.dp))
            GradientButton("책자로 결과 마저 확인하기", onOpenBook, Modifier.fillMaxWidth())
        }
    }
}

@Composable
internal fun HanokFallbackScene(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "lantern")
    val glow by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
        label = "glow"
    )
    Canvas(modifier = modifier) {
        drawRect(Color(0xFF0D0907))
        val floorTop = size.height * 0.58f
        val floor = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width * 0.30f, floorTop)
            lineTo(size.width * 0.70f, floorTop)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(floor, Color(0xFF1B110A))
        repeat(4) { index ->
            val x = size.width * (0.15f + index * 0.23f)
            drawLine(Color(0xFF120A05), Offset(x, size.height), Offset(size.width * 0.5f, floorTop), strokeWidth = 2f)
        }
        drawRect(Color(0xFFF5EDE0), topLeft = Offset(size.width * 0.08f, size.height * 0.16f), size = androidx.compose.ui.geometry.Size(size.width * 0.34f, size.height * 0.42f))
        drawRect(Color(0xFFF5EDE0), topLeft = Offset(size.width * 0.58f, size.height * 0.16f), size = androidx.compose.ui.geometry.Size(size.width * 0.34f, size.height * 0.42f))
        repeat(4) { row ->
            val y = size.height * (0.22f + row * 0.08f)
            drawLine(Color(0xFF8C6448), Offset(size.width * 0.08f, y), Offset(size.width * 0.42f, y), strokeWidth = 1f)
            drawLine(Color(0xFF8C6448), Offset(size.width * 0.58f, y), Offset(size.width * 0.92f, y), strokeWidth = 1f)
        }
        drawCircle(Color(0xFFF59E0B).copy(alpha = glow * 0.34f), radius = size.width * 0.22f, center = Offset(size.width * 0.72f, size.height * 0.63f))
        drawRect(Color(0xFFFDE68A).copy(alpha = glow), topLeft = Offset(size.width * 0.70f, size.height * 0.36f), size = androidx.compose.ui.geometry.Size(size.width * 0.07f, size.height * 0.08f))
        drawRoundRect(Color(0xFF3A2312), topLeft = Offset(size.width * 0.34f, size.height * 0.80f), size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.05f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        listOf(
            Offset(size.width * 0.20f, size.height * 0.42f),
            Offset(size.width * 0.82f, size.height * 0.50f),
            Offset(size.width * 0.56f, size.height * 0.38f)
        ).forEachIndexed { index, dot ->
            drawCircle(Color(0xFFFDE68A).copy(alpha = 0.35f + index * 0.12f), radius = 3f + index, center = dot)
        }
    }
}

@Composable
internal fun VoiceWave() {
    val transition = rememberInfiniteTransition(label = "voiceWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "phase"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(18) { index ->
            val h = (8 + ((index % 5) * 6) + phase * 12).dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.dp)
                    .height(h)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFDE68A).copy(alpha = 0.42f))
            )
        }
    }
}

