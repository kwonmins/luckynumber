package com.example.unum.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.unum.R
import com.example.unum.data.model.PremiumTopic
import com.example.unum.ui.theme.*
import kotlin.math.*

enum class FortuneArt { SUN, HEART, COINS, CASE, LEAF, CLOVER, BOOK, SCROLL, RINGS, BLOSSOM, MOON, MOON_STARS, COMPASS, MIRROR, PEOPLE, CALENDAR, NUMBERS, GIFT }

fun PremiumTopic.fortuneArt(): FortuneArt = when (this) {
    PremiumTopic.ROMANCE -> FortuneArt.HEART
    PremiumTopic.CAREER -> FortuneArt.COMPASS
    PremiumTopic.MONEY -> FortuneArt.COINS
    PremiumTopic.STUDY -> FortuneArt.BOOK
    PremiumTopic.HEALTH -> FortuneArt.LEAF
    PremiumTopic.BUSINESS -> FortuneArt.CASE
    PremiumTopic.GENERAL -> FortuneArt.MOON_STARS
    PremiumTopic.SELF_ESTEEM -> FortuneArt.MIRROR
    PremiumTopic.RELATIONSHIP -> FortuneArt.PEOPLE
}

/** Render an atlas cell without changing the source artwork or decoding it every frame. */
@Composable
fun PastelSuri(pose: Int = 0, modifier: Modifier = Modifier, description: String? = "수리") {
    val resources = LocalContext.current.resources
    val atlas = remember(resources) { BitmapFactory.decodeResource(resources, R.drawable.suri_pastel_atlas) }
    val frame = remember(atlas, pose) {
        val index = pose.coerceIn(0, 11)
        val x = index % 4 * atlas.width / 4
        val y = index / 4 * atlas.height / 3
        Bitmap.createBitmap(atlas, x, y, atlas.width / 4, atlas.height / 3).asImageBitmap()
    }
    Image(bitmap = frame, contentDescription = description, modifier = modifier)
}

