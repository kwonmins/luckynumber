package com.example.unum.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.ui.components.BookThumbnailCard
import com.example.unum.ui.components.DailyFortuneTopicSection
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.TodayFortuneCard
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

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenInput: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBook: (FortuneBook) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val latestBundle = uiState.latestBundle
    val dailyFortune = viewModel.dailyFortune()
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
                TodayFortuneCard(
                    result = dailyFortune,
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
                DailyFortuneTopicSection(
                    result = dailyFortune,
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

private data class RoomEntry(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val background: Color,
    val onClick: () -> Unit
)

