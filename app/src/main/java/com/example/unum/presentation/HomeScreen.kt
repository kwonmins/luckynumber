package com.example.unum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.unum.data.model.FortuneBook
import com.example.unum.ui.components.AppHeader
import com.example.unum.ui.components.FortuneBookCover
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.PremiumBadge
import com.example.unum.ui.components.RecentBooksRow
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.TodaySummaryCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenInput: () -> Unit,
    onOpenResult: () -> Unit,
    onOpenPremium: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(18.dp)) }
            item {
                AppHeader(
                    title = "숫자로 오늘의 흐름을 정리해볼까요",
                    subtitle = "생년월일을 입력하면 무료 결과를 먼저 보고, 더 궁금한 고민은 AI 상담으로 이어볼 수 있어요.",
                    eyebrow = "홈"
                )
            }
            item {
                SurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    tonalColor = Surface2,
                    contentPadding = 20
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        PremiumBadge("빠른 시작")
                        Text("내 운명수 확인하기", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "복잡한 설명보다 먼저 이해되는 무료 결과를 보여드릴게요.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        GradientButton("생년월일 입력하기", onOpenInput, Modifier.fillMaxWidth())
                        if (latestBundle != null) {
                            SecondaryButton("방금 본 무료 결과 이어보기", onOpenResult, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            item {
                TodaySummaryCard(
                    title = if (latestBundle == null) "오늘의 안내" else "최근 확인한 무료 결과",
                    body = if (latestBundle == null) {
                        "지금은 아직 결과가 없어요. 생년월일을 입력하면 운명수와 초년, 중년, 말년 흐름을 바로 보여드릴게요."
                    } else {
                        "운명수 ${latestBundle.numbers.destiny} · 코드 ${latestBundle.numbers.code}\n${latestBundle.content.lifeRecord.summaryText}"
                    }
                )
            }
            item {
                MascotGuideCard(
                    message = "무료 결과만으로도 흐름을 이해할 수 있게 정리하고, 더 자세한 내용은 부담 없이 AI 상담으로 이어지게 만들었어요.",
                    imageRes = MascotArt.Home
                )
            }
            item {
                SectionTitle("AI 프리미엄 추천")
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, contentPadding = 18) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("지금 고민을 더 자세히 보고 싶다면", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text("월별 흐름, 조심할 포인트, 다시 읽기 좋은 정리까지 한 번에 확인할 수 있어요.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        GradientButton("AI 프리미엄 운세 확인하기", onOpenPremium, Modifier.fillMaxWidth())
                    }
                }
            }
            if (recentBooks.isNotEmpty()) {
                item {
                    SectionTitle("최근 본 운세책")
                }
                item {
                    SectionCaption("최근에 열어본 책부터 다시 이어볼 수 있어요.")
                }
                item {
                    RecentBooksRow(
                        books = recentBooks,
                        onBookClick = { book ->
                            viewModel.selectSavedBook(book)
                            onOpenBook(book)
                        }
                    )
                }
            }
            if (uiState.savedBooks.isNotEmpty()) {
                item { SectionTitle("보관함 미리보기") }
                item {
                    FortuneBookCover(
                        book = uiState.savedBooks.first(),
                        compact = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}
