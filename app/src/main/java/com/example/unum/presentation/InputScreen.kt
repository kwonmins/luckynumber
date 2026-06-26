package com.example.unum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.HomeFormState
import com.example.unum.ui.components.DateInputRow
import com.example.unum.ui.components.GenderSelector
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToggleSegment
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun InputScreen(
    viewModel: AppViewModel,
    onCalculated: () -> Unit,
    onBack: () -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val canSubmit = uiState.formState.isBirthInputComplete()

    if (uiState.isLoading) {
        AnalysisLoadingScreen(formState = uiState.formState)
        return
    }

    MysticBackground(modifier = Modifier.fillMaxSize(), animatedWaves = true) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "뒤로", tint = TextSecondary)
                    }
                    Text("생년월일 입력", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.size(48.dp))
                }
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), borderColor = Border, contentPadding = 20) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("오늘의 핵심수를 계산할게요", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                            Text("생년월일을 입력하면 성향 리포트를 바로 미리볼 수 있어요.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FormLabel("날짜 기준")
                            ToggleSegment(uiState.formState.calendarType, viewModel::setCalendarType)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FormLabel("성별")
                            GenderSelector(uiState.formState.gender, viewModel::setGender)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FormLabel("생년월일")
                            DateInputRow(
                                year = uiState.formState.year,
                                month = uiState.formState.month,
                                day = uiState.formState.day,
                                onYearChange = viewModel::updateYear,
                                onMonthChange = viewModel::updateMonth,
                                onDayChange = viewModel::updateDay
                            )
                            SectionCaption("예: 1999 / 03 / 13")
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface2)
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("입력하신 정보는 결과 계산과 운세노트 구성에만 사용됩니다.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        uiState.inputError?.let {
                            Text(it, color = Rose, style = MaterialTheme.typography.bodySmall)
                        }
                        GradientButton(
                            text = "결과 미리보기",
                            onClick = { viewModel.calculateAndStore(onSuccess = onCalculated) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canSubmit
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(text, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
}

private fun HomeFormState.isBirthInputComplete(): Boolean {
    return year.length == 4 && month.isNotBlank() && day.isNotBlank()
}
