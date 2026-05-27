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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.ui.components.DateInputRow
import com.example.unum.ui.components.GenderSelector
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotLoadingCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
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
fun InputScreen(viewModel: AppViewModel, onCalculated: () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    MysticBackground(modifier = Modifier.fillMaxSize()) {
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
                    Text("생년월일 정보", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                    Text("1/3", color = Accent, style = MaterialTheme.typography.labelLarge)
                }
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Color(0xFFFFFCF5), borderColor = Border, contentPadding = 20) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("생년월일 정보를 입력해주세요", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                            Text("수리의 운세노트가 읽을 첫 숫자입니다.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(Surface2)
                                .border(1.dp, Border, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("양력 입력은 내부에서 음력 기준으로 변환해 계산합니다.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        uiState.inputError?.let {
                            Text(it, color = Rose, style = MaterialTheme.typography.bodySmall)
                        }
                        if (uiState.isLoading) {
                            MascotLoadingCard("수리가 숫자 리포트를 정리하고 있어요")
                        } else {
                            GradientButton(
                                text = "결과 보기",
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

@Composable
private fun FormLabel(text: String) {
    Text(text, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
}
