package com.example.unum.presentation.input

import com.example.unum.presentation.premium.AnalysisLoadingScreen
import com.example.unum.presentation.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.HomeFormState
import com.example.unum.ui.components.GenderSelector
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToggleSegment
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.DeepNavy
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "뒤로 가기",
                            tint = TextPrimary
                        )
                    }
                    Text("나의 수리 읽기", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                }
            }
            item { InputEditorialHeader() }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InputLabel(number = "01", title = "어떤 달력을 사용하나요?")
                    ToggleSegment(uiState.formState.calendarType, viewModel::setCalendarType)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InputLabel(number = "02", title = "성별을 선택해주세요")
                    GenderSelector(uiState.formState.gender, viewModel::setGender)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InputLabel(number = "03", title = "태어난 날짜를 입력해주세요")
                    SurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        tonalColor = Surface,
                        borderColor = Border,
                        contentPadding = 16
                    ) {
                        OracleBirthKeypad(
                            formState = uiState.formState,
                            onYearChange = viewModel::updateYear,
                            onMonthChange = viewModel::updateMonth,
                            onDayChange = viewModel::updateDay
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface2)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).background(Surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("개인정보는 안전하게 다뤄요", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                        Text("생년월일은 수리 계산에만 사용되며 화면 밖으로 공개되지 않아요.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            uiState.inputError?.let { message ->
                item {
                    Text(
                        message,
                        color = Rose,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            item {
                GradientButton(
                    text = if (canSubmit) "나의 수리 확인하기  →" else "생년월일을 완성해주세요",
                    onClick = { viewModel.calculateAndStore(onSuccess = onCalculated) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun InputEditorialHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(DeepNavy)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("BIRTH NUMBER · 3 STEPS", color = Accent.copy(alpha = 0.92f), style = MaterialTheme.typography.labelMedium)
        Text(
            "태어난 날에는\n나만의 결이 있어요",
            color = Surface,
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            "세 가지 정보만 알려주면 핵심 숫자와 오늘의 흐름을 바로 읽어드릴게요.",
            color = Surface.copy(alpha = 0.68f),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(width = if (index == 0) 38.dp else 18.dp, height = 4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (index == 0) Accent else Color.White.copy(alpha = 0.16f))
                )
            }
        }
    }
}

@Composable
private fun InputLabel(number: String, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Accent.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Accent, style = MaterialTheme.typography.labelMedium)
        }
        Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun OracleBirthKeypad(
    formState: HomeFormState,
    onYearChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onDayChange: (String) -> Unit
) {
    var activeField by rememberSaveable { mutableStateOf("year") }
    val fields = listOf(
        Triple("year", "년", formState.year),
        Triple("month", "월", formState.month),
        Triple("day", "일", formState.day)
    )
    val currentValue = when (activeField) {
        "month" -> formState.month
        "day" -> formState.day
        else -> formState.year
    }
    val maxLength = if (activeField == "year") 4 else 2

    fun update(value: String) {
        when (activeField) {
            "month" -> onMonthChange(value)
            "day" -> onDayChange(value)
            else -> onYearChange(value)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            fields.forEach { (id, label, value) ->
                val selected = activeField == id
                Column(
                    modifier = Modifier.weight(if (id == "year") 1.42f else 1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) DeepNavy else Surface2)
                            .border(1.dp, if (selected) DeepNavy else Border, RoundedCornerShape(18.dp))
                            .clickable { activeField = id },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                value.ifBlank { if (id == "year") "____" else "__" },
                                color = when {
                                    selected && value.isBlank() -> Surface.copy(alpha = 0.38f)
                                    selected -> Surface
                                    value.isBlank() -> TextMuted.copy(alpha = 0.50f)
                                    else -> TextPrimary
                                },
                                fontSize = if (id == "year") 21.sp else 25.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                label,
                                color = if (selected) Surface.copy(alpha = 0.58f) else TextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
        keys.chunked(3).forEach { rowKeys ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (key.isBlank()) Color.Transparent else Surface2)
                            .then(
                                if (key.isBlank()) Modifier
                                else Modifier.clickable {
                                    if (key == "⌫") {
                                        update(currentValue.dropLast(1))
                                    } else {
                                        val next = (currentValue + key).take(maxLength)
                                        update(next)
                                        if (next.length == maxLength) {
                                            activeField = when (activeField) {
                                                "year" -> "month"
                                                "month" -> "day"
                                                else -> "day"
                                            }
                                        }
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            key,
                            color = if (key == "⌫") Accent else TextPrimary,
                            fontSize = if (key == "⌫") 17.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        Text(
            "예시  ·  1999년 03월 13일",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

private fun HomeFormState.isBirthInputComplete(): Boolean {
    return year.length == 4 && month.isNotBlank() && day.isNotBlank()
}
