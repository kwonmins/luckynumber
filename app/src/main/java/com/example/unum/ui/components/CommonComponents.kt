package com.example.unum.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unum.R
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.GenderOption
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.AccentDark
import com.example.unum.ui.theme.AppCanvas
import com.example.unum.ui.theme.Background
import com.example.unum.ui.theme.BackgroundAlt
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.DeepNavy
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Overlay
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.Surface3
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MysticBackground(
    modifier: Modifier = Modifier,
    animatedWaves: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.background(Background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(AppCanvas, Background, BackgroundAlt.copy(alpha = 0.28f))))
        }
        if (animatedWaves) {
            FortuneWaveField(Modifier.fillMaxSize())
        }
        content()
    }
}

@Composable
private fun FortuneWaveField(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fortuneWaveField")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fortuneWavePhase"
    )
    Canvas(modifier = modifier) {
        repeat(2) { waveIndex ->
            val yBase = size.height * (0.24f + waveIndex * 0.18f)
            val color = listOf(Accent, Gold)[waveIndex]
            val alpha = listOf(0.065f, 0.045f)[waveIndex]
            val stroke = listOf(1.4.dp, 1.dp)[waveIndex].toPx()
            val points = 96
            var previous: Offset? = null
            for (i in 0..points) {
                val x = size.width * i / points
                val wave = sin((i / 18f + phase * 2f + waveIndex * 0.7f) * PI).toFloat()
                val y = yBase + wave * (18.dp.toPx() + waveIndex * 5.dp.toPx())
                val current = Offset(x, y)
                previous?.let {
                    drawLine(
                        color = color.copy(alpha = alpha),
                        start = it,
                        end = current,
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
                previous = current
            }
        }
    }
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    contentPadding: Int = 16,
    tonalColor: Color = Surface,
    borderColor: Color = Border,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 5.dp,
                shape = cardShape,
                clip = false,
                ambientColor = DeepNavy.copy(alpha = 0.035f),
                spotColor = DeepNavy.copy(alpha = 0.08f)
            )
            .clip(cardShape)
            .background(tonalColor)
            .border(1.dp, borderColor, cardShape)
            .padding(contentPadding.dp)
    ) {
        content()
    }
}

@Composable
fun AppHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    eyebrow: String = "수리의 운세노트"
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Accent.copy(alpha = 0.08f))
                .border(1.dp, Accent.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
                .padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(Accent, CircleShape)
            )
            Text(eyebrow, color = Accent, style = MaterialTheme.typography.labelMedium)
        }
        Text(title, style = MaterialTheme.typography.displayLarge, color = TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun MascotGuideCard(
    message: String,
    modifier: Modifier = Modifier,
    @DrawableRes imageRes: Int = R.drawable.mascot_icon,
    title: String = "안내"
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 16,
        tonalColor = Surface,
        borderColor = Border
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Accent.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                PastelSuri(pose = 1, modifier = Modifier.size(64.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MascotLoadingCard(
    message: String,
    modifier: Modifier = Modifier,
    @DrawableRes imageRes: Int = R.drawable.suri_loading
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 16,
        tonalColor = Surface,
        borderColor = Border
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(message, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            androidx.compose.material3.LinearProgressIndicator(
                color = Accent,
                trackColor = Surface3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ToggleSegment(selected: CalendarType, onSelected: (CalendarType) -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 5, tonalColor = Surface2, borderColor = Border) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TogglePill("양력", selected == CalendarType.SOLAR, Modifier.weight(1f)) { onSelected(CalendarType.SOLAR) }
            TogglePill("음력", selected == CalendarType.LUNAR, Modifier.weight(1f)) { onSelected(CalendarType.LUNAR) }
        }
    }
}

@Composable
private fun RowScope.TogglePill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Accent else Surface)
            .border(1.dp, if (selected) Accent else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Surface else TextSecondary, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DateInputRow(
    year: String,
    month: String,
    day: String,
    onYearChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DateField(year, onYearChange, "연도", Modifier.weight(1.6f))
        DateField(month, onMonthChange, "월", Modifier.weight(1f))
        DateField(day, onDayChange, "일", Modifier.weight(1f))
    }
}

@Composable
private fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        placeholder = { Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Accent,
            unfocusedBorderColor = Border,
            cursorColor = Accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun GenderSelector(selected: GenderOption, onSelected: (GenderOption) -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 5, tonalColor = Surface2, borderColor = Border) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GenderOption.entries.forEach { option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected == option) Accent else Surface)
                        .border(1.dp, if (selected == option) Accent else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { onSelected(option) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(option.label, color = if (selected == option) Surface else TextSecondary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Accent,
    contentColor: Color = Surface
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = color.copy(alpha = 0.16f),
                spotColor = color.copy(alpha = 0.22f)
            )
            .clip(shape)
            .background(Brush.horizontalGradient(if (enabled) listOf(color, AccentDark) else listOf(Surface3, Surface3)))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) contentColor else TextSecondary, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Surface)
            .border(1.dp, Border, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) TextPrimary else TextMuted, style = MaterialTheme.typography.labelLarge)
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem("home", "홈", Icons.Outlined.Home),
    BottomNavItem("fortune", "운세", Icons.Outlined.Explore),
    BottomNavItem("library", "스크랩", Icons.Outlined.BookmarkBorder),
    BottomNavItem("premium", "프리미엄", Icons.Outlined.AutoStories),
    BottomNavItem("settings", "마이", Icons.Outlined.Person)
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    dark: Boolean = false
) {
    val navShape = RoundedCornerShape(0.dp)
    val inactiveColor = TextMuted
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .shadow(
                elevation = 0.dp,
                shape = navShape,
                clip = false,
                ambientColor = DeepNavy.copy(alpha = 0.06f),
                spotColor = DeepNavy.copy(alpha = 0.15f)
            )
            .clip(navShape)
            .border(1.dp, Border, navShape),
        containerColor = Surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        item.label,
                        tint = if (selected) Accent else inactiveColor
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected) Accent else inactiveColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    indicatorColor = Accent.copy(alpha = 0.10f),
                    unselectedIconColor = inactiveColor,
                    unselectedTextColor = inactiveColor
                )
            )
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
}

@Composable
fun SectionCaption(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.bodySmall, color = TextMuted)
}

@Composable
fun EmptyStateView(
    title: String,
    description: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Surface,
        borderColor = Border,
        contentPadding = 22
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Accent.copy(alpha = 0.10f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(it, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
                }
            }
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            Text(description, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            GradientButton(actionText, onActionClick, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun KeywordPills(items: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.take(3).forEach {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Surface2)
                    .border(1.dp, Border, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.size(6.dp).background(Gold, CircleShape))
                Text(it, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun NumberPill(label: String, value: String, accent: Color = Accent, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, accent.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(value, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun PremiumBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Accent.copy(alpha = 0.08f))
            .border(1.dp, Accent.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(text, color = Accent, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Accent,
    trailing: @Composable (() -> Unit)? = null
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Surface,
        borderColor = Border,
        contentPadding = 16
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            trailing?.invoke() ?: Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accentColor, CircleShape)
            )
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        trailing = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}
