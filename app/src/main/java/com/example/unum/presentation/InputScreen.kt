package com.example.unum.presentation

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.HomeFormState
import com.example.unum.ui.components.GenderSelector
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToggleSegment
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
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
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                        Text(
                            "‹  뒤로",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp)
                        )
                        Text("탄생수 입력", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("당신의 생년월일을\n입력해주세요", color = TextPrimary, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                    }
                    Image(
                        painter = painterResource(MascotArt.Input),
                        contentDescription = "생년월일 입력을 안내하는 수리",
                        modifier = Modifier.size(118.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormLabel("날짜 기준")
                    ToggleSegment(uiState.formState.calendarType, viewModel::setCalendarType)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormLabel("성별")
                    GenderSelector(uiState.formState.gender, viewModel::setGender)
                }
            }
            item {
                OracleBirthKeypad(
                    formState = uiState.formState,
                    onYearChange = viewModel::updateYear,
                    onMonthChange = viewModel::updateMonth,
                    onDayChange = viewModel::updateDay
                )
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, borderColor = Border, contentPadding = 14) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                        Text(
                            "생년월일은 결과 계산에만 사용되며 외부에 공개되지 않아요.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            uiState.inputError?.let { message ->
                item { Text(message, color = Rose, style = MaterialTheme.typography.bodySmall) }
            }
            item {
                GradientButton(
                    text = "수리 계산하기  →",
                    onClick = { viewModel.calculateAndStore(onSuccess = onCalculated) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit
                )
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
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
        Triple("year", "년도", formState.year),
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

    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            fields.forEach { (id, label, value) ->
                val selected = activeField == id
                Column(
                    modifier = Modifier.weight(if (id == "year") 1.45f else 1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(label, color = if (selected) Accent else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (selected) Accent.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (selected) Accent.copy(alpha = 0.68f) else Border, RoundedCornerShape(13.dp))
                            .clickable { activeField = id },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            value.ifBlank { if (id == "year") "____" else "__" },
                            color = if (value.isBlank()) TextMuted.copy(alpha = 0.45f) else TextPrimary,
                            fontSize = if (id == "year") 18.sp else 23.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (selected) Accent else Color.Transparent)
                    )
                }
            }
        }

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
        keys.chunked(3).forEach { rowKeys ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (key.isBlank()) Color.Transparent else Color.White.copy(alpha = 0.065f))
                            .then(
                                if (key.isBlank()) Modifier
                                else Modifier
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(13.dp))
                                    .clickable {
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
                            color = if (key == "⌫") Rose else TextPrimary,
                            fontSize = if (key == "⌫") 17.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        Text("예: 1999 / 03 / 13", color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(text, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
}

private fun HomeFormState.isBirthInputComplete(): Boolean {
    return year.length == 4 && month.isNotBlank() && day.isNotBlank()
}
