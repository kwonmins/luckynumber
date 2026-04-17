package com.example.unum.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MascotLoadingCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun HomeScreen(viewModel: AppViewModel, onOpenFortune: () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Spacer(Modifier.height(18.dp)) }
            item {
                AppHeader(
                    title = "당신의 운명의 수",
                    subtitle = "양력 생년월일을 알려주시면 수리가 숨은 숫자를 읽어드립니다."
                )
            }
            item {
                MascotGuideCard("평소 알고 있는 양력 생년월일만 입력해주세요. 계산은 보이지 않는 달의 흐름까지 살펴 진행할게요.")
            }
            item { DateInputRow(uiState.formState.year, uiState.formState.month, uiState.formState.day, viewModel::updateYear, viewModel::updateMonth, viewModel::updateDay) }
            item { GenderSelector(uiState.formState.gender, viewModel::setGender) }
            item {
                GradientButton("입력 지우기", viewModel::clearBirthInput, Modifier.fillMaxWidth())
            }
            item {
                uiState.inputError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
                }
            }
            item {
                if (uiState.isLoading) {
                    MascotLoadingCard("수리가 운명을 보고 있습니다")
                } else {
                    GradientButton("운명수 확인하기", { viewModel.calculateAndStore(onSuccess = onOpenFortune) }, Modifier.fillMaxWidth())
                }
            }
            item { SectionTitle("최근 조회") }
            items(uiState.recentSearches) { recent ->
                SurfaceCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.loadRecentSearch(recent, onSuccess = onOpenFortune) },
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(recent.code, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(recent.dateLabel, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text(recent.subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "삭제",
                            color = Rose,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.clickable { viewModel.removeRecentSearch(recent) }
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

