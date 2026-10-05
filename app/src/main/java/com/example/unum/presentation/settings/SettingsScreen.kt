package com.example.unum.presentation.settings

import com.example.unum.presentation.*

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.AuthState
import com.example.unum.data.model.AuthUser
import com.example.unum.data.model.ReaderFontScale
import com.example.unum.ui.components.AppTopBar
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SettingsRow
import com.example.unum.ui.components.SettingsSwitchRow
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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
                AppTopBar(
                    title = "마이페이지",
                    subtitle = "프로필과 앱 설정을 관리하세요"
                )
            }
            item { MyPageProfileCard(uiState = uiState) }
            item { MyNumbersCard(uiState = uiState) }
            item {
                AccountCard(
                    authState = uiState.authState,
                    activity = activity,
                    onKakaoLogin = viewModel::signInWithKakao,
                    onLogout = viewModel::signOut
                )
            }
            item {
                SettingsSwitchRow(
                    title = "알림 받기",
                    subtitle = "매일 오늘의 핵심수를 알려드려요.",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }
            item { SectionTitle("읽기 설정") }
            item { FontScaleSelector(uiState.readerFontScale, viewModel::setReaderFontScale) }
            item {
                SettingsRow(
                    title = "문의하기",
                    subtitle = "오류 제보와 기능 의견을 보내주세요.",
                    accentColor = Accent
                )
            }
            item {
                SettingsRow(
                    title = "결제 복원",
                    subtitle = "이전에 구매한 프리미엄 이용권을 확인합니다."
                )
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun MyPageProfileCard(uiState: AppUiState) {
    val user = (uiState.authState as? AuthState.SignedIn)?.user
    val bundle = uiState.latestBundle
    val birthLabel = bundle?.displayInput?.let {
        "${it.year}.${it.month}.${it.day} · ${it.gender.label}"
    } ?: "생년월일 미입력"
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Color.Transparent,
        borderColor = Color.Transparent,
        contentPadding = 0
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Gold.copy(alpha = 0.22f), Color(0xFF0A0B1A), Color(0xFF060710))),
                    RoundedCornerShape(18.dp)
                )
                .background(Color.Transparent)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Gold.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(MascotArt.Settings),
                                contentDescription = "내 프로필을 안내하는 수리",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(user?.displayName ?: "운세노트 사용자", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(birthLabel, color = Color.White.copy(alpha = 0.74f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(
                        "프리미엄",
                        color = Gold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .background(Gold.copy(alpha = 0.13f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat("저장된 리포트", uiState.savedBooks.size.toString())
                    ProfileStat("이번달 분석", uiState.recentSearches.size.toString())
                    ProfileStat("연속 방문", "7일")
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color.White.copy(alpha = 0.70f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MyNumbersCard(uiState: AppUiState) {
    val numbers = uiState.latestBundle?.numbers
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = Accent)
                Text("나의 수리 번호", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                NumberTile("핵심수", numbers?.destiny?.toString() ?: "?", Gold, Modifier.weight(1f))
                NumberTile("초년수", numbers?.early?.toString() ?: "?", Color(0xFFA78BFA), Modifier.weight(1f))
                NumberTile("중년수", numbers?.middle?.toString() ?: "?", Accent, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NumberTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, color = color, style = MaterialTheme.typography.titleLarge)
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AccountCard(
    authState: AuthState,
    activity: Activity?,
    onKakaoLogin: (Activity) -> Unit,
    onLogout: () -> Unit
) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface2,
        borderColor = Border,
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("계정", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(
                "로그인하면 새로 만든 프리미엄 책자가 자동으로 계정에 저장됩니다.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            when (authState) {
                AuthState.SignedOut -> LoginButtons(activity, onKakaoLogin)
                is AuthState.SignedIn -> SignedInPanel(
                    user = authState.user,
                    onLogout = onLogout
                )
            }

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
    onLogout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsRow(
            title = "로그인됨",
            subtitle = "${user.displayName}님 · ${user.provider.label} 계정",
            accentColor = Accent
        )
        SecondaryActionChip(
            text = "로그아웃",
            modifier = Modifier.fillMaxWidth(),
            tone = Rose,
            onClick = onLogout
        )
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
