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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBook
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.RecentBooksRow
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("오늘의 운세노트", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("내 숫자로 조용히 열어보는 오늘의 기록", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("수", color = Accent, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Color(0xFFFFFCF5), borderColor = Border, contentPadding = 18) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("최근 노트", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    latestBundle?.let { "나의 성향 리포트" } ?: "아직 열린 노트가 없어요",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    latestBundle?.let { "운명수 ${it.numbers.destiny} · 정리율 60%" } ?: "생년월일 입력으로 시작하세요",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(Accent.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(latestBundle?.numbers?.destiny?.toString() ?: "+", color = Accent, style = MaterialTheme.typography.displayMedium)
                            }
                        }
                        latestBundle?.let {
                            Text(it.content.lifeRecord.oneLineAdvice, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NoteProgressBar(progress = 0.60f, modifier = Modifier.weight(1f))
                                Text("리포트 보기", color = Accent, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 10.dp))
                            }
                        } ?: Text(
                            "무료 리포트를 먼저 열면 운세노트가 이어집니다.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            HomeNoteButton("무료\n리포트", Accent, onOpenInput, Modifier.weight(1f))
                            HomeNoteButton("운세\n노트", Blue, onOpenPremium, Modifier.weight(1f))
                            HomeNoteButton("보관함", Mint, onOpenLibrary, Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickMenuCard("입력", "무료", Accent, onOpenInput, Modifier.weight(1f))
                    QuickMenuCard("결과", "요약", Blue, onOpenResult, Modifier.weight(1f))
                    QuickMenuCard("상담", "노트", Mint, onOpenPremium, Modifier.weight(1f))
                    QuickMenuCard("기록", "보관", Rose, onOpenLibrary, Modifier.weight(1f))
                }
            }
            if (recentBooks.isNotEmpty()) {
                item { SectionTitle("보관된 노트") }
                item { SectionCaption("저장된 운세노트를 다시 열어볼 수 있습니다.") }
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
                item { SectionCaption("운세노트를 만들면 작은 책자 표지로 이곳에 쌓입니다.") }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun NoteProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Surface2)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
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
                .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(7.dp).background(color, CircleShape))
        }
        Text(text, color = TextPrimary, style = MaterialTheme.typography.bodySmall, maxLines = 2)
    }
}

@Composable
private fun QuickMenuCard(
    title: String,
    caption: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(9.dp).background(color, CircleShape))
        Text(title, color = TextPrimary, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        Text(caption, color = TextMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
}
