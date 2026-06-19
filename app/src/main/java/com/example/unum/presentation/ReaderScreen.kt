package com.example.unum.presentation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.ReaderFontScale
import com.example.unum.ui.components.FortuneBookReader
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun ReaderScreen(viewModel: AppViewModel, bookId: String?) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val book = uiState.savedBooks.firstOrNull { it.bookId == bookId } ?: uiState.savedBooks.firstOrNull()
    val shareBook = rememberFortuneBookShareHandler()

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        if (book == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "열 수 있는 운세노트가 아직 없어요. 프리미엄 운세노트를 먼저 만들어주세요.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@MysticBackground
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .padding(top = 12.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompactReaderHeader(
                title = book.coverTitle,
                subtitle = if (book.bookType == FortuneBookType.COMPATIBILITY) {
                    "가로로 넘기며 두 사람의 흐름을 책처럼 읽어보세요."
                } else {
                    "책장을 넘기듯 필요한 섹션을 천천히 읽어보세요."
                },
                fontScale = uiState.readerFontScale,
                onShare = { shareBook(book) }
            )
            FortuneBookReader(
                book = book,
                fontScale = uiState.readerFontScale,
                onBookmarkClick = { viewModel.toggleBookmark(book) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun CompactReaderHeader(
    title: String,
    subtitle: String,
    fontScale: ReaderFontScale,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Accent.copy(alpha = 0.08f))
                .border(1.dp, Accent.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(Accent, CircleShape)
            )
            Text(
                "수리의 운세노트",
                color = Accent,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = (12f * fontScale.multiplier).sp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = (22f * fontScale.multiplier).sp,
                    lineHeight = (28f * fontScale.multiplier).sp
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onShare) {
                Icon(Icons.Rounded.Share, contentDescription = "공유", tint = TextSecondary)
            }
        }
        Text(
            subtitle,
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = (12f * fontScale.multiplier).sp,
                lineHeight = (18f * fontScale.multiplier).sp
            )
        )
    }
}
