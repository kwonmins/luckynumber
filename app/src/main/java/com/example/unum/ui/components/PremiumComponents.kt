package com.example.unum.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.example.unum.ui.theme.Background
import com.example.unum.ui.theme.BackgroundAlt
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.BorderStrong
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.Surface3
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    .background(if (selected == topic) Accent.copy(alpha = 0.16f) else Surface2)
                    .border(1.dp, if (selected == topic) Accent.copy(alpha = 0.40f) else Border, RoundedCornerShape(999.dp))
                    .clickable { onSelected(topic) }
                    .padding(horizontal = 13.dp, vertical = 9.dp)
            ) {
                Text(topic.label, color = if (selected == topic) TextPrimary else TextSecondary, style = MaterialTheme.typography.bodySmall)
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
    val palette = bookCoverPalette(book.coverTheme)
    val mascotRes = premiumThemeMascot(book.coverTheme)
    val coverHeight = if (compact) 260.dp else 360.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(coverHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(palette.first, palette.second)))
            .border(1.dp, palette.third, RoundedCornerShape(8.dp))
            .padding(if (compact) 18.dp else 22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                PremiumBadge(book.concernTopic)
                Text(formatBookDate(book.createdAt), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(book.coverTitle, color = TextPrimary, style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displayMedium)
                Text(book.coverSubtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    coverTags(book).forEach { tag ->
                        CoverTag(tag)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("다시 읽는 포인트", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    Text(book.summary, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = if (compact) 2 else 3)
                }
                Image(
                    painter = painterResource(mascotRes),
                    contentDescription = "수리",
                    modifier = Modifier
                        .size(if (compact) 74.dp else 92.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun CoverTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Surface.copy(alpha = 0.34f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
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
    val mascotRes = premiumThemeMascot(book.coverTheme)
    val imageSize = if (compact) 42.dp else 64.dp
    val contentPadding = if (compact) 10.dp else 14.dp
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(palette.first, palette.second)))
            .border(1.dp, palette.third, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            PremiumBadge(book.concernTopic)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    book.coverTitle,
                    color = TextPrimary,
                    style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                    maxLines = if (compact) 2 else 3
                )
                Text(
                    formatBookDate(book.createdAt),
                    color = TextSecondary,
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall
                )
            }
        }
        Image(
            painter = painterResource(mascotRes),
            contentDescription = book.concernTopic,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(imageSize)
                .clip(RoundedCornerShape(8.dp))
                .background(Surface.copy(alpha = 0.12f))
                .padding(4.dp),
            contentScale = ContentScale.Fit
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
            .background(if (selected) Surface2 else Surface)
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
            tint = if (book.isBookmarked) Gold else TextMuted,
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

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            pageSpacing = 12.dp
        ) { page ->
            ReaderPageScaffold {
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
                        title = "추천할 월",
                        month = book.bestMonth.ifBlank { "-" },
                        reason = book.bestMonthReason.ifBlank { "리듬을 부드럽게 타기 좋은 시기예요." },
                        color = Mint,
                        mascotRes = premiumThemeMascot(book.coverTheme)
                    )
                    hasMonthPages && page == riskyMonthPage -> MonthPage(
                        title = "주의할 월",
                        month = book.riskyMonth.ifBlank { "-" },
                        reason = book.riskyMonthReason.ifBlank { "속도를 조금 낮추고 다시 확인하면 좋아요." },
                        color = Rose,
                        mascotRes = premiumThemeMascot(book.coverTheme)
                    )
                    else -> Spacer(modifier = Modifier.fillMaxSize())
                }
            }
        }
        PagerDots(pageCount = pageCount, currentPage = pagerState.currentPage)
    }
}

