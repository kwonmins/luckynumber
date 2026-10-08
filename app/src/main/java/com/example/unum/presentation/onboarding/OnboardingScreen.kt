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
fun OnboardingScreen(isSigningIn: Boolean,errorMessage: String?,onStartAsGuest: () -> Unit,onStartWithKakao: () -> Unit) {
    MysticBackground(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            QuietLandscape(Modifier.fillMaxWidth().fillMaxHeight(.23f).align(Alignment.BottomCenter),blossoms=true)
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().navigationBarsPadding().padding(horizontal=28.dp,vertical=26.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(20.dp)) {
                Spacer(Modifier.height(40.dp))
                QuietEntrance(delay=100) {FortuneIllustration(FortuneArt.MOON,Modifier.size(70.dp))}
                Spacer(Modifier.height(16.dp))
                Text("수리운세",color=DeepNavy,style=MaterialTheme.typography.displayMedium)
                Text("숫자에 담긴, 당신의 오늘과 내일",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
                Text("오늘, 당신의 이야기에\n좋은 흐름이 되길",color=TextSecondary,style=MaterialTheme.typography.bodySmall,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(22.dp))
                QuietEntrance(delay=200) {PastelSuri(0,Modifier.size(108.dp))}
                errorMessage?.let {Text(it,color=Rose,style=MaterialTheme.typography.bodySmall)}
                GradientButton("시작하기",onStartAsGuest,Modifier.fillMaxWidth())
                SecondaryButton(if(isSigningIn) "로그인 중…" else "카카오로 시작하기",onStartWithKakao,Modifier.fillMaxWidth(),enabled=!isSigningIn)
                Text("로그인 없이도 오늘의 운세를 볼 수 있어요",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(35.dp))
            }
        }
    }
}
