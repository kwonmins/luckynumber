package com.example.unum.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.unum.R
import com.example.unum.data.model.DailyFortuneResult
import com.example.unum.data.model.DailyFortuneTopic
import com.example.unum.data.model.DailyTopicFortune
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun TodayFortuneCard(
    result: DailyFortuneResult?,
    onOpenInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = result?.coreSummary
        ?: "생년월일을 입력하면 오늘 날짜와 내 숫자를 함께 읽어 매일 다른 핵심수를 보여드려요."

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (result == null) 3.dp else 10.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (result == null) {
                    Brush.verticalGradient(listOf(Surface, Color(0xFFF8FBFF)))
                } else {
                    Brush.linearGradient(listOf(Accent, Color(0xFF1E40AF)))
                }
            )
            .border(
                1.dp,
                if (result == null) Border else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(18.dp)
            )
            .then(if (result == null) Modifier.clickable(onClick = onOpenInput) else Modifier)
            .padding(18.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Accent.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.18f),
                    radius = size.minDimension * 0.42f
                ),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.88f, size.height * 0.18f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "TODAY NUMBER",
                        color = if (result == null) Accent else Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        if (result == null) "입력이 필요해요" else "오늘의 핵심수",
                        color = if (result == null) TextPrimary else Color.White,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        result?.coreTitle ?: "매일 바뀌는 리딩 준비",
                        color = if (result == null) TextSecondary else Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                DailyNumberMedallion(number = result?.coreNumber, onDark = result != null)
            }
            Text(
                summary,
                color = if (result == null) TextSecondary else Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyLarge
            )
            if (result == null) {
                Text("생년월일 입력하기", color = Accent, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun DailyFortuneTopicSection(
    result: DailyFortuneResult?,
    onOpenInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (result == null) {
        DailyFortuneInputPrompt(onOpenInput = onOpenInput, modifier = modifier)
        return
    }

    val listState = rememberLazyListState()
    var selectedIndex by remember(result.date) { mutableIntStateOf(0) }
    val focusedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                0
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minBy { item ->
                    kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                }.index
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            itemsIndexed(result.topics) { index, reading ->
                DailyFortuneTopicCard(
                    reading = reading,
                    focused = index == focusedIndex || index == selectedIndex,
                    onClick = { selectedIndex = index }
                )
            }
        }
        DailyFortuneTopicDetail(reading = result.topics[selectedIndex])
    }
}

@Composable
private fun DailyNumberMedallion(number: Int?, onDark: Boolean) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(if (onDark) Color.White.copy(alpha = 0.18f) else Accent.copy(alpha = 0.08f))
            .border(1.dp, if (onDark) Color.White.copy(alpha = 0.24f) else Accent.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(number?.toString() ?: "?", color = if (onDark) Color.White else Accent, style = MaterialTheme.typography.displayLarge)
    }
}

@Composable
private fun DailyFortuneInputPrompt(onOpenInput: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFFBF2))
            .border(1.dp, Gold.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenInput)
            .padding(18.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.numerology_self),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.72f)),
                contentScale = ContentScale.Fit
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("분야별 오늘 운세를 보려면 생년월일이 필요해요", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("입력 후에는 오늘 날짜를 기준으로 연애, 일, 돈, 배움, 자기관리 운세가 매일 새로 바뀍니다.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text("생년월일 입력하기", color = Gold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DailyFortuneTopicDetail(reading: DailyTopicFortune) {
    val visual = visualFor(reading.topic)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(visual.color.copy(alpha = 0.09f))
            .border(1.dp, visual.color.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(visual.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.72f))
                    .padding(5.dp),
                contentScale = ContentScale.Fit
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${visual.title} 오늘의 운세", color = visual.color, style = MaterialTheme.typography.labelLarge)
                Text(reading.message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun DailyFortuneTopicCard(reading: DailyTopicFortune, focused: Boolean, onClick: () -> Unit) {
    val visual = visualFor(reading.topic)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val lift by animateDpAsState(if (pressed) 8.dp else if (focused) 5.dp else 0.dp, label = "dailyCardLift")
    val scale by animateFloatAsState(if (pressed) 1.04f else if (focused) 1.02f else 0.97f, label = "dailyCardScale")
    val shadow by animateDpAsState(if (focused || pressed) 14.dp else 4.dp, label = "dailyCardShadow")

    Column(
        modifier = Modifier
            .width(132.dp)
            .height(160.dp)
            .offset(y = -lift)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(shadow, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        visual.color.copy(alpha = if (focused || pressed) 0.28f else 0.20f),
                        visual.color.copy(alpha = if (focused || pressed) 0.16f else 0.11f),
                        Color.White.copy(alpha = 0.86f)
                    )
                )
            )
            .border(1.dp, visual.color.copy(alpha = if (focused || pressed) 0.56f else 0.30f), RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(visual.word, color = visual.color, style = MaterialTheme.typography.labelLarge)
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.72f))
                .border(1.dp, visual.color.copy(alpha = 0.30f), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(visual.imageRes),
                contentDescription = "${visual.title} 운세",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(visual.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(visual.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun visualFor(topic: DailyFortuneTopic): DailyTopicVisual = when (topic) {
    DailyFortuneTopic.LOVE -> DailyTopicVisual("LOVE", "연애", "오늘의 온도", Rose, R.drawable.numerology_love)
    DailyFortuneTopic.WORK -> DailyTopicVisual("WORK", "일과 진로", "오늘의 방향", Accent, R.drawable.numerology_career)
    DailyFortuneTopic.MONEY -> DailyTopicVisual("MONEY", "돈", "오늘의 관리", Mint, R.drawable.numerology_money)
    DailyFortuneTopic.STUDY -> DailyTopicVisual("STUDY", "배움", "오늘의 집중", Gold, R.drawable.numerology_study)
    DailyFortuneTopic.SELF -> DailyTopicVisual("SELF", "나 자신", "오늘의 중심", Color(0xFF0891B2), R.drawable.numerology_self)
}

private data class DailyTopicVisual(
    val word: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    @param:DrawableRes val imageRes: Int
)
