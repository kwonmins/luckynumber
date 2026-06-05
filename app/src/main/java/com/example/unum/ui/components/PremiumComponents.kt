package com.example.unum.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue

private object PremiumTokens {
    val GoldLight = Color(0xFFF7D56A)
    val Gold = Color(0xFFD4A84B)
    val GoldDeep = Color(0xFFA67C35)
    val GoldFoil = Color(0xFFF0C96A)

    val Ink = Color(0xFF0C0B0F)
    val InkWarm = Color(0xFF13100F)
    val Surface0 = Color(0xFF1A1714)
    val Surface1 = Color(0xFF231F1B)
    val Surface2 = Color(0xFF2C2720)

    val BorderGold = Color(0x33D4A84B)
    val BorderSubtle = Color(0x1AFFFFFF)

    val TextGold = Color(0xFFD4A84B)
    val TextCream = Color(0xFFF5EDD8)
    val TextMuted = Color(0xFF8A7B65)
    val TextDim = Color(0xFF5A4F3F)
}

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
    val coverHeight = if (compact) 268.dp else 390.dp
    val mainTitle = coverDisplayTitle(book)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(coverHeight)
            .shadow(if (compact) 12.dp else 22.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(coverColors))
            .border(1.dp, foilColor.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(26) { index ->
                val y = size.height * (index + 1) / 19f
                drawLine(
                    Color.White.copy(alpha = 0.020f),
                    Offset(20f, y),
                    Offset(size.width - 20f, y + if (index % 2 == 0) 8f else -6f),
                    strokeWidth = 1f
                )
                drawLine(
                    Color.Black.copy(alpha = 0.20f),
                    Offset(22f, y + 8f),
                    Offset(size.width - 22f, y),
                    strokeWidth = 1f
                )
            }
            val inset = if (compact) 18f else 24f
            val ornament = if (compact) 28f else 42f
            val stroke = if (compact) 1.1f else 1.6f
            val c = foilColor.copy(alpha = 0.78f)
            drawLine(c, Offset(inset, inset + ornament), Offset(inset, inset), stroke)
            drawLine(c, Offset(inset, inset), Offset(inset + ornament, inset), stroke)
            drawLine(c, Offset(size.width - inset - ornament, inset), Offset(size.width - inset, inset), stroke)
            drawLine(c, Offset(size.width - inset, inset), Offset(size.width - inset, inset + ornament), stroke)
            drawLine(c, Offset(inset, size.height - inset - ornament), Offset(inset, size.height - inset), stroke)
            drawLine(c, Offset(inset, size.height - inset), Offset(inset + ornament, size.height - inset), stroke)
            drawLine(c, Offset(size.width - inset - ornament, size.height - inset), Offset(size.width - inset, size.height - inset), stroke)
            drawLine(c, Offset(size.width - inset, size.height - inset - ornament), Offset(size.width - inset, size.height - inset), stroke)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 12.dp else 16.dp)
                .border(1.dp, foilColor.copy(alpha = 0.70f), RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 17.dp else 23.dp)
                .border(1.dp, foilColor.copy(alpha = 0.24f), RoundedCornerShape(9.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(if (compact) 18.dp else 26.dp, if (compact) 242.dp else 348.dp)
                .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.30f),
                            ribbonColor.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.40f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = if (compact) 23.dp else 31.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
        ) {
            repeat(if (compact) 23 else 34) {
                Box(
                    modifier = Modifier
                        .size(2.dp)
                        .clip(CircleShape)
                        .background(foilColor.copy(alpha = 0.48f))
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = if (compact) 22.dp else 30.dp)
                .size(if (compact) 14.dp else 18.dp, if (compact) 62.dp else 88.dp)
                .clip(RoundedCornerShape(bottomStart = 7.dp, bottomEnd = 7.dp))
                .background(Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.94f), ribbonColor)))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (compact) 44.dp else 58.dp,
                    top = if (compact) 34.dp else 48.dp,
                    end = if (compact) 28.dp else 38.dp,
                    bottom = if (compact) 24.dp else 34.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
            ) {
                Text(
                    coverKicker(book),
                    color = foilColor,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp, fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp)
            ) {
                Text(
                    mainTitle,
                    color = foilColor,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .size(width = if (compact) 86.dp else 128.dp, height = 1.dp)
                        .background(foilColor.copy(alpha = 0.70f))
                )
                Text(book.coverSubtitle, color = PremiumTokens.TextMuted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, maxLines = if (compact) 1 else 2)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)) {
                NotebookSeal(
                    text = if (book.bookType == FortuneBookType.COMPATIBILITY) book.relationshipNumber?.toString() ?: "수" else book.destiny.toString(),
                    color = foilColor,
                    size = if (compact) 58 else 76
                )
                Text("수리의 운세노트", color = PremiumTokens.TextMuted, style = MaterialTheme.typography.labelSmall)
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
    val accentColor = noteAccentColor(book.coverTheme)
    val foilColor = leatherFoilColor(book.coverTheme)
    val coverColors = leatherCoverColors(book.coverTheme)
    val contentPadding = if (compact) 8.dp else 12.dp
    Box(
        modifier = modifier
            .shadow(if (compact) 4.dp else 8.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(coverColors))
            .border(1.dp, foilColor.copy(alpha = 0.44f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(contentPadding)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(13) { index ->
                val y = size.height * (index + 1) / 11f
                drawLine(
                    Color.White.copy(alpha = 0.016f),
                    Offset(8f, y),
                    Offset(size.width - 8f, y + if (index % 2 == 0) 4f else -3f),
                    strokeWidth = 1f
                )
            }
            val inset = 6f
            val ornament = if (compact) 13f else 20f
            val c = foilColor.copy(alpha = 0.72f)
            drawLine(c, Offset(inset, inset + ornament), Offset(inset, inset), 1f)
            drawLine(c, Offset(inset, inset), Offset(inset + ornament, inset), 1f)
            drawLine(c, Offset(size.width - inset - ornament, inset), Offset(size.width - inset, inset), 1f)
            drawLine(c, Offset(size.width - inset, inset), Offset(size.width - inset, inset + ornament), 1f)
            drawLine(c, Offset(inset, size.height - inset - ornament), Offset(inset, size.height - inset), 1f)
            drawLine(c, Offset(inset, size.height - inset), Offset(inset + ornament, size.height - inset), 1f)
            drawLine(c, Offset(size.width - inset - ornament, size.height - inset), Offset(size.width - inset, size.height - inset), 1f)
            drawLine(c, Offset(size.width - inset, size.height - inset - ornament), Offset(size.width - inset, size.height - inset), 1f)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, foilColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(if (compact) 7.dp else 9.dp, if (compact) 100.dp else 168.dp)
                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), leatherRibbonColor(book.coverTheme), Color.Black.copy(alpha = 0.34f))))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = if (compact) 8.dp else 10.dp)
                .size(if (compact) 10.dp else 14.dp, if (compact) 34.dp else 50.dp)
                .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                .background(Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.92f), leatherRibbonColor(book.coverTheme))))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (compact) 11.dp else 14.dp, top = if (compact) 8.dp else 12.dp, bottom = if (compact) 4.dp else 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(coverKicker(book), color = foilColor, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = if (compact) 0.7.sp else 1.0.sp), textAlign = TextAlign.Center, maxLines = 1)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 8.dp)) {
                Text(
                    coverDisplayTitle(book),
                    color = foilColor,
                    style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = if (compact) 2 else 3
                )
                Box(
                    modifier = Modifier
                        .size(width = if (compact) 34.dp else 52.dp, height = 1.dp)
                        .background(foilColor.copy(alpha = 0.68f))
                )
                Text(book.coverSubtitle, color = PremiumTokens.TextMuted, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 1)
            }
            NotebookSeal(
                text = if (book.bookType == FortuneBookType.COMPATIBILITY) book.relationshipNumber?.toString() ?: "수" else book.destiny.toString(),
                color = foilColor,
                size = if (compact) 30 else 42
            )
        }
    }
}

