package com.example.unum.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.example.unum.data.model.ReaderFontScale
import com.example.unum.ui.components.AppHeader
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SettingsRow
import com.example.unum.ui.components.SettingsSwitchRow
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
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
                    title = "설정",
                    subtitle = "읽기 환경과 앱 정보를 정리합니다.",
                    eyebrow = "설정"
                )
            }
            item {
                SettingsSwitchRow(
                    title = "알림 받기",
                    subtitle = "새로운 리포트 흐름이나 읽기 리마인드를 알려드릴 수 있어요.",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }
            item { SectionTitle("읽기 설정") }
            item { FontScaleSelector(uiState.readerFontScale, viewModel::setReaderFontScale) }
            item {
                SettingsRow(
                    title = "문의하기",
                    subtitle = "오류 제보나 기능 의견을 정리해두는 자리예요.",
                    accentColor = Accent
                )
            }
            item {
                SettingsRow(
                    title = "결제 복원",
                    subtitle = "추후 결제 연동 시 이 위치에서 복원 흐름을 연결합니다."
                )
            }
            item {
                MascotGuideCard(
                    title = "개발자 이야기",
                    message = "숫자를 과한 예언이 아니라 성향과 선택을 정리하는 도구로 다루는 앱입니다.",
                    imageRes = MascotArt.Settings
                )
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun FontScaleSelector(selected: ReaderFontScale, onSelected: (ReaderFontScale) -> Unit) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface2,
        borderColor = Border,
        contentPadding = 16
    ) {
        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("글자 크기", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text("리포트 읽기 화면에 바로 반영돼요.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ReaderFontScale.entries.forEach { scale ->
                    val isSelected = scale == selected
                    SurfaceCard(
                        modifier = Modifier.weight(1f),
                        tonalColor = if (isSelected) Accent.copy(alpha = 0.16f) else Surface2,
                        borderColor = if (isSelected) Accent.copy(alpha = 0.42f) else Border,
                        contentPadding = 0
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { onSelected(scale) },
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(scale.label, color = if (isSelected) TextPrimary else TextSecondary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
