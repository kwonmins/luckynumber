package com.example.unum.presentation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.PremiumMode
import com.example.unum.ui.components.AppHeader
import com.example.unum.ui.components.DateInputRow
import com.example.unum.ui.components.FortuneBookCover
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MascotLoadingCard
import com.example.unum.ui.components.MiniNumerologySummary
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToggleSegment
import com.example.unum.ui.components.TopicChipGroup
import com.example.unum.ui.components.premiumTopicMascot
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import com.example.unum.ui.theme.Mint

@Composable
fun PremiumScreen(
    viewModel: AppViewModel,
    onRequestPersonalConsultation: () -> Unit,
    onRequestCompatibilityConsultation: () -> Unit,
    onOpenBook: (FortuneBook) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bundle = uiState.latestBundle
    val latestPersonalBook = uiState.savedBooks.firstOrNull { it.bookType == FortuneBookType.PERSONAL }
    val latestCompatibilityBook = uiState.savedBooks.firstOrNull { it.bookType == FortuneBookType.COMPATIBILITY }
    val previewBook = when (uiState.premiumMode) {
        PremiumMode.PERSONAL -> latestPersonalBook
        PremiumMode.COMPATIBILITY -> latestCompatibilityBook
    }
    MysticBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(18.dp)) }
            item {
                AppHeader(
                    title = if (uiState.premiumMode == PremiumMode.PERSONAL) "수리의 운세노트" else "수리의 궁합노트",
                    subtitle = if (uiState.premiumMode == PremiumMode.PERSONAL) {
                        "무료 성향을 바탕으로 상황별 해석, 주의할 장면, 이번 달 행동을 책자처럼 정리해요."
                    } else {
                        "두 사람의 성향이 실제 관계에서 어떻게 살아나고 부딪히는지 노트처럼 정리해요."
                    },
                    eyebrow = "프리미엄 운세노트"
                )
            }
            item {
                PremiumModeSelector(
                    selected = uiState.premiumMode,
                    onSelected = viewModel::setPremiumMode
                )
            }
            item {
                FortuneNoteIntroCard(isCompatibility = uiState.premiumMode == PremiumMode.COMPATIBILITY)
            }
            if (uiState.premiumMode == PremiumMode.PERSONAL) {
                if (bundle == null) {
                    item {
                        MascotGuideCard(
                            title = "먼저 무료 리포트가 필요합니다",
                            message = "운세노트는 기본 숫자 리포트를 바탕으로 이어집니다. 입력 화면에서 무료 리포트를 먼저 생성하세요.",
                            imageRes = premiumTopicMascot(uiState.premiumTopic)
                        )
                    }
                } else {
                    item {
                        SectionTitle("운세노트에 담을 고민")
                    }
                    item { MiniNumerologySummary(bundle.numbers.destiny, bundle.numbers.early, bundle.numbers.middle, bundle.numbers.late) }
                    item { TopicChipGroup(uiState.premiumTopic, viewModel::selectPremiumTopic) }
                    item { SectionCaption("연애, 일, 돈, 관계처럼 실제로 궁금한 장면을 짧게 적어주세요.") }
                    item {
                        PremiumTextField(
                            value = uiState.premiumConcern,
                            onValueChange = viewModel::updatePremiumConcern,
                            placeholder = "지금 가장 궁금한 고민을 적어주세요.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(176.dp),
                            singleLine = false
                        )
                    }
                    item {
                        uiState.inputError?.let {
                            Text(it, color = Rose, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    item {
                        if (uiState.isPremiumLoading) {
                            MascotLoadingCard(
                                "수리가 고민과 숫자 흐름을 운세노트로 정리하고 있어요",
                                imageRes = premiumTopicMascot(uiState.premiumTopic)
                            )
                        } else {
                            GradientButton("운세노트 만들기", onRequestPersonalConsultation, Modifier.fillMaxWidth())
                        }
                    }
                }
            } else {
                item {
                    MascotGuideCard(
                        title = "궁합 분석 방식",
                        message = "남자와 여자의 숫자를 따로 읽고, 둘 사이에 생기는 관계 숫자를 다시 분석합니다.",
                        imageRes = com.example.unum.ui.components.MascotArt.Premium
                    )
                }
                item {
                    PartnerBirthCard(
                        title = "남자 생년월일",
                        calendarType = uiState.compatibilityForm.male.calendarType,
                        year = uiState.compatibilityForm.male.year,
                        month = uiState.compatibilityForm.male.month,
                        day = uiState.compatibilityForm.male.day,
                        onCalendarSelected = viewModel::setCompatibilityMaleCalendarType,
                        onYearChange = viewModel::updateCompatibilityMaleYear,
                        onMonthChange = viewModel::updateCompatibilityMaleMonth,
                        onDayChange = viewModel::updateCompatibilityMaleDay
                    )
                }
                item {
                    PartnerBirthCard(
                        title = "여자 생년월일",
                        calendarType = uiState.compatibilityForm.female.calendarType,
                        year = uiState.compatibilityForm.female.year,
                        month = uiState.compatibilityForm.female.month,
                        day = uiState.compatibilityForm.female.day,
                        onCalendarSelected = viewModel::setCompatibilityFemaleCalendarType,
                        onYearChange = viewModel::updateCompatibilityFemaleYear,
                        onMonthChange = viewModel::updateCompatibilityFemaleMonth,
                        onDayChange = viewModel::updateCompatibilityFemaleDay
                    )
                }
                item { SectionTitle("추가로 궁금한 점") }
                item { SectionCaption("선택 입력이에요. 연락, 결혼, 생활 리듬처럼 보고 싶은 장면을 적어주세요.") }
                item {
                    PremiumTextField(
                        value = uiState.compatibilityConcern,
                        onValueChange = viewModel::updateCompatibilityConcern,
                        placeholder = "더 보고 싶은 궁금한 점이 있다면 적어주세요.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(152.dp),
                        singleLine = false
                    )
                }
                item {
                    uiState.inputError?.let {
                        Text(it, color = Rose, style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    if (uiState.isPremiumLoading) {
                            MascotLoadingCard(
                            "수리가 두 사람의 성향과 관계 흐름을 노트로 정리하고 있어요",
                            imageRes = com.example.unum.ui.components.MascotArt.Premium
                        )
                    } else {
                        GradientButton("궁합노트 만들기", onRequestCompatibilityConsultation, Modifier.fillMaxWidth())
                    }
                }
                item {
                    SecondaryButton("궁합 입력 지우기", viewModel::clearCompatibilityInput, Modifier.fillMaxWidth())
                }
            }

            previewBook?.let { book ->
                item { SectionTitle(if (uiState.premiumMode == PremiumMode.PERSONAL) "최근 운세노트" else "최근 궁합노트") }
                item {
                    FortuneBookCover(
                        book = book,
                        compact = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SecondaryButton(if (book.bookType == FortuneBookType.COMPATIBILITY) "궁합노트 펼치기" else "노트 펼치기", onClick = { onOpenBook(book) }, modifier = Modifier.fillMaxWidth())
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun FortuneNoteIntroCard(isCompatibility: Boolean) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Accent.copy(alpha = 0.18f), contentPadding = 18) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Accent.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("수", color = Accent, style = MaterialTheme.typography.labelLarge)
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                    Text(if (isCompatibility) "궁합노트에 담을 질문" else "운세노트에 담을 고민", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (isCompatibility) {
                            "연락, 서운함, 생활 리듬처럼 실제 관계 장면으로 풀어냅니다."
                        } else {
                            "무료 결과에서 끝나지 않고, 고민별 케이스와 방치 시 손해까지 짚습니다."
                        },
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                QuickTopic("연애", Rose, Modifier.weight(1f))
                QuickTopic("일", Accent, Modifier.weight(1f))
                QuickTopic("돈", Mint, Modifier.weight(1f))
                QuickTopic("관계", Accent, Modifier.weight(1f))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface2)
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("노트 문장 예시", color = Accent, style = MaterialTheme.typography.labelMedium)
                    Text("그냥 넘기면 좋은 흐름도 피곤한 관계나 버티는 생활로 바뀔 수 있습니다.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun QuickTopic(text: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PremiumModeSelector(
    selected: PremiumMode,
    onSelected: (PremiumMode) -> Unit
) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), contentPadding = 6, tonalColor = Surface2) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PremiumModeTab(
                text = PremiumMode.PERSONAL.label,
                selected = selected == PremiumMode.PERSONAL,
                onClick = { onSelected(PremiumMode.PERSONAL) },
                modifier = Modifier.weight(1f)
            )
            PremiumModeTab(
                text = PremiumMode.COMPATIBILITY.label,
                selected = selected == PremiumMode.COMPATIBILITY,
                onClick = { onSelected(PremiumMode.COMPATIBILITY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.PremiumModeTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Accent.copy(alpha = 0.16f) else Surface)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) TextPrimary else TextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun PartnerBirthCard(
    title: String,
    calendarType: com.example.unum.data.model.CalendarType,
    year: String,
    month: String,
    day: String,
    onCalendarSelected: (com.example.unum.data.model.CalendarType) -> Unit,
    onYearChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onDayChange: (String) -> Unit
) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, contentPadding = 20) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                SectionCaption("양력을 넣으면 내부에서 음력으로 바꿔 계산하고, 음력을 넣으면 그대로 계산해요.")
            }
            ToggleSegment(calendarType, onCalendarSelected)
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

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        placeholder = { Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
        singleLine = singleLine,
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
