package com.example.unum.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.DailyFortuneResult
import com.example.unum.data.model.DailyFortuneTopic
import com.example.unum.data.model.DailyTopicFortune
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
fun TodayFortuneCard(
    result: DailyFortuneResult?,
    onOpenInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(30.dp)
    val summary = result?.coreSummary
        ?: "생년월일을 알려주면 오늘의 날짜와 나의 숫자를 함께 읽어드려요."

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, clip = false, spotColor = DeepNavy.copy(alpha = 0.20f))
            .clip(shape)
            .background(DeepNavy)
            .then(if (result == null) Modifier.clickable(onClick = onOpenInput) else Modifier)
            .padding(22.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.035f),
                radius = size.width * 0.46f,
                center = Offset(size.width * 0.98f, size.height * 0.02f)
            )
            drawCircle(
                color = Gold.copy(alpha = 0.24f),
                radius = size.width * 0.27f,
                center = Offset(size.width * 0.92f, size.height * 0.08f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        "오늘의 수리력",
                        color = Gold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        result?.coreTitle ?: "오늘의 결을 준비해요",
                        color = Color(0xFFFFFCF6),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Accent, CircleShape)
                        .border(6.dp, Color.White.copy(alpha = 0.10f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result?.coreNumber?.toString() ?: "?",
                        color = Color(0xFFFFFCF6),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
            Text(summary, color = Color.White.copy(alpha = 0.76f), style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    result?.date?.toString()?.replace("-", ".") ?: "생년월일이 필요해요",
                    color = Color.White.copy(alpha = 0.48f),
                    style = MaterialTheme.typography.bodySmall
                )
                if (result == null) {
                    Text("지금 입력하기  →", color = Gold, style = MaterialTheme.typography.labelLarge)
                } else {
                    Text("매일 자정에 새로 읽어요", color = Gold, style = MaterialTheme.typography.bodySmall)
                }
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
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(24.dp))
                .clickable(onClick = onOpenInput)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("분야별 흐름은 생년월일 입력 후 열려요", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("연애, 일, 돈, 배움, 마음의 흐름을 오늘 기준으로 짧게 정리해드려요.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text("생년월일 입력하기  →", color = Accent, style = MaterialTheme.typography.labelLarge)
            }
        }
        return
    }

    var selectedIndex by remember(result.date) { mutableIntStateOf(0) }
    val selected = result.topics.getOrElse(selectedIndex) { result.topics.first() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 1.dp, vertical = 3.dp)
        ) {
            itemsIndexed(result.topics) { index, reading ->
                TopicChip(
                    reading = reading,
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index }
                )
            }
        }
        TopicDetail(reading = selected)
    }
}

@Composable
private fun TopicChip(
    reading: DailyTopicFortune,
    selected: Boolean,
    onClick: () -> Unit
) {
    val visual = visualFor(reading.topic)
    val background by animateColorAsState(
        if (selected) visual.color else Surface,
        label = "topicChipBackground"
    )
    val elevation by animateDpAsState(if (selected) 5.dp else 0.dp, label = "topicChipElevation")

    Row(
        modifier = Modifier
            .shadow(elevation, RoundedCornerShape(999.dp), clip = false)
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(1.dp, if (selected) visual.color else Border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(
            visual.icon,
            contentDescription = null,
            tint = if (selected) Surface else visual.color,
            modifier = Modifier.size(17.dp)
        )
        Text(
            visual.title,
            color = if (selected) Surface else TextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun TopicDetail(reading: DailyTopicFortune) {
    val visual = visualFor(reading.topic)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(24.dp))
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(visual.color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(visual.icon, contentDescription = null, tint = visual.color, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("${visual.title}의 흐름", color = visual.color, style = MaterialTheme.typography.labelLarge)
            Text(reading.message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun visualFor(topic: DailyFortuneTopic): DailyTopicVisual = when (topic) {
    DailyFortuneTopic.LOVE -> DailyTopicVisual("연애", Rose, Icons.Rounded.Favorite)
    DailyFortuneTopic.WORK -> DailyTopicVisual("일", Blue, Icons.Rounded.Work)
    DailyFortuneTopic.MONEY -> DailyTopicVisual("돈", Mint, Icons.Rounded.Savings)
    DailyFortuneTopic.STUDY -> DailyTopicVisual("배움", Gold, Icons.AutoMirrored.Rounded.MenuBook)
    DailyFortuneTopic.SELF -> DailyTopicVisual("마음", Accent, Icons.Rounded.SelfImprovement)
    DailyFortuneTopic.HEALTH -> DailyTopicVisual("건강", Mint, Icons.Rounded.SelfImprovement)
    DailyFortuneTopic.LUCK -> DailyTopicVisual("행운", Gold, Icons.Rounded.Savings)
}

private data class DailyTopicVisual(
    val title: String,
    val color: Color,
    val icon: ImageVector
)
