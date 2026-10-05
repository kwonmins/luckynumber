package com.example.unum.presentation.keycap

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

private val Ink = Color(0xFF070A22)
private val InkDeep = Color(0xFF030516)
private val Glass = Color(0xFF171B3A)
private val White = Color(0xFFF7F7FF)
private val Muted = Color(0xFF9A9FBE)
private val Mint = Color(0xFF77F7C8)
private val Pink = Color(0xFFFF78C7)
private val Purple = Color(0xFFB19CFF)
private val Blue = Color(0xFF77B7FF)
private val Yellow = Color(0xFFFFDA6E)

private data class CapTheme(
    val name: String,
    val vibe: String,
    val colors: List<Color>,
    val accent: Color,
    val unlocked: Boolean = true
)

private val capThemes = listOf(
    CapTheme("Cosmic Jelly", "dreamy + glossy", listOf(Purple, Pink, Mint, Blue), Mint),
    CapTheme("Matcha Pop", "fresh + fizzy", listOf(Mint, Color(0xFFC4FF82), White, Purple), Mint),
    CapTheme("Berry Byte", "sweet + bold", listOf(Pink, Color(0xFFFFA3B8), Purple, White), Pink),
    CapTheme("Cloud Nine", "soft + floaty", listOf(Blue, White, Color(0xFFC7DDFF), Purple), Blue, false)
)

private enum class AppTab(val label: String) {
    Explore("Explore"), Customize("Customize"), Collection("Collection"), Profile("Profile")
}

