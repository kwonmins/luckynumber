package com.example.unum.presentation.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.presentation.AppViewModel
import com.example.unum.presentation.library.rememberFortuneBookShareHandler
import com.example.unum.ui.components.*
import com.example.unum.ui.theme.*

@Composable
fun ReaderScreen(viewModel: AppViewModel, bookId: String?) {
    val state=viewModel.uiState.collectAsStateWithLifecycle().value
    val book=state.savedBooks.firstOrNull {it.bookId==bookId} ?: state.savedBooks.firstOrNull()
    val share=rememberFortuneBookShareHandler()
    MysticBackground(Modifier.fillMaxSize()) {
        if(book==null) {
            Box(Modifier.fillMaxSize().padding(24.dp),contentAlignment=androidx.compose.ui.Alignment.Center) { Text("아직 저장된 운세가 없어요.",color=TextSecondary) }
        } else {
            val scale=state.readerFontScale.multiplier
            LazyColumn(contentPadding=PaddingValues(20.dp,12.dp,20.dp,28.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
                item {
                    AppTopBar("운세 결과",subtitle=book.concernTopic,action={ Row {
                        IconButton(onClick={viewModel.toggleBookmark(book)}) { Icon(if(book.isBookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,"스크랩",tint=Accent) }
                        IconButton(onClick={share(book)}) {Icon(Icons.Rounded.Share,"PDF 저장 및 공유",tint=Accent)}
                    } })
                }
                item {
                    SurfaceCard(contentPadding=0) {
                        Column {
                            MoonGarden(Modifier.fillMaxWidth().height(130.dp))
                            Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                                Text(book.coverTitle,color=DeepNavy,style=MaterialTheme.typography.titleLarge)
                                Text(book.coverSubtitle,color=Accent,style=MaterialTheme.typography.bodySmall)
                                Text(book.summary,color=TextSecondary,style=MaterialTheme.typography.bodyLarge.copy(fontSize=(16*scale).sp,lineHeight=(27*scale).sp))
                            }
                        }
                    }
                }
                book.chapters.forEachIndexed { index,chapter -> item {
                    SurfaceCard {
                        Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                            Text("${(index+1).toString().padStart(2,'0')} · ${chapter.title}",color=DeepNavy,style=MaterialTheme.typography.titleMedium)
                            if(chapter.lead.isNotBlank()) Text(chapter.lead,color=Accent,style=MaterialTheme.typography.bodyMedium)
                            chapter.body.filter(String::isNotBlank).forEach {Text(it,color=TextSecondary,style=MaterialTheme.typography.bodyLarge.copy(fontSize=(16*scale).sp,lineHeight=(28*scale).sp))}
                            if(chapter.highlightQuote.isNotBlank()) Text(chapter.highlightQuote,color=DeepNavy,style=MaterialTheme.typography.bodyMedium)
                            chapter.actionTip.filter(String::isNotBlank).forEach {Text(it,color=TextSecondary,style=MaterialTheme.typography.bodyMedium)}
                        }
                    }
                } }
                item { MascotGuideCard("당신의 속도로, 좋은 흐름을 만들어가세요.",title="수리의 한마디") }
            }
        }
    }
}
