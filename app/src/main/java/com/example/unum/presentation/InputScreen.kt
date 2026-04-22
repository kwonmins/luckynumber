package com.example.unum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.ui.components.AppHeader
import com.example.unum.ui.components.DateInputRow
import com.example.unum.ui.components.GenderSelector
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MascotLoadingCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToggleSegment
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun InputScreen(viewModel: AppViewModel, onCalculated: () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

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
                    title = "생년월일을 입력해주세요",
                    subtitle = "양력과 음력을 선택한 뒤 숫자의 흐름을 바로 확인할 수 있어요.",
                    eyebrow = "입력"
                )
            }
            item {
                MascotGuideCard(
                    title = "수리의 한마디",
                    message = "입력은 간단하게, 결과는 이해하기 쉽게 보여드릴게요.",
                    imageRes = MascotArt.Input
                )
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = com.example.unum.ui.theme.Surface2, contentPadding = 20) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("기본 정보", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            SectionCaption("입력한 생년월일은 결과 계산에만 사용돼요.")
                        }
                        ToggleSegment(uiState.formState.calendarType, viewModel::setCalendarType)
                        DateInputRow(
                            year = uiState.formState.year,
                            month = uiState.formState.month,
                            day = uiState.formState.day,
                            onYearChange = viewModel::updateYear,
                            onMonthChange = viewModel::updateMonth,
                            onDayChange = viewModel::updateDay
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("성별", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            GenderSelector(uiState.formState.gender, viewModel::setGender)
                        }
                        uiState.inputError?.let {
                            Text(it, color = Rose, style = MaterialTheme.typography.bodySmall)
                        }
                        if (uiState.isLoading) {
                            MascotLoadingCard(
                                "수리가 운명의 수를 정리하고 있어요",
                                imageRes = MascotArt.Input
                            )
                        } else {
                            GradientButton(
                                text = "무료 결과 확인하기",
                                onClick = { viewModel.calculateAndStore(onSuccess = onCalculated) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        SecondaryButton("입력 지우기", viewModel::clearBirthInput, Modifier.fillMaxWidth())
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}
