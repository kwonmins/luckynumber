package com.example.unum.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unum.ui.components.*
import com.example.unum.ui.theme.*

@Composable
fun OnboardingScreen(isSigningIn: Boolean, errorMessage: String?, onStartAsGuest: () -> Unit, onStartWithKakao: () -> Unit) {
    MysticBackground(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            MoonGarden(Modifier.fillMaxWidth().height(260.dp).align(Alignment.BottomCenter))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)) {
                Spacer(Modifier.height(32.dp))
                FortuneIllustration(FortuneArt.MOON,Modifier.size(54.dp))
                Text("수리운세",color=DeepNavy,style=MaterialTheme.typography.displayMedium)
                Text("오늘, 당신의 이야기에\n좋은 흐름이 되길",color=TextSecondary,style=MaterialTheme.typography.bodyLarge,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                PastelSuri(1,Modifier.size(190.dp))
                Text("수리와 함께 나를 알아가는 시간",color=Accent,style=MaterialTheme.typography.titleSmall)
                Text("생년월일로 오늘의 흐름을 읽고,\n마음에 담아둔 질문을 나눠보세요.",color=TextSecondary,style=MaterialTheme.typography.bodyMedium,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                errorMessage?.let { Text(it,color=Rose,style=MaterialTheme.typography.bodySmall) }
                GradientButton("시작하기",onStartAsGuest,Modifier.fillMaxWidth())
                SecondaryButton(if(isSigningIn) "로그인 중…" else "카카오로 시작하기",onStartWithKakao,Modifier.fillMaxWidth(),enabled=!isSigningIn)
                Text("로그인 없이도 무료 운세를 볼 수 있어요",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
