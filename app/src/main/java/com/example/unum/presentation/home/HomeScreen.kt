package com.example.unum.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.*
import com.example.unum.presentation.AppViewModel
import com.example.unum.ui.components.*
import com.example.unum.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(viewModel: AppViewModel, onOpenInput: () -> Unit, onOpenPremium: () -> Unit,
               onOpenLibrary: () -> Unit, onOpenSettings: () -> Unit, onOpenBook: (FortuneBook) -> Unit,
               onOpenToday: () -> Unit = onOpenInput, onOpenFeature: (String) -> Unit = {}) {
    val state=viewModel.uiState.collectAsStateWithLifecycle().value
    val daily=viewModel.dailyFortune()
    val discovery by viewModel.discovery.collectAsStateWithLifecycle()
    MysticBackground(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding=PaddingValues(20.dp,12.dp,20.dp,24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            item {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                    Text("수리운세",color=DeepNavy,style=MaterialTheme.typography.titleMedium)
                    TextButton(onClick={onOpenFeature("benefits")}) {Text("🍀 ${discovery.points} P",color=AccentDark)}
                }
            }
            item {
                val name=(state.authState as? AuthState.SignedIn)?.user?.displayName?.takeIf {it.isNotBlank()}
                val greeting=when(LocalTime.now().hour) {in 5..11 -> "좋은 아침이에요,";in 12..17 -> "편안한 오후 보내세요,";else -> "오늘도 수고했어요,"}
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                    Column(verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        Text(greeting,color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
                        Text(if(name==null) "당신의 오늘을 함께 읽어요 ☾" else "$name 님 ☾",color=DeepNavy,style=MaterialTheme.typography.titleMedium)
                        Text(com.example.unum.data.content.TarotCatalog.today().format(DateTimeFormatter.ofPattern("M월 d일 EEEE",Locale.KOREAN)),color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                    }
                    PastelSuri(0,Modifier.size(52.dp))
                }
            }
            item { QuietEntrance {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Surface2)) {
                    QuietLandscape(Modifier.matchParentSize(),blossoms=true)
                    Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        Text("오늘의 운세",color=DeepNavy,style=MaterialTheme.typography.labelLarge)
                        if(daily!=null) ScoreLabel(daily.score,large=true) else Text("오늘의 흐름을 만나보세요",color=DeepNavy,style=MaterialTheme.typography.titleLarge)
                        Text(daily?.coreSummary ?: "생년월일을 알려주시면 당신의 오늘을 차분히 읽어드릴게요.",color=TextSecondary,style=MaterialTheme.typography.bodyMedium,maxLines=3)
                        GradientButton("오늘의 운세 보기 →",{if(daily==null) onOpenInput() else onOpenToday()},Modifier.fillMaxWidth())
                    }
                }
            } }
            daily?.let { result ->
                item {FortuneTrend((-2..2).mapNotNull {viewModel.dailyFortune(result.date.plusDays(it.toLong()))}) {onOpenFeature("day/${it.date}")}}
                item {com.example.unum.ads.NativeAdPlacement()}
                item {FortuneTimeSection(result)}
            }
            item { SectionHeader("다양한 운세",actionLabel="모두 보기",onAction={onOpenFeature("explore")}) }
            item {com.example.unum.presentation.discovery.DiscoveryMenuGrid(onOpenFeature)}
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(AccentDark).clickable {viewModel.resetPremiumFlow();onOpenPremium()}.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text("☾  프리미엄운세",color=Surface,style=MaterialTheme.typography.labelLarge)
                    Text("당신이 궁금한 한 가지를\n조금 더 깊이 들여다볼까요?",color=Surface,style=MaterialTheme.typography.bodyMedium)
                    Text("고민 물어보기 →",color=Surface,style=MaterialTheme.typography.labelMedium)
                }
            }
            if(state.savedBooks.isNotEmpty()) {
                item {SectionHeader("나의 보관함",actionLabel="전체 보기",onAction=onOpenLibrary)}
                item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {state.savedBooks.take(2).forEach {book -> BookThumbnailCard(book,Modifier.weight(1f).height(190.dp),onClick={viewModel.selectSavedBook(book);onOpenBook(book)})}} }
            }
            item {com.example.unum.ads.BannerAdPlacement()}
        }
    }
}
