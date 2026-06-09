package com.example.unum.presentation

import android.app.Activity
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.AuthState
import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.ReaderFontScale
import com.example.unum.data.model.UserSyncState
import com.example.unum.ui.components.AppHeader
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SettingsRow
import com.example.unum.ui.components.SettingsSwitchRow
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val activity = LocalContext.current as? Activity

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
                    subtitle = "계정, 책자 동기화, 읽기 환경을 관리합니다.",
                    eyebrow = "MY NOTE"
                )
            }
            item {
                AccountCard(
                    authState = uiState.authState,
                    syncState = uiState.userSyncState,
                    activity = activity,
                    onKakaoLogin = viewModel::signInWithKakao,
                    onSync = viewModel::syncCurrentUserBooks,
                    onLogout = viewModel::signOut
                )
            }
            item {
                SettingsSwitchRow(
                    title = "알림 받기",
                    subtitle = "새 리포트 흐름이나 읽기 리마인드를 받을 수 있게 준비해둡니다.",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }
            item { SectionTitle("읽기 설정") }
            item { FontScaleSelector(uiState.readerFontScale, viewModel::setReaderFontScale) }
            item {
                SettingsRow(
                    title = "문의하기",
                    subtitle = "오류 제보와 기능 의견을 정리해둘 자리입니다.",
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
                    title = "개발 메모",
                    message = "계정 데이터는 저장소 인터페이스를 통해 흐르므로, Supabase에서 라즈베리파이 서버 DB로 바꿀 때 화면 코드는 거의 건드리지 않게 설계했습니다.",
                    imageRes = MascotArt.Settings
                )
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun AccountCard(
    authState: AuthState,
    syncState: UserSyncState,
    activity: Activity?,
    onKakaoLogin: (Activity) -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit
) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface2,
        borderColor = Border,
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("계정 동기화", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(
                "카카오로 로그인하면 만든 책자를 사용자별 DB에 저장할 수 있습니다.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            when (authState) {
                AuthState.SignedOut -> LoginButtons(activity, onKakaoLogin)
                is AuthState.SignedIn -> SignedInPanel(
                    user = authState.user,
                    syncState = syncState,
                    onSync = onSync,
                    onLogout = onLogout
                )
            }

            SyncMessage(syncState)
        }
    }
}

@Composable
private fun LoginButtons(
    activity: Activity?,
    onKakaoLogin: (Activity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GradientButton(
            text = "카카오로 시작하기",
            onClick = { activity?.let(onKakaoLogin) },
            modifier = Modifier.fillMaxWidth(),
            enabled = activity != null
        )
    }
}

@Composable
private fun SignedInPanel(
    user: AuthUser,
    syncState: UserSyncState,
    onSync: () -> Unit,
    onLogout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsRow(
            title = "${user.displayName}님",
            subtitle = "${user.provider.label} 계정으로 연결됨",
            accentColor = Accent
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryActionChip(
                text = if (syncState is UserSyncState.Syncing) "동기화 중" else "책자 동기화",
                modifier = Modifier.weight(1f),
                onClick = onSync
            )
            SecondaryActionChip(
                text = "로그아웃",
                modifier = Modifier.weight(1f),
                tone = Rose,
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun SecondaryActionChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: androidx.compose.ui.graphics.Color = Accent,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = tone, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SyncMessage(syncState: UserSyncState) {
    val message = when (syncState) {
        UserSyncState.Idle -> "로그인 전에는 책자가 이 기기에만 저장됩니다."
        UserSyncState.Syncing -> "계정 정보를 확인하고 책자를 맞추는 중입니다."
        is UserSyncState.Synced -> syncState.message
        is UserSyncState.Failed -> syncState.message
    }
    Text(message, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun FontScaleSelector(selected: ReaderFontScale, onSelected: (ReaderFontScale) -> Unit) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface2,
        borderColor = Border,
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("글자 크기", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text("책자 읽기 화면에 바로 반영됩니다.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ReaderFontScale.entries.forEach { scale ->
                    val isSelected = scale == selected
                    SurfaceCard(
                        modifier = Modifier.weight(1f),
                        tonalColor = if (isSelected) Accent.copy(alpha = 0.16f) else Surface2,
                        borderColor = if (isSelected) Accent.copy(alpha = 0.42f) else Border,
                        contentPadding = 0
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { onSelected(scale) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                scale.label,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