@Composable
fun InteractiveBookArchiveShelf(
    books: List<FortuneBook>,
    selectedBookId: String?,
    onBookOpen: (FortuneBook) -> Unit,
    onBookmarkClick: (FortuneBook) -> Unit,
    modifier: Modifier = Modifier
) {
    if (books.isEmpty()) return

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val coverWidth = 176.dp
    val coverHeight = 238.dp
    val initialIndex = books.indexOfFirst { it.bookId == selectedBookId }.takeIf { it >= 0 } ?: 0

    LaunchedEffect(selectedBookId, books.size) {
        listState.scrollToItem(initialIndex)
    }

    val focusedBook by remember(books, listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs(item.offset + item.size / 2f - viewportCenter)
            }
            books.getOrNull(centeredItem?.index ?: initialIndex) ?: books.first()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(318.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF111318), Color(0xFF17110E), Color(0xFF0C0B0F))
                    )
                )
                .border(1.dp, PremiumTokens.BorderGold, RoundedCornerShape(18.dp))
        ) {
            val sidePadding = if (maxWidth > coverWidth) (maxWidth - coverWidth) / 2 else 24.dp

            Canvas(Modifier.fillMaxSize()) {
                val baseY = size.height * 0.82f
                drawArc(
                    color = PremiumTokens.Gold.copy(alpha = 0.16f),
                    startAngle = 198f,
                    sweepAngle = 144f,
                    useCenter = false,
                    topLeft = Offset(size.width * -0.18f, baseY - size.width * 0.62f),
                    size = androidx.compose.ui.geometry.Size(size.width * 1.36f, size.width * 0.78f),
                    style = Stroke(width = 2f)
                )
            }

            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .align(Alignment.BottomCenter),
                contentPadding = PaddingValues(horizontal = sidePadding),
                horizontalArrangement = Arrangement.spacedBy((-38).dp),
                verticalAlignment = Alignment.Bottom
            ) {
                itemsIndexed(books, key = { _, book -> book.bookId }) { index, book ->
                    val layoutInfo = listState.layoutInfo
                    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
                    val offsetFromCenter = itemInfo?.let {
                        ((it.offset + it.size / 2f) - viewportCenter) / it.size
                    } ?: (index - listState.firstVisibleItemIndex).toFloat()
                    val clampedOffset = offsetFromCenter.coerceIn(-3f, 3f)
                    val distance = abs(clampedOffset)
                    val scale = (1.08f - distance * 0.11f).coerceIn(0.78f, 1.08f)
                    val sideDrop = with(density) { (distance * 42f).dp.toPx() }
                    val forwardLift = with(density) { if (distance < 0.35f) (-12).dp.toPx() else 0.dp.toPx() }
                    val coverAlpha = (1f - distance * 0.12f).coerceIn(0.58f, 1f)

                    BookThumbnailCard(
                        book = book,
                        modifier = Modifier
                            .size(width = coverWidth, height = coverHeight)
                            .zIndex(10f - distance)
                            .graphicsLayer {
                                cameraDistance = 18f * density.density
                                transformOrigin = TransformOrigin(0.5f, 1f)
                                rotationZ = clampedOffset * 8f
                                rotationY = -clampedOffset * 10f
                                translationY = sideDrop + forwardLift
                                scaleX = scale
                                scaleY = scale
                                alpha = coverAlpha
                            },
                        compact = false,
                        onClick = {
                            if (distance < 0.38f) {
                                onBookOpen(book)
                            } else {
                                scope.launch { listState.animateScrollToItem(index) }
                            }
                        }
                    )
                }
            }
        }

        SurfaceCard(
            modifier = Modifier.fillMaxWidth(),
            tonalColor = PremiumTokens.Surface0,
            borderColor = PremiumTokens.BorderGold,
            contentPadding = 16
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CoverTag(focusedBook.concernTopic, accentColor = leatherFoilColor(focusedBook.coverTheme))
                    Text(focusedBook.coverTitle, color = PremiumTokens.TextCream, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Text(
                        "${formatBookDate(focusedBook.createdAt)} · 가운데 책을 탭해서 펼치기",
                        color = PremiumTokens.TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    imageVector = if (focusedBook.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = "북마크",
                    tint = if (focusedBook.isBookmarked) PremiumTokens.Gold else PremiumTokens.TextMuted,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onBookmarkClick(focusedBook) }
                        .padding(9.dp)
                )
            }
        }
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
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookThumbnailCard(
            book = book,
            modifier = Modifier.size(width = 136.dp, height = 190.dp),
            compact = false,
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
    val hasMonthPages = book.bookType == FortuneBookType.PERSONAL &&
        book.coverTheme != "romance" &&
        book.bestMonth.isNotBlank() &&
        book.riskyMonth.isNotBlank()
    val pageCount = book.chapters.size + 2 + if (hasMonthPages) 2 else 0
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
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
                        rotationY = rawOffset * 42f
                        translationX = rawOffset * -18f
                        scaleX = 1f - pageOffset * 0.045f
                        scaleY = 1f - pageOffset * 0.030f
                        alpha = 1f - pageOffset * 0.12f
                        shadowElevation = (18f + pageOffset * 18f) * density
                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (rawOffset > 0) 0f else 1f,
                            pivotFractionY = 0.5f
                        )
                    }
            ) {
                when {
                    page == 0 -> OverviewPage(book = book, onBookmarkClick = onBookmarkClick)
                    page == 1 -> ContentsPage(
                        book = book,
                        onChapterClick = { chapterIndex ->
                            scope.launch { pagerState.animateScrollToPage(chapterStartPage + chapterIndex) }
                        }
                    )
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
            .shadow(12.dp, RoundedCornerShape(8.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF8EA))
            .border(
                1.dp,
                Color(0xFFD7C9AA),
                RoundedCornerShape(8.dp)
            )
            .padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 18.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(26) { index ->
                val y = size.height * (index + 1) / 27f
                drawCircle(
                    color = Color(0xFF8B7A5B).copy(alpha = 0.035f),
                    radius = if (index % 3 == 0) 2.4f else 1.5f,
                    center = Offset(
                        x = size.width * ((index * 37 % 100) / 100f),
                        y = y
                    )
                )
            }
            drawLine(
                color = Color(0xFFBCA98A).copy(alpha = 0.16f),
                start = Offset(0f, size.height * 0.98f),
                end = Offset(size.width, size.height * 0.96f),
                strokeWidth = 1f
            )
        }
        content()
    }
}

