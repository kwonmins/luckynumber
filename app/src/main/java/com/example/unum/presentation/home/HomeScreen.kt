package com.example.unum.presentation.home

import com.example.unum.presentation.*

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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.AuthState
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.ui.components.BookThumbnailCard
import com.example.unum.ui.components.DailyFortuneTopicSection
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.TodayFortuneCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.DeepNavy
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val dailyFortune = viewModel.dailyFortune()
    val displayName = (uiState.authState as? AuthState.SignedIn)?.user?.displayName ?: "오늘의 나"

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(22.dp),
            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 108.dp)
        ) {
            item { AlmanacHeader(displayName = displayName, onOpenSettings = onOpenSettings) }
            item { TodayFortuneCard(result = dailyFortune, onOpenInput = onOpenInput) }
            item {
                FortuneActionGrid(
                    onOpenInput = onOpenInput,
                    onOpenPremium = onOpenPremium,
                    onOpenLibrary = onOpenLibrary
                )
            }
            item {
                EditorialSectionHeader(
                    number = "01",
                    title = "오늘의 다섯 가지 흐름",
                    description = "가장 궁금한 영역부터 가볍게 펼쳐보세요."
                )
            }
            item {
                DailyFortuneTopicSection(
                    result = dailyFortune,
                    onOpenInput = onOpenInput,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                EditorialSectionHeader(
                    number = "02",
                    title = "나를 설명하는 한 문장",
                    description = "최근 입력한 생년월일을 바탕으로 읽었어요."
                )
            }
            item { InsightNote(bundle = uiState.latestBundle, onOpenInput = onOpenInput) }
            item {
                if (uiState.savedBooks.isEmpty()) {
                    EmptyArchiveCard(onOpenPremium = onOpenPremium)
                } else {
                    SavedBooksShelf(
                        books = uiState.savedBooks.take(5),
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
private fun AlmanacHeader(displayName: String, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "UNUM · DAILY ALMANAC",
                color = Accent,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "안녕하세요, $displayName",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN).format(Date()),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(17.dp))
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = "설정 열기", tint = DeepNavy, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun FortuneActionGrid(
    onOpenInput: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PrimaryActionCard(
            title = "내 수리\n다시 읽기",
            description = "생년월일로 확인",
            icon = Icons.Rounded.CalendarMonth,
            onClick = onOpenInput,
            modifier = Modifier.weight(1.08f).height(164.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactActionCard(
                title = "깊은 운세노트",
                subtitle = "고민을 담아 읽기",
                icon = Icons.Rounded.AutoStories,
                tint = Gold,
                onClick = onOpenPremium,
                modifier = Modifier.fillMaxWidth().height(77.dp)
            )
            CompactActionCard(
                title = "나의 보관함",
                subtitle = "지난 기록 펼치기",
                icon = Icons.Rounded.Bookmarks,
                tint = Mint,
                onClick = onOpenLibrary,
                modifier = Modifier.fillMaxWidth().height(77.dp)
            )
        }
    }
}

@Composable
private fun PrimaryActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Accent)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.13f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Surface, modifier = Modifier.size(21.dp))
            }
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = Surface.copy(alpha = 0.82f),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Surface, style = MaterialTheme.typography.titleLarge)
            Text(description, color = Surface.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CompactActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(tint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EditorialSectionHeader(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(number, color = Accent, style = MaterialTheme.typography.labelLarge)
        Box(Modifier.padding(top = 9.dp).size(width = 34.dp, height = 1.dp).background(Border))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(description, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InsightNote(bundle: NumerologyResultBundle?, onOpenInput: () -> Unit) {
    val title = bundle?.content?.destinyProfile?.title ?: "아직 나의 수리를 읽지 않았어요"
    val body = bundle?.freeReading?.opening
        ?: bundle?.content?.destinyProfile?.summary
        ?: "생년월일을 입력하면 당신의 기본 성향을 한 문장으로 정리해드려요."

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(26.dp))
            .then(if (bundle == null) Modifier.clickable(onClick = onOpenInput) else Modifier)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Surface2, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                bundle?.numbers?.destiny?.toString() ?: "未",
                color = Accent,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
            if (bundle == null) {
                Text("지금 입력하기  →", color = Accent, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EmptyArchiveCard(onOpenPremium: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(DeepNavy)
            .clickable(onClick = onOpenPremium)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("첫 번째 운세노트를 만들어보세요", color = Surface, style = MaterialTheme.typography.titleMedium)
            Text("조금 더 깊고 오래 남는 해석", color = Surface.copy(alpha = 0.64f), style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Gold)
    }
}

@Composable
private fun SavedBooksShelf(
    books: List<FortuneBook>,
    onOpenLibrary: () -> Unit,
    onBookClick: (FortuneBook) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("최근 운세노트", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("기억하고 싶은 해석을 다시 펼쳐보세요.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "전체 보기",
                color = Accent,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable(onClick = onOpenLibrary).padding(8.dp)
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 18.dp)
        ) {
            items(books, key = { it.bookId }) { book ->
                BookThumbnailCard(
                    book = book,
                    modifier = Modifier.size(width = 132.dp, height = 180.dp),
                    compact = true,
                    onClick = { onBookClick(book) }
                )
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}
