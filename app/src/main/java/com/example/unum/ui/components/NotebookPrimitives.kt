package com.example.unum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.BookLine
import com.example.unum.ui.theme.BookPaperEdge
import com.example.unum.ui.theme.TextMuted

/**
 * Small visual atoms that define the "premium paper notebook" language.
 *
 * Keep these primitives intentionally dumb: they do not know about fortune data
 * or navigation. Screens and book components compose them into covers, pages,
 * archive thumbnails, and reader tabs.
 */
@Composable
fun NotebookSeal(
    text: String,
    color: Color = Accent,
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.34f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            style = if (size < 50) MaterialTheme.typography.titleMedium else MaterialTheme.typography.displayMedium
        )
    }
}

@Composable
fun BookPageEdges(
    modifier: Modifier = Modifier,
    accentColor: Color,
    count: Int = 5
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.End
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size((20 + index * 3).dp, 2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accentColor.copy(alpha = 0.14f))
            )
        }
    }
}

@Composable
fun NotebookSideTabs(
    modifier: Modifier = Modifier,
    accentColor: Color,
    compact: Boolean,
    labels: List<String> = if (compact) listOf("1", "2") else listOf("1", "2", "3")
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp),
        horizontalAlignment = Alignment.End
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .size(width = if (compact) 18.dp else 22.dp, height = if (compact) 28.dp else 34.dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                    .background(if (index == 0) accentColor else BookPaperEdge)
                    .border(
                        1.dp,
                        if (index == 0) accentColor.copy(alpha = 0.26f) else BookLine,
                        RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == 0) Color.White else TextMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
