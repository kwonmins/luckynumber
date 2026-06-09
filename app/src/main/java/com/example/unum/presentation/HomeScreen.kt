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
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFF8FBFF))
            .border(1.dp, Color(0xFFDCE6F3), RoundedCornerShape(26.dp))
            .then(if (bundle == null) Modifier.clickable(onClick = onOpenInput) else Modifier)
            .padding(24.dp)
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
                    Text("TODAY NUMBER", color = Accent, style = MaterialTheme.typography.labelLarge)
                    Text(
                        if (bundle == null) "입력이 필요해요" else "오늘의 핵심수",
                        color = TextPrimary,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        if (bundle == null) "매일 바뀌는 리딩 준비" else dailyCoreTitle(todayNumber ?: 1),
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                NumberMedallion(number = todayNumber)
            }
            Text(summary, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
            if (bundle == null) {
                Text("생년월일 입력하기", color = Accent, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun NumberMedallion(number: Int?) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(CircleShape)
            .background(Accent.copy(alpha = 0.08f))
            .border(1.dp, Accent.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(number?.toString() ?: "?", color = Accent, style = MaterialTheme.typography.displayLarge)
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
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp)
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
            .width(148.dp)
            .height(174.dp)
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
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(card.word, color = card.color, style = MaterialTheme.typography.labelLarge)
        Box(
            modifier = Modifier
                .size(64.dp)
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
    val text = bundle?.content?.lifeRecord?.oneLineAdvice
        ?: "생년월일을 입력하면 오늘 날짜에 맞춘 한 줄 조언이 표시됩니다."
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
            Text(if (bundle == null) "오늘의 한 줄 조언 준비" else "오늘의 한 줄 조언", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
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
    1 -> "오늘은 작게라도 먼저 시작할 때 흐름이 열립니다. 완벽한 계획보다 첫 행동 하나가 더 큰 단서가 돼요."
    2 -> "오늘은 속도를 내기보다 균형을 맞추는 쪽이 좋습니다. 대화와 조율을 먼저 두면 일이 부드러워집니다."
    3 -> "오늘은 생각을 밖으로 꺼낼수록 운이 움직입니다. 말, 글, 기록처럼 표현하는 행동에 힘이 붙어요."
    4 -> "오늘은 정리와 기준 세우기에 알맞습니다. 해야 할 일을 작게 나누면 마음의 압박이 줄어듭니다."
    5 -> "오늘은 익숙한 루틴에 작은 변화를 주기 좋습니다. 다만 충동보다 실험이라는 마음으로 움직여보세요."
    6 -> "오늘은 나와 주변을 돌보는 흐름이 강합니다. 책임을 다하되, 혼자 떠안지 않는 균형이 중요해요."
    7 -> "오늘은 깊게 보고 천천히 결정하는 날입니다. 당장 결론보다 관찰과 확인이 더 좋은 선택을 만듭니다."
    8 -> "오늘은 현실적인 실행력이 올라옵니다. 돈, 일, 약속처럼 구체적인 결과가 필요한 일에 집중해보세요."
    else -> "오늘은 마무리와 정리에 힘이 있습니다. 끝낼 것은 끝내고, 남길 것은 차분히 남겨보세요."
} + " (${today.monthValue}/${today.dayOfMonth} 기준)"

private fun dailyTopicMessage(topic: PremiumTopic, dailyNumber: Int, today: LocalDate): String {
    val messages = when (topic) {
        PremiumTopic.ROMANCE -> listOf(
            "마음을 확인하고 싶다면 결론부터 묻기보다 가벼운 안부를 먼저 건네보세요. 오늘은 부드러운 속도가 관계를 살립니다.",
            "상대의 반응을 해석하기 전에 내가 원하는 온도를 먼저 정리해보세요. 감정의 방향이 훨씬 선명해집니다.",
            "오늘은 긴 설명보다 짧은 진심이 더 잘 닿습니다. 부담 없는 표현 하나가 관계의 공기를 바꿀 수 있어요.",
            "기다림이 길어졌다면 혼자 단정하지 않는 게 좋습니다. 확인은 짧게, 기대는 가볍게 두세요."
        )
        PremiumTopic.CAREER -> listOf(
            "큰 결정을 밀어붙이기보다 지금 맡은 일의 우선순위를 다시 세우기 좋은 날입니다. 기준 하나가 다음 선택을 편하게 만듭니다.",
            "오늘은 새 일을 벌이기보다 이미 시작한 일을 정돈할수록 흐름이 좋아집니다. 작은 완료가 자신감을 만듭니다.",
            "일의 방향이 흔들린다면 해야 할 일과 하지 않아도 되는 일을 나눠보세요. 덜어내는 선택도 전진입니다.",
            "진로 고민은 감정만으로 결론 내기 어렵습니다. 오늘은 조건, 시간, 체력의 현실값을 먼저 적어보세요."
        )
        PremiumTopic.MONEY -> listOf(
            "새로운 지출을 막는 것보다 돈이 새는 통로를 찾는 데 운이 붙습니다. 작은 고정비 하나만 점검해도 마음이 가벼워집니다.",
            "오늘은 크게 벌 생각보다 안전하게 남기는 감각이 중요합니다. 지출 전 한 번 멈추면 후회가 줄어듭니다.",
            "돈 문제는 감정과 섞일수록 흐려집니다. 필요한 돈, 쓰고 싶은 돈, 미뤄도 되는 돈을 따로 적어보세요.",
            "작은 수입이나 할인보다 흐름을 안정시키는 선택이 더 좋습니다. 오늘은 기록이 곧 운 관리입니다."
        )
        PremiumTopic.SELF_ESTEEM -> listOf(
            "비교가 올라오면 잠깐 멈추고 내가 이미 해낸 일을 먼저 세어보세요. 오늘의 운은 나를 몰아붙이지 않을 때 더 잘 열립니다.",
            "오늘은 나를 증명하려 애쓰기보다 회복시키는 선택이 필요합니다. 잘 쉬는 것도 중요한 흐름입니다.",
            "마음이 흔들리면 큰 목표 대신 지킬 수 있는 약속 하나만 정해보세요. 작은 성공이 중심을 다시 세웁니다.",
            "다른 사람의 속도에 맞추느라 내 리듬을 잃기 쉽습니다. 오늘은 내 페이스를 조용히 지켜주세요."
        )
        PremiumTopic.RELATIONSHIP -> listOf(
            "오늘은 관계의 거리를 다시 조절하기 좋습니다. 가까워질 사람과 잠시 쉬어갈 사람을 구분해보세요.",
            "괜찮은 척 넘긴 감정이 있다면 짧게라도 표현해보세요. 말하지 않은 마음은 쉽게 오해가 됩니다.",
            "부탁과 거절의 선을 부드럽게 세우면 관계 피로가 줄어듭니다. 오늘은 착한 척보다 솔직한 균형이 좋아요.",
            "오래 미뤄둔 대화가 있다면 결론보다 분위기부터 열어보세요. 작은 시작이 충분합니다."
        )
    }
    return messages[dailyMessageIndex(topic.ordinal, dailyNumber, today, messages.size)]
}

private fun dailyStudyMessage(dailyNumber: Int, today: LocalDate): String {
    val messages = listOf(
        "많이 하는 날보다 다시 잡는 날에 가깝습니다. 익숙한 내용을 한 번 더 정리하면 흩어진 감각이 제자리로 돌아옵니다.",
        "오늘은 새 내용을 욕심내기보다 핵심 개념 하나를 정확히 붙잡는 게 좋습니다. 깊이가 속도보다 유리해요.",
        "집중이 흐트러지면 시간을 늘리지 말고 단위를 줄여보세요. 짧은 반복이 오늘의 배움운을 살립니다.",
        "기록과 복습에 좋은 흐름입니다. 손으로 한 번 정리하면 머릿속 연결이 훨씬 단단해집니다."
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
