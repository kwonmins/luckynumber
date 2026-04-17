package com.example.unum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MascotLoadingCard
import com.example.unum.ui.components.MiniNumerologySummary
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.PremiumResultBox
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.TopicChipGroup
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun PremiumScreen(viewModel: AppViewModel, onRequestConsultation: () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bundle = uiState.latestBundle

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        if (bundle == null) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.Center) {
                item { Text("먼저 운세 결과를 확인해주세요.", color = TextSecondary, style = MaterialTheme.typography.bodyLarge) }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item { Spacer(Modifier.height(18.dp)) }
                item { SectionTitle("프리미엄 운세 상담") }
                item {
                    Text(
                        "숫자의 흐름과 지금의 고민을 함께 읽어, 당신에게 필요한 방향을 정리합니다.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                item {
                    MascotGuideCard("고민을 편하게 적어주세요. 수리가 운명의 숫자와 월별 흐름을 함께 살펴볼게요.")
                }
                item { MiniNumerologySummary(bundle.numbers.destiny, bundle.numbers.early, bundle.numbers.middle, bundle.numbers.late) }
                item { SectionTitle("고민 분야") }
                item { TopicChipGroup(uiState.premiumTopic, viewModel::selectPremiumTopic) }
                item {
                    PremiumTextField(
                        value = uiState.premiumConcern,
                        onValueChange = viewModel::updatePremiumConcern,
                        placeholder = "지금 마음에 걸리는 고민을 적어주세요.",
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        singleLine = false
                    )
                }
                item {
                    uiState.inputError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    if (uiState.isPremiumLoading) {
                        MascotLoadingCard("수리가 운명을 보고 있습니다")
                    } else {
                        GradientButton("광고 보고 상담 요청하기", onRequestConsultation, Modifier.fillMaxWidth())
                    }
                }
                uiState.premiumResult?.let { item { PremiumResultBox(it) } }
                item { Spacer(Modifier.height(90.dp)) }
            }
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Accent.copy(alpha = 0.28f),
            unfocusedBorderColor = Border,
            cursorColor = Accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

