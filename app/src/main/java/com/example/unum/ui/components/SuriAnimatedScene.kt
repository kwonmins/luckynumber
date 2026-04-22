package com.example.unum.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.unum.R
import com.example.unum.data.model.PremiumMode
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.SuriSpeechSceneState
import com.example.unum.data.model.SuriSpeechScript
import com.example.unum.data.model.SuriSpeechSegment
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.BorderStrong
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private data class SuriSceneStep(
    val frameRes: List<Int>,
    val title: String,
    val message: String,
    val chip: String
)

@Composable
fun SuriAnimatedSceneCard(
    mode: PremiumMode,
    topic: PremiumTopic,
    modifier: Modifier = Modifier,
    speechScript: SuriSpeechScript? = null
) {
    val floatTransition = rememberInfiniteTransition(label = "suri-scene")
    val bobOffset by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "suri-bob"
    )

    SurfaceCard(
        modifier = modifier.fillMaxWidth(),
        tonalColor = Surface2,
        borderColor = BorderStrong,
        contentPadding = 0
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(336.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.suri_hanok_scene_cropped),
                contentDescription = "수리 한옥 상담 배경",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFF7E7D2).copy(alpha = 0.32f),
                                Color(0xFF8B5C38).copy(alpha = 0.18f),
                                Color(0xFF322117).copy(alpha = 0.45f)
                            )
                        )
                    )
            )

            if (speechScript == null) {
                IdleSceneCardContent(
                    mode = mode,
                    topic = topic,
                    bobOffset = bobOffset
                )
            } else {
                SpokenSceneCardContent(
                    speechScript = speechScript,
                    mode = mode,
                    bobOffset = bobOffset
                )
            }
        }
    }
}

