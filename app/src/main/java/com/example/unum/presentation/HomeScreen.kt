package com.example.unum.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.R
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumTopic
import com.example.unum.ui.components.BookThumbnailCard
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenInput: () -> Unit,
    onOpenResult: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBook: (FortuneBook) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val latestBundle = uiState.latestBundle
    val recentBooks = uiState.savedBooks.take(5)

    MysticBackground(modifier = Modifier.fillMaxSize(), animatedWaves = false) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 104.dp)
        ) {
            item { RoomHeader(onOpenSettings = onOpenSettings) }
            item {
                TodayNumberCard(
                    bundle = latestBundle,
                    onOpenInput = onOpenInput
                )
            }
            item {
                RoomEntryRow(
                    onOpenInput = onOpenInput,
                    onOpenPremium = onOpenPremium,
                    onOpenLibrary = onOpenLibrary
                )
            }
            item {
                SectionTitleBlock(
                    title = "분야별 운세",
                    subtitle = "타로 카드 대신 수리학 숫자 흐름으로 해석합니다"
                )
            }
            item {
                NumberCardRow(
                    bundle = latestBundle,
                    onOpenInput = onOpenInput,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { OneLineRoomNote(latestBundle, onOpenInput) }
            item {
                if (recentBooks.isEmpty()) {
                    EmptyRoomCard(onOpenPremium = onOpenPremium)
                } else {
                    SavedNotesSection(
                        books = recentBooks,
                        onOpenLibrary = onOpenLibrary,
                        onBookClick = { book ->
                            viewModel.selectSavedBook(book)
                            onOpenBook(book)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomHeader(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("오늘의 수리 리포트", color = TextPrimary, style = MaterialTheme.typography.displayMedium)
                    Text("숫자는 점수가 아니라 오늘을 읽는 언어예요", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Surface2)
                .border(1.dp, Border, CircleShape)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = null, tint = Rose, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun TodayNumberCard(
    bundle: NumerologyResultBundle?,
    onOpenInput: () -> Unit
) {
    val today = LocalDate.now()
    val todayNumber = bundle?.let { dailyCoreNumber(it, today) }
    val summary = if (bundle == null) {
        "생년월일을 입력하면 오늘 날짜와 내 숫자를 함께 읽어 매일 다른 핵심수를 보여드려요."
    } else {
        dailyCoreSummary(todayNumber ?: 1, today)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (bundle == null) 3.dp else 10.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (bundle == null) {
                    Brush.verticalGradient(listOf(Surface, Color(0xFFF8FBFF)))
                } else {
                    Brush.linearGradient(listOf(Accent, Color(0xFF1E40AF)))
                }
            )
            .border(
                1.dp,
                if (bundle == null) Border else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(18.dp)
            )
            .then(if (bundle == null) Modifier.clickable(onClick = onOpenInput) else Modifier)
            .padding(18.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Accent.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.18f),
                    radius = size.minDimension * 0.42f
                ),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.88f, size.height * 0.18f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "TODAY NUMBER",
                        color = if (bundle == null) Accent else Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        if (bundle == null) "입력이 필요해요" else "오늘의 핵심수",
                        color = if (bundle == null) TextPrimary else Color.White,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        if (bundle == null) "매일 바뀌는 리딩 준비" else dailyCoreTitle(todayNumber ?: 1),
                        color = if (bundle == null) TextSecondary else Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                NumberMedallion(number = todayNumber, onDark = bundle != null)
            }
            Text(
                summary,
                color = if (bundle == null) TextSecondary else Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyLarge
            )
            if (bundle == null) {
                Text("생년월일 입력하기", color = Accent, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun NumberMedallion(number: Int?, onDark: Boolean = false) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(if (onDark) Color.White.copy(alpha = 0.18f) else Accent.copy(alpha = 0.08f))
            .border(1.dp, if (onDark) Color.White.copy(alpha = 0.24f) else Accent.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(number?.toString() ?: "?", color = if (onDark) Color.White else Accent, style = MaterialTheme.typography.displayLarge)
    }
}

@Composable
private fun SectionTitleBlock(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RoomEntryRow(
    onOpenInput: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val entries = listOf(
        RoomEntry("생년월일\n리딩", Icons.Rounded.EditCalendar, Accent, Accent.copy(alpha = 0.10f), onOpenInput),
        RoomEntry("프리미엄\n책자", Icons.Rounded.AutoStories, Gold, Color(0xFFFFF8E7), onOpenPremium),
        RoomEntry("보관함", Icons.Rounded.Bookmarks, Mint, Color(0xFFEAFBF4), onOpenLibrary)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        entries.forEach { entry ->
            RoomEntryCard(entry, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RoomEntryCard(entry: RoomEntry, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(entry.background)
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .clickable(onClick = entry.onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(entry.icon, contentDescription = null, tint = entry.color, modifier = Modifier.size(22.dp))
            Text(entry.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun NumberCardRow(
    bundle: NumerologyResultBundle?,
    onOpenInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (bundle == null) {
        DailyFortuneInputPrompt(onOpenInput = onOpenInput, modifier = modifier)
        return
    }

    val today = LocalDate.now()
    val dailyNumber = dailyCoreNumber(bundle, today)
    val cards = listOf(
        NumberCard("LOVE", "연애", "오늘의 온도", Rose, PremiumTopic.ROMANCE, R.drawable.numerology_love, dailyTopicMessage(PremiumTopic.ROMANCE, dailyNumber, today)),
        NumberCard("WORK", "일과 진로", "오늘의 방향", Accent, PremiumTopic.CAREER, R.drawable.numerology_career, dailyTopicMessage(PremiumTopic.CAREER, dailyNumber, today)),
        NumberCard("MONEY", "돈", "오늘의 관리", Mint, PremiumTopic.MONEY, R.drawable.numerology_money, dailyTopicMessage(PremiumTopic.MONEY, dailyNumber, today)),
        NumberCard("STUDY", "배움", "오늘의 집중", Gold, PremiumTopic.CAREER, R.drawable.numerology_study, dailyStudyMessage(dailyNumber, today)),
        NumberCard("SELF", "나 자신", "오늘의 중심", Color(0xFF0891B2), PremiumTopic.SELF_ESTEEM, R.drawable.numerology_self, dailyTopicMessage(PremiumTopic.SELF_ESTEEM, dailyNumber, today))
    )
    val listState = rememberLazyListState()
    var selectedIndex by remember { mutableIntStateOf(0) }
    val focusedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                0
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minBy { item ->
                    kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                }.index
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp)
        ) {
            itemsIndexed(cards) { index, card ->
                NumberReadingCard(
                    card = card,
                    focused = index == focusedIndex || index == selectedIndex,
                    onClick = { selectedIndex = index }
                )
            }
        }
        DailyTopicFortuneCard(card = cards[selectedIndex])
    }
}

@Composable
private fun DailyFortuneInputPrompt(onOpenInput: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFFBF2))
            .border(1.dp, Gold.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenInput)
            .padding(18.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.numerology_self),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.72f)),
                contentScale = ContentScale.Fit
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("분야별 오늘 운세를 보려면 생년월일이 필요해요", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("입력 후에는 오늘 날짜를 기준으로 연애, 일, 돈, 배움, 자기관리 운세가 매일 새로 바뀝니다.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text("생년월일 입력하기", color = Gold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DailyTopicFortuneCard(card: NumberCard) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(card.color.copy(alpha = 0.09f))
            .border(1.dp, card.color.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(card.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.72f))
                    .padding(5.dp),
                contentScale = ContentScale.Fit
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${card.title} 오늘의 운세", color = card.color, style = MaterialTheme.typography.labelLarge)
                Text(card.todayMessage, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun NumberReadingCard(card: NumberCard, focused: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val lift by animateDpAsState(
        targetValue = when {
            pressed -> 8.dp
            focused -> 5.dp
            else -> 0.dp
        },
        label = "numberCardLift"
    )
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 1.04f
            focused -> 1.02f
            else -> 0.97f
        },
        label = "numberCardScale"
    )
    val shadow by animateDpAsState(
        targetValue = if (focused || pressed) 14.dp else 4.dp,
        label = "numberCardShadow"
    )

    Column(
        modifier = Modifier
            .width(132.dp)
            .height(160.dp)
            .offset(y = -lift)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(shadow, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        card.color.copy(alpha = if (focused || pressed) 0.28f else 0.20f),
                        card.color.copy(alpha = if (focused || pressed) 0.16f else 0.11f),
                        Color.White.copy(alpha = 0.86f)
                    )
                )
            )
            .border(1.dp, card.color.copy(alpha = if (focused || pressed) 0.56f else 0.30f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(card.word, color = card.color, style = MaterialTheme.typography.labelLarge)
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.72f))
                .border(1.dp, card.color.copy(alpha = 0.30f), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(card.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(card.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(card.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OneLineRoomNote(bundle: NumerologyResultBundle?, onOpenInput: () -> Unit) {
    val text = bundle?.freeReading?.opening ?: bundle?.freeReading?.core
        ?: "생년월일을 입력하면 나의 성향 흐름을 짧게 미리볼 수 있어요."
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(22.dp))
            .then(if (bundle == null) Modifier.clickable(onClick = onOpenInput) else Modifier)
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(if (bundle == null) "성향 미리보기 준비" else "성향 한 줄", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            if (bundle == null) {
                Text("생년월일 입력하기", color = Accent, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EmptyRoomCard(onOpenPremium: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFFFFBF2))
            .border(1.dp, Gold.copy(alpha = 0.32f), RoundedCornerShape(22.dp))
            .clickable(onClick = onOpenPremium)
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("아직 저장된 운세노트가 없어요", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text("프리미엄 책자를 만들면 보관함에 쌓입니다.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SavedNotesSection(
    books: List<FortuneBook>,
    onOpenLibrary: () -> Unit,
    onBookClick: (FortuneBook) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitleBlock(title = "저장된 운세노트", subtitle = "최근 만든 책자를 다시 펼쳐보세요")
            Text(
                "전체",
                color = Accent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onOpenLibrary)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(24.dp))
                .padding(vertical = 18.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 18.dp)
            ) {
                items(books, key = { it.bookId }) { book ->
                    BookThumbnailCard(
                        book = book,
                        modifier = Modifier.size(width = 132.dp, height = 178.dp),
                        compact = true,
                        onClick = { onBookClick(book) }
                    )
                }
            }
        }
    }
}

private fun dailyCoreNumber(bundle: NumerologyResultBundle, today: LocalDate): Int {
    val total = digitSum(today.year) +
        today.monthValue +
        today.dayOfMonth +
        bundle.numbers.destiny +
        bundle.numbers.early +
        bundle.numbers.middle +
        bundle.numbers.late
    return reduceToSingleDigit(total)
}

private fun dailyCoreTitle(number: Int): String = when (number) {
    1 -> "시작형"
    2 -> "조율형"
    3 -> "표현형"
    4 -> "정리형"
    5 -> "전환형"
    6 -> "돌봄형"
    7 -> "집중형"
    8 -> "실행형"
    else -> "마무리형"
}

private fun dailyCoreSummary(number: Int, today: LocalDate): String = when (number) {
    1 -> "시작의 기운이 선명한 날입니다. 작게 열린 가능성이 다음 흐름을 부를 수 있어요."
    2 -> "조율의 기운이 강한 날입니다. 속도보다 균형이 맞을 때 일이 부드럽게 이어집니다."
    3 -> "표현의 기운이 살아나는 날입니다. 생각과 감정이 밖으로 흐를 때 분위기가 가벼워집니다."
    4 -> "기준의 기운이 또렷한 날입니다. 복잡한 마음도 질서가 잡히면 한결 안정됩니다."
    5 -> "변화의 기운이 들어오는 날입니다. 익숙한 흐름 안에서 새로운 가능성이 보일 수 있어요."
    6 -> "돌봄의 기운이 강한 날입니다. 책임감과 다정함이 함께 드러나기 쉽습니다."
    7 -> "집중의 기운이 깊어지는 날입니다. 서두른 결론보다 관찰의 힘이 더 크게 작용합니다."
    8 -> "현실 감각이 살아나는 날입니다. 말보다 결과의 결이 더 잘 보이는 흐름입니다."
    else -> "마무리의 기운이 선명한 날입니다. 남길 것과 흘려보낼 것이 자연스럽게 구분됩니다."
} + " (${today.monthValue}/${today.dayOfMonth} 기준)"

private fun dailyTopicMessage(topic: PremiumTopic, dailyNumber: Int, today: LocalDate): String {
    val messages = when (topic) {
        PremiumTopic.ROMANCE -> listOf(
            "마음의 온도가 예민하게 느껴지는 흐름입니다. 결론보다 분위기의 변화가 먼저 보일 수 있어요.",
            "상대의 반응보다 내 마음의 속도가 더 크게 느껴질 수 있습니다. 호감과 부담의 경계가 중요합니다.",
            "긴 설명보다 짧은 진심이 더 잘 닿는 흐름입니다. 관계의 공기가 조금 부드러워질 수 있어요.",
            "기다림이 길어질수록 마음이 앞서가기 쉽습니다. 기대가 가벼울수록 관계의 온도도 편안해집니다."
        )
        PremiumTopic.CAREER -> listOf(
            "큰 결정의 압박보다 일의 우선순위가 더 또렷하게 보이는 흐름입니다. 기준이 잡히면 마음도 가벼워집니다.",
            "새 일을 늘리는 힘보다 이미 시작한 일을 정돈하는 힘이 강합니다. 작은 완료감이 자신감을 만듭니다.",
            "일의 방향이 흔들릴 때는 덜어내는 선택도 전진처럼 작용합니다. 모든 부담을 성과로 바꿀 필요는 없어요.",
            "진로 고민은 감정과 현실 조건이 함께 움직이는 흐름입니다. 체력과 마음의 여유가 중요한 단서가 됩니다."
        )
        PremiumTopic.MONEY -> listOf(
            "돈이 새는 통로가 눈에 들어오기 쉬운 흐름입니다. 큰 변화보다 안정감의 회복이 더 중요합니다.",
            "크게 버는 생각보다 안전하게 남기는 감각이 강한 날입니다. 충동보다 기준이 운을 안정시킵니다.",
            "돈 문제는 감정과 섞일수록 흐려질 수 있습니다. 마음이 급할수록 기준의 존재가 더 중요해집니다.",
            "작은 수입이나 할인보다 흐름을 안정시키는 선택이 더 잘 맞습니다. 돈의 속도보다 균형이 먼저입니다."
        )
        PremiumTopic.SELF_ESTEEM -> listOf(
            "비교의 감각이 올라오기 쉬운 흐름입니다. 스스로를 몰아붙이지 않을 때 중심이 더 잘 살아납니다.",
            "증명보다 회복의 결이 중요한 날입니다. 쉬어가는 감각도 자기 신뢰의 일부가 될 수 있습니다.",
            "마음이 흔들릴 때는 큰 목표보다 작은 안정감이 더 크게 작용합니다. 중심이 천천히 돌아오는 흐름입니다.",
            "다른 사람의 속도가 크게 보일 수 있습니다. 내 리듬을 잃지 않는 감각이 중요합니다."
        )
        PremiumTopic.RELATIONSHIP -> listOf(
            "관계의 거리가 다시 보이는 흐름입니다. 가까움과 피로감의 경계가 조금 더 선명해질 수 있어요.",
            "괜찮은 척 넘긴 감정이 마음에 남기 쉬운 날입니다. 말하지 않은 마음은 오해처럼 커질 수 있습니다.",
            "부탁과 거절의 선이 관계 피로를 좌우할 수 있습니다. 착함보다 솔직한 균형이 더 편안합니다.",
            "오래 미뤄둔 대화의 공기가 떠오를 수 있습니다. 결론보다 분위기가 먼저 움직이는 흐름입니다."
        )
    }
    return messages[dailyMessageIndex(topic.ordinal, dailyNumber, today, messages.size)]
}

private fun dailyStudyMessage(dailyNumber: Int, today: LocalDate): String {
    val messages = listOf(
        "많이 넣는 흐름보다 다시 읽는 흐름에 가깝습니다. 익숙한 내용도 다른 결로 보일 수 있어요.",
        "새 내용을 욕심내기보다 핵심 개념 하나가 또렷해지는 날입니다. 깊이가 속도보다 유리합니다.",
        "집중이 쉽게 흩어질 수 있지만, 짧은 반복에는 힘이 붙습니다. 작은 단위의 배움이 잘 맞습니다.",
        "복습의 결이 좋은 흐름입니다. 이미 아는 내용 안에서 새로운 연결이 보일 수 있습니다."
    )
    return messages[dailyMessageIndex(4, dailyNumber, today, messages.size)]
}

private fun dailyMessageIndex(topicSeed: Int, dailyNumber: Int, today: LocalDate, size: Int): Int {
    return (today.dayOfYear + dailyNumber * 3 + topicSeed * 5).floorMod(size)
}

private fun digitSum(value: Int): Int = value.absoluteValue.toString().sumOf { it.digitToInt() }

private fun reduceToSingleDigit(value: Int): Int {
    var current = value.absoluteValue
    while (current > 9) {
        current = digitSum(current)
    }
    return current.coerceAtLeast(1)
}

private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus

private data class RoomEntry(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val background: Color,
    val onClick: () -> Unit
)

private data class NumberCard(
    val word: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val topic: PremiumTopic,
    @param:DrawableRes val imageRes: Int,
    val todayMessage: String
)

