package com.example.unum.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.ui.components.AppHeader
import com.example.unum.ui.components.FortuneBookReader
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.theme.TextSecondary

@Composable
fun ReaderScreen(viewModel: AppViewModel, bookId: String?) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val book = uiState.savedBooks.firstOrNull { it.bookId == bookId } ?: uiState.savedBooks.firstOrNull()

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        if (book == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "열 수 있는 운세책이 아직 없어요. AI 프리미엄 운세를 먼저 확인해주세요.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(18.dp))
                AppHeader(
                    title = book.coverTitle,
                    subtitle = if (book.bookType == com.example.unum.data.model.FortuneBookType.COMPATIBILITY) {
                        "가로로 넘기며 두 사람의 기운과 궁합 흐름을 읽어보세요. 긴 문장은 페이지 안에서 천천히 스크롤할 수 있어요."
                    } else {
                        "가로로 넘기며 읽고, 긴 문장은 페이지 안에서 천천히 스크롤해보세요."
                    },
                    eyebrow = "읽기"
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
}
