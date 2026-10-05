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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumMode
import com.example.unum.ui.components.DateInputRow
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToggleSegment
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun PremiumModeSelector(selected: PremiumMode, onSelected: (PremiumMode) -> Unit) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, borderColor = Border, contentPadding = 4) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PremiumModePill(
                title = "운세노트",
                body = "내 생년월일로 제작",
                selected = selected == PremiumMode.PERSONAL,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(PremiumMode.PERSONAL) }
            )
            PremiumModePill(
                title = "궁합노트",
                body = "두 사람의 숫자 흐름 비교",
                selected = selected == PremiumMode.COMPATIBILITY,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(PremiumMode.COMPATIBILITY) }
            )
        }
    }
}

@Composable
internal fun PremiumModePill(
    title: String,
    body: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "premiumModeScale"
    )
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Accent.copy(alpha = 0.16f) else Surface)
            .border(1.dp, if (selected) Accent.copy(alpha = 0.74f) else Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = if (selected) Accent else TextPrimary, style = MaterialTheme.typography.labelLarge)
        Text(body, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun CompatibilityGreetingCard() {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Rose.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text("궁", color = Rose, style = MaterialTheme.typography.titleMedium)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text("안녕하세요. 관계를 알려주세요.", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                Text("두 사람의 흐름을 책자 형태로 정리해드릴게요.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun CompatibilityFormSection(uiState: AppUiState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("관계 유형 선택", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        CompatibilityStatusSelector(
            selected = uiState.compatibilityForm.relationshipStatus,
            onSelected = viewModel::setCompatibilityRelationshipStatus
        )
        Text("상대방 정보 입력", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        MyBirthFixedCard(bundle = uiState.latestBundle)
        PartnerBirthInputCard(
            title = "상대방 정보",
            subtitle = "성별과 생년월일만 입력해 주세요.",
            selectedGender = uiState.compatibilityForm.partnerGender,
            onGenderSelected = viewModel::setCompatibilityPartnerGender,
            calendarType = uiState.compatibilityForm.partner.calendarType,
            year = uiState.compatibilityForm.partner.year,
            month = uiState.compatibilityForm.partner.month,
            day = uiState.compatibilityForm.partner.day,
            onCalendarSelected = viewModel::setCompatibilityPartnerCalendarType,
            onYearChange = viewModel::updateCompatibilityPartnerYear,
            onMonthChange = viewModel::updateCompatibilityPartnerMonth,
            onDayChange = viewModel::updateCompatibilityPartnerDay,
            accentColor = Rose
        )
    }
}

@Composable
internal fun CompatibilityStatusSelector(
    selected: CompatibilityRelationshipStatus,
    onSelected: (CompatibilityRelationshipStatus) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(CompatibilityRelationshipStatus.entries) { status ->
            CompatibilityStatusCard(
                status = status,
                selected = selected == status,
                onClick = { onSelected(status) }
            )
        }
    }
}

@Composable
internal fun CompatibilityStatusCard(
    status: CompatibilityRelationshipStatus,
    selected: Boolean,
    onClick: () -> Unit
) {
    val lift by animateFloatAsState(
        targetValue = if (selected) -10f else 0f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "compatibilityStatusLift"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 0.96f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "compatibilityStatusScale"
    )
    val glow by animateFloatAsState(
        targetValue = if (selected) 0.38f else 0.08f,
        animationSpec = tween(durationMillis = 360),
        label = "compatibilityStatusGlow"
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
                        listOf(Rose.copy(alpha = 0.24f), Surface.copy(alpha = 0.94f))
                    } else {
                        listOf(Surface2.copy(alpha = 0.86f), Surface.copy(alpha = 0.94f))
                    }
                )
            )
            .border(1.dp, if (selected) Rose.copy(alpha = 0.72f) else Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Rose.copy(alpha = glow),
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
            Text(
                status.label,
                color = if (selected) Rose else TextSecondary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(if (selected) "궁합 활성" else "밀어서 선택", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun MyBirthFixedCard(bundle: NumerologyResultBundle?) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface2.copy(alpha = 0.58f),
        borderColor = Border.copy(alpha = 0.52f),
        contentPadding = 12
    ) {
        val input = bundle?.displayInput
        val dateText = input?.let {
            "${calendarLabel(it.calendarType)} ${it.year}.${it.month}.${it.day} · ${genderLabel(it.gender)}"
        } ?: "생년월일을 먼저 입력해 주세요"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("내 정보", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
            Text(dateText, color = TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(TextMuted.copy(alpha = 0.08f))
                    .border(1.dp, TextMuted.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text("고정", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
internal fun PartnerBirthInputCard(
    title: String,
    subtitle: String,
    selectedGender: GenderOption,
    onGenderSelected: (GenderOption) -> Unit,
    calendarType: CalendarType,
    year: String,
    month: String,
    day: String,
    onCalendarSelected: (CalendarType) -> Unit,
    onYearChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    accentColor: Color
) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 12) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PartnerGenderSelector(
                    selected = selectedGender,
                    onSelected = onGenderSelected,
                    accentColor = accentColor,
                    modifier = Modifier.weight(0.95f)
                )
                ToggleSegment(
                    selected = calendarType,
                    onSelected = onCalendarSelected,
                    modifier = Modifier.weight(1.05f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("생년월일", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                DateInputRow(
                    year = year,
                    month = month,
                    day = day,
                    onYearChange = onYearChange,
                    onMonthChange = onMonthChange,
                    onDayChange = onDayChange
                )
            }
        }
    }
}

@Composable
internal fun PartnerGenderSelector(
    selected: GenderOption,
    onSelected: (GenderOption) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier.fillMaxWidth()) {
        listOf(GenderOption.MALE, GenderOption.FEMALE).forEach { gender ->
            val isSelected = selected == gender
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) accentColor.copy(alpha = 0.12f) else Surface2.copy(alpha = 0.72f))
                    .border(1.dp, if (isSelected) accentColor.copy(alpha = 0.68f) else Border, RoundedCornerShape(8.dp))
                    .clickable { onSelected(gender) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(genderLabel(gender), color = if (isSelected) accentColor else TextSecondary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

internal fun calendarLabel(type: CalendarType): String = when (type) {
    CalendarType.SOLAR -> "양력"
    CalendarType.LUNAR -> "음력"
}

internal fun genderLabel(gender: GenderOption): String = when (gender) {
    GenderOption.MALE -> "남성"
    GenderOption.FEMALE -> "여성"
    GenderOption.NONE -> "미선택"
}

