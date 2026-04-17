package com.example.unum.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.unum.R
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.GenderOption
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Background
import com.example.unum.ui.theme.Blue
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.BorderStrong
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Mint
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun MysticBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.background(Background)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(listOf(Accent.copy(alpha = 0.18f), Color.Transparent), center = Offset(size.width * 0.12f, size.height * 0.08f), radius = size.minDimension * 0.34f),
                radius = size.minDimension * 0.34f,
                center = Offset(size.width * 0.12f, size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Blue.copy(alpha = 0.14f), Color.Transparent), center = Offset(size.width * 0.86f, size.height * 0.22f), radius = size.minDimension * 0.28f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.86f, size.height * 0.22f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Mint.copy(alpha = 0.10f), Color.Transparent), center = Offset(size.width * 0.74f, size.height * 0.86f), radius = size.minDimension * 0.36f),
                radius = size.minDimension * 0.36f,
                center = Offset(size.width * 0.74f, size.height * 0.86f)
            )
        }
        content()
    }
}

@Composable
fun SurfaceCard(modifier: Modifier = Modifier, contentPadding: Int = 16, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .padding(contentPadding.dp)
    ) { content() }
}

@Composable
fun LogoChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Accent.copy(alpha = 0.12f))
            .border(1.dp, Accent.copy(alpha = 0.20f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("✦", color = Accent, style = MaterialTheme.typography.labelMedium)
        Text("수리학", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun MascotGuideCard(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "수리"
) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 14) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.mascot_icon),
                contentDescription = title,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Accent, style = MaterialTheme.typography.labelLarge)
                Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MascotLoadingCard(message: String, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 16) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.suri_loading),
                contentDescription = "운명을 보는 수리",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(message, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                androidx.compose.material3.LinearProgressIndicator(
                    color = Gold,
                    trackColor = Surface2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AppHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LogoChip()
        Text(title, style = MaterialTheme.typography.displayLarge, color = TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun ToggleSegment(selected: CalendarType, onSelected: (CalendarType) -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 6) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TogglePill("음력", selected == CalendarType.LUNAR, Modifier.weight(1f)) { onSelected(CalendarType.LUNAR) }
            TogglePill("양력", selected == CalendarType.SOLAR, Modifier.weight(1f)) { onSelected(CalendarType.SOLAR) }
        }
    }
}

@Composable
private fun RowScope.TogglePill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Accent.copy(alpha = 0.14f) else Surface2)
            .border(1.dp, if (selected) Accent.copy(alpha = 0.35f) else Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) TextPrimary else TextSecondary, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DateInputRow(
    year: String, month: String, day: String,
    onYearChange: (String) -> Unit, onMonthChange: (String) -> Unit, onDayChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DateField(year, onYearChange, "연도", Modifier.weight(1.6f))
        DateField(month, onMonthChange, "월", Modifier.weight(1f))
        DateField(day, onDayChange, "일", Modifier.weight(1f))
    }
}

@Composable
private fun DateField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        placeholder = { Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Accent.copy(alpha = 0.28f),
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
    SurfaceCard(modifier = modifier.fillMaxWidth(), contentPadding = 6) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenderOption.entries.forEach { option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected == option) Surface2 else Surface)
                        .border(1.dp, if (selected == option) Accent.copy(alpha = 0.32f) else Border, RoundedCornerShape(14.dp))
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
    val colors = if (enabled) listOf(Color(0xFF7C6FCD), Accent, Color(0xFF9ECDE8)) else listOf(Color(0x667C6FCD), Accent.copy(alpha = 0.45f), Color(0x669ECDE8))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors))
            .border(1.dp, BorderStrong, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Background, style = MaterialTheme.typography.labelLarge)
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
val bottomNavItems = listOf(
    BottomNavItem("home", "홈", Icons.Rounded.Home),
    BottomNavItem("fortune", "운세", Icons.Rounded.AutoAwesome),
    BottomNavItem("premium", "상담", Icons.Rounded.ChatBubbleOutline),
    BottomNavItem("story", "이야기", Icons.Rounded.AutoStories)
)

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier.fillMaxWidth().border(1.dp, Border, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = Surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, item.label, tint = if (selected) Accent else TextMuted) },
                label = { Text(item.label, color = if (selected) TextPrimary else TextMuted, style = MaterialTheme.typography.bodySmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = TextPrimary,
                    indicatorColor = Accent.copy(alpha = 0.10f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
}

@Composable
fun KeywordPills(items: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.take(3).forEach {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Surface2).border(1.dp, Border, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.background(Accent.copy(alpha = 0.9f), CircleShape).padding(3.dp))
                Text(it, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