@Composable
private fun IdleSceneCardContent(
    mode: PremiumMode,
    topic: PremiumTopic,
    bobOffset: Float
) {
    val steps = remember(mode, topic) { sceneSteps(mode, topic) }
    var stepIndex by remember(mode, topic) { mutableIntStateOf(0) }
    val currentStep = steps[stepIndex]

    LaunchedEffect(steps) {
        while (true) {
            delay(2600)
            stepIndex = (stepIndex + 1) % steps.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BubbleHeader(
            chip = currentStep.chip,
            title = currentStep.title,
            message = currentStep.message,
            trailing = {
                SoftPill(text = "대기 중", active = false)
            }
        )

        SceneMascot(
            frameRes = currentStep.frameRes,
            contentDescription = currentStep.title,
            bobOffset = bobOffset,
            frameDurationMillis = 980L
        )

        Text(
            text = if (mode == PremiumMode.PERSONAL) {
                "수리가 고민 흐름에 맞춰 설명할 준비를 하고 있어요."
            } else {
                "두 사람의 기운과 관계 흐름을 순서대로 읽어드릴 준비를 하고 있어요."
            },
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SpokenSceneCardContent(
    speechScript: SuriSpeechScript,
    mode: PremiumMode,
    bobOffset: Float
) {
    val player = rememberSuriSpeechPlayer()

    LaunchedEffect(speechScript.scriptId) {
        player.updateScript(speechScript)
    }

    val activeSegment = speechScript.segments.getOrElse(player.currentIndex) { speechScript.segments.first() }
    val statusText = when {
        player.errorMessage != null -> player.errorMessage!!
        player.isReady && player.isPlaying -> "문장 ${player.currentIndex + 1}/${speechScript.segments.size}를 읽고 있어요."
        player.isReady -> "시스템 음성으로 바로 재생할 수 있어요."
        else -> "기기 음성 엔진을 준비하고 있어요."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BubbleHeader(
            chip = activeSegment.chip,
            title = activeSegment.title,
            message = activeSegment.body,
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoftPill(
                        text = when {
                            player.isPlaying -> "멈추기"
                            player.currentIndex > 0 -> "이어서 듣기"
                            else -> "수리 목소리 듣기"
                        },
                        active = player.isPlaying,
                        enabled = player.isReady,
                        onClick = player::togglePlayback
                    )
                    SoftPill(
                        text = "처음부터",
                        active = false,
                        enabled = player.isReady,
                        onClick = player::replay
                    )
                }
            }
        )

        SceneMascot(
            frameRes = speechSpriteFrames(activeSegment, player.isPlaying),
            contentDescription = activeSegment.title,
            bobOffset = bobOffset,
            frameDurationMillis = if (player.isPlaying) 340L else 920L
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = speechScript.title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = speechScript.subtitle,
                color = Color.White.copy(alpha = 0.84f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = statusText,
                color = if (player.errorMessage == null) Color.White.copy(alpha = 0.76f) else Color(0xFFFFE2E0),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                speechScript.segments.forEachIndexed { index, segment ->
                    val selected = index == player.currentIndex
                    Box(
                        modifier = Modifier
                            .size(width = if (selected) 22.dp else 10.dp, height = 6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                when {
                                    selected && player.isPlaying -> Gold
                                    selected -> Accent.copy(alpha = 0.88f)
                                    segment.state == SuriSpeechSceneState.CAUTION -> Rose.copy(alpha = 0.38f)
                                    else -> Color.White.copy(alpha = 0.24f)
                                }
                            )
                    )
                }
            }
            Text(
                text = if (mode == PremiumMode.PERSONAL) {
                    "지금은 Android 시스템 TTS로 읽고 있어요. 나중에 립싱크나 더 세밀한 모션으로 확장하기 쉽게 분리해뒀어요."
                } else {
                    "궁합 대사도 같은 구조로 재생돼요. 음성 엔진이나 애니메이션 툴이 바뀌어도 이 스크립트 구조는 그대로 쓸 수 있어요."
                },
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BubbleHeader(
    chip: String,
    title: String,
    message: String,
    trailing: @Composable (() -> Unit)? = null
) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface.copy(alpha = 0.90f),
        borderColor = Gold.copy(alpha = 0.25f),
        contentPadding = 14
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Accent)
                    )
                    Text(
                        chip,
                        color = Accent,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                trailing?.invoke()
            }
            Text(
                title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                message,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SoftPill(
    text: String,
    active: Boolean,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val background = when {
        !enabled -> Surface2.copy(alpha = 0.52f)
        active -> Accent.copy(alpha = 0.16f)
        else -> Surface2.copy(alpha = 0.92f)
    }
    val textColor = when {
        !enabled -> TextMuted
        active -> TextPrimary
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .clickable(enabled = enabled && onClick != null) { onClick?.invoke() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 1.dp)
        )
    }
}

@Composable
private fun SceneMascot(
    frameRes: List<Int>,
    contentDescription: String,
    bobOffset: Float,
    frameDurationMillis: Long
) {
    val frames = remember(frameRes) { frameRes.ifEmpty { listOf(R.drawable.suri_pose_01) } }
    var frameIndex by remember(frames, frameDurationMillis) { mutableIntStateOf(0) }

    LaunchedEffect(frames, frameDurationMillis) {
        frameIndex = 0
        if (frames.size > 1) {
            while (true) {
                delay(frameDurationMillis)
                frameIndex = (frameIndex + 1) % frames.size
            }
        }
    }

    val currentFrame = frames[frameIndex]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xFF3A281F).copy(alpha = 0.18f),
                            Color(0xFF2A1C16).copy(alpha = 0.28f)
                        )
                    )
                )
        )
        Crossfade(
            targetState = currentFrame,
            label = "suri-scene-step"
        ) { res ->
            Image(
                painter = painterResource(res),
                contentDescription = contentDescription,
                modifier = Modifier
                    .height(188.dp)
                    .offset(y = bobOffset.dp)
                    .alpha(0.98f),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun speechSpriteFrames(segment: SuriSpeechSegment, isPlaying: Boolean): List<Int> {
    val idleFrames = when (segment.state) {
        SuriSpeechSceneState.GREETING -> listOf(R.drawable.suri_pose_02, R.drawable.suri_expression_02)
        SuriSpeechSceneState.EXPLAIN -> listOf(R.drawable.suri_pose_03, R.drawable.suri_expression_01)
        SuriSpeechSceneState.FOCUS -> listOf(R.drawable.suri_pose_05, R.drawable.suri_expression_03)
        SuriSpeechSceneState.HIGHLIGHT -> listOf(R.drawable.suri_pose_04, R.drawable.suri_pose_07)
        SuriSpeechSceneState.CAUTION -> listOf(R.drawable.suri_expression_04, R.drawable.suri_expression_08)
        SuriSpeechSceneState.COMFORT -> listOf(R.drawable.suri_expression_06, R.drawable.suri_expression_02)
        SuriSpeechSceneState.CLOSING -> listOf(R.drawable.suri_pose_08, R.drawable.suri_expression_02)
    }

    if (!isPlaying) return idleFrames

    return when (segment.state) {
        SuriSpeechSceneState.GREETING -> listOf(R.drawable.suri_pose_02, R.drawable.suri_expression_02, R.drawable.suri_pose_03)
        SuriSpeechSceneState.EXPLAIN -> listOf(R.drawable.suri_pose_03, R.drawable.suri_expression_02, R.drawable.suri_pose_03)
        SuriSpeechSceneState.FOCUS -> listOf(R.drawable.suri_pose_05, R.drawable.suri_expression_03, R.drawable.suri_pose_05)
        SuriSpeechSceneState.HIGHLIGHT -> listOf(R.drawable.suri_pose_04, R.drawable.suri_pose_07, R.drawable.suri_expression_02)
        SuriSpeechSceneState.CAUTION -> listOf(R.drawable.suri_expression_04, R.drawable.suri_expression_05, R.drawable.suri_expression_08)
        SuriSpeechSceneState.COMFORT -> listOf(R.drawable.suri_expression_06, R.drawable.suri_expression_02, R.drawable.suri_pose_08)
        SuriSpeechSceneState.CLOSING -> listOf(R.drawable.suri_pose_08, R.drawable.suri_expression_02, R.drawable.suri_pose_02)
    }
}

private fun sceneSteps(mode: PremiumMode, topic: PremiumTopic): List<SuriSceneStep> {
    val topicLine = when (topic) {
        PremiumTopic.ROMANCE -> "연애 흐름을 중심으로"
        PremiumTopic.CAREER -> "일과 진로 흐름을 중심으로"
        PremiumTopic.MONEY -> "돈의 흐름을 중심으로"
        PremiumTopic.SELF_ESTEEM -> "마음의 중심과 자신감을 중심으로"
        PremiumTopic.RELATIONSHIP -> "인간관계의 결을 중심으로"
    }

    return if (mode == PremiumMode.COMPATIBILITY) {
        listOf(
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_02, R.drawable.suri_expression_02),
                title = "두 사람의 흐름을 읽어볼게요",
                message = "남자와 여자 각자의 기본 기운을 먼저 보고, 그다음 둘 사이의 궁합수를 차분하게 읽어드릴게요.",
                chip = "궁합 인사"
            ),
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_03, R.drawable.suri_expression_01),
                title = "각자의 기운이 먼저입니다",
                message = "서로가 다르다고 보기보다 어떤 기세가 관계 안에서 먼저 움직이는지 차근차근 짚어보는 방식이에요.",
                chip = "기본 기운"
            ),
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_05, R.drawable.suri_expression_03),
                title = "둘 사이의 결을 살펴봅니다",
                message = "궁합수는 누가 맞고 틀리다가 아니라, 둘 사이에 어떤 공기가 형성되는지를 보여주는 단서예요.",
                chip = "궁합수"
            ),
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_expression_04, R.drawable.suri_expression_08),
                title = "주의할 결도 함께 봅니다",
                message = "잘 맞는 점만이 아니라 말의 온도, 기세, 생활 리듬이 어디에서 흔들리는지도 함께 정리해드릴게요.",
                chip = "주의 흐름"
            ),
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_08, R.drawable.suri_expression_02),
                title = "오래 가는 방향으로 정리합니다",
                message = "두 사람이 더 편안해지려면 어떤 리듬이 필요한지, 무겁지 않게 현실적인 조언으로 마무리해드릴게요.",
                chip = "마무리"
            )
        )
    } else {
        listOf(
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_02, R.drawable.suri_expression_02),
                title = "어서 오세요",
                message = "지금 마음에 걸리는 고민을 편하게 적어주세요. 짧은 문장이어도 충분히 읽어낼 수 있어요.",
                chip = "인사"
            ),
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_03, R.drawable.suri_expression_01),
                title = "숫자의 흐름을 먼저 읽습니다",
                message = "$topicLine 지금 어떤 리듬이 강한지부터 차분히 짚어볼게요.",
                chip = "흐름 읽기"
            ),
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_05, R.drawable.suri_expression_03),
                title = "보이지 않던 결을 짚어드릴게요",
                message = "겉으로 드러난 고민뿐 아니라 안에서 반복되는 마음의 패턴도 함께 읽어보는 편이 좋습니다.",
                chip = "핵심 포인트"
            ),
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_04, R.drawable.suri_pose_07),
                title = "정리된 결과로 보여드릴게요",
                message = "흐름이 길어도 읽기 편하게 카드와 책 형식으로 나누어 다시 꺼내보기 좋게 정리해드릴게요.",
                chip = "결과 정리"
            ),
            SuriSceneStep(
                frameRes = listOf(R.drawable.suri_pose_08, R.drawable.suri_expression_06),
                title = "마무리는 가볍게 남길게요",
                message = "겁을 주기보다 지금의 방향을 편안하게 붙잡을 수 있도록 현실적인 조언으로 정리해드릴게요.",
                chip = "부드러운 조언"
            )
        )
    }
}