@Composable
fun FortuneIllustration(art: FortuneArt, modifier: Modifier = Modifier.size(48.dp)) {
    Canvas(modifier) {
        val s = size.minDimension
        val c = center
        val gold = Brush.linearGradient(listOf(Color(0xFFF7E7CB), Color(0xFFD8B68F)), Offset.Zero, Offset(s,s))
        val pink = Brush.linearGradient(listOf(Color(0xFFF5E0E5), Color(0xFFDCAEBB)), Offset.Zero, Offset(s,s))
        val violet = Brush.linearGradient(listOf(Color(0xFFE5DEED), Color(0xFFAA9ABB)), Offset.Zero, Offset(s,s))
        val green = Brush.linearGradient(listOf(Color(0xFFDEE9D9), Color(0xFF9DB59C)), Offset.Zero, Offset(s,s))
        when (art) {
            FortuneArt.SUN -> {
                repeat(8) { i -> val a = i*PI/4; drawLine(Color(0xFFEABB84), c+Offset(cos(a).toFloat(),sin(a).toFloat())*s*.35f,c+Offset(cos(a).toFloat(),sin(a).toFloat())*s*.46f,s*.045f,StrokeCap.Round) }
                drawCircle(gold,s*.27f,c); drawCircle(Color.White.copy(alpha=.35f),s*.1f,c-Offset(s*.07f,s*.08f))
            }
            FortuneArt.HEART -> {
                val p = Path().apply { moveTo(s*.5f,s*.86f); cubicTo(s*.02f,s*.56f,s*.04f,s*.16f,s*.29f,s*.16f); cubicTo(s*.41f,s*.16f,s*.47f,s*.24f,s*.5f,s*.31f); cubicTo(s*.6f,s*.06f,s*.92f,s*.13f,s*.92f,s*.36f); cubicTo(s*.92f,s*.56f,s*.7f,s*.76f,s*.5f,s*.86f); close() }
                drawPath(p,pink); drawCircle(Color.White.copy(alpha=.55f),s*.055f,Offset(s*.28f,s*.29f))
            }
            FortuneArt.COINS -> repeat(3) { i ->
                val y=s*(.66f-i*.2f); drawRoundRect(gold,Offset(s*.23f,y),Size(s*.55f,s*.19f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(s*.07f)); drawOval(Color(0xFFFFE7B4),Offset(s*.23f,y-s*.06f),Size(s*.55f,s*.14f)); drawOval(Color(0xFFE6B27B),Offset(s*.28f,y-s*.035f),Size(s*.45f,s*.085f),style=Stroke(s*.015f))
            }
            FortuneArt.CASE -> {
                drawRoundRect(Color(0xFFAC9BBD),Offset(s*.36f,s*.13f),Size(s*.28f,s*.25f),androidx.compose.ui.geometry.CornerRadius(s*.05f),style=Stroke(s*.065f))
                drawRoundRect(violet,Offset(s*.13f,s*.3f),Size(s*.74f,s*.52f),androidx.compose.ui.geometry.CornerRadius(s*.11f)); drawLine(Color(0xFF8A789E),Offset(s*.14f,s*.51f),Offset(s*.86f,s*.51f),s*.02f); drawCircle(gold,s*.06f,Offset(s*.5f,s*.53f))
            }
            FortuneArt.LEAF -> {
                val p=Path().apply { moveTo(s*.15f,s*.83f); cubicTo(s*.08f,s*.25f,s*.57f,s*.19f,s*.87f,s*.08f); cubicTo(s*.9f,s*.64f,s*.5f,s*.84f,s*.15f,s*.83f); close() }
                drawPath(p,green); drawLine(Color(0xFF75957E),Offset(s*.1f,s*.91f),Offset(s*.8f,s*.19f),s*.035f,StrokeCap.Round)
            }
            FortuneArt.CLOVER, FortuneArt.BLOSSOM -> {
                val count=if(art==FortuneArt.CLOVER) 4 else 5
                repeat(count) { i -> rotate(i*360f/count,pivot=c) { drawOval(if(art==FortuneArt.CLOVER) green else pink,Offset(s*.32f,s*.07f),Size(s*.36f,s*.5f)) } }
                drawCircle(gold,s*.065f,c)
            }
            FortuneArt.BOOK -> {
                drawRoundRect(violet,Offset(s*.12f,s*.23f),Size(s*.76f,s*.58f),androidx.compose.ui.geometry.CornerRadius(s*.05f))
                repeat(2) { side -> drawRoundRect(Color(0xFFFFEBD8),Offset(s*(.16f+side*.35f),s*.18f),Size(s*.33f,s*.57f),androidx.compose.ui.geometry.CornerRadius(s*.025f)); repeat(3) { line -> drawLine(Color(0xFFE5CBAE),Offset(s*(.2f+side*.35f),s*(.42f+line*.09f)),Offset(s*(.44f+side*.35f),s*(.42f+line*.09f)),s*.018f) } }
                drawLine(Color(0xFFC9B7CC),Offset(s*.5f,s*.2f),Offset(s*.5f,s*.77f),s*.025f)
            }
            FortuneArt.SCROLL -> {
                drawRect(Color(0xFFFFE5D1),Offset(s*.25f,s*.18f),Size(s*.49f,s*.62f))
                listOf(.13f,.75f).forEach { y -> drawRoundRect(gold,Offset(s*.2f,s*y),Size(s*.59f,s*.14f),androidx.compose.ui.geometry.CornerRadius(s*.07f)) }
                repeat(3) { i -> drawLine(Color(0xFFD8BAAA),Offset(s*.33f,s*(.38f+i*.1f)),Offset(s*.65f,s*(.38f+i*.1f)),s*.02f) }
            }
            FortuneArt.RINGS -> { drawCircle(Color(0xFFEAB47B),s*.25f,Offset(s*.38f,s*.57f),style=Stroke(s*.10f)); drawCircle(Color(0xFFFFD69D),s*.25f,Offset(s*.63f,s*.57f),style=Stroke(s*.085f)); drawCircle(pink,s*.09f,Offset(s*.62f,s*.23f)) }
            FortuneArt.MOON, FortuneArt.MOON_STARS -> {
                val crescent = Path().apply { moveTo(s*.54f,s*.13f); cubicTo(s*.08f,s*.04f,s*.04f,s*.67f,s*.4f,s*.82f); cubicTo(s*.67f,s*.94f,s*.84f,s*.7f,s*.85f,s*.55f); cubicTo(s*.51f,s*.76f,s*.3f,s*.32f,s*.54f,s*.13f); close() }
                drawPath(crescent, if(art==FortuneArt.MOON_STARS) violet else gold)
                if(art==FortuneArt.MOON_STARS) {
                    listOf(Offset(s*.74f,s*.2f) to s*.065f,Offset(s*.88f,s*.38f) to s*.035f).forEach { (point,r) ->
                        drawLine(Color(0xFFD8B68F),point-Offset(r,0f),point+Offset(r,0f),s*.04f,StrokeCap.Round)
                        drawLine(Color(0xFFD8B68F),point-Offset(0f,r),point+Offset(0f,r),s*.04f,StrokeCap.Round)
                    }
                }
            }
            FortuneArt.COMPASS -> {
                drawCircle(Color(0xFFE4ECF3),s*.37f,c)
                drawCircle(Color(0xFF9DB3C7),s*.37f,c,style=Stroke(s*.03f))
                val needle=Path().apply { moveTo(s*.67f,s*.24f); lineTo(s*.57f,s*.56f); lineTo(s*.29f,s*.73f); lineTo(s*.41f,s*.42f); close() }
                drawPath(needle,Brush.linearGradient(listOf(Color(0xFFB3C6D8),Color(0xFF7F9DB7))))
                drawCircle(Color(0xFFFFFCF7),s*.045f,c)
            }
            FortuneArt.MIRROR -> {
                drawLine(Color(0xFFD0B498),Offset(s*.5f,s*.64f),Offset(s*.5f,s*.9f),s*.07f,StrokeCap.Round)
                drawRoundRect(gold,Offset(s*.32f,s*.85f),Size(s*.36f,s*.08f),androidx.compose.ui.geometry.CornerRadius(s*.04f))
                drawOval(gold,Offset(s*.21f,s*.08f),Size(s*.58f,s*.66f))
                drawOval(Color(0xFFE6EDF0),Offset(s*.27f,s*.14f),Size(s*.46f,s*.54f))
                drawLine(Color.White.copy(alpha=.85f),Offset(s*.36f,s*.42f),Offset(s*.55f,s*.21f),s*.035f,StrokeCap.Round)
                drawLine(Color.White.copy(alpha=.7f),Offset(s*.43f,s*.54f),Offset(s*.63f,s*.32f),s*.025f,StrokeCap.Round)
            }
            FortuneArt.PEOPLE -> {
                drawCircle(violet,s*.15f,Offset(s*.34f,s*.28f)); drawCircle(Brush.linearGradient(listOf(Color(0xFFDDECEF),Color(0xFF9EBCC3))),s*.13f,Offset(s*.71f,s*.34f))
                drawRoundRect(violet,Offset(s*.12f,s*.49f),Size(s*.45f,s*.34f),androidx.compose.ui.geometry.CornerRadius(s*.15f))
                drawRoundRect(Color(0xFFB7CDD1),Offset(s*.59f,s*.52f),Size(s*.29f,s*.29f),androidx.compose.ui.geometry.CornerRadius(s*.12f))
            }
            FortuneArt.CALENDAR -> {
                drawRoundRect(violet,Offset(s*.16f,s*.22f),Size(s*.68f,s*.65f),androidx.compose.ui.geometry.CornerRadius(s*.1f))
                drawRoundRect(Color(0xFFFFFCF5),Offset(s*.21f,s*.39f),Size(s*.58f,s*.4f),androidx.compose.ui.geometry.CornerRadius(s*.05f))
                listOf(.33f,.67f).forEach { x -> drawLine(Color(0xFFAA9ABB),Offset(s*x,s*.13f),Offset(s*x,s*.3f),s*.065f,StrokeCap.Round) }
                drawLine(Color(0xFF9DB59C),Offset(s*.35f,s*.57f),Offset(s*.47f,s*.68f),s*.05f,StrokeCap.Round)
                drawLine(Color(0xFF9DB59C),Offset(s*.47f,s*.68f),Offset(s*.66f,s*.48f),s*.05f,StrokeCap.Round)
            }
            FortuneArt.NUMBERS -> {
                drawRoundRect(violet,Offset(s*.14f,s*.12f),Size(s*.72f,s*.76f),androidx.compose.ui.geometry.CornerRadius(s*.12f))
                val line=Color(0xFFF8F3EC)
                listOf(.39f,.61f).forEach { x -> drawLine(line,Offset(s*x,s*.32f),Offset(s*(x-.06f),s*.71f),s*.05f,StrokeCap.Round) }
                listOf(.45f,.61f).forEach { y -> drawLine(line,Offset(s*.28f,s*y),Offset(s*.71f,s*y),s*.05f,StrokeCap.Round) }
            }
            FortuneArt.GIFT -> {
                drawRoundRect(pink,Offset(s*.17f,s*.39f),Size(s*.66f,s*.45f),androidx.compose.ui.geometry.CornerRadius(s*.07f))
                drawRoundRect(gold,Offset(s*.13f,s*.33f),Size(s*.74f,s*.14f),androidx.compose.ui.geometry.CornerRadius(s*.04f))
                drawLine(Color(0xFFD8B68F),Offset(s*.5f,s*.36f),Offset(s*.5f,s*.82f),s*.08f)
                drawOval(Color(0xFFDCAEBB),Offset(s*.26f,s*.14f),Size(s*.24f,s*.2f),style=Stroke(s*.04f)); drawOval(Color(0xFFDCAEBB),Offset(s*.5f,s*.14f),Size(s*.24f,s*.2f),style=Stroke(s*.04f))
            }
        }
    }
}

/** Restrained mountains and blossoms drawn from the supplied background references. */
@Composable
fun MoonGarden(modifier: Modifier = Modifier, premium: Boolean = false) {
    Canvas(modifier) {
        drawRect(Brush.linearGradient(if(premium) listOf(Color(0xFFADA0BF),Color(0xFF81718F)) else listOf(Color(0xFFE4DFEB),Color(0xFFFFE9DA))))
        drawCircle(Color(0xFFFFEBCD),size.height*.16f,Offset(size.width*.18f,size.height*.26f))
        drawCircle(if(premium) Color(0xFFA99BB9) else Color(0xFFE4DFEB),size.height*.15f,Offset(size.width*.22f,size.height*.21f))
        repeat(3) { layer ->
            val p=Path().apply { moveTo(0f,size.height); lineTo(0f,size.height*(.67f+layer*.08f)); repeat(9) { i -> lineTo(size.width*i/8,size.height*(.62f+layer*.1f+sin(i*1.8+layer).toFloat()*.07f)) }; lineTo(size.width,size.height); close() }
            drawPath(p,Color(0xFF9F94B5).copy(alpha=.12f+layer*.09f))
        }
        val x=size.width*.9f
        drawLine(Color(0xFFAA8895).copy(alpha=.6f),Offset(size.width,size.height),Offset(x,size.height*.12f),2.dp.toPx())
        repeat(9) { i ->
            val y=size.height*(.18f+i*.085f); val bx=x+(if(i%2==0) -1 else 1)*size.width*.04f
            drawLine(Color(0xFFAA8895).copy(alpha=.5f),Offset(x+size.width*.04f,y+size.height*.09f),Offset(bx,y),1.dp.toPx())
            repeat(5) { petal -> val a=petal*2*PI/5; drawCircle(Color(0xFFF0B8C0).copy(alpha=.8f),3.dp.toPx(),Offset(bx+cos(a).toFloat()*4.dp.toPx(),y+sin(a).toFloat()*4.dp.toPx())) }
            drawCircle(Color(0xFFFFE2A7),1.5.dp.toPx(),Offset(bx,y))
        }
    }
}

@Composable
fun FortuneBanner(title: String, subtitle: String, premium: Boolean = false, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(126.dp).clip(RoundedCornerShape(20.dp)).clickable(role=Role.Button,onClick=onClick)) {
        MoonGarden(Modifier.matchParentSize(),premium)
        Column(Modifier.padding(start=24.dp,top=26.dp,end=100.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
            Text(title,color=if(premium) Surface else DeepNavy,style=MaterialTheme.typography.titleMedium)
            Text(subtitle,color=if(premium) Surface.copy(alpha=.86f) else TextSecondary,style=MaterialTheme.typography.bodySmall)
        }
        FortuneIllustration(if(premium) FortuneArt.BOOK else FortuneArt.MOON,Modifier.align(Alignment.CenterEnd).padding(end=20.dp).size(58.dp))
    }
}
