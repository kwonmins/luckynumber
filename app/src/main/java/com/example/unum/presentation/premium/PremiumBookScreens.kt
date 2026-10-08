package com.example.unum.presentation.premium
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.TextMuted

import com.example.unum.presentation.*

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.BookSpecs
import com.example.unum.data.model.BookThemeId
import com.example.unum.data.model.BookThemeSpecs
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.resolvedThemeId
import com.example.unum.presentation.spec.FeatureSpecs
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun BookCoverScreen(
    book: FortuneBook?,
    onReset: () -> Unit,
    onOpen: () -> Unit,
    flipModifier: Modifier
) {
    var revealStarted by remember(book?.bookId) { mutableStateOf(false) }
    LaunchedEffect(book?.bookId) { revealStarted = true }
    val reveal by animateFloatAsState(
        targetValue = if (revealStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 820, easing = FastOutSlowInEasing),
        label = "bookCoverReveal"
    )
    BookStageScaffold {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = reveal
                    scaleX = 0.94f + reveal * 0.06f
                    scaleY = 0.94f + reveal * 0.06f
                    translationY = (1f - reveal) * 28f
                }
                .bookTurnGestures(onPrevious = null, onNext = onOpen)
                .then(flipModifier),
            contentAlignment = Alignment.Center
        ) {
            PremiumBookCover(book = book, modifier = Modifier.fillMaxSize())
            CoverRevealAura(reveal = reveal, identity = bookIdentityFor(book), modifier = Modifier.fillMaxSize())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("처음으로", onReset, Modifier.weight(1f))
            GradientButton("노트 펼치기", onOpen, Modifier.weight(1f))
        }
    }
}

@Composable
internal fun CoverRevealAura(reveal: Float, identity: BookIdentityTheme, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val shadowAlpha = (1f - reveal).coerceIn(0f, 1f)
        drawRect(Color.Black.copy(alpha = shadowAlpha * 0.42f))
        val lineProgress = reveal.coerceIn(0f, 1f)
        val left = size.width * 0.08f
        val top = size.height * 0.03f
        val right = size.width * (0.08f + 0.84f * lineProgress)
        val bottom = size.height * 0.97f
        drawLine(identity.foil.copy(alpha = 0.60f * lineProgress), Offset(left, top), Offset(right, top), strokeWidth = 2.2f)
        drawLine(identity.foil.copy(alpha = 0.42f * lineProgress), Offset(left, bottom), Offset(right, bottom), strokeWidth = 1.6f)
        drawCircle(
            identity.foil.copy(alpha = 0.18f * (1f - kotlin.math.abs(0.55f - reveal)).coerceIn(0f, 1f)),
            radius = size.minDimension * 0.42f,
            center = Offset(size.width * 0.50f, size.height * 0.40f)
        )
    }
}

@Composable
internal fun BookTocScreen(
    book: FortuneBook?,
    onBack: () -> Unit,
    onRead: () -> Unit,
    flipModifier: Modifier
) {
    val identity = bookIdentityFor(book)
    val tocTitles = if (book?.bookType == FortuneBookType.COMPATIBILITY) {
        listOf("남자 성향", "여자 성향", "둘의 궁합수", "생활 흐름")
    } else {
        listOf("지혜의 본질", "상황별 해석", "주의할 장면", "이번 달 행동 지침")
    }
    BookStageScaffold {
        PaperPage(
            identity = identity,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .bookTurnGestures(onPrevious = onBack, onNext = onRead)
                .then(flipModifier)
        ) {
            Text("목차", color = TextPrimary, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(identity.caption, color = identity.accent, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(2.dp))
            tocTitles.forEachIndexed { index, title ->
                TocLine(index + 1, title, "p.${4 + index * 10}", identity, onClick = onRead)
            }
            Spacer(Modifier.weight(1f))
            Text("03", color = identity.accent.copy(alpha = 0.48f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("돌아가기", onBack, Modifier.weight(1f))
            GradientButton("해석서 읽기", onRead, Modifier.weight(1f))
        }
    }
}

@Composable
internal fun BookDetailScreen(
    book: FortuneBook?,
    concern: String,
    onBack: () -> Unit,
    onShare: (FortuneBook) -> Unit,
    onArchive: () -> Unit,
    flipModifier: Modifier
) {
    val chapter = book?.chapters?.firstOrNull()
    val identity = bookIdentityFor(book)
    val isCompatibility = book?.bookType == FortuneBookType.COMPATIBILITY
    BookStageScaffold {
        PaperPage(
            identity = identity,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .bookTurnGestures(onPrevious = onBack, onNext = null)
                .then(flipModifier)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(24.dp).background(identity.accent, CircleShape), contentAlignment = Alignment.Center) {
                    Text("2", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        if (isCompatibility) "두 사람 궁합 해석 비책" else "상황별 고민 해결 비책",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        book?.concernTopic ?: if (isCompatibility) "궁합 특화 카운슬링" else "연애 특화 카운슬링",
                        color = identity.accent,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = { book?.let(onShare) }) {
                    Icon(Icons.Rounded.Share, contentDescription = "공유", tint = identity.accent)
                }
            }
            DetailBox(
                if (isCompatibility) "궁금한 관계 질문" else "던지신 질문",
                concern.ifBlank {
                    book?.concernText.orEmpty().ifBlank {
                        if (isCompatibility) "두 사람 사이의 전반적인 궁합 흐름" else "지금 마음속에서 가장 자주 떠오르는 고민"
                    }
                },
                identity.accentDeep
            )
            DetailBox(if (isCompatibility) "마찰이 생기는 장면" else "주의할 장면", chapter?.highlightQuote ?: book?.summary.orEmpty(), Rose)
            DetailBox(
                if (isCompatibility) "관계 흐름 포인트" else "읽는 포인트",
                chapter?.actionTip?.joinToString("\n") { "• $it" } ?: if (isCompatibility) {
                    "서로의 속도와 말의 온도가 관계의 편안함을 좌우하는 흐름입니다."
                } else {
                    "지금 반복되는 분위기를 차분히 읽는 것이 리포트의 핵심입니다."
                },
                identity.accent
            )
            Spacer(Modifier.weight(1f))
            Text("15", color = identity.accent.copy(alpha = 0.48f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("목차보기", onBack, Modifier.weight(1f))
            GradientButton("보관함에 보관", onArchive, Modifier.weight(1f))
        }
    }
}

@Composable
internal fun BookStageScaffold(content: @Composable ColumnScope.() -> Unit) {
    MysticBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

internal fun Modifier.bookTurnGestures(
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?
): Modifier = this
    .pointerInput(onPrevious, onNext) {
        detectTapGestures { offset ->
            when {
                offset.x > size.width * 0.62f -> onNext?.invoke()
                offset.x < size.width * 0.38f -> onPrevious?.invoke()
            }
        }
    }
    .pointerInput(onPrevious, onNext) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragStart = { totalDrag = 0f },
            onHorizontalDrag = { change, dragAmount ->
                totalDrag += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    totalDrag < -72f -> onNext?.invoke()
                    totalDrag > 72f -> onPrevious?.invoke()
                }
            }
        )
    }

@Composable
internal fun PremiumBookCover(book: FortuneBook?, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        com.example.unum.ui.components.MoonlightBookCover(book, Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.985f))
    }
}

