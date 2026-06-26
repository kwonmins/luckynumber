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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.PremiumMode
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun PaymentScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var monthly by rememberSaveable { mutableStateOf(true) }
    val modeLabel = if (uiState.premiumMode == PremiumMode.COMPATIBILITY) "궁합노트" else "운세노트"

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "뒤로",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onBack)
                        .padding(4.dp)
                )
                Column {
                    Text("프리미엄 시작하기", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                    Text("지금 구독하고 나만의 맞춤 비책을 받아보세요", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlanOption(
                    title = "월간 구독",
                    subtitle = "매월 · 언제든 해지 가능",
                    price = "₩4,900",
                    badge = "인기",
                    selected = monthly,
                    onClick = { monthly = true }
                )
                PlanOption(
                    title = "단건 구매",
                    subtitle = "$modeLabel 리포트 1건",
                    price = "₩2,900",
                    badge = null,
                    selected = !monthly,
                    onClick = { monthly = false }
                )
            }

            SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("포함된 기능", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                    FeatureLine("무제한 프리미엄 책자 제작")
                    FeatureLine("맞춤 핵심 질문 & 비책")
                    FeatureLine("전체 인생 흐름 리포트")
                    FeatureLine("리포트 무제한 저장")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TrustBadge(Icons.Rounded.Security, "안전 결제", Modifier.weight(1f))
                TrustBadge(Icons.Rounded.Lock, "개인정보 보호", Modifier.weight(1f))
                TrustBadge(Icons.Rounded.CreditCard, "언제든 해지", Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = if (monthly) "월 ₩4,900으로 시작하기" else "₩2,900 결제하기",
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlanOption(
    title: String,
    subtitle: String,
    price: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    SurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalColor = if (selected) Accent.copy(alpha = 0.08f) else Surface,
        borderColor = if (selected) Accent else Border,
        contentPadding = 14
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(1.dp, if (selected) Accent else Border, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Box(Modifier.size(10.dp).background(Accent, CircleShape))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(title, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                        badge?.let {
                            Text(it, color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.background(Accent, RoundedCornerShape(999.dp)).padding(horizontal = 7.dp, vertical = 2.dp))
                        }
                    }
                    Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(price, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FeatureLine(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(22.dp).background(Accent.copy(alpha = 0.10f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Verified, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
        }
        Text(text, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TrustBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Surface2.copy(alpha = 0.78f), RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        Text(text, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}
