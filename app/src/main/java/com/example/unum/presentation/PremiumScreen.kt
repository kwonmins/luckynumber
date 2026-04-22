package com.example.unum.presentation

import androidx.compose.foundation.background
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
import com.example.unum.ui.components.PremiumBadge
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SuriAnimatedSceneCard
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
    val speechScript = viewModel.buildCurrentPremiumSpeechScript()

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
                    title = if (uiState.premiumMode == PremiumMode.PERSONAL) "AI 프리미엄 운세" else "AI 궁합",
                    subtitle = if (uiState.premiumMode == PremiumMode.PERSONAL) {
                        "무료 결과를 바탕으로 더 길고 정리된 해석을 받아볼 수 있어요."
                    } else {
                        "남자와 여자 생년월일을 넣으면 각자의 기운과 둘 사이의 관계 흐름을 함께 읽어드려요."
                    },
                    eyebrow = "프리미엄"
                )
            }
            item {
                PremiumModeSelector(
                    selected = uiState.premiumMode,
                    onSelected = viewModel::setPremiumMode
                )
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, contentPadding = 18) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PremiumBadge(if (uiState.premiumMode == PremiumMode.PERSONAL) "AI 프리미엄" else "AI 궁합")
                        Text(
                            if (uiState.premiumMode == PremiumMode.PERSONAL) "더 길고 자세한 해석" else "두 사람의 관계 흐름 정리",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (uiState.premiumMode == PremiumMode.PERSONAL) {
                                "월별 흐름, 조심할 포인트, 다시 읽기 좋은 장별 정리까지 함께 담아드려요."
                            } else {
                                "남자 기본 기운, 여자 기본 기운, 궁합수, 잘 맞는 점과 부딪히는 지점까지 읽기 좋게 정리해드려요."
                            },
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item {
                SuriAnimatedSceneCard(
                    mode = uiState.premiumMode,
                    topic = uiState.premiumTopic,
                    modifier = Modifier.fillMaxWidth(),
                    speechScript = speechScript
                )
            }

            if (uiState.premiumMode == PremiumMode.PERSONAL) {
                if (bundle == null) {
                    item {
                        MascotGuideCard(
                            title = "먼저 무료 결과를 확인해주세요",
                            message = "개인 프리미엄 운세는 무료 결과를 바탕으로 더 깊게 이어집니다. 먼저 생년월일로 기본 흐름을 확인해보세요.",
                            imageRes = premiumTopicMascot(uiState.premiumTopic)
                        )
                    }
                } else {
                    item {
                        MascotGuideCard(
                            title = "수리의 안내",
                            message = "한 문장으로 적어도 괜찮아요. 고민의 결만 보여도 숫자의 흐름과 함께 정리해볼 수 있어요.",
                            imageRes = premiumTopicMascot(uiState.premiumTopic)
                        )
                    }
                    item { MiniNumerologySummary(bundle.numbers.destiny, bundle.numbers.early, bundle.numbers.middle, bundle.numbers.late) }
                    item { SectionTitle("고민 분야") }
                    item { TopicChipGroup(uiState.premiumTopic, viewModel::selectPremiumTopic) }
                    item { SectionCaption("예: 올해 연애를 시작할 수 있을까요, 이직을 준비해도 괜찮을까요") }
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
                                "수리가 고민과 숫자의 흐름을 함께 정리하고 있어요",
                                imageRes = premiumTopicMascot(uiState.premiumTopic)
                            )
                        } else {
                            GradientButton("광고 보고 AI 프리미엄 운세 확인하기", onRequestPersonalConsultation, Modifier.fillMaxWidth())
                        }
                    }
                }
            } else {
                item {
                    MascotGuideCard(
                        title = "궁합 읽는 방식",
                        message = "남자와 여자의 운명수를 먼저 읽고, 그다음 둘 사이에 생기는 궁합수를 따로 읽어드려요.",
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
                item { SectionCaption("선택 입력이에요. 예: 연애로 이어질 수 있을까요, 결혼까지 보아도 괜찮을까요") }
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
                            "수리가 두 사람의 기운과 관계 흐름을 함께 정리하고 있어요",
                            imageRes = com.example.unum.ui.components.MascotArt.Premium
                        )
                    } else {
                        GradientButton("광고 보고 AI 궁합 확인하기", onRequestCompatibilityConsultation, Modifier.fillMaxWidth())
                    }
                }
                item {
                    SecondaryButton("궁합 입력 지우기", viewModel::clearCompatibilityInput, Modifier.fillMaxWidth())
                }
            }

            previewBook?.let { book ->
                item { SectionTitle(if (uiState.premiumMode == PremiumMode.PERSONAL) "방금 생성된 프리미엄 운세" else "방금 생성된 궁합") }
                item {
                    FortuneBookCover(
                        book = book,
                        compact = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SecondaryButton("자세히 읽기", onClick = { onOpenBook(book) }, modifier = Modifier.fillMaxWidth())
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
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
