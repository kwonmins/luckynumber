package com.example.unum.presentation.premium

import com.example.unum.presentation.*

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun PremiumCompactChoice(
    text: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) accent.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.045f))
            .border(1.dp, if (selected) accent else Color.White.copy(alpha = 0.09f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else Color.White.copy(alpha = 0.46f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun PremiumDarkDateField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter(Char::isDigit).take(maxLength)) },
        modifier = modifier,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, textAlign = TextAlign.Center),
        placeholder = {
            Text(placeholder, color = Color.White.copy(alpha = 0.28f), fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        },
        shape = RoundedCornerShape(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Black.copy(alpha = 0.28f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.28f),
            focusedBorderColor = PremiumEntryPink.copy(alpha = 0.72f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
            cursorColor = PremiumEntryPink,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
internal fun PremiumDarkConcernField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, lineHeight = 21.sp),
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.28f), style = MaterialTheme.typography.bodySmall, lineHeight = 19.sp) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Black.copy(alpha = 0.28f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.28f),
            focusedBorderColor = PremiumEntryBlue.copy(alpha = 0.72f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
            cursorColor = PremiumEntryBlue,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
internal fun PremiumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    colors: List<Color>,
    enabled: Boolean = true,
    contentColor: Color = Color.White
) {
    val shape = RoundedCornerShape(13.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(if (enabled) 16.dp else 0.dp, shape, clip = false)
            .clip(shape)
            .background(
                if (enabled) Brush.linearGradient(colors)
                else Brush.linearGradient(colors.map { it.copy(alpha = 0.26f) })
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) contentColor else contentColor.copy(alpha = 0.40f), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
    }
}

@Composable
internal fun PremiumEntryHint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PremiumEntryGold.copy(alpha = 0.08f))
            .border(1.dp, PremiumEntryGold.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✦", color = PremiumEntryGold, fontSize = 12.sp)
        Text(text, color = Color.White.copy(alpha = 0.56f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun PremiumTrustLine(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.30f),
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

