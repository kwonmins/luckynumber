package com.example.unum.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.unum.R
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.GenderOption
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Background
import com.example.unum.ui.theme.BackgroundAlt
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.BorderStrong
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

@Composable
fun MysticBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(Background, BackgroundAlt, Background)
            )
        )
    ) {
        content()
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tonalColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
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
    eyebrow: String = "수리 운세"
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Surface2)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(Accent, CircleShape)
            )
            Text(eyebrow, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
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
    title: String = "수리의 안내"
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 14,
        tonalColor = Surface2,
        borderColor = BorderStrong
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Gold, style = MaterialTheme.typography.labelLarge)
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
        tonalColor = Surface2,
        borderColor = BorderStrong
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = "수리",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface)
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(message, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                androidx.compose.material3.LinearProgressIndicator(
                    color = Accent,
                    trackColor = Surface3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ToggleSegment(selected: CalendarType, onSelected: (CalendarType) -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 6, tonalColor = Surface2) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Accent.copy(alpha = 0.18f) else Surface)
            .border(1.dp, if (selected) Accent.copy(alpha = 0.55f) else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) TextPrimary else TextSecondary, style = MaterialTheme.typography.labelLarge)
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
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Accent,
            unfocusedBorderColor = BorderStrong,
            cursorColor = Accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun GenderSelector(selected: GenderOption, onSelected: (GenderOption) -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 6, tonalColor = Surface2) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenderOption.entries.forEach { option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected == option) Mint.copy(alpha = 0.16f) else Surface)
                        .border(1.dp, if (selected == option) Mint.copy(alpha = 0.50f) else Border, RoundedCornerShape(8.dp))
                        .clickable { onSelected(option) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(option.label, color = if (selected == option) TextPrimary else TextSecondary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(Accent, Blue))
                } else {
                    Brush.horizontalGradient(listOf(Accent.copy(alpha = 0.35f), Blue.copy(alpha = 0.35f)))
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .border(1.dp, BorderStrong, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem("home", "홈", Icons.Rounded.Home),
    BottomNavItem("fortune", "무료 결과", Icons.Rounded.AutoAwesome),
    BottomNavItem("premium", "AI 상담", Icons.Rounded.AutoStories),
    BottomNavItem("library", "보관함", Icons.Rounded.Bookmarks),
    BottomNavItem("settings", "설정", Icons.Rounded.Settings)
)

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
        containerColor = Overlay,
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
                        tint = if (selected) TextPrimary else TextMuted
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected) TextPrimary else TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TextPrimary,
                    selectedTextColor = TextPrimary,
                    indicatorColor = Accent.copy(alpha = 0.18f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
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
                Box(Modifier.size(6.dp).background(Accent, CircleShape))
                Text(it, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun NumberPill(label: String, value: String, accent: Color = Accent, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
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
            .background(Gold.copy(alpha = 0.14f))
            .border(1.dp, Gold.copy(alpha = 0.32f), RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(text, color = Gold, style = MaterialTheme.typography.labelMedium)
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
        tonalColor = Surface2,
        borderColor = BorderStrong,
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
