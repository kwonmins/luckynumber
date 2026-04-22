package com.example.unum.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBook
import com.example.unum.ui.components.AppHeader
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.PremiumArchiveRow
import com.example.unum.ui.components.RecentBooksRow
import com.example.unum.ui.components.SectionCaption
import com.example.unum.ui.components.SectionTitle
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToggleSegment
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

private enum class LibraryFilter(val label: String) {
    ALL("전체"),
    RECENT("최근 본"),
    BOOKMARKED("북마크")
}

@Composable
fun LibraryScreen(viewModel: AppViewModel, onOpenBook: (FortuneBook) -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var filter by rememberSaveable { mutableStateOf(LibraryFilter.ALL) }

    val allBooks = uiState.savedBooks
    val filteredBooks = when (filter) {
        LibraryFilter.ALL -> allBooks
        LibraryFilter.RECENT -> allBooks.sortedByDescending { it.lastOpenedAt ?: it.createdAt }
        LibraryFilter.BOOKMARKED -> allBooks.filter { it.isBookmarked }
    }

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
                    title = "내 프리미엄 보관함",
                    subtitle = "개인 운세와 궁합 결과를 최근 본 순서대로 다시 열어볼 수 있어요.",
                    eyebrow = "보관함"
                )
            }
            if (allBooks.isEmpty()) {
                item {
                    MascotGuideCard(
                        title = "보관함이 비어 있어요",
                        message = "AI 프리미엄 운세를 확인하면 이곳에 자동으로 저장돼요.",
                        imageRes = MascotArt.Library
                    )
                }
                item {
                    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, contentPadding = 18) {
                        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            androidx.compose.material3.Text("첫 운세책이 생기면 여기서 다시 볼 수 있어요.", color = TextPrimary)
                            androidx.compose.material3.Text("무료 결과를 본 뒤 AI 상담으로 넘어가면 저장 흐름이 이어집니다.", color = TextSecondary)
                        }
                    }
                }
            } else {
                item { SectionTitle("최근 본 운세책") }
                item { SectionCaption("이전에 읽던 책을 바로 다시 열어볼 수 있어요.") }
                item {
                    RecentBooksRow(
                        books = allBooks.take(5),
                        onBookClick = { book ->
                            viewModel.selectSavedBook(book)
                            onOpenBook(book)
                        }
                    )
                }
                item { SectionTitle("정렬 보기") }
                item {
                    LibraryFilterRow(selected = filter, onSelected = { filter = it })
                }
                item { SectionTitle("전체 목록") }
                items(filteredBooks, key = { it.bookId }) { book ->
                    PremiumArchiveRow(
                        book = book,
                        selected = book.bookId == uiState.selectedBookId,
                        onClick = {
                            viewModel.selectSavedBook(book)
                            onOpenBook(book)
                        },
                        onBookmarkClick = { viewModel.toggleBookmark(book) }
                    )
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun LibraryFilterRow(selected: LibraryFilter, onSelected: (LibraryFilter) -> Unit) {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        LibraryFilter.entries.forEach { filter ->
            val isSelected = selected == filter
            SurfaceCard(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 2.dp)
                    .fillMaxWidth(),
                tonalColor = if (isSelected) com.example.unum.ui.theme.Accent.copy(alpha = 0.16f) else Surface2,
                borderColor = if (isSelected) com.example.unum.ui.theme.Accent.copy(alpha = 0.42f) else com.example.unum.ui.theme.Border,
                contentPadding = 0
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onSelected(filter) },
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = filter.label,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
