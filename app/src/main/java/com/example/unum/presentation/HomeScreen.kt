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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.RecentBooksRow
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenInput: () -> Unit,
    onOpenResult: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenBook: (FortuneBook) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val latestBundle = uiState.latestBundle
    val recentBooks = uiState.savedBooks.take(5)

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }
            item { HomeHeader() }
            item {
                RecentReportCard(
                    bundle = latestBundle,
                    onOpenInput = onOpenInput,
                    onOpenResult = onOpenResult
                )
            }
            item { TodayInsightCard(latestBundle) }
            item { TodayFortuneList(latestBundle) }
            if (recentBooks.isNotEmpty()) {
                item { SectionTitle("보관된 노트") }
                item { SectionCaption("저장된 운세노트를 작은 책자 표지로 다시 열어볼 수 있습니다.") }
                item {
                    RecentBooksRow(
                        books = recentBooks,
                        onBookClick = { book ->
                            viewModel.selectSavedBook(book)
                            onOpenBook(book)
                        }
                    )
                }
            } else {
                item { SectionTitle("보관된 노트") }
                item { SectionCaption("운세노트를 만들면 이곳에 책자처럼 쌓입니다.") }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("좋은 하루예요", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            Text("오늘의 운세노트", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Surface)
                .border(1.dp, Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .background(Rose, CircleShape)
            )
        }
    }
}

@Composable
private fun RecentReportCard(
    bundle: NumerologyResultBundle?,
    onOpenInput: () -> Unit,
    onOpenResult: () -> Unit
) {
    SurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (bundle == null) onOpenInput() else onOpenResult() },
        tonalColor = Surface,
        borderColor = Border,
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("최근 리포트", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Text(
                        bundle?.let { "나의 성향 리포트" } ?: "아직 열린 노트가 없어요",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        bundle?.let { "2026.05.27" } ?: "생년월일 입력으로 시작하세요",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = bundle?.numbers?.destiny?.toString() ?: "+",
                    color = Accent,
                    style = MaterialTheme.typography.displayLarge
                )
            }
            NoteProgressBar(progress = if (bundle == null) 0.18f else 0.85f)
            Text(
                text = "리포트 보기",
                color = Accent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun NoteProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Surface2)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Accent)
        )
    }
}

@Composable
private fun HomeNoteButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.20f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(7.dp).background(color, CircleShape))
        }
        Text(text, color = TextPrimary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
}

@Composable
private fun TodayInsightCard(bundle: NumerologyResultBundle?) {
    val text = bundle?.content?.lifeRecord?.summaryText
        ?: "작은 신호를 무시하지 마세요. 커지기 전에 잡는 사람이 결국 유리합니다."
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface2,
        borderColor = Border,
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("오늘의 인사이트", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
        }
    }
}

@Composable
private fun TodayFortuneList(bundle: NumerologyResultBundle?) {
    val destiny = bundle?.numbers?.destiny ?: 7
    val keyword = bundle?.content?.destinyProfile?.title ?: "집중형"
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("오늘의 5대 핵심 영역 운세", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text("운명수 $destiny", color = Accent, style = MaterialTheme.typography.labelMedium)
        }
        FortuneItem(
            title = "연애 관계",
            body = "$keyword 특성 때문에 상대의 반응 하나에 과하게 몰입하면 마음이 먼저 닳을 수 있습니다.",
            color = Rose,
            icon = Icons.Rounded.Favorite
        )
        FortuneItem(
            title = "직장 성취",
            body = "복잡한 일을 끝까지 붙잡는 힘이 살아납니다. 단, 혼자 다 떠안으면 일이 더 피곤해질 수 있습니다.",
            color = Accent,
            icon = Icons.Rounded.Work
        )
        FortuneItem(
            title = "학업 지혜",
            body = "깊게 파는 공부에는 강하지만 범위를 무작정 넓히면 집중력이 흐트러질 수 있습니다.",
            color = Mint,
            icon = Icons.Rounded.School
        )
        FortuneItem(
            title = "금전 흐름",
            body = "불필요한 약속과 충동 결제를 줄이면 지갑이 한결 안정됩니다.",
            color = Gold,
            icon = Icons.Rounded.Payments
        )
        FortuneItem(
            title = "신체 건강",
            body = "무리한 몰입을 방치하면 피로가 몸으로 내려올 수 있으니 눈, 목, 수면을 각별히 챙기세요.",
            color = Color(0xFF0891B2),
            icon = Icons.Rounded.HealthAndSafety
        )
    }
}

@Composable
private fun FortuneItem(title: String, body: String, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .padding(13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(title, color = color, style = MaterialTheme.typography.labelLarge)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
