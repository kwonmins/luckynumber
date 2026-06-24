package com.example.unum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.BookSpecs
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.matchesBookSpec
import com.example.unum.data.model.resolvedThemeId
import com.example.unum.presentation.spec.LibrarySection
import com.example.unum.ui.components.MascotArt
import com.example.unum.ui.components.MascotGuideCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.InteractiveBookArchiveShelf
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun LibraryScreen(viewModel: AppViewModel, onOpenBook: (FortuneBook) -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val shareBook = rememberFortuneBookShareHandler()
    var filter by rememberSaveable { mutableStateOf(LibrarySection.ALL) }
    var pendingDeleteBookId by rememberSaveable { mutableStateOf<String?>(null) }

    val allBooks = uiState.savedBooks
    val pendingDeleteBook = pendingDeleteBookId?.let { id -> allBooks.firstOrNull { it.bookId == id } }
    val filteredBooks = allBooks.filter { it.matchesFilter(filter) }

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(18.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextPrimaryHeader("보관함")
                    Icon(Icons.Rounded.Search, contentDescription = "검색", tint = TextMuted)
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(com.example.unum.ui.theme.Surface)
                        .border(1.dp, Border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 13.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = TextMuted)
                        androidx.compose.material3.Text("리포트 검색", color = TextMuted, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                LibraryFilterRow(selected = filter, onSelected = { filter = it })
            }
            if (allBooks.isEmpty()) {
                item {
                    MascotGuideCard(
                        title = "저장된 운세노트가 없어요",
                        message = "운세노트를 만들면 이곳에 자동으로 저장됩니다.",
                        imageRes = MascotArt.Library
                    )
                }
                item {
                    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, contentPadding = 18) {
                        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            androidx.compose.material3.Text("첫 운세노트가 생기면 여기서 다시 볼 수 있어요.", color = TextPrimary)
                            androidx.compose.material3.Text("무료 결과를 본 뒤 운세노트로 넘어가면 기록이 이어집니다.", color = TextSecondary)
                        }
                    }
                }
            } else if (filteredBooks.isEmpty()) {
                item {
                    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, contentPadding = 18) {
                        androidx.compose.material3.Text("선택한 필터에 해당하는 책자가 아직 없어요.", color = TextSecondary)
                    }
                }
            } else {
                item {
                    InteractiveBookArchiveShelf(
                        books = filteredBooks,
                        selectedBookId = uiState.selectedBookId,
                        onBookOpen = { book ->
                            viewModel.selectSavedBook(book)
                            onOpenBook(book)
                        },
                        onBookmarkClick = { book -> viewModel.toggleBookmark(book) },
                        onShareClick = { book -> shareBook(book) },
                        onDeleteClick = { book -> pendingDeleteBookId = book.bookId }
                    )
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }

        if (pendingDeleteBook != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteBookId = null },
                title = { Text("책자를 삭제할까요?") },
                text = {
                    Text(
                        "${pendingDeleteBook.coverTitle} 책자가 이 기기와 로그인 계정 DB에서 함께 삭제됩니다."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSavedBook(pendingDeleteBook)
                            pendingDeleteBookId = null
                        }
                    ) {
                        Text("삭제")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteBookId = null }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}

@Composable
private fun TextPrimaryHeader(text: String) {
    androidx.compose.material3.Text(text, color = TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
}

@Composable
private fun TextMutedAction(text: String) {
    androidx.compose.material3.Text(text, color = TextMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
}

private fun FortuneBook.matchesFilter(filter: LibrarySection): Boolean = when (filter) {
    LibrarySection.ALL -> true
    LibrarySection.ROMANCE -> matchesPersonalTopic(PremiumTopic.ROMANCE)
    LibrarySection.CAREER -> matchesPersonalTopic(PremiumTopic.CAREER)
    LibrarySection.MONEY -> matchesPersonalTopic(PremiumTopic.MONEY)
    LibrarySection.SELF -> matchesPersonalTopic(PremiumTopic.SELF_ESTEEM)
    LibrarySection.RELATIONSHIP -> matchesPersonalTopic(PremiumTopic.RELATIONSHIP)
    LibrarySection.COMPATIBILITY -> matchesCompatibilityTopic()
    LibrarySection.COUPLE -> matchesCompatibilityStatus(CompatibilityRelationshipStatus.COUPLE)
    LibrarySection.CRUSH -> matchesCompatibilityStatus(CompatibilityRelationshipStatus.CRUSH)
    LibrarySection.REUNION -> matchesCompatibilityStatus(CompatibilityRelationshipStatus.REUNION)
}

private fun FortuneBook.matchesPersonalTopic(topic: PremiumTopic): Boolean {
    return matchesBookSpec(BookSpecs.forTopic(topic))
}

private fun FortuneBook.matchesCompatibilityTopic(): Boolean {
    val searchableText = "$concernTopic $coverTitle"
    return bookType == FortuneBookType.COMPATIBILITY ||
        resolvedThemeId().isCompatibility ||
        searchableText.contains("궁합", ignoreCase = true)
}

private fun FortuneBook.matchesCompatibilityStatus(status: CompatibilityRelationshipStatus): Boolean {
    if (!matchesCompatibilityTopic()) return false
    return matchesBookSpec(BookSpecs.forStatus(status))
}

@Composable
private fun LibraryFilterRow(selected: LibrarySection, onSelected: (LibrarySection) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        items(LibrarySection.entries) { filter ->
            val isSelected = selected == filter
            SurfaceCard(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .width(filter.chipWidthDp.dp),
                tonalColor = if (isSelected) com.example.unum.ui.theme.Accent else Surface2,
                borderColor = if (isSelected) com.example.unum.ui.theme.Accent.copy(alpha = 0.42f) else com.example.unum.ui.theme.Border,
                contentPadding = 0
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 9.dp)
                        .clickable { onSelected(filter) },
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = filter.label,
                        color = if (isSelected) androidx.compose.ui.graphics.Color.White else TextSecondary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
