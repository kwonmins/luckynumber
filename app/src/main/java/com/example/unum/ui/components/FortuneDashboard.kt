package com.example.unum.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.unum.data.model.DailyFortuneResult
import com.example.unum.ui.theme.*

fun fortuneTimeScores(result: DailyFortuneResult): List<Int> = (0..2).map { index ->
    (result.score + Math.floorMod(result.date.toEpochDay() + result.coreNumber * 7 + index * 11, 19L).toInt() - 9).coerceIn(0,100)
}

@Composable
fun FortuneTimeSection(result: DailyFortuneResult) {
    var selected by remember(result.date,result.coreNumber) { mutableIntStateOf(0) }
    val scores=fortuneTimeScores(result)
    val labels=listOf("오전","오후","밤")
    val hours=listOf("07:00–11:59","12:00–17:59","18:00–23:59")
    val messages=listOf("할 일을 정리하고 중요한 일부터 시작해 보세요.","대화와 실행에 시간을 써보세요. 잠깐의 휴식도 도움이 돼요.","하루를 돌아보고 편안한 마무리에 집중해 보세요.")
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("시간대별 흐름",color=DeepNavy,style=MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            labels.forEachIndexed { i,label ->
                Column(Modifier.weight(1f).background(if(i==selected) Surface2 else Surface,RoundedCornerShape(16.dp)).clickable { selected=i }.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)) {
                    Text(scores[i].toString(),color=DeepNavy,style=MaterialTheme.typography.headlineSmall)
                    Text(label,color=DeepNavy,style=MaterialTheme.typography.labelMedium)
                    Text(hours[i],color=TextSecondary,style=MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text("${labels[selected]} · ${messages[selected]}",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
        Text("날짜와 수리 숫자로 계산한 시간대별 참고 지표예요.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun FortuneTrend(days: List<DailyFortuneResult>,onDate: (DailyFortuneResult) -> Unit) {
    if(days.isEmpty()) return
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Canvas(Modifier.fillMaxWidth().height(72.dp)) {
            val points=days.mapIndexed { i,d -> Offset(8.dp.toPx()+(size.width-16.dp.toPx())*i/(days.size-1).coerceAtLeast(1),size.height*(1f-d.score/100f)) }
            val line=Path().apply { points.forEachIndexed {i,p->if(i==0) moveTo(p.x,p.y) else lineTo(p.x,p.y)} }
            drawPath(line,Accent,style=Stroke(2.dp.toPx(),cap=StrokeCap.Round))
            points.forEachIndexed {i,p->drawCircle(if(i==2) AccentDark else Accent,if(i==2) 5.dp.toPx() else 3.dp.toPx(),p)}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {days.forEachIndexed { i,d ->
            Column(Modifier.weight(1f).clickable {onDate(d)}.padding(vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {Text(listOf("그제","어제","오늘","내일","모레").getOrElse(i){"${d.date.dayOfMonth}일"},color=if(i==2) AccentDark else TextSecondary,style=MaterialTheme.typography.labelSmall);Text("${d.score}",color=DeepNavy,style=MaterialTheme.typography.labelMedium)}
        } }
    }
}
