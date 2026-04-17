package com.example.unum.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.unum.R
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        "안녕하세요, 저는 숫자로 당신의 운명을 봐주는 수리입니다.",
        "양력 생년월일을 입력해주시면 숨은 숫자의 흐름을 읽어드릴게요.",
        "기본 운명과 고민 상담도 함께 들려드리니 가볍고 재미있게 활용해주세요."
    )

    MysticBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.suri_loading),
                contentDescription = "수리",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(22.dp))
            SurfaceCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("수리의 안내", color = Accent, style = MaterialTheme.typography.labelLarge)
                    Text(pages[page], color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("${page + 1} / ${pages.size}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == page) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (index == page) Accent else Surface2)
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            GradientButton(
                text = if (page == pages.lastIndex) "시작하기" else "다음",
                onClick = {
                    if (page == pages.lastIndex) onFinished() else page += 1
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
