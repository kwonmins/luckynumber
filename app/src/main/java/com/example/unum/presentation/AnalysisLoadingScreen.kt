package com.example.unum.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.HomeFormState
import com.example.unum.domain.NumerologyCalculator
import com.example.unum.ui.theme.Accent

@Composable
fun AnalysisLoadingScreen(formState: HomeFormState) {
    val birthInput = NumerologyCalculator.toBirthInput(formState)
    val number = birthInput?.let { NumerologyCalculator.calculate(it).destiny }?.toString() ?: "?"
    val label = birthInput?.let {
        "${it.year}년 ${it.month}월 ${it.day}일 · ${it.gender.label} · ${NumerologyCalculator.calendarLabel(it.calendarType)}"
    } ?: "생년월일을 확인하고 있어요"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            listOf(
                Offset(size.width * 0.16f, size.height * 0.18f),
                Offset(size.width * 0.84f, size.height * 0.28f),
                Offset(size.width * 0.20f, size.height * 0.72f),
                Offset(size.width * 0.78f, size.height * 0.78f)
            ).forEachIndexed { index, offset ->
                drawCircle(
                    color = Color(0xFF93C5FD).copy(alpha = 0.28f - index * 0.03f),
                    radius = (2 + index).dp.toPx(),
                    center = offset
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(128.dp)) {
                    drawCircle(
                        color = Accent.copy(alpha = 0.22f),
                        radius = 60.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Accent.copy(alpha = 0.38f),
                        radius = 46.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .background(Accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(number, color = Color.White, style = MaterialTheme.typography.displayLarge)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("생년월일을 분석하고 있어요", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(label, color = Color(0xFF93C5FD), style = MaterialTheme.typography.bodySmall)
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                LoadingStep("핵심 번호 계산", done = true)
                LoadingStep("성향 패턴 분석", done = true)
                LoadingStep("인생 흐름 매핑", done = false)
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { 0.68f },
                    modifier = Modifier.fillMaxWidth(),
                    color = Accent,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
                Text("잠시만 기다려주세요...", color = Accent.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LoadingStep(text: String, done: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(if (done) Accent else Color.White.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            } else {
                Box(Modifier.size(7.dp).background(Accent.copy(alpha = 0.56f), CircleShape))
            }
        }
        Text(text, color = if (done) Color.White else Color.White.copy(alpha = 0.48f), style = MaterialTheme.typography.bodySmall)
    }
}
