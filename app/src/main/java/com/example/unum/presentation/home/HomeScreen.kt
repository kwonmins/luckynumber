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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.DailyFortuneTopic
import com.example.unum.presentation.AppViewModel
import com.example.unum.ui.components.*
import com.example.unum.ui.theme.*

@Composable
fun HomeScreen(viewModel: AppViewModel, onOpenInput: () -> Unit, onOpenPremium: () -> Unit,
               onOpenLibrary: () -> Unit, onOpenSettings: () -> Unit, onOpenBook: (FortuneBook) -> Unit) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val daily = viewModel.dailyFortune()
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    selectedCategory?.let { label ->
        val bundle = state.latestBundle
        val message = when(label) {
            "애정운" -> daily?.topics?.firstOrNull { it.topic == DailyFortuneTopic.LOVE }?.message ?: bundle?.freeReading?.relationship
            "금전운" -> daily?.topics?.firstOrNull { it.topic == DailyFortuneTopic.MONEY }?.message ?: bundle?.freeReading?.money
            "직장운" -> daily?.topics?.firstOrNull { it.topic == DailyFortuneTopic.WORK }?.message ?: bundle?.freeReading?.work
            "건강운" -> bundle?.freeReading?.caution
            "행운운" -> bundle?.freeReading?.action
            else -> daily?.coreSummary ?: bundle?.freeReading?.opening
        }
        AlertDialog(onDismissRequest={selectedCategory=null},containerColor=Surface,
            title={Text(label,color=DeepNavy)},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) { PastelSuri(6,Modifier.size(90.dp)); Text(message.orEmpty(),color=TextSecondary); if(label=="건강운") Text("생활 흐름을 위한 참고 메시지예요.",style=MaterialTheme.typography.bodySmall,color=TextMuted) }},
            confirmButton={TextButton(onClick={selectedCategory=null}) {Text("확인",color=Accent)}})
    }
    MysticBackground(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            item {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                    Text("수리운세", style=MaterialTheme.typography.titleLarge, color=DeepNavy)
                    IconButton(onClick=onOpenSettings) { Icon(Icons.Outlined.Notifications,"알림 설정",tint=Accent) }
                }
            }
            item { FortuneBanner("오늘의 운세", "지금, 당신에게 필요한\n메시지를 확인해보세요.", onClick=onOpenInput) }
            item {
                Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    SectionHeader("무료 운세",description="오늘의 작은 흐름을 만나보세요")
                    val categories=listOf("총운" to FortuneArt.SUN,"애정운" to FortuneArt.HEART,"금전운" to FortuneArt.COINS,"직장운" to FortuneArt.CASE,"건강운" to FortuneArt.LEAF,"행운운" to FortuneArt.CLOVER)
                    categories.chunked(3).forEach { row ->
                        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            row.forEach { (label,art) ->
                                Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Surface).border(1.dp,Border,RoundedCornerShape(18.dp)).clickable { if(state.latestBundle==null) onOpenInput() else selectedCategory=label }.padding(vertical=14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                                    FortuneIllustration(art,Modifier.size(48.dp))
                                    Text(label,color=DeepNavy,style=MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
            item { FortuneBanner("프리미엄 운세", "당신이 궁금한 질문에\n더 깊이 답해드려요.", premium=true,onClick={viewModel.resetPremiumFlow(); onOpenPremium()}) }
            item {
                Row(Modifier.fillMaxWidth().background(Surface2.copy(alpha=.6f),RoundedCornerShape(20.dp)).padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                    PastelSuri(1,Modifier.size(88.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        Text("수리와 함께, 천천히",color=DeepNavy,style=MaterialTheme.typography.titleSmall)
                        Text(daily?.coreSummary ?: "당신의 이야기를 들려주세요.\n수리가 오늘의 흐름을 함께 읽어드릴게요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall,maxLines=3)
                    }
                }
            }
            if (daily != null) {
                item { SectionHeader("오늘의 메시지") }
                items(daily.topics.size) { index ->
                    val topic=daily.topics[index]
                    val art=when(topic.topic) { DailyFortuneTopic.LOVE -> FortuneArt.HEART; DailyFortuneTopic.WORK -> FortuneArt.CASE; DailyFortuneTopic.MONEY -> FortuneArt.COINS; DailyFortuneTopic.STUDY -> FortuneArt.BOOK; else -> FortuneArt.CLOVER }
                    SurfaceCard { Row(horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically) { FortuneIllustration(art); Text(topic.message,color=TextSecondary,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.weight(1f)) } }
                }
            }
            item { SectionHeader("나의 운세 보관함",description=if(state.savedBooks.isEmpty()) "마음에 남은 이야기를 모아두세요" else "지난 이야기를 다시 펼쳐보세요",actionLabel="전체 보기",onAction=onOpenLibrary) }
            state.savedBooks.take(2).forEach { book -> item { BookThumbnailCard(book=book,onClick={viewModel.selectSavedBook(book);onOpenBook(book)}) } }
        }
    }
}
