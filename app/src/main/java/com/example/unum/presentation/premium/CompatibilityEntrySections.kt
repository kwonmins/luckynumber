package com.example.unum.presentation.premium

import com.example.unum.presentation.*

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unum.R
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.GenderOption

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun PremiumCompatibilityEntry(
    uiState: AppUiState,
    viewModel: AppViewModel,
    canStart: Boolean,
    onStart: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PremiumEntryPink.copy(alpha = 0.27f), PremiumEntryViolet.copy(alpha = 0.30f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.11f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.66f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("두 사람의 숫자 흐름", color = Color(0xFFF9A8D4), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Text("마음의 거리와\n관계의 타이밍을 읽어요", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 21.sp))
                Text("궁합 분석 · 관계 비책 · 타이밍", color = Color.White.copy(alpha = 0.52f), fontSize = 10.sp)
            }
            Image(
                painter = painterResource(R.drawable.suri_reader_compatibility),
                contentDescription = "궁합을 읽는 수리",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(126.dp)
                    .fillMaxHeight()
                    .padding(top = 5.dp, end = 5.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("관계 유형", color = Color.White.copy(alpha = 0.80f), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                CompatibilityRelationshipStatus.entries.forEach { status ->
                    val selected = uiState.compatibilityForm.relationshipStatus == status
                    val (emoji, label) = relationshipPresentation(status)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (selected) PremiumEntryPink.copy(alpha = 0.31f) else Color.White.copy(alpha = 0.055f))
                            .border(
                                1.dp,
                                if (selected) PremiumEntryPink.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(13.dp)
                            )
                            .clickable { viewModel.setCompatibilityRelationshipStatus(status) }
                            .padding(vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 20.sp)
                        Text(label, color = if (selected) Color.White else Color.White.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        PremiumPartnerInputCard(uiState = uiState, viewModel = viewModel)
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("관계에서 가장 궁금한 점", color = Color.White.copy(alpha = 0.80f), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            PremiumDarkConcernField(
                value = uiState.compatibilityConcern,
                onValueChange = viewModel::updateCompatibilityConcern,
                placeholder = "예) 서로 마음은 있는데 자꾸 어긋나요. 관계가 좋아질 시기를 알고 싶어요..."
            )
            Text("상황과 궁금한 점을 6자 이상 적어주세요.", color = Color.White.copy(alpha = 0.30f), fontSize = 10.sp)
        }
        if (uiState.latestBundle == null) {
            PremiumEntryHint("먼저 내 생년월일 분석을 완료해 주세요.")
        }
        PremiumPrimaryButton(
            text = "✦  궁합 리포트 제작하기",
            onClick = onStart,
            enabled = canStart,
            colors = listOf(PremiumEntryPink, PremiumEntryViolet)
        )
        PremiumTrustLine("🔒  상대방 정보는 분석에만 안전하게 사용돼요")
    }
}

internal fun relationshipPresentation(status: CompatibilityRelationshipStatus): Pair<String, String> = when (status) {
    CompatibilityRelationshipStatus.COUPLE -> "💑" to "연인·배우자"
    CompatibilityRelationshipStatus.CRUSH -> "💌" to "짝사랑"
    CompatibilityRelationshipStatus.REUNION -> "🔁" to "재회"
}

@Composable
internal fun PremiumPartnerInputCard(uiState: AppUiState, viewModel: AppViewModel) {
    val partner = uiState.compatibilityForm.partner
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(15.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("상대방 정보", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Text(
                uiState.latestBundle?.displayInput?.let { "내 정보 ${it.year}.${it.month}.${it.day}" } ?: "내 정보 미입력",
                color = Color.White.copy(alpha = 0.34f),
                fontSize = 10.sp
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(GenderOption.MALE to "남성", GenderOption.FEMALE to "여성").forEach { (gender, label) ->
                val selected = uiState.compatibilityForm.partnerGender == gender
                PremiumCompactChoice(
                    text = label,
                    selected = selected,
                    accent = PremiumEntryPink,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setCompatibilityPartnerGender(gender) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(CalendarType.SOLAR to "양력", CalendarType.LUNAR to "음력").forEach { (calendar, label) ->
                PremiumCompactChoice(
                    text = label,
                    selected = partner.calendarType == calendar,
                    accent = PremiumEntryViolet,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setCompatibilityPartnerCalendarType(calendar) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            PremiumDarkDateField(
                value = partner.year,
                onValueChange = viewModel::updateCompatibilityPartnerYear,
                placeholder = "연도",
                maxLength = 4,
                modifier = Modifier.weight(1.5f)
            )
            PremiumDarkDateField(
                value = partner.month,
                onValueChange = viewModel::updateCompatibilityPartnerMonth,
                placeholder = "월",
                maxLength = 2,
                modifier = Modifier.weight(1f)
            )
            PremiumDarkDateField(
                value = partner.day,
                onValueChange = viewModel::updateCompatibilityPartnerDay,
                placeholder = "일",
                maxLength = 2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

