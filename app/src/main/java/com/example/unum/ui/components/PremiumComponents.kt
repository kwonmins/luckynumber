package com.example.unum.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unum.R
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookChapter
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.ReaderFontScale
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.BookLine
import com.example.unum.ui.theme.BookPaper
import com.example.unum.ui.theme.BookPaperEdge
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.BorderStrong
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun MiniNumerologySummary(destiny: Int, early: Int, middle: Int, late: Int, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Surface2,
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("이번 해석에 반영되는 숫자", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberPill("운명수", destiny.toString(), Accent, Modifier.weight(1f))
                NumberPill("초년", early.toString(), Blue, Modifier.weight(1f))
                NumberPill("중년", middle.toString(), Mint, Modifier.weight(1f))
                NumberPill("말년", late.toString(), Rose, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun TopicChipGroup(selected: PremiumTopic, onSelected: (PremiumTopic) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PremiumTopic.entries.forEach { topic ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected == topic) Accent else Surface)
                    .border(1.dp, if (selected == topic) Accent.copy(alpha = 0.40f) else Border, RoundedCornerShape(999.dp))
                    .clickable { onSelected(topic) }
                    .padding(horizontal = 13.dp, vertical = 9.dp)
            ) {
                Text(topic.label, color = if (selected == topic) Color.White else TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun FortuneBookCover(
    book: FortuneBook,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val accentColor = noteAccentColor(book.coverTheme)
    val coverColors = leatherCoverColors(book.coverTheme)
    val foilColor = leatherFoilColor(book.coverTheme)
    val ribbonColor = leatherRibbonColor(book.coverTheme)
    val coverHeight = if (compact) 260.dp else 360.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(coverHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(coverColors))
            .border(1.dp, Color.Black.copy(alpha = 0.32f), RoundedCornerShape(8.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(18) { index ->
                val y = size.height * (index + 1) / 19f
                drawLine(
                    Color.White.copy(alpha = 0.026f),
                    Offset(16f, y),
                    Offset(size.width - 16f, y + if (index % 2 == 0) 8f else -6f),
                    strokeWidth = 1f
                )
                drawLine(
                    Color.Black.copy(alpha = 0.16f),
                    Offset(18f, y + 8f),
                    Offset(size.width - 18f, y),
                    strokeWidth = 1f
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 8.dp else 10.dp)
                .border(1.dp, foilColor.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 12.dp else 15.dp)
                .border(1.dp, foilColor.copy(alpha = 0.28f), RoundedCornerShape(6.dp))
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = if (compact) 17.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 9.dp)
        ) {
            repeat(if (compact) 23 else 32) {
                Box(
                    modifier = Modifier
                        .size(2.dp)
                        .clip(CircleShape)
                        .background(foilColor.copy(alpha = 0.58f))
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(if (compact) 14.dp else 18.dp, if (compact) 220.dp else 318.dp)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(Color.Black.copy(alpha = 0.22f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = if (compact) 20.dp else 28.dp)
                .size(if (compact) 16.dp else 20.dp, if (compact) 66.dp else 88.dp)
                .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                .background(Brush.verticalGradient(listOf(ribbonColor, accentColor.copy(alpha = 0.78f))))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (compact) 42.dp else 54.dp,
                    top = if (compact) 24.dp else 34.dp,
                    end = if (compact) 24.dp else 34.dp,
                    bottom = if (compact) 22.dp else 30.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    if (book.bookType == FortuneBookType.COMPATIBILITY) "PREMIUM MATCH NOTE" else "PREMIUM FORTUNE NOTE",
                    color = foilColor,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(formatBookDate(book.createdAt), color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)) {
                Text(
                    if (compact) {
                        if (book.bookType == FortuneBookType.COMPATIBILITY) "수리의 궁합노트" else "수리의 운세노트"
                    } else {
                        if (book.bookType == FortuneBookType.COMPATIBILITY) "수리의\n궁합노트" else "수리의\n운세노트"
                    },
                    color = foilColor,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displayMedium
                )
                Box(
                    modifier = Modifier
                        .size(width = if (compact) 92.dp else 132.dp, height = 2.dp)
                        .background(foilColor.copy(alpha = 0.86f))
                )
                Text(book.coverTitle, color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                Text(book.coverSubtitle, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall, maxLines = if (compact) 1 else 2)
            }
            if (!compact) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFB91C1C).copy(alpha = 0.94f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("수리", color = Color.White, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("핵심 요약", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                    Text(book.summary, color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodyMedium, maxLines = if (compact) 2 else 3)
                }
                NotebookSeal(
                    text = if (book.bookType == FortuneBookType.COMPATIBILITY) {
                        book.relationshipNumber?.toString() ?: "수"
                    } else {
                        book.destiny.toString()
                    },
                    color = foilColor,
                    size = if (compact) 64 else 82
                )
            }
        }
    }
}

@Composable
private fun CoverTag(text: String, accentColor: Color = Accent) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = accentColor, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RecentBooksRow(
    books: List<FortuneBook>,
    onBookClick: (FortuneBook) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books, key = { it.bookId }) { book ->
            BookThumbnailCard(book = book, onClick = { onBookClick(book) })
        }
    }
}

@Composable
fun BookThumbnailCard(
    book: FortuneBook,
    modifier: Modifier = Modifier.size(width = 148.dp, height = 198.dp),
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val palette = bookCoverPalette(book.coverTheme)
    val accentColor = noteAccentColor(book.coverTheme)
    val contentPadding = if (compact) 10.dp else 14.dp
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BookPaper)
            .border(1.dp, BookLine, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(contentPadding)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(if (compact) 5.dp else 7.dp, if (compact) 98.dp else 166.dp)
                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                .background(accentColor.copy(alpha = 0.88f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = if (compact) 8.dp else 10.dp)
                .size(if (compact) 12.dp else 16.dp, if (compact) 36.dp else 50.dp)
                .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                .background(accentColor.copy(alpha = 0.82f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (compact) 7.dp else 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(if (book.bookType == FortuneBookType.COMPATIBILITY) "궁합노트" else "운세노트", color = accentColor, style = MaterialTheme.typography.labelMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (compact) book.coverTitle else if (book.bookType == FortuneBookType.COMPATIBILITY) "수리의\n궁합노트" else "수리의\n운세노트",
                    color = TextPrimary,
                    style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                    maxLines = if (compact) 2 else 3
                )
                if (!compact) {
                    Text(book.coverTitle, color = accentColor, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                Text(
                    formatBookDate(book.createdAt),
                    color = TextSecondary,
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall
                )
            }
        }
        NotebookSeal(
            text = if (book.bookType == FortuneBookType.COMPATIBILITY) {
                book.relationshipNumber?.toString() ?: "수"
            } else {
                book.destiny.toString()
            },
            color = accentColor,
            size = if (compact) 34 else 46,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun PremiumArchiveRow(
    book: FortuneBook,
    selected: Boolean,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Accent.copy(alpha = 0.06f) else Surface)
            .border(1.dp, if (selected) Accent.copy(alpha = 0.45f) else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookThumbnailCard(
            book = book,
            modifier = Modifier.size(width = 88.dp, height = 120.dp),
            compact = true,
            onClick = onClick
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(book.coverTitle, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(book.coverSubtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            Text(
                book.chapters.firstOrNull()?.highlightQuote ?: book.summary,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Text(
                if (selected) "열람 중" else "탭해서 다시 읽기",
                color = if (selected) Accent else TextMuted,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Icon(
            imageVector = if (book.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
            contentDescription = "북마크",
            tint = if (book.isBookmarked) Accent else TextMuted,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onBookmarkClick)
                .padding(6.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FortuneBookReader(
    book: FortuneBook,
    fontScale: ReaderFontScale,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMonthPages = book.bookType == FortuneBookType.PERSONAL
    val pageCount = book.chapters.size + 2 + if (hasMonthPages) 2 else 0
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val chapterStartPage = 2
    val bestMonthPage = if (hasMonthPages) chapterStartPage + book.chapters.size else -1
    val riskyMonthPage = if (hasMonthPages) bestMonthPage + 1 else -1

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                readerPageTitle(book, pagerState.currentPage),
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "${pagerState.currentPage + 1} / $pageCount",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            pageSpacing = 0.dp
        ) { page ->
            val rawOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val pageOffset = rawOffset.absoluteValue.coerceIn(0f, 1f)
            ReaderPageScaffold(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .graphicsLayer {
                        cameraDistance = 18f * density
                        rotationY = rawOffset * 30f
                        scaleX = 1f - pageOffset * 0.035f
                        scaleY = 1f - pageOffset * 0.025f
                        alpha = 1f - pageOffset * 0.10f
                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (rawOffset > 0) 0f else 1f,
                            pivotFractionY = 0.5f
                        )
                    }
            ) {
                when {
                    page == 0 -> OverviewPage(book = book, onBookmarkClick = onBookmarkClick)
                    page == 1 -> ContentsPage(book)
                    page in chapterStartPage until chapterStartPage + book.chapters.size -> ChapterPage(
                        index = page - 1,
                        chapter = book.chapters[page - chapterStartPage],
                        fontScale = fontScale,
                        coverTheme = book.coverTheme
                    )
                    hasMonthPages && page == bestMonthPage -> MonthPage(
                        title = "올해 추천 월",
                        month = book.bestMonth.ifBlank { "-" },
                        reason = book.bestMonthReason.ifBlank { "리듬을 부드럽게 타기 좋은 시기예요." },
                        color = Mint
                    )
                    hasMonthPages && page == riskyMonthPage -> MonthPage(
                        title = "다음 주의 월",
                        month = book.riskyMonth.ifBlank { "-" },
                        reason = book.riskyMonthReason.ifBlank { "속도를 조금 낮추고 다시 확인하면 좋아요." },
                        color = Rose
                    )
                    else -> Spacer(modifier = Modifier.fillMaxSize())
                }
            }
        }
        PagerDots(pageCount = pageCount, currentPage = pagerState.currentPage)
    }
}

@Composable
private fun ReaderPageScaffold(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(10.dp, RoundedCornerShape(10.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
            .background(BookPaper)
            .border(1.dp, BookPaperEdge, RoundedCornerShape(8.dp))
            .padding(start = 16.dp, top = 14.dp, end = 20.dp, bottom = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(5.dp, 360.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Accent.copy(alpha = 0.72f))
        )
        NotebookSideTabs(
            modifier = Modifier.align(Alignment.CenterEnd),
            accentColor = Accent,
            compact = true
        )
        PageCornerFold(Modifier.align(Alignment.BottomEnd))
        content()
    }
}

@Composable
private fun PageCornerFold(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(topStart = 8.dp))
            .background(BookPaperEdge)
            .border(1.dp, BookLine, RoundedCornerShape(topStart = 8.dp))
    )
}

@Composable
private fun OverviewPage(book: FortuneBook, onBookmarkClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FortuneBookCover(book = book, compact = true, modifier = Modifier.fillMaxWidth())
        ReaderMetaRow(book = book, onBookmarkClick = onBookmarkClick)
        NotebookGuideCard(
            title = "읽는 순서",
            body = if (book.bookType == FortuneBookType.COMPATIBILITY) {
                "목차를 먼저 보고, 남자 성향과 여자 성향을 차례로 읽은 뒤 궁합수와 생활 흐름을 넘겨보세요."
            } else {
                "목차를 먼저 보고, 필요한 섹션부터 한 장씩 넘겨 읽어보세요. 추천할 월과 주의할 월은 뒤쪽에서 다시 정리합니다."
            }
        )
        NotebookSummaryCard(
            summaryText = book.summary,
            oneLineAdvice = book.chapters.firstOrNull()?.highlightQuote ?: "지금 가장 마음에 남는 문장부터 천천히 읽어보세요."
        )
    }
}

@Composable
private fun NotebookGuideCard(title: String, body: String, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Color(0xFFFFFAEF),
        borderColor = Color(0xFFE9DDC9),
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NotebookSummaryCard(summaryText: String, oneLineAdvice: String, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Accent.copy(alpha = 0.07f),
        borderColor = Accent.copy(alpha = 0.16f),
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("첫 장 요약", color = Accent, style = MaterialTheme.typography.labelLarge)
            Text(summaryText, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(oneLineAdvice, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MonthPage(
    title: String,
    month: String,
    reason: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MonthHighlightCard(
            title = title,
            month = month,
            reason = reason,
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
        NotebookGuideCard(
            title = "이 페이지를 읽는 법",
            body = "월별 흐름은 확정된 결과가 아니라 움직임의 온도입니다. 좋은 달에는 작은 행동을 만들고, 주의 달에는 충동을 하루 늦추는 쪽이 좋습니다."
        )
    }
}

@Composable
private fun MonthHighlightCard(
    title: String,
    month: String,
    reason: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    SurfaceCard(
        modifier = modifier,
        tonalColor = Color(0xFFFFFAEF),
        borderColor = color.copy(alpha = 0.32f),
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
            Text(month, color = color, style = MaterialTheme.typography.displayMedium)
            Text(reason, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ContentsCard(book: FortuneBook) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = BookPaper,
        borderColor = BookPaperEdge,
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = Accent)
                Text("목차", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
            book.chapters.forEachIndexed { index, chapter ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Accent.copy(alpha = 0.08f))
                            .border(1.dp, Accent.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = Accent, style = MaterialTheme.typography.labelLarge)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text(chapter.title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text(chapter.lead, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("p.${14 + index * 6}", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ContentsPage(book: FortuneBook) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ContentsCard(book)
    }
}

@Composable
private fun ChapterPage(
    index: Int,
    chapter: FortuneBookChapter,
    fontScale: ReaderFontScale,
    coverTheme: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ChapterCard(
            index = index,
            chapter = chapter,
            fontScale = fontScale,
            coverTheme = coverTheme
        )
    }
}

@Composable
private fun ChapterCard(
    index: Int,
    chapter: FortuneBookChapter,
    fontScale: ReaderFontScale,
    coverTheme: String
) {
    val bodyStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = (14f * fontScale.multiplier).sp,
        lineHeight = (22f * fontScale.multiplier).sp
    )
    val palette = bookCoverPalette(coverTheme)
    val accentColor = noteAccentColor(coverTheme)

    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = BookPaper,
        borderColor = palette.third.copy(alpha = 0.28f),
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(index.toString(), color = accentColor, style = MaterialTheme.typography.labelLarge)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(chapter.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(chapter.lead, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFFAEF))
                    .border(1.dp, Color(0xFFE9DDC9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("이 장의 핵심", color = accentColor, style = MaterialTheme.typography.labelMedium)
                    Text(chapter.lead, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            chapter.body.forEach { paragraph ->
                Text(paragraph, color = TextSecondary, style = bodyStyle)
            }
            StickyNote(label = "주의할 장면", text = chapter.highlightQuote, color = Rose)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.07f))
                    .border(1.dp, accentColor.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("이번 주 행동", color = accentColor, style = MaterialTheme.typography.labelLarge)
                chapter.actionTip.forEach { tip ->
                    Text("• $tip", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun StickyNote(label: String, text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(label, color = color, style = MaterialTheme.typography.labelLarge)
            Text(text, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PagerDots(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (index == currentPage) 18.dp else 8.dp, 8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (index == currentPage) Accent else Border)
            )
        }
    }
}

private fun readerPageTitle(book: FortuneBook, page: Int): String {
    val hasMonthPages = book.bookType == FortuneBookType.PERSONAL
    val bestMonthPage = if (hasMonthPages) book.chapters.size + 2 else -1
    val riskyMonthPage = if (hasMonthPages) bestMonthPage + 1 else -1
    return when {
        page == 0 -> "표지와 요약"
        page == 1 -> "목차"
        page in 2 until (2 + book.chapters.size) -> book.chapters.getOrNull(page - 2)?.title ?: "읽기"
        hasMonthPages && page == bestMonthPage -> "올해 추천 월"
        hasMonthPages && page == riskyMonthPage -> "다음 주의 월"
        else -> "읽기"
    }
}

@Composable
fun ReaderMetaRow(
    book: FortuneBook,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Color(0xFFFFFAEF),
        borderColor = Color(0xFFE9DDC9),
        contentPadding = 16
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("생성일 ${formatBookDate(book.createdAt)}", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                Text(
                    when (book.bookType) {
                        FortuneBookType.COMPATIBILITY -> "궁합수 ${book.relationshipNumber ?: "-"} · ${book.concernTopic}"
                        FortuneBookType.PERSONAL -> book.concernTopic
                    },
                    color = Accent,
                    style = MaterialTheme.typography.labelLarge
                )
                if (book.bookType == FortuneBookType.COMPATIBILITY) {
                    Text(
                        "남자 ${book.maleBirthLabel.orEmpty()} · 여자 ${book.femaleBirthLabel.orEmpty()}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onBookmarkClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (book.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = null,
                    tint = if (book.isBookmarked) Accent else TextMuted
                )
                Text(if (book.isBookmarked) "북마크됨" else "북마크", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatBookDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(timestamp))
}

private fun coverTags(book: FortuneBook): List<String> {
    return if (book.bookType == FortuneBookType.COMPATIBILITY) {
        listOfNotNull(
            book.maleDestiny?.let { "남자 $it" },
            book.femaleDestiny?.let { "여자 $it" },
            book.relationshipNumber?.let { "궁합수 $it" }
        )
    } else {
        listOf("운명수 ${book.destiny}", "코드 ${book.code}")
    }
}

private fun bookCoverPalette(theme: String): Triple<Color, Color, Color> {
    return when (theme) {
        PremiumTopic.ROMANCE.name.lowercase() -> Triple(Color(0xFFFFF7F4), Color(0xFFFFFFFF), Rose.copy(alpha = 0.22f))
        PremiumTopic.CAREER.name.lowercase() -> Triple(Color(0xFFF4F7FF), Color(0xFFFFFFFF), Blue.copy(alpha = 0.22f))
        PremiumTopic.MONEY.name.lowercase() -> Triple(Color(0xFFF2FAF5), Color(0xFFFFFFFF), Mint.copy(alpha = 0.22f))
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> Triple(Color(0xFFFFFAEF), Color(0xFFFFFFFF), Accent.copy(alpha = 0.22f))
        PremiumTopic.RELATIONSHIP.name.lowercase() -> Triple(Color(0xFFFFF7F4), Color(0xFFFFFFFF), Rose.copy(alpha = 0.20f))
        "compatibility" -> Triple(Color(0xFFFFFAEF), Color(0xFFFFFFFF), Accent.copy(alpha = 0.22f))
        else -> Triple(Surface, Surface2, BorderStrong)
    }
}

private fun leatherCoverColors(theme: String): List<Color> {
    return when (theme) {
        PremiumTopic.MONEY.name.lowercase() -> listOf(Color(0xFF1F5B4C), Color(0xFF0E332C), Color(0xFF061C18))
        PremiumTopic.RELATIONSHIP.name.lowercase() -> listOf(Color(0xFF9C5D32), Color(0xFF673719), Color(0xFF30170C))
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> listOf(Color(0xFF171A2A), Color(0xFF101225), Color(0xFF070813))
        else -> listOf(Color(0xFF222633), Color(0xFF10131B), Color(0xFF05070C))
    }
}

private fun leatherFoilColor(theme: String): Color {
    return when (theme) {
        PremiumTopic.RELATIONSHIP.name.lowercase() -> Color(0xFFF8E3A3)
        else -> Color(0xFFF7D56A)
    }
}

private fun leatherRibbonColor(theme: String): Color {
    return when (theme) {
        PremiumTopic.ROMANCE.name.lowercase(), "compatibility" -> Color(0xFFB91C1C)
        PremiumTopic.CAREER.name.lowercase() -> Blue
        PremiumTopic.MONEY.name.lowercase() -> Mint
        PremiumTopic.RELATIONSHIP.name.lowercase() -> Gold
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> Color(0xFF7C3AED)
        else -> Accent
    }
}

private fun noteAccentColor(theme: String): Color {
    return when (theme) {
        PremiumTopic.ROMANCE.name.lowercase() -> Rose
        PremiumTopic.CAREER.name.lowercase() -> Blue
        PremiumTopic.MONEY.name.lowercase() -> Mint
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> Accent
        PremiumTopic.RELATIONSHIP.name.lowercase() -> Rose
        "compatibility" -> Accent
        else -> Accent
    }
}

private fun chapterMascotRes(theme: String, chapterIndex: Int): Int {
    val primaryMascot = premiumThemeMascot(theme)
    val orderedMascots = listOf(
        primaryMascot,
        R.drawable.suri_scroll,
        R.drawable.suri_writer,
        R.drawable.suri_tea,
        R.drawable.suri_coins,
        R.drawable.suri_hanbok
    ).distinct()
    return orderedMascots[chapterIndex % orderedMascots.size]
}