@Composable
fun KeycapStudioScreen() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedTheme by rememberSaveable { mutableIntStateOf(0) }
    val tab = AppTab.entries[selectedTab]
    val theme = capThemes[selectedTheme]

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(InkDeep, Ink, Color(0xFF0D0D2E))))
    ) {
        BackgroundGlow()
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomBar(tab) { selectedTab = it.ordinal } }
        ) { padding ->
            when (tab) {
                AppTab.Explore -> ExplorePage(
                    Modifier.padding(padding), theme, selectedTheme
                ) { selectedTheme = it }
                AppTab.Customize -> CustomizePage(
                    Modifier.padding(padding), theme, selectedTheme
                ) { selectedTheme = it }
                AppTab.Collection -> CollectionPage(Modifier.padding(padding))
                AppTab.Profile -> ProfilePage(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun BackgroundGlow() = Canvas(Modifier.fillMaxSize()) {
    drawCircle(
        Brush.radialGradient(listOf(Purple.copy(.16f), Color.Transparent)),
        size.width * .62f,
        Offset(size.width * .92f, size.height * .13f)
    )
    drawCircle(
        Brush.radialGradient(listOf(Mint.copy(.09f), Color.Transparent)),
        size.width * .55f,
        Offset(size.width * .02f, size.height * .57f)
    )
}

@Composable
private fun ExplorePage(
    modifier: Modifier,
    theme: CapTheme,
    selectedTheme: Int,
    onThemeSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { TopBar() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hey, Juno", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("  ✦  ", color = Pink, fontSize = 13.sp)
                    LevelPill()
                }
                Text(
                    "Build your dream board.", color = White,
                    fontSize = 28.sp, lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = (-.7).sp
                )
            }
        }
        item { KeyboardHero(theme) }
        item {
            SectionHeader("Pick a vibe", "See all")
            Spacer(Modifier.height(12.dp))
            ThemeCarousel(selectedTheme, onThemeSelected)
        }
        item { DailyQuestCard() }
        item {
            SectionHeader("Trending caps", "Fresh drops")
            Spacer(Modifier.height(12.dp))
            TrendingRow()
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(37.dp)
                    .shadow(18.dp, RoundedCornerShape(12.dp), ambientColor = Mint, spotColor = Mint)
                    .background(Mint, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("C", color = Ink, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Box(
                    Modifier.align(Alignment.TopEnd).offset(2.dp, (-2).dp).size(10.dp)
                        .background(Pink, CircleShape).border(2.dp, Ink, CircleShape)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text("cap", color = White, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = (-.6).sp)
            Text("pop", color = Mint, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = (-.6).sp)
        }
        Box {
            IconButton(
                onClick = {},
                modifier = Modifier.size(42.dp).background(Glass.copy(.76f), CircleShape)
                    .border(1.dp, White.copy(.08f), CircleShape)
            ) { Icon(Icons.Rounded.NotificationsNone, "Notifications", tint = White) }
            Box(
                Modifier.align(Alignment.TopEnd).offset((-4).dp, 3.dp).size(9.dp)
                    .background(Pink, CircleShape).border(2.dp, Ink, CircleShape)
            )
        }
    }
}

@Composable
private fun LevelPill() {
    Row(
        Modifier.background(Yellow.copy(.13f), RoundedCornerShape(50))
            .border(1.dp, Yellow.copy(.28f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Star, null, tint = Yellow, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text("LV.12", color = Yellow, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
    }
}

@Composable
private fun KeyboardHero(theme: CapTheme) {
    val transition = rememberInfiniteTransition(label = "keyboard")
    val bob by transition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )
    GlassPanel(Modifier.fillMaxWidth().height(316.dp), 30.dp) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    Brush.radialGradient(listOf(theme.accent.copy(.22f), Color.Transparent)),
                    size.width * .55f,
                    Offset(size.width * .5f, size.height * .53f)
                )
                val shadow = Path().apply {
                    moveTo(size.width * .08f, size.height * .72f)
                    lineTo(size.width * .89f, size.height * .59f)
                    lineTo(size.width * .98f, size.height * .81f)
                    lineTo(size.width * .15f, size.height * .92f)
                    close()
                }
                drawPath(shadow, Color.Black.copy(.24f))
            }
            Row(
                Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(Mint, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("LIVE PREVIEW", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(theme.name, color = White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Jelly series · 84 keys", color = Muted, fontSize = 12.sp)
                }
                Row(
                    Modifier.background(White.copy(.07f), RoundedCornerShape(12.dp))
                        .border(1.dp, White.copy(.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.ViewInAr, null, tint = Purple, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("3D", color = White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            Box(
                Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 4.dp)
                    .offset(y = 28.dp)
                    .graphicsLayer {
                        rotationX = 13f
                        rotationZ = -5f
                        cameraDistance = 14f * density
                        translationY = bob
                    }
            ) { KeyboardCanvas(theme.colors, Modifier.fillMaxWidth().height(178.dp)) }
            Sticker(
                Icons.Rounded.Bolt, Yellow,
                Modifier.align(Alignment.CenterStart).offset(12.dp, 51.dp).graphicsLayer { rotationZ = -12f }
            )
            Sticker(
                Icons.Rounded.AutoAwesome, Pink,
                Modifier.align(Alignment.CenterEnd).offset((-13).dp, 21.dp).graphicsLayer { rotationZ = 11f }
            )
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ViewInAr, null, tint = Muted, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Drag to rotate", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {}, modifier = Modifier.height(38.dp),
                    contentPadding = PaddingValues(horizontal = 15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Palette, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Try this set", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun KeyboardCanvas(colors: List<Color>, modifier: Modifier = Modifier) {
    val animated = colors.mapIndexed { index, color ->
        animateColorAsState(color, tween(450, delayMillis = index * 35), label = "cap$index").value
    }
    Canvas(modifier) {
        val left = size.width * .055f
        val top = size.height * .12f
        val boardW = size.width * .89f
        val boardH = size.height * .73f
        val radius = min(size.width, size.height) * .075f
        drawRoundRect(
            Color.Black.copy(.42f), Offset(left + 3f, top + 9f), Size(boardW, boardH),
            CornerRadius(radius)
        )
        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFF48456F), Color(0xFF1C1D3D))),
            Offset(left, top), Size(boardW, boardH), CornerRadius(radius)
        )
        drawRoundRect(
            White.copy(.13f), Offset(left + 2f, top + 2f), Size(boardW - 4f, boardH - 4f),
            CornerRadius(radius - 2f), style = Stroke(1.4f)
        )

        val columns = 12
        val gap = boardW * .012f
        val side = boardW * .055f
        val keyW = (boardW - side * 2 - gap * (columns - 1)) / columns
        val keyH = boardH * .137f
        val rowGap = boardH * .045f
        val startX = left + side
        val startY = top + boardH * .13f
        val labels = listOf(
            listOf("ESC", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "⌫"),
            listOf("TAB", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "]"),
            listOf("CAP", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "↵"),
            listOf("⇧", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "⇧")
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        labels.forEachIndexed { row, keys ->
            keys.forEachIndexed { column, label ->
                drawCap(
                    startX + column * (keyW + gap), startY + row * (keyH + rowGap),
                    keyW, keyH, animated[(row * 3 + column) % animated.size], label, paint
                )
            }
        }
        val bottom = startY + 4 * (keyH + rowGap)
        val unit = keyW + gap
        drawCap(startX, bottom, keyW * 1.45f, keyH, animated[3 % animated.size], "FN", paint)
        drawCap(startX + unit * 1.58f, bottom, keyW, keyH, animated[0], "⌘", paint)
        drawCap(startX + unit * 2.72f, bottom, unit * 5f, keyH, animated[2 % animated.size], "CAPPOP", paint)
        drawCap(startX + unit * 7.87f, bottom, keyW, keyH, animated[1 % animated.size], "◀", paint)
        drawCap(startX + unit * 9.01f, bottom, keyW, keyH, animated[2 % animated.size], "▲", paint)
        drawCap(startX + unit * 10.15f, bottom, keyW, keyH, animated[3 % animated.size], "▶", paint)
        drawCircle(Pink, keyW * .17f, Offset(left + boardW * .94f, top + boardH * .07f))
        drawCircle(White.copy(.55f), keyW * .055f, Offset(left + boardW * .935f, top + boardH * .055f))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCap(
    x: Float, y: Float, width: Float, height: Float, color: Color, label: String, paint: Paint
) {
    val round = min(width, height) * .25f
    drawRoundRect(
        color.copy(.45f), Offset(x, y + height * .13f), Size(width, height), CornerRadius(round)
    )
    drawRoundRect(
        Brush.verticalGradient(listOf(color.copy(.98f), color.copy(.72f)), y, y + height),
        Offset(x, y), Size(width, height), CornerRadius(round)
    )
    drawRoundRect(
        White.copy(.38f), Offset(x + width * .09f, y + height * .08f),
        Size(width * .82f, height * .7f), CornerRadius(round * .72f), style = Stroke(1f)
    )
    paint.color = Ink.copy(.78f).toArgb()
    paint.textSize = (min(width, height) * if (label.length > 2) .2f else .3f).coerceAtLeast(5.5f)
    drawContext.canvas.nativeCanvas.drawText(label, x + width / 2, y + height * .6f, paint)
}

@Composable
private fun Sticker(icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier.size(38.dp).shadow(12.dp, CircleShape, ambientColor = color, spotColor = color)
            .background(color, CircleShape).border(3.dp, White, CircleShape),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = Ink, modifier = Modifier.size(19.dp)) }
}

@Composable
private fun SectionHeader(title: String, action: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
        Text(action, color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ThemeCarousel(selected: Int, onSelected: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 6.dp)) {
        itemsIndexed(capThemes) { index, theme ->
            ThemeCard(theme, index == selected) { onSelected(index) }
        }
    }
}

@Composable
private fun ThemeCard(theme: CapTheme, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(146.dp).height(164.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) theme.accent.copy(.13f) else Glass.copy(.78f)
        ),
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) theme.accent else White.copy(.07f))
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            MiniKeycaps(theme.colors, Modifier.fillMaxWidth().height(76.dp))
            if (!theme.unlocked) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(26.dp).background(Ink.copy(.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Lock, null, tint = White, modifier = Modifier.size(13.dp)) }
            } else if (selected) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(26.dp).background(theme.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Check, null, tint = Ink, modifier = Modifier.size(15.dp)) }
            }
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(theme.name, color = White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(theme.vibe, color = Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun MiniKeycaps(colors: List<Color>, modifier: Modifier = Modifier) = Canvas(modifier) {
    val key = size.width * .18f
    val gap = size.width * .035f
    repeat(2) { row ->
        repeat(5) { column ->
            val x = column * (key + gap) + if (row == 1) key * .28f else 0f
            val y = row * (key * .62f) + 5f
            val color = colors[(column + row * 2) % colors.size]
            drawRoundRect(color.copy(.45f), Offset(x, y + 5f), Size(key, key * .62f), CornerRadius(8f))
            drawRoundRect(
                Brush.verticalGradient(listOf(color, color.copy(.7f))),
                Offset(x, y), Size(key, key * .62f), CornerRadius(8f)
            )
        }
    }
}

@Composable
private fun DailyQuestCard() {
    GlassPanel(Modifier.fillMaxWidth().height(118.dp), 24.dp) {
        Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(62.dp).background(Brush.linearGradient(listOf(Purple, Pink)), RoundedCornerShape(19.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.EmojiEvents, null, tint = White, modifier = Modifier.size(31.dp))
                Text(
                    "+80", color = Ink, fontSize = 9.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.TopEnd).offset(5.dp, (-5).dp)
                        .background(Yellow, CircleShape).padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("DAILY QUEST", color = Pink, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                Text("Style 3 keyboards", color = White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(7.dp).background(White.copy(.08f), CircleShape)) {
                        Box(
                            Modifier.fillMaxWidth(.66f).fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(Pink, Purple)), CircleShape)
                        )
                    }
                    Spacer(Modifier.width(9.dp))
                    Text("2/3", color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Rounded.ChevronRight, null, tint = Muted)
        }
    }
}

@Composable
private fun TrendingRow() {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TrendChip("♡", "Heart Pop", Pink)
        TrendChip("☁", "Cloudy", Blue)
        TrendChip("⚡", "Power Up", Yellow)
    }
}

@Composable
private fun TrendChip(symbol: String, title: String, color: Color) {
    Row(
        Modifier.width(152.dp).background(Glass.copy(.76f), RoundedCornerShape(20.dp))
            .border(1.dp, White.copy(.07f), RoundedCornerShape(20.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).background(color.copy(.2f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) { Text(symbol, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black) }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("1.2k saves", color = Muted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun CustomizePage(
    modifier: Modifier,
    theme: CapTheme,
    selectedTheme: Int,
    onThemeSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { PageTitle("Keycap lab", "Make every key feel like yours.", Icons.Rounded.Tune) }
        item {
            GlassPanel(Modifier.fillMaxWidth().height(250.dp), 30.dp) {
                Column(
                    Modifier.fillMaxSize().padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(theme.name, color = White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Row(
                            Modifier.background(Pink.copy(.15f), RoundedCornerShape(50))
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Favorite, null, tint = Pink, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Saved", color = Pink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    KeyboardCanvas(theme.colors, Modifier.fillMaxWidth().height(190.dp))
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Color recipe", color = White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Row(
                    Modifier.clickable { onThemeSelected((selectedTheme + 1) % capThemes.size) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Shuffle, null, tint = Mint, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Surprise me", color = Mint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(13.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                capThemes.forEachIndexed { index, item ->
                    Box(
                        Modifier.size(if (selectedTheme == index) 50.dp else 44.dp)
                            .clickable { onThemeSelected(index) }
                            .background(item.accent, CircleShape)
                            .then(
                                if (selectedTheme == index) Modifier.border(3.dp, White, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedTheme == index) {
                            Icon(Icons.Rounded.Check, null, tint = Ink, modifier = Modifier.size(19.dp))
                        }
                    }
                }
                Box(
                    Modifier.size(44.dp).border(1.dp, White.copy(.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Add, null, tint = White, modifier = Modifier.size(19.dp)) }
            }
        }
        item {
            SectionHeader("Special keys", "Edit layout")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpecialKey("ESC", "✦", Pink, Modifier.weight(1f))
                SpecialKey("ENTER", "↵", Mint, Modifier.weight(1f))
                SpecialKey("SPACE", "CAPPOP", Purple, Modifier.weight(1f))
            }
        }
        item {
            Button(
                onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)
            ) {
                Icon(Icons.Rounded.ViewInAr, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Preview my board", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SpecialKey(label: String, symbol: String, color: Color, modifier: Modifier) {
    Column(
        modifier.background(Glass.copy(.8f), RoundedCornerShape(19.dp))
            .border(1.dp, White.copy(.07f), RoundedCornerShape(19.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(1.5f)
                .background(color.copy(.85f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) { Text(symbol, color = Ink, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1) }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CollectionPage(modifier: Modifier) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { PageTitle("My collection", "Tiny treasures, big personality.", Icons.Rounded.CollectionsBookmark) }
        item {
            GlassPanel(Modifier.fillMaxWidth().height(146.dp), 26.dp) {
                Row(Modifier.fillMaxSize().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(84.dp)
                            .background(Brush.linearGradient(listOf(Purple, Pink)), RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Diamond, null, tint = White, modifier = Modifier.size(40.dp))
                        Text(
                            "12", color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.align(Alignment.BottomEnd).offset(4.dp, 4.dp)
                                .background(Yellow, CircleShape).padding(7.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("KEYCAP COLLECTOR", color = Purple, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 1.sp)
                        Text("12 / 24 sets", color = White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Spacer(Modifier.height(9.dp))
                        Box(Modifier.fillMaxWidth().height(8.dp).background(White.copy(.08f), CircleShape)) {
                            Box(
                                Modifier.fillMaxWidth(.5f).fillMaxHeight()
                                    .background(Brush.horizontalGradient(listOf(Purple, Pink)), CircleShape)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("2 more to unlock a mystery cap", color = Muted, fontSize = 10.sp)
                    }
                }
            }
        }
        item { SectionHeader("Your sets", "12 collected") }
        itemsIndexed(capThemes) { index, theme -> CollectionSet(theme, index) }
    }
}

@Composable
private fun CollectionSet(theme: CapTheme, index: Int) {
    Row(
        Modifier.fillMaxWidth().background(Glass.copy(.72f), RoundedCornerShape(22.dp))
            .border(1.dp, White.copy(.07f), RoundedCornerShape(22.dp)).padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(88.dp).background(theme.accent.copy(.1f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) { MiniKeycaps(theme.colors, Modifier.fillMaxWidth().height(60.dp)) }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(theme.name, color = White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text(if (theme.unlocked) "Complete set" else "18 / 84 keys", color = Muted, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val rarity = when (index) { 0 -> "ULTRA RARE"; 1 -> "RARE"; else -> "COMMON" }
                Box(Modifier.size(6.dp).background(theme.accent, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text(rarity, color = theme.accent, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
            }
        }
        Icon(
            if (theme.unlocked) Icons.Rounded.ChevronRight else Icons.Rounded.Lock,
            null, tint = Muted, modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ProfilePage(modifier: Modifier) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { PageTitle("Your space", "The person behind the keycaps.", Icons.Rounded.Person) }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        Modifier.size(104.dp)
                            .background(Brush.linearGradient(listOf(Purple, Pink, Mint)), CircleShape)
                            .padding(4.dp).background(Ink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.size(82.dp).background(Purple.copy(.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("J", color = White, fontWeight = FontWeight.Black, fontSize = 42.sp) }
                    }
                    Box(
                        Modifier.size(30.dp).background(Mint, CircleShape).border(3.dp, Ink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.Bolt, null, tint = Ink, modifier = Modifier.size(15.dp)) }
                }
                Spacer(Modifier.height(10.dp))
                Text("JunoKeys", color = White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                Text("@juno.caps · joined the party 8 months ago", color = Muted, fontSize = 11.sp)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileStat("12", "SETS", Purple, Modifier.weight(1f))
                ProfileStat("2.8K", "LIKES", Pink, Modifier.weight(1f))
                ProfileStat("36", "REMIXES", Mint, Modifier.weight(1f))
            }
        }
        item {
            GlassPanel(Modifier.fillMaxWidth().height(142.dp), 24.dp) {
                Column(Modifier.padding(17.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, tint = Yellow, modifier = Modifier.size(19.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Level 12", color = White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                        Text("2,480 XP", color = Yellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(10.dp).background(White.copy(.08f), CircleShape)) {
                        Box(
                            Modifier.fillMaxWidth(.72f).fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(Yellow, Pink)), CircleShape)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("520 XP until Level 13 · keep styling!", color = Muted, fontSize = 11.sp)
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileMenu("My moodboards", Icons.Rounded.Palette, Purple)
                ProfileMenu("Achievements", Icons.Rounded.EmojiEvents, Yellow)
                ProfileMenu("App settings", Icons.Rounded.Tune, Mint)
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier.background(Glass.copy(.76f), RoundedCornerShape(20.dp))
            .border(1.dp, White.copy(.07f), RoundedCornerShape(20.dp)).padding(vertical = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 19.sp, fontWeight = FontWeight.Black)
        Text(label, color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .9.sp)
    }
}

@Composable
private fun ProfileMenu(title: String, icon: ImageVector, color: Color) {
    Row(
        Modifier.fillMaxWidth().background(Glass.copy(.72f), RoundedCornerShape(18.dp)).padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).background(color.copy(.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = color, modifier = Modifier.size(19.dp)) }
        Spacer(Modifier.width(12.dp))
        Text(title, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = Muted, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun PageTitle(title: String, subtitle: String, icon: ImageVector) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, color = White, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, letterSpacing = (-.6).sp)
            Text(subtitle, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Box(
            Modifier.size(44.dp).background(Mint.copy(.13f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Mint, modifier = Modifier.size(22.dp)) }
    }
}

@Composable
private fun GlassPanel(modifier: Modifier, cornerRadius: Dp, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = Glass.copy(.68f),
        border = BorderStroke(1.dp, White.copy(.09f)),
        shadowElevation = 18.dp
    ) { content() }
}

@Composable
private fun BottomBar(selected: AppTab, onSelected: (AppTab) -> Unit) {
    Surface(
        color = Color(0xF20A0D24),
        shadowElevation = 24.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, White.copy(.06f))
    ) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(72.dp)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                val icon = when (tab) {
                    AppTab.Explore -> Icons.Rounded.Explore
                    AppTab.Customize -> Icons.Rounded.Keyboard
                    AppTab.Collection -> Icons.Rounded.CollectionsBookmark
                    AppTab.Profile -> Icons.Rounded.Person
                }
                val active = selected == tab
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .clickable { onSelected(tab) }.padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (active) {
                            Box(
                                Modifier.size(width = 38.dp, height = 29.dp)
                                    .background(Mint.copy(.14f), RoundedCornerShape(11.dp))
                            )
                        }
                        Icon(icon, tab.label, tint = if (active) Mint else Muted, modifier = Modifier.size(22.dp))
                        if (tab == AppTab.Collection) {
                            Box(Modifier.align(Alignment.TopEnd).size(6.dp).background(Pink, CircleShape))
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tab.label, color = if (active) White else Muted, fontSize = 9.sp,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Clip, textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
