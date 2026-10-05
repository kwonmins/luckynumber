package com.example.unum.presentation.premium

import com.example.unum.presentation.*

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.PremiumTopic
import com.example.unum.presentation.spec.FeatureSpecs
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun SuriGreetingCard() {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text("수", color = Accent, style = MaterialTheme.typography.titleMedium)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text("안녕하세요. 고민을 말씀해주세요.", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                Text("답답하거나 갈피를 잡기 힘든 중요한 고민을 책자 형태로 정리해드릴게요.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun TopicGrid(selected: PremiumTopic, onSelected: (PremiumTopic) -> Unit) {
    val topics = FeatureSpecs.premiumTopicPlans.map { it.topic }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(topics) { topic ->
            MagneticTopicCard(
                topic = topic,
                selected = selected == topic,
                onClick = { onSelected(topic) }
            )
        }
    }
}

@Composable
internal fun MagneticTopicCard(topic: PremiumTopic, selected: Boolean, onClick: () -> Unit) {
    val lift by animateFloatAsState(
        targetValue = if (selected) -10f else 0f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "topicLift"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 0.96f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "topicScale"
    )
    val glow by animateFloatAsState(
        targetValue = if (selected) 0.38f else 0.08f,
        animationSpec = tween(durationMillis = 360),
        label = "topicGlow"
    )
    Box(
        modifier = Modifier
            .width(128.dp)
            .height(82.dp)
            .graphicsLayer {
                translationY = lift
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (selected) 14.dp else 3.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    if (selected) {
                        listOf(Accent.copy(alpha = 0.24f), Surface.copy(alpha = 0.94f))
                    } else {
                        listOf(Surface2.copy(alpha = 0.86f), Surface.copy(alpha = 0.94f))
                    }
                )
            )
            .border(1.dp, if (selected) Accent.copy(alpha = 0.72f) else Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Accent.copy(alpha = glow),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.86f, size.height * 0.18f)
            )
            drawCircle(
                color = Gold.copy(alpha = glow * 0.55f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.12f, size.height * 0.84f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(topic.bookLabel(), color = if (selected) Accent else TextSecondary, style = MaterialTheme.typography.labelLarge)
            Text(if (selected) "끌림 활성" else "밀어서 선택", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun PremiumConcernField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "해답을 찾고 싶은 상세한 사연이나 사건을 자유롭게 적어주세요."
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
        placeholder = {
            Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Accent,
            unfocusedBorderColor = Border,
            cursorColor = Accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

