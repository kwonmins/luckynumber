package com.example.unum.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import com.example.unum.ui.theme.*
import kotlin.math.*

@Composable
fun QuietEntrance(delay: Int = 0, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val progress by animateFloatAsState(if(visible) 1f else 0f,tween(400,delay),label="gentle-entry")
    Box(modifier.graphicsLayer { alpha=progress;translationY=(1-progress)*10.dp.toPx() }) {content()}
}

@Composable
fun QuietLandscape(modifier: Modifier = Modifier, blossoms: Boolean = false) {
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(Color.Transparent,Surface2.copy(alpha=.7f))))
        repeat(3) { layer ->
            val p=Path().apply {
                moveTo(0f,size.height);lineTo(0f,size.height*(.55f+layer*.12f))
                for(i in 0..20) lineTo(size.width*i/20,size.height*(.58f+layer*.13f+sin(i*.65+layer).toFloat()*.10f))
                lineTo(size.width,size.height);close()
            }
            drawPath(p,Accent.copy(alpha=.07f+layer*.025f))
        }
        repeat(3) { i -> drawOval(Surface.copy(alpha=.65f),Offset(size.width*(i*.34f-.1f),size.height*.6f),Size(size.width*.5f,size.height*.13f)) }
        if(!blossoms) {
            val c=Offset(size.width*.5f,size.height*.28f)
            drawCircle(Gold.copy(alpha=.38f),size.height*.09f,c)
            drawCircle(Background,size.height*.085f,c+Offset(size.height*.036f,-size.height*.026f))
        } else {
            val x=size.width*.87f
            drawLine(Accent.copy(alpha=.22f),Offset(size.width,size.height),Offset(x,size.height*.28f),1.4.dp.toPx())
            repeat(6) {i ->
                val c=Offset(x+(i%2)*size.width*.04f,size.height*(.34f+i*.09f))
                repeat(5) {p -> val a=p*2*PI/5;drawCircle(Rose.copy(alpha=.30f),3.dp.toPx(),c+Offset(cos(a).toFloat()*4.dp.toPx(),sin(a).toFloat()*4.dp.toPx()))}
            }
        }
    }
}

@Composable
fun ScoreLabel(score: Int, modifier: Modifier = Modifier, large: Boolean = false) {
    Row(modifier,verticalAlignment=androidx.compose.ui.Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        Text(score.toString(),color=DeepNavy,style=if(large) MaterialTheme.typography.displayLarge else MaterialTheme.typography.titleLarge)
        Text("/ 100",color=TextSecondary,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(bottom=4.dp))
    }
}

fun com.example.unum.data.model.DailyFortuneTopic.label(): String = when(this) {
    com.example.unum.data.model.DailyFortuneTopic.LOVE -> "애정"
    com.example.unum.data.model.DailyFortuneTopic.MONEY -> "재물"
    com.example.unum.data.model.DailyFortuneTopic.WORK -> "직장"
    com.example.unum.data.model.DailyFortuneTopic.HEALTH -> "건강"
    com.example.unum.data.model.DailyFortuneTopic.LUCK -> "행운"
    com.example.unum.data.model.DailyFortuneTopic.STUDY -> "학업"
    else -> "마음"
}