@Composable
private fun ReaderPageScaffold(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundAlt)
            .border(1.dp, BorderStrong, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        content()
    }
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
        MascotGuideCard(
            title = "책 읽는 순서",
            message = if (book.bookType == FortuneBookType.COMPATIBILITY) {
                "목차를 먼저 보고, 남자 기운과 여자 기운을 차례로 읽은 뒤 궁합수와 생활 흐름을 넘겨보세요."
            } else {
                "목차를 먼저 보고, 필요한 장부터 한 장씩 넘겨 읽어보세요. 추천할 월과 주의할 월은 뒤쪽에서 다시 정리해드릴게요."
            },
            imageRes = premiumThemeMascot(book.coverTheme)
        )
        SummaryBanner(
            summaryText = book.summary,
            oneLineAdvice = book.chapters.firstOrNull()?.highlightQuote ?: "지금 가장 마음에 남는 문장부터 천천히 읽어보세요."
        )
    }
}

@Composable
private fun MonthPage(
    title: String,
    month: String,
    reason: String,
    color: Color,
    mascotRes: Int
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
        SurfaceCard(
            modifier = Modifier.fillMaxWidth(),
            tonalColor = Surface,
            borderColor = color.copy(alpha = 0.32f),
            contentPadding = 18
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(mascotRes),
                    contentDescription = title,
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface2)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(reason, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
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
        tonalColor = Surface2,
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
        tonalColor = Surface,
        borderColor = BorderStrong,
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = Gold)
                Text("목차", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
            book.chapters.forEachIndexed { index, chapter ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("${index + 1}", color = Accent, style = MaterialTheme.typography.labelLarge)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(chapter.title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text(chapter.lead, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
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
    val mascotRes = chapterMascotRes(coverTheme, index - 1)

    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface,
        borderColor = palette.third.copy(alpha = 0.28f),
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Accent.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(index.toString(), color = TextPrimary, style = MaterialTheme.typography.labelLarge)
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
                    .background(palette.first.copy(alpha = 0.16f))
                    .border(1.dp, palette.third.copy(alpha = 0.24f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(end = 86.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("이 장의 핵심", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                    Text(chapter.lead, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(74.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface.copy(alpha = 0.18f))
                        .border(1.dp, palette.third.copy(alpha = 0.24f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(mascotRes),
                        contentDescription = chapter.title,
                        modifier = Modifier.size(58.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            chapter.body.forEach { paragraph ->
                Text(paragraph, color = TextSecondary, style = bodyStyle)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gold.copy(alpha = 0.10f))
                    .border(1.dp, Gold.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Text(chapter.highlightQuote, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("실천 팁", color = Mint, style = MaterialTheme.typography.labelLarge)
                chapter.actionTip.forEach { tip ->
                    Text("• $tip", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
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
        hasMonthPages && page == bestMonthPage -> "추천할 월"
        hasMonthPages && page == riskyMonthPage -> "주의할 월"
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
        tonalColor = Surface2,
        borderColor = BorderStrong,
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
                    tint = if (book.isBookmarked) Gold else TextMuted
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
        PremiumTopic.ROMANCE.name.lowercase() -> Triple(Color(0xFF33213E), Color(0xFF5C365E), Rose.copy(alpha = 0.40f))
        PremiumTopic.CAREER.name.lowercase() -> Triple(Color(0xFF13263F), Color(0xFF28446D), Blue.copy(alpha = 0.40f))
        PremiumTopic.MONEY.name.lowercase() -> Triple(Color(0xFF1B2D27), Color(0xFF355746), Mint.copy(alpha = 0.42f))
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> Triple(Color(0xFF2C2547), Color(0xFF4B3A71), Accent.copy(alpha = 0.42f))
        PremiumTopic.RELATIONSHIP.name.lowercase() -> Triple(Color(0xFF3A2D1B), Color(0xFF5A4630), Gold.copy(alpha = 0.40f))
        "compatibility" -> Triple(Color(0xFF352544), Color(0xFF624378), Gold.copy(alpha = 0.36f))
        else -> Triple(Surface2, Surface3, BorderStrong)
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