@Composable
private fun PageCornerFold(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(topStart = 8.dp))
            .background(PremiumTokens.Surface2)
            .border(1.dp, PremiumTokens.BorderGold, RoundedCornerShape(topStart = 8.dp))
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
        NotebookSummaryCardLight(
            summaryText = book.summary,
            oneLineAdvice = book.chapters.firstOrNull()?.highlightQuote ?: "지금 가장 마음에 남는 문장부터 천천히 읽어보세요."
        )
    }
}

@Composable
private fun NotebookGuideCard(title: String, body: String, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Color(0xFFFBF6EA),
        borderColor = PremiumTokens.Gold.copy(alpha = 0.18f),
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NotebookSummaryCardLight(summaryText: String, oneLineAdvice: String, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Color(0xFFFFFAEF),
        borderColor = PremiumTokens.GoldDeep.copy(alpha = 0.28f),
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("첫 답변", color = PremiumTokens.GoldDeep, style = MaterialTheme.typography.labelLarge)
            Text(summaryText, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(oneLineAdvice, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NotebookSummaryCard(summaryText: String, oneLineAdvice: String, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Color(0xFFFFFAEF),
        borderColor = PremiumTokens.GoldDeep.copy(alpha = 0.28f),
        contentPadding = 16
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("첫 장 요약", color = PremiumTokens.GoldLight, style = MaterialTheme.typography.labelLarge)
            Text(summaryText, color = PremiumTokens.TextCream, style = MaterialTheme.typography.bodyMedium)
            Text(oneLineAdvice, color = PremiumTokens.TextMuted, style = MaterialTheme.typography.bodySmall)
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
        tonalColor = PremiumTokens.Surface0,
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
                Text(title, color = PremiumTokens.TextCream, style = MaterialTheme.typography.titleMedium)
            }
            Text(month, color = color, style = MaterialTheme.typography.displayMedium)
            Text(reason, color = PremiumTokens.TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ContentsCard(book: FortuneBook, onChapterClick: (Int) -> Unit = {}) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Color(0xFFFBF6EA),
        borderColor = PremiumTokens.Gold.copy(alpha = 0.24f),
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = PremiumTokens.GoldDeep)
                Text("목차", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(
                    if (book.bookType == FortuneBookType.COMPATIBILITY) "PREMIUM MATCH NOTE" else "PREMIUM FORTUNE NOTE",
                    color = PremiumTokens.GoldDeep,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp, fontWeight = FontWeight.SemiBold)
                )
            }
            book.chapters.forEachIndexed { index, chapter ->
                val scale by animateFloatAsState(
                    targetValue = if (index == 0) 1.02f else 1f,
                    animationSpec = tween(durationMillis = 360),
                    label = "contentsRowScale"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onChapterClick(index) }
                        .background(if (index == 0) PremiumTokens.GoldLight.copy(alpha = 0.10f) else Color.Transparent)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PremiumTokens.GoldLight.copy(alpha = 0.16f))
                            .border(1.dp, PremiumTokens.Gold.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = PremiumTokens.GoldDeep, style = MaterialTheme.typography.labelLarge)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text(chapter.title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text(chapter.lead, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("p.${14 + index * 6}", color = PremiumTokens.TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ContentsPage(book: FortuneBook, onChapterClick: (Int) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ContentsCard(book, onChapterClick)
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
        SalonChapterCard(
            index = index,
            chapter = chapter,
            fontScale = fontScale,
            coverTheme = coverTheme
        )
    }
}

@Composable
private fun SalonChapterCard(
    index: Int,
    chapter: FortuneBookChapter,
    fontScale: ReaderFontScale,
    coverTheme: String
) {
    val bodyStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = (17f * fontScale.multiplier).sp,
        lineHeight = (31f * fontScale.multiplier).sp
    )
    val palette = bookCoverPalette(coverTheme)
    val accentColor = noteAccentColor(coverTheme)
    val ribbonColor = if (chapter.lead.contains("주의")) Rose else accentColor

    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Color.Transparent,
        borderColor = Color.Transparent,
        contentPadding = 0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            if (chapter.lead.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                            .background(ribbonColor.copy(alpha = 0.86f))
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            chapter.lead,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            Text(
                chapter.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                color = TextPrimary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Image(
                painter = painterResource(chapterMascotRes(coverTheme, index)),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(108.dp)
                    .graphicsLayer {
                        alpha = 0.96f
                        translationY = -4.dp.toPx()
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .height(1.dp)
                    .background(Color(0xFFB7AA92).copy(alpha = 0.45f))
            )

            if (chapter.highlightQuote.isNotBlank()) {
                Text(
                    chapter.highlightQuote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    color = ribbonColor,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                )
            }

            SequentialParagraphs(
                paragraphs = chapter.body.filter { it.isNotBlank() }.take(3),
                bodyStyle = bodyStyle
            )

            if (chapter.actionTip.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFFAEF))
                        .border(1.dp, palette.third.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("복사하기 좋은 문장", color = ribbonColor, style = MaterialTheme.typography.labelLarge)
                    chapter.actionTip.forEach { tip ->
                        Text(tip, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
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
    val foilColor = leatherFoilColor(coverTheme)

    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Color(0xFFFBF6EA),
        borderColor = palette.third.copy(alpha = 0.34f),
        contentPadding = 18
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(foilColor.copy(alpha = 0.16f), CircleShape)
                        .border(1.dp, foilColor.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(index.toString(), color = foilColor, style = MaterialTheme.typography.labelLarge)
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
                    .border(1.dp, foilColor.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("이 장의 핵심", color = foilColor, style = MaterialTheme.typography.labelMedium)
                    Text(chapter.lead, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            SequentialParagraphs(paragraphs = chapter.body, bodyStyle = bodyStyle)
            StickyNote(label = "주의할 장면", text = chapter.highlightQuote, color = Rose)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PremiumTokens.Surface0)
                    .border(1.dp, foilColor.copy(alpha = 0.24f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("이번 주 행동", color = foilColor, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                chapter.actionTip.forEach { tip ->
                    Text("• $tip", color = PremiumTokens.TextCream, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SequentialParagraphs(
    paragraphs: List<String>,
    bodyStyle: androidx.compose.ui.text.TextStyle
) {
    var visibleCount by remember(paragraphs) { mutableStateOf(0) }
    LaunchedEffect(paragraphs) {
        visibleCount = 0
        paragraphs.indices.forEach { index ->
            delay(if (index == 0) 80 else 260)
            visibleCount = index + 1
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        paragraphs.forEachIndexed { index, paragraph ->
            AnimatedVisibility(
                visible = index < visibleCount,
                enter = fadeIn(tween(240)) + expandVertically()
            ) {
                Text(paragraph, color = TextSecondary, style = bodyStyle)
            }
        }
    }
}

@Composable
private fun StickyNote(label: String, text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PremiumTokens.Surface0)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(PremiumTokens.BorderGold, PremiumTokens.BorderSubtle)
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(Brush.verticalGradient(listOf(color, color.copy(alpha = 0.30f))))
        )

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = color,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = text,
                color = PremiumTokens.TextCream,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
            )
        }
    }
}

@Composable
private fun PagerDots(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 28.dp else 6.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "dotWidth"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.3f,
                animationSpec = tween(durationMillis = 300),
                label = "dotAlpha"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(width = width, height = 6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isActive) {
                            Brush.horizontalGradient(listOf(PremiumTokens.GoldLight, PremiumTokens.Gold))
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    PremiumTokens.TextDim.copy(alpha = alpha),
                                    PremiumTokens.TextDim.copy(alpha = alpha)
                                )
                            )
                        }
                    )
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PremiumTokens.Surface0)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(PremiumTokens.BorderGold, Color.Transparent)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "생성일 ${formatBookDate(book.createdAt)}",
                    color = PremiumTokens.TextMuted,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp)
                )
                MaterialSurface(
                    shape = RoundedCornerShape(6.dp),
                    color = PremiumTokens.GoldDeep.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (book.bookType) {
                            FortuneBookType.COMPATIBILITY -> "궁합수 ${book.relationshipNumber ?: "-"} · ${book.concernTopic}"
                            FortuneBookType.PERSONAL -> book.concernTopic
                        },
                        color = PremiumTokens.GoldLight,
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (book.bookType == FortuneBookType.COMPATIBILITY) {
                    Text(
                        text = "남 ${book.maleBirthLabel.orEmpty()}  ·  여 ${book.femaleBirthLabel.orEmpty()}",
                        color = PremiumTokens.TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onBookmarkClick)
                    .background(
                        if (book.isBookmarked) PremiumTokens.GoldDeep.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (book.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = null,
                    tint = if (book.isBookmarked) PremiumTokens.Gold else PremiumTokens.TextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (book.isBookmarked) "저장됨" else "저장",
                    color = if (book.isBookmarked) PremiumTokens.Gold else PremiumTokens.TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun formatBookDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(timestamp))
}

private fun coverDisplayTitle(book: FortuneBook): String {
    return when (book.bookType) {
        FortuneBookType.COMPATIBILITY -> book.coverTitle.replace(" 두 사람의 ", "\n").replace(" ", "\n")
        FortuneBookType.PERSONAL -> book.coverTitle.replace(" ", "\n")
    }
}

private fun coverKicker(book: FortuneBook): String {
    return when (book.coverTheme) {
        PremiumTopic.ROMANCE.name.lowercase() -> "PREMIUM ROMANCE NOTE"
        PremiumTopic.CAREER.name.lowercase() -> "PREMIUM CAREER NOTE"
        PremiumTopic.MONEY.name.lowercase() -> "PREMIUM MONEY NOTE"
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> "PREMIUM SELF NOTE"
        PremiumTopic.RELATIONSHIP.name.lowercase() -> "PREMIUM RELATION NOTE"
        "compatibility" -> "PREMIUM MATCH NOTE"
        else -> "PREMIUM FORTUNE NOTE"
    }
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
        PremiumTopic.ROMANCE.name.lowercase() ->
            Triple(Color(0xFF1C1018), Color(0xFF2A1520), Color(0xFF8B3A5A).copy(alpha = 0.28f))
        PremiumTopic.CAREER.name.lowercase() ->
            Triple(Color(0xFF0F131C), Color(0xFF172030), Color(0xFF3A6EBB).copy(alpha = 0.28f))
        PremiumTopic.MONEY.name.lowercase() ->
            Triple(Color(0xFF0D1910), Color(0xFF112318), Color(0xFF2A7A52).copy(alpha = 0.28f))
        PremiumTopic.SELF_ESTEEM.name.lowercase() ->
            Triple(Color(0xFF191208), Color(0xFF241A0A), PremiumTokens.Gold.copy(alpha = 0.22f))
        PremiumTopic.RELATIONSHIP.name.lowercase() ->
            Triple(Color(0xFF1C1018), Color(0xFF2A1520), Color(0xFF9B5C3A).copy(alpha = 0.26f))
        "compatibility" ->
            Triple(Color(0xFF191208), Color(0xFF241A0A), PremiumTokens.Gold.copy(alpha = 0.22f))
        else ->
            Triple(PremiumTokens.Ink, PremiumTokens.InkWarm, PremiumTokens.BorderSubtle)
    }
}

private fun leatherCoverColors(theme: String): List<Color> {
    return when (theme) {
        PremiumTopic.MONEY.name.lowercase() ->
            listOf(Color(0xFF0E3D2C), Color(0xFF072418), Color(0xFF020F09))
        PremiumTopic.RELATIONSHIP.name.lowercase() ->
            listOf(Color(0xFF6B3519), Color(0xFF3E1B08), Color(0xFF190A02))
        PremiumTopic.SELF_ESTEEM.name.lowercase() ->
            listOf(Color(0xFF1A1625), Color(0xFF0D0B17), Color(0xFF04030A))
        PremiumTopic.ROMANCE.name.lowercase() ->
            listOf(Color(0xFF4A1528), Color(0xFF280B17), Color(0xFF0F0308))
        PremiumTopic.CAREER.name.lowercase() ->
            listOf(Color(0xFF0D1E3A), Color(0xFF060F1E), Color(0xFF020508))
        else ->
            listOf(Color(0xFF151017), Color(0xFF0A0810), Color(0xFF030205))
    }
}

private fun leatherFoilColor(theme: String): Color {
    return when (theme) {
        PremiumTopic.ROMANCE.name.lowercase() -> Color(0xFFF2C4A0)
        PremiumTopic.CAREER.name.lowercase() -> Color(0xFFB8D4F5)
        PremiumTopic.MONEY.name.lowercase() -> Color(0xFFA8E6C4)
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> Color(0xFFF7D56A)
        PremiumTopic.RELATIONSHIP.name.lowercase() -> Color(0xFFF0C080)
        "compatibility" -> Color(0xFFF7D56A)
        else -> Color(0xFFD4A84B)
    }
}

private fun leatherRibbonColor(theme: String): Color {
    return when (theme) {
        PremiumTopic.ROMANCE.name.lowercase(), "compatibility" -> Color(0xFF8B1A2E)
        PremiumTopic.CAREER.name.lowercase() -> Color(0xFF1A3A6B)
        PremiumTopic.MONEY.name.lowercase() -> Color(0xFF0D4028)
        PremiumTopic.RELATIONSHIP.name.lowercase() -> Color(0xFF7A5010)
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> Color(0xFF3A1565)
        else -> Color(0xFF5A3A10)
    }
}

private fun noteAccentColor(theme: String): Color {
    return when (theme) {
        PremiumTopic.ROMANCE.name.lowercase() -> Color(0xFFBB6680)
        PremiumTopic.CAREER.name.lowercase() -> Color(0xFF5A8EC4)
        PremiumTopic.MONEY.name.lowercase() -> Color(0xFF4AAA7A)
        PremiumTopic.SELF_ESTEEM.name.lowercase() -> PremiumTokens.Gold
        PremiumTopic.RELATIONSHIP.name.lowercase() -> Color(0xFFBB7755)
        "compatibility" -> PremiumTokens.Gold
        else -> PremiumTokens.Gold
    }
}

private fun chapterMascotRes(theme: String, chapterIndex: Int): Int {
    val normalizedTheme = theme.lowercase()
    val orderedMascots = when (normalizedTheme) {
        PremiumTopic.ROMANCE.name.lowercase() -> listOf(
            R.drawable.suri_reader_romance,
            R.drawable.suri_anim_romance_hero,
            R.drawable.suri_reader_compatibility,
            R.drawable.suri_reader_caution,
            R.drawable.suri_reader_action,
            R.drawable.suri_scroll
        )
        "compatibility" -> listOf(
            R.drawable.suri_reader_compatibility,
            R.drawable.suri_reader_romance,
            R.drawable.suri_reader_caution,
            R.drawable.suri_anim_consult_07,
            R.drawable.suri_reader_action,
            R.drawable.suri_hanbok
        )
        PremiumTopic.MONEY.name.lowercase() -> listOf(
            R.drawable.suri_reader_money_cutout,
            R.drawable.suri_anim_money_01,
            R.drawable.suri_coins,
            R.drawable.suri_reader_caution,
            R.drawable.suri_reader_action
        )
        PremiumTopic.CAREER.name.lowercase() -> listOf(
            R.drawable.suri_reader_action,
            R.drawable.suri_anim_writer_hero,
            R.drawable.suri_writer,
            R.drawable.suri_reader_caution
        )
        else -> listOf(
            premiumThemeMascot(theme),
            R.drawable.suri_scroll,
            R.drawable.suri_writer,
            R.drawable.suri_tea,
            R.drawable.suri_reader_caution,
            R.drawable.suri_hanbok
        )
    }.distinct()
    return orderedMascots[chapterIndex % orderedMascots.size]
}
