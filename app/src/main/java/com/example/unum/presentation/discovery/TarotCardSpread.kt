package com.example.unum.presentation.discovery

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.unum.R
import com.example.unum.data.content.TarotDraw
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

/** One shared unscaled atlas, rather than decoding it once for each of the 22 backs. */
private object TarotSprites {
    @Volatile private var cached: ImageBitmap? = null
    fun load(resources: Resources): ImageBitmap = cached ?: synchronized(this) {
        cached ?: BitmapFactory.decodeResource(resources, R.drawable.suri_tarot_atlas,
            BitmapFactory.Options().apply { inScaled = false }).asImageBitmap().also { cached = it }
    }
}

@Composable
private fun rememberTarotAtlas(): ImageBitmap {
    val resources = LocalContext.current.resources
    return remember(resources) { TarotSprites.load(resources) }
}

/** Slots 0..21 are the major arcana; 22 is the common back; 23 is the menu art. */
private fun DrawScope.drawTarotTile(atlas: ImageBitmap, slot: Int, width: Float, height: Float) {
    val column = slot % 6
    val row = slot / 6
    // Inset the generated gutters; round shared grid boundaries consistently.
    val left = (column * atlas.width / 6f).roundToInt() + 4
    val top = (row * atlas.height / 4f).roundToInt() + 4
    val right = ((column + 1) * atlas.width / 6f).roundToInt() - 4
    val bottom = ((row + 1) * atlas.height / 4f).roundToInt() - 4
    drawImage(atlas, srcOffset = IntOffset(left, top), srcSize = IntSize(right - left, bottom - top),
        dstSize = IntSize(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1)))
}

@Composable
fun TarotArtwork(slot: Int, modifier: Modifier = Modifier) {
    require(slot in 0..23)
    val atlas = rememberTarotAtlas()
    Canvas(modifier.clip(RoundedCornerShape(12.dp))) {
        drawTarotTile(atlas, slot, size.width, size.height)
    }
}

@Composable
fun TarotSpreadPicker(enabled: Boolean, onDraw: () -> Unit) {
    val atlas = rememberTarotAtlas()
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    var dragLift by remember { mutableFloatStateOf(0f) }
    val lift by animateFloatAsState(if (selected == null) 0f else 1f, tween(180), label = "tarot-selection")
    val currentOnDraw by rememberUpdatedState(onDraw)
    fun selectAt(x: Float, width: Float) {
        val margin = width * .20f
        selected = (((x - margin) / (width - margin * 2)).coerceIn(0f, 1f) * 21).roundToInt()
    }
    fun moveSelection(delta: Int) { selected = ((selected ?: 10) + delta).coerceIn(0, 21) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().testTag("tarot-spread")
                .semantics {
                    contentDescription = "수리 타로 카드 22장 펼침"
                    stateDescription = selected?.let { "${it + 1}번째 카드 선택됨" } ?: "아직 고른 카드가 없어요"
                    if (enabled) customActions = listOf(
                        CustomAccessibilityAction("이전 카드") { moveSelection(-1); true },
                        CustomAccessibilityAction("다음 카드") { moveSelection(1); true }
                    )
                }
                .pointerInput(enabled) {
                    if (enabled) detectTapGestures { point -> selectAt(point.x, size.width.toFloat()) }
                }
                .pointerInput(enabled) {
                    if (enabled) detectDragGestures(
                        onDragStart = { point -> selectAt(point.x, size.width.toFloat()); dragLift = 0f },
                        onDragCancel = { dragLift = 0f },
                        onDragEnd = {
                            val shouldDraw = dragLift >= 64.dp.toPx()
                            dragLift = 0f
                            if (shouldDraw) currentOnDraw()
                        },
                        onDrag = { change, delta ->
                            change.consume()
                            selectAt(change.position.x, size.width.toFloat())
                            dragLift = (dragLift - delta.y).coerceIn(0f, 90.dp.toPx())
                        }
                    )
                }) {
                val cardWidth = size.width * .245f
                val cardHeight = cardWidth * 1.5f
                val margin = size.width * .20f
                val spacing = (size.width - margin * 2) / 21
                fun paint(index: Int, chosen: Boolean) {
                    val position = (index - 10.5f) / 10.5f
                    val x = margin + index * spacing - cardWidth / 2
                    val y = 50.dp.toPx() + abs(position) * 34.dp.toPx() -
                        if (chosen) 28.dp.toPx() * lift + dragLift else 0f
                    withTransform({
                        translate(x, y)
                        rotate(if (chosen) 0f else position * 34f, Offset(cardWidth / 2, cardHeight / 2))
                    }) {
                        // A quiet shadow separates overlapping opaque backs.
                        drawRoundRect(DeepNavy.copy(alpha = .10f), topLeft = Offset(2.dp.toPx(), 3.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(cardWidth, cardHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
                        drawTarotTile(atlas, 22, cardWidth, cardHeight)
                    }
                }
                repeat(22) { if (it != selected) paint(it, false) }
                selected?.let { paint(it, true) }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { moveSelection(-1) }, enabled = enabled,
                modifier = Modifier.semantics { contentDescription = "이전 카드 선택" }) { Text("←") }
            Text(selected?.let { "${it + 1} / 22 · 마음에 드는 카드인가요?" } ?: "좌우로 쓸어 한 장을 골라보세요",
                modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { moveSelection(1) }, enabled = enabled,
                modifier = Modifier.semantics { contentDescription = "다음 카드 선택" }) { Text("→") }
        }
        Text("고른 카드를 위로 끌어 뽑거나, 아래 버튼으로 확정해요.",
            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        GradientButton(if (enabled) "선택한 카드 뽑기" else "오늘의 카드를 확인하고 있어요",
            onDraw, Modifier.fillMaxWidth().testTag("tarot-confirm"), enabled = enabled && selected != null)
    }
}

@Composable
fun TarotIllustratedFace(draw: TarotDraw, animate: Boolean = false, onRevealed: () -> Unit = {}) {
    val progress = remember(draw.date, draw.cardId) { Animatable(if (animate) 0f else 1f) }
    val completed by rememberUpdatedState(onRevealed)
    LaunchedEffect(draw.date, draw.cardId) {
        if (animate) progress.animateTo(1f, tween(850))
        completed()
    }
    val front = progress.value >= .5f
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.width(206.dp).aspectRatio(2f / 3f)
            .graphicsLayer {
                rotationY = progress.value * 180f
                cameraDistance = 12 * density
                translationY = (1 - progress.value) * 24.dp.toPx()
            }.testTag("tarot-face").semantics {
                contentDescription = if (front) "수리 ${draw.card.name} 카드 정방향" else "수리 타로 카드 뒷면"
            }) {
            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = if (front) 180f else 0f }) {
                TarotArtwork(if (front) draw.cardId else 22, Modifier.fillMaxSize())
                if (front) Text(draw.card.name, color = DeepNavy, style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp))
            }
        }
        if (front) Text("${draw.theme.label} · 정방향", color = TextSecondary,
            style = MaterialTheme.typography.bodySmall)
    }
}
