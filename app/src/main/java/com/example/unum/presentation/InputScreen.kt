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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.ui.components.DateInputRow
import com.example.unum.ui.components.GenderSelector
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotLoadingCard
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onBack)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "뒤로", tint = TextSecondary)
                    }
                    Text("정보 입력", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("1/3", color = Accent, style = MaterialTheme.typography.labelLarge)
                }
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), borderColor = Border, contentPadding = 20) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("생년월일 정보를 입력해주세요", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                            Text("정확할수록 더 좋은 해석을 제공해요.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
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
                            Text("입력하신 정보는 결과 계산과 운세노트 구성에만 사용됩니다.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        uiState.inputError?.let {
                            Text(it, color = Rose, style = MaterialTheme.typography.bodySmall)
                        }
                        if (uiState.isLoading) {
                            MascotLoadingCard("수리가 숫자 리포트를 정리하고 있어요")
                        } else {
                            GradientButton(
                                text = "다음",
                                onClick = { viewModel.calculateAndStore(onSuccess = onCalculated) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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