@Composable
internal fun PaperPage(
    identity: BookIdentityTheme,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.985f)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(identity.coverTop, identity.coverMid, identity.coverBottom)))
            .border(1.dp, Color.Black.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(18) { index ->
                val y = size.height * (index + 1) / 19f
                drawLine(Color.White.copy(alpha = 0.022f), Offset(16f, y), Offset(size.width - 16f, y - 4f), strokeWidth = 1f)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(18.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f))))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(10.dp)
                .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(identity.pageTop, identity.page, Color(0xFFF4EBD9))))
                .border(1.dp, identity.edge, RoundedCornerShape(8.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
internal fun TocLine(index: Int, title: String, page: String, identity: BookIdentityTheme, onClick: (() -> Unit)? = null) {
    val focus by animateFloatAsState(
        targetValue = if (index == 2) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "tocFocus"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = 1f + focus * 0.025f
                scaleY = 1f + focus * 0.025f
                translationX = focus * 6f
            }
            .clip(RoundedCornerShape(8.dp))
            .background(if (index == 2) identity.tint.copy(alpha = 0.34f) else Color.Transparent)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(25.dp).background(identity.accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Text(index.toString(), color = identity.accent, style = MaterialTheme.typography.bodySmall)
            }
            Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        }
        Text(page, color = identity.accent.copy(alpha = 0.52f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun DetailBox(title: String, body: String, color: Color) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Color.White.copy(alpha = 0.56f),
        borderColor = color.copy(alpha = 0.18f),
        contentPadding = 0
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(title, color = color, style = MaterialTheme.typography.labelLarge)
                Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

internal data class BookIdentityTheme(
    val shortName: String,
    val caption: String,
    val accent: Color,
    val accentDeep: Color,
    val ribbon: Color,
    val foil: Color,
    val stitch: Color,
    val coverTop: Color,
    val coverMid: Color,
    val coverBottom: Color,
    val tint: Color,
    val page: Color,
    val pageTop: Color,
    val edge: Color
)

internal fun bookIdentityFor(book: FortuneBook?): BookIdentityTheme {
    val themeId = book?.resolvedThemeId() ?: BookThemeId.ROMANCE
    val theme = BookThemeSpecs.get(themeId)
    val spec = book?.let(BookSpecs::forBook) ?: BookSpecs.forTheme(themeId)
    return BookIdentityTheme(
        shortName = theme.displayName,
        caption = spec?.coverKicker ?: "PREMIUM FORTUNE NOTE",
        accent = theme.readerAccentColor.toComposeColor(),
        accentDeep = theme.readerAccentDeepColor.toComposeColor(),
        ribbon = theme.pdfRibbonColor.toComposeColor(),
        foil = theme.pdfFoilColor.toComposeColor(),
        stitch = theme.readerStitchColor.toComposeColor(),
        coverTop = theme.readerCoverTopColor.toComposeColor(),
        coverMid = theme.readerCoverMidColor.toComposeColor(),
        coverBottom = theme.readerCoverBottomColor.toComposeColor(),
        tint = theme.readerTintColor.toComposeColor(),
        page = theme.readerPageColor.toComposeColor(),
        pageTop = theme.readerPageTopColor.toComposeColor(),
        edge = theme.readerEdgeColor.toComposeColor()
    )
}

internal fun Long.toComposeColor(): Color = Color(this)

internal fun Int.toComposeColor(): Color = Color(toLong() and 0xFFFFFFFF)

internal fun PremiumTopic.bookLabel(): String = FeatureSpecs.planFor(this).bookLabel
