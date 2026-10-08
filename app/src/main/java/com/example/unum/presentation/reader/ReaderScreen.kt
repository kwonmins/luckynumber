package com.example.unum.presentation.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
            // At most three quiet illustrations per reading, including the caution section.
            val cautionIndex = book.chapters.indexOfFirst {
                listOf("주의", "조심", "갈등", "위험", "엇갈", "경계").any { word ->
                    word in it.title || word in it.lead
                }
            }
            val illustratedChapters = setOf(0, cautionIndex, book.chapters.lastIndex)
            LazyColumn(contentPadding=PaddingValues(20.dp,12.dp,20.dp,28.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
                item {
                    AppTopBar("운세 결과",subtitle=book.concernTopic,action={ Row {
                        IconButton(onClick={viewModel.toggleBookmark(book)}) { Icon(if(book.isBookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,"스크랩",tint=Accent) }
                        IconButton(onClick={share(book)}) {Icon(Icons.Rounded.Share,"PDF 저장 및 공유",tint=Accent)}
                    } })
                }
                if (book.bestMonth.isNotBlank() || book.riskyMonth.isNotBlank()) {
                    item {
                        Column(
                            Modifier.padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("시기를 읽는 포인트", color = DeepNavy, style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MonthInsight(
                                    title = "추천 시기",
                                    month = book.bestMonth,
                                    reason = book.bestMonthReason,
                                    modifier = Modifier.weight(1f)
                                )
                                MonthInsight(
                                    title = "주의 시기",
                                    month = book.riskyMonth,
                                    reason = book.riskyMonthReason,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                item {
                    Box {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                MoonlightBookCover(book, Modifier.width(240.dp).height(350.dp))
                            }
                            Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                                Text(book.coverTitle,color=DeepNavy,style=MaterialTheme.typography.titleLarge)
                                Text(book.coverSubtitle,color=Accent,style=MaterialTheme.typography.bodySmall)
                                Text(book.summary,color=TextSecondary,style=MaterialTheme.typography.bodyLarge.copy(fontSize=(16*scale).sp,lineHeight=(27*scale).sp))
                            }
                        }
                    }
                }
                book.chapters.forEachIndexed { index,chapter -> item {
                    Box(Modifier.padding(horizontal=8.dp,vertical=12.dp)) {
                        Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
                            Text("${(index+1).toString().padStart(2,'0')} · ${chapter.title}",color=DeepNavy,style=MaterialTheme.typography.titleMedium)
                            if(chapter.lead.isNotBlank()) Text(chapter.lead,color=Accent,style=MaterialTheme.typography.bodyMedium)
                            if (index in illustratedChapters) {
                                val reaction = resultReaction(chapter.title + " " + chapter.lead)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PastelSuri(reaction.pose, Modifier.size(52.dp), description = null)
                                    Text(reaction.caption, color = AccentDark, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            chapter.body.filter(String::isNotBlank).forEach {Text(it,color=TextSecondary,style=MaterialTheme.typography.bodyLarge.copy(fontSize=(16*scale).sp,lineHeight=(28*scale).sp))}
                            if(chapter.highlightQuote.isNotBlank()) Text(chapter.highlightQuote,color=DeepNavy,style=MaterialTheme.typography.bodyMedium)
                            chapter.actionTip.filter(String::isNotBlank).forEach {Text(it,color=TextSecondary,style=MaterialTheme.typography.bodyMedium)}
                        }
                    }
                } }
                item { Text("당신의 속도로, 좋은 흐름을 만들어가세요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(horizontal=8.dp)) }
            }
        }
    }
}

@Composable
private fun MonthInsight(title: String, month: String, reason: String, modifier: Modifier) {
    Column(
        modifier.background(Surface2, androidx.compose.foundation.shape.RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(title, color = AccentDark, style = MaterialTheme.typography.labelMedium)
        Text(month, color = DeepNavy, style = MaterialTheme.typography.titleSmall)
        if (reason.isNotBlank()) Text(reason, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

private data class SuriReaction(val pose: Int, val caption: String)

/** Match section headings, not isolated words in the interpretation itself. */
private fun resultReaction(heading: String): SuriReaction {
    fun mentions(vararg words: String) = words.any(heading::contains)
    return when {
        mentions("주의", "조심", "갈등", "위험", "엇갈", "경계") ->
            SuriReaction(2, "이 부분은 조금 더 살펴봐요.")
        mentions("추천", "조언", "실천", "행동", "방향", "정리", "오래 가", "습관") ->
            SuriReaction(5, "내가 할 수 있는 것부터 천천히 해봐요.")
        mentions("건강", "휴식", "마음 돌", "회복") ->
            SuriReaction(10, "잠시 쉬어가는 시간도 챙겨요.")
        mentions("연애", "궁합", "사랑", "관계", "끌리", "재회", "두 사람") ->
            SuriReaction(7, "서로의 마음을 차분히 읽어봐요.")
        mentions("기회", "강점", "장점", "가능성") ->
            SuriReaction(6, "나에게 도움이 될 실마리를 찾아봐요.")
        else -> SuriReaction(4, "지금의 이야기를 함께 읽어봐요.")
    }
}
