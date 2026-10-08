package com.example.unum.presentation.result

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.presentation.AppViewModel
import com.example.unum.ui.components.*
import com.example.unum.ui.theme.*

@Composable
fun ResultScreen(viewModel: AppViewModel,onOpenInput: () -> Unit,onOpenPremium: () -> Unit,date: java.time.LocalDate? = null, onlyLuck: Boolean = false) {
    val state=viewModel.uiState.collectAsStateWithLifecycle().value
    val result=viewModel.dailyFortune(date ?: com.example.unum.data.content.TarotCatalog.today())
    val context = LocalContext.current
    var expanded by remember {mutableStateOf(false)}
    MysticBackground(Modifier.fillMaxSize()) {
        if(result==null) Box(Modifier.fillMaxSize().padding(24.dp),contentAlignment=Alignment.Center) {
            EmptyStateView("오늘의 흐름을 준비해요","생년월일을 알려주시면 오늘의 운세를 읽어드릴게요.","생년월일 입력하기",onOpenInput)
        } else LazyColumn(contentPadding=PaddingValues(20.dp,12.dp,20.dp,28.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
            item {
                AppTopBar(if(onlyLuck) "오늘의 행운 아이템" else if(date==null) "오늘의 운세" else "날짜별 운세", subtitle="${result.date.monthValue}월 ${result.date.dayOfMonth}일", action = {
                    IconButton(onClick = {
                        val shareText = buildString {
                            appendLine("수리운세 · ${result.date} · ${result.score}/100")
                            appendLine(result.coreSummary)
                            result.topics.forEach { appendLine("${it.topic.label()} · ${it.score}/100 · ${it.keyword}\n${it.message}") }
                        }
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }, "오늘의 운세 공유"))
                    }) { Icon(Icons.Outlined.Share, "운세 공유하기", tint = Accent) }
                })
            }
            item { QuietEntrance {
                Column(Modifier.fillMaxWidth().background(Surface2,RoundedCornerShape(22.dp)).padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    ScoreLabel(result.score,large=true)
                    Text(if(result.score>=80) "좋은 흐름이 이어지는 하루" else "나의 속도로 균형을 찾는 하루",color=DeepNavy,style=MaterialTheme.typography.titleLarge)
                    Text("날짜와 수리 숫자로 계산한 참고 지표예요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                }
            } }
            item { Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {Text("오늘의 한줄",color=DeepNavy,style=MaterialTheme.typography.titleMedium);Text(result.coreSummary,color=TextSecondary,style=MaterialTheme.typography.bodyLarge)} }
            if(!onlyLuck) item {FortuneTimeSection(result)}
            item {com.example.unum.ads.NativeAdPlacement()}
            result.topics.filter { !onlyLuck || it.topic==com.example.unum.data.model.DailyFortuneTopic.LUCK }.forEach {topic -> item {
                Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {Text(if(topic.topic == com.example.unum.data.model.DailyFortuneTopic.LUCK) "행운" else "${topic.topic.label()}운",color=DeepNavy,style=MaterialTheme.typography.titleMedium);ScoreLabel(topic.score)}
                    Text(topic.keyword,color=AccentDark,style=MaterialTheme.typography.labelLarge)
                    Text(topic.message,color=TextSecondary,style=MaterialTheme.typography.bodyLarge)
                    HorizontalDivider(color=Border,modifier=Modifier.padding(top=8.dp))
                }
            } }
            item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                SmallGuide("오늘 하면 좋은 것",state.latestBundle?.freeReading?.action.orEmpty(),Modifier.weight(1f))
                SmallGuide("오늘 피하면 좋은 것",state.latestBundle?.freeReading?.caution.orEmpty(),Modifier.weight(1f))
            } }
            item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {listOf("숫자" to result.luckyNumber.toString(),"색" to result.luckyColor,"시간" to result.luckyTime).forEach {(label,value) -> SmallGuide("행운의 $label",value,Modifier.weight(1f))}} }
            item { Text("행운 요소는 상징적인 참고 정보예요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall) }
            item { TextButton(onClick={expanded=!expanded}) {Text(if(expanded) "기본 수리 풀이 접기" else "나의 기본 수리 풀이 보기",color=AccentDark)} }
            if(expanded) state.latestBundle?.freeReading?.let {reading ->
                listOf("나의 성향" to reading.opening,"숫자가 만나는 흐름" to reading.core,"초년" to reading.early,"중년" to reading.middle,"후년" to reading.late,"관계" to reading.relationship,"일" to reading.work,"돈" to reading.money).forEach {(title,body) -> item {Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {Text(title,color=DeepNavy,style=MaterialTheme.typography.titleSmall);Text(body,color=TextSecondary,style=MaterialTheme.typography.bodyMedium)}}}
            }
            item { GradientButton("한 가지 고민을 더 깊이 물어보기",onOpenPremium,Modifier.fillMaxWidth()) }
            item { SecondaryButton("생년월일 다시 입력하기",onOpenInput,Modifier.fillMaxWidth()) }
            item {com.example.unum.ads.BannerAdPlacement()}
        }
    }
}
@Composable
private fun SmallGuide(title: String,body: String,modifier: Modifier) {
    Column(modifier.background(Surface,RoundedCornerShape(18.dp)).padding(14.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {Text(title,color=DeepNavy,style=MaterialTheme.typography.labelMedium);Text(body,color=TextSecondary,style=MaterialTheme.typography.bodySmall)}
}
