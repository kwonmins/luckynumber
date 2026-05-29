package com.example.unum.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.PremiumTopic
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.theme.Accent
import com.example.unum.ui.theme.BookLine
import com.example.unum.ui.theme.BookPaper
import com.example.unum.ui.theme.BookPaperEdge
import com.example.unum.ui.theme.Border
import com.example.unum.ui.theme.Gold
import com.example.unum.ui.theme.Rose
import com.example.unum.ui.theme.Surface
import com.example.unum.ui.theme.Surface2
import com.example.unum.ui.theme.TextMuted
import com.example.unum.ui.theme.TextPrimary
import com.example.unum.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PremiumScreen(
    viewModel: AppViewModel,
    onRequestPersonalConsultation: () -> Unit,
    onRequestCompatibilityConsultation: () -> Unit,
    onOpenBook: (FortuneBook) -> Unit,
    onOpenLibrary: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val latestBook = uiState.savedBooks.firstOrNull { it.bookType == FortuneBookType.PERSONAL }
    val bookSteps = remember { setOf(PremiumFlowStep.COVER, PremiumFlowStep.TOC, PremiumFlowStep.DETAIL) }
    val flipRotation = remember { Animatable(0f) }
    var previousBookStep by remember { mutableStateOf(uiState.premiumFlowStep) }

    LaunchedEffect(uiState.premiumFlowStep) {
        val nextStep = uiState.premiumFlowStep
        if (nextStep in bookSteps && previousBookStep in bookSteps && nextStep != previousBookStep) {
            val direction = if (nextStep.ordinal > previousBookStep.ordinal) 1f else -1f
            flipRotation.snapTo(86f * direction)
            flipRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween<Float>(durationMillis = 620, easing = FastOutSlowInEasing)
            )
        } else if (nextStep in bookSteps && previousBookStep !in bookSteps) {
            flipRotation.snapTo(52f)
            flipRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween<Float>(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        previousBookStep = nextStep
    }

    val flipModifier = Modifier.graphicsLayer {
        cameraDistance = 24f * density
        rotationY = flipRotation.value
        transformOrigin = TransformOrigin(
            pivotFractionX = if (flipRotation.value >= 0f) 0f else 1f,
            pivotFractionY = 0.5f
        )
        scaleX = 1f - kotlin.math.abs(flipRotation.value) / 2800f
        scaleY = 1f - kotlin.math.abs(flipRotation.value) / 3600f
    }

    AnimatedContent(
        targetState = uiState.premiumFlowStep,
        transitionSpec = {
            (fadeIn(tween(220)) + scaleIn(initialScale = 0.98f)) togetherWith
                (fadeOut(tween(180)) + scaleOut(targetScale = 0.98f))
        },
        label = "premiumBookFlow"
    ) { step ->
        when (step) {
            PremiumFlowStep.FORM -> MysticBackground(Modifier.fillMaxSize()) {
                PremiumFormScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onStart = {
                        viewModel.setPremiumFlowStep(PremiumFlowStep.LOADING)
                        onRequestPersonalConsultation()
                    }
                )
            }
            PremiumFlowStep.LOADING -> PremiumLoadingScreen(
                isLoading = uiState.isPremiumLoading,
                hasBook = latestBook != null,
                onDone = { viewModel.setPremiumFlowStep(PremiumFlowStep.VOICE_CHOICE) }
            )
            PremiumFlowStep.VOICE_CHOICE -> VoiceChoiceScreen(
                onRead = { viewModel.setPremiumFlowStep(PremiumFlowStep.HANOK_READING) },
                onSkip = { viewModel.setPremiumFlowStep(PremiumFlowStep.COVER) }
            )
            PremiumFlowStep.HANOK_READING -> HanokReadingScreen(
                advice = viewModel.buildCurrentPremiumSpeechScript()?.segments?.joinToString(" ") { it.body }
                    ?: latestBook?.summary
                    ?: "지금의 고민은 조급하게 결론내리기보다, 마음의 온도를 먼저 확인할 때 더 선명하게 풀립니다.",
                onSkip = { viewModel.setPremiumFlowStep(PremiumFlowStep.COVER) },
                onOpenBook = { viewModel.setPremiumFlowStep(PremiumFlowStep.COVER) }
            )
            PremiumFlowStep.COVER -> BookCoverScreen(
                book = latestBook,
                onReset = viewModel::resetPremiumFlow,
                onOpen = { viewModel.setPremiumFlowStep(PremiumFlowStep.TOC) },
                flipModifier = flipModifier
            )
            PremiumFlowStep.TOC -> BookTocScreen(
                book = latestBook,
                onBack = { viewModel.setPremiumFlowStep(PremiumFlowStep.COVER) },
                onRead = { viewModel.setPremiumFlowStep(PremiumFlowStep.DETAIL) },
                flipModifier = flipModifier
            )
            PremiumFlowStep.DETAIL -> BookDetailScreen(
                book = latestBook,
                concern = uiState.premiumConcern,
                onBack = { viewModel.setPremiumFlowStep(PremiumFlowStep.TOC) },
                onArchive = onOpenLibrary,
                flipModifier = flipModifier
            )
        }
    }
}

@Composable
private fun PremiumFormScreen(
    uiState: AppUiState,
    viewModel: AppViewModel,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1:1 고민 상담 책자 신청", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Icon(Icons.Rounded.AutoStories, contentDescription = null, tint = TextMuted)
            }
            SuriGreetingCard()
            Text("고민 분야 선택", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            TopicGrid(selected = uiState.premiumTopic, onSelected = viewModel::selectPremiumTopic)
            Text("나의 상세한 고민 내용", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            PremiumConcernField(
                value = uiState.premiumConcern,
                onValueChange = viewModel::updatePremiumConcern
            )
            if (uiState.latestBundle == null) {
                Text(
                    "책자는 생년월일 입력값과 지금 적은 고민을 함께 읽어 만들어요. 먼저 입력 화면에서 생년월일을 저장해주세요.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            uiState.inputError?.let { Text(it, color = Rose, style = MaterialTheme.typography.bodySmall) }
        }
        GradientButton(
            text = "프리미엄 맞춤 비책 제책하기",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.latestBundle != null
        )
    }
}

@Composable
private fun SuriGreetingCard() {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text("수", color = Accent, style = MaterialTheme.typography.titleMedium)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text("안녕하세요. 고민을 말씀해주세요.", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                Text("답답하거나 갈피를 잡기 힘든 중요한 고민을 책자 형태로 정리해드릴게요.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TopicGrid(selected: PremiumTopic, onSelected: (PremiumTopic) -> Unit) {
    val topics = listOf(PremiumTopic.ROMANCE, PremiumTopic.CAREER, PremiumTopic.MONEY, PremiumTopic.RELATIONSHIP)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        topics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { topic ->
                    val isSelected = selected == topic
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Accent.copy(alpha = 0.08f) else Surface)
                            .border(1.dp, if (isSelected) Accent else Border, RoundedCornerShape(8.dp))
                            .clickable { onSelected(topic) }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(topic.bookLabel(), color = if (isSelected) Accent else TextSecondary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumConcernField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
        placeholder = {
            Text("해답을 찾고 싶은 상세한 사연이나 사건을 자유롭게 적어주세요.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Accent,
            unfocusedBorderColor = Border,
            cursorColor = Accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
private fun PremiumLoadingScreen(isLoading: Boolean, hasBook: Boolean, onDone: () -> Unit) {
    LaunchedEffect(isLoading, hasBook) {
        if (!isLoading && hasBook) {
            delay(2_500)
            onDone()
        }
    }
    MysticBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                PaperStackAnimation()
                Text(
                    "수리가 당신의 숫자 흐름과\n적어주신 사연을 조용히 풀고 있어요",
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("고민 분야에 맞는 깊이 있는 비책을 제책 중입니다.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = Accent,
                trackColor = Surface2
            )
        }
    }
}

@Composable
private fun PaperStackAnimation() {
    val transition = rememberInfiniteTransition(label = "paperStack")
    val offset by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "offset"
    )
    Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        PaperSheet(rotation = -10f, y = offset - 2f)
        PaperSheet(rotation = 7f, y = -offset)
        PaperSheet(rotation = 0f, y = offset / 2f, active = true)
    }
}

@Composable
private fun PaperSheet(rotation: Float, y: Float, active: Boolean = false) {
    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 78.dp)
            .graphicsLayer(rotationZ = rotation, translationY = y)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) BookPaper else Surface)
            .border(1.dp, if (active) BookPaperEdge else Border, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (active) Text("수", color = Gold, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun VoiceChoiceScreen(onRead: () -> Unit, onSkip: () -> Unit) {
    MysticBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Rounded.Headphones, contentDescription = null, tint = Accent, modifier = Modifier.size(48.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("수리가 직접 결과를 읽어줍니다", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(
                    "건너뛰기를 누르시면 바로 책자를 통해 결과를 보실 수 있어요.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton("수리가 읽어주세요", onRead, Modifier.fillMaxWidth())
                SecondaryButton("건너뛰기", onSkip, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun HanokReadingScreen(advice: String, onSkip: () -> Unit, onOpenBook: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF16120E))
    ) {
        HanokFallbackScene(Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.68f), Color.Transparent, Color.Black.copy(alpha = 0.82f))))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SURI'S SANCTUARY", color = Color(0xCCFDE68A), style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("건너뛰기", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            VoiceWave()
            SurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                tonalColor = Color.Black.copy(alpha = 0.56f),
                borderColor = Color(0x33F59E0B),
                contentPadding = 16
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0x33F59E0B))
                            .border(1.dp, Color(0x44FDE68A), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text("SURI'S WHISPER", color = Color(0xFFFDE68A), style = MaterialTheme.typography.labelMedium)
                    }
                    Text(
                        advice,
                        modifier = Modifier
                            .heightIn(max = 148.dp)
                            .verticalScroll(rememberScrollState()),
                        color = Color(0xFFF4EAE1),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("은은히 일렁이는 불빛 아래, 수리의 목소리에 조용히 귀 기울여 보세요.", color = Color(0xFFB6A99D), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(10.dp))
            GradientButton("책자로 결과 마저 확인하기", onOpenBook, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun HanokFallbackScene(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "lantern")
    val glow by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
        label = "glow"
    )
    Canvas(modifier = modifier) {
        drawRect(Color(0xFF0D0907))
        val floorTop = size.height * 0.58f
        val floor = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width * 0.30f, floorTop)
            lineTo(size.width * 0.70f, floorTop)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(floor, Color(0xFF1B110A))
        repeat(4) { index ->
            val x = size.width * (0.15f + index * 0.23f)
            drawLine(Color(0xFF120A05), Offset(x, size.height), Offset(size.width * 0.5f, floorTop), strokeWidth = 2f)
        }
        drawRect(Color(0xFFF5EDE0), topLeft = Offset(size.width * 0.08f, size.height * 0.16f), size = androidx.compose.ui.geometry.Size(size.width * 0.34f, size.height * 0.42f))
        drawRect(Color(0xFFF5EDE0), topLeft = Offset(size.width * 0.58f, size.height * 0.16f), size = androidx.compose.ui.geometry.Size(size.width * 0.34f, size.height * 0.42f))
        repeat(4) { row ->
            val y = size.height * (0.22f + row * 0.08f)
            drawLine(Color(0xFF8C6448), Offset(size.width * 0.08f, y), Offset(size.width * 0.42f, y), strokeWidth = 1f)
            drawLine(Color(0xFF8C6448), Offset(size.width * 0.58f, y), Offset(size.width * 0.92f, y), strokeWidth = 1f)
        }
        drawCircle(Color(0xFFF59E0B).copy(alpha = glow * 0.34f), radius = size.width * 0.22f, center = Offset(size.width * 0.72f, size.height * 0.63f))
        drawRect(Color(0xFFFDE68A).copy(alpha = glow), topLeft = Offset(size.width * 0.70f, size.height * 0.36f), size = androidx.compose.ui.geometry.Size(size.width * 0.07f, size.height * 0.08f))
        drawRoundRect(Color(0xFF3A2312), topLeft = Offset(size.width * 0.34f, size.height * 0.80f), size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.05f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        listOf(
            Offset(size.width * 0.20f, size.height * 0.42f),
            Offset(size.width * 0.82f, size.height * 0.50f),
            Offset(size.width * 0.56f, size.height * 0.38f)
        ).forEachIndexed { index, dot ->
            drawCircle(Color(0xFFFDE68A).copy(alpha = 0.35f + index * 0.12f), radius = 3f + index, center = dot)
        }
    }
}

@Composable
private fun VoiceWave() {
    val transition = rememberInfiniteTransition(label = "voiceWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "phase"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(18) { index ->
            val h = (8 + ((index % 5) * 6) + phase * 12).dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.dp)
                    .height(h)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFDE68A).copy(alpha = 0.42f))
            )
        }
    }
}

@Composable
private fun BookCoverScreen(
    book: FortuneBook?,
    onReset: () -> Unit,
    onOpen: () -> Unit,
    flipModifier: Modifier
) {
    BookStageScaffold {
        PremiumBookCover(
            book = book,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .bookTurnGestures(onPrevious = null, onNext = onOpen)
                .then(flipModifier)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("처음으로", onReset, Modifier.weight(1f))
            GradientButton("노트 펼치기", onOpen, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BookTocScreen(
    book: FortuneBook?,
    onBack: () -> Unit,
    onRead: () -> Unit,
    flipModifier: Modifier
) {
    val identity = bookIdentityFor(book)
    BookStageScaffold {
        PaperPage(
            identity = identity,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .bookTurnGestures(onPrevious = onBack, onNext = onRead)
                .then(flipModifier)
        ) {
            Text("목차", color = TextPrimary, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(identity.caption, color = identity.accent, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(2.dp))
            listOf("지혜의 본질", "상황별 해석", "주의할 장면", "이번 달 행동 지침").forEachIndexed { index, title ->
                TocLine(index + 1, title, "p.${4 + index * 10}", identity)
            }
            Spacer(Modifier.weight(1f))
            Text("03", color = identity.accent.copy(alpha = 0.48f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("돌아가기", onBack, Modifier.weight(1f))
            GradientButton("해석서 읽기", onRead, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BookDetailScreen(
    book: FortuneBook?,
    concern: String,
    onBack: () -> Unit,
    onArchive: () -> Unit,
    flipModifier: Modifier
) {
    val chapter = book?.chapters?.firstOrNull()
    val identity = bookIdentityFor(book)
    BookStageScaffold {
        PaperPage(
            identity = identity,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .bookTurnGestures(onPrevious = onBack, onNext = null)
                .then(flipModifier)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).background(identity.accent, CircleShape), contentAlignment = Alignment.Center) {
                    Text("2", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
                Column {
                    Text("상황별 고민 해결 비책", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(book?.concernTopic ?: "연애 특화 카운슬링", color = identity.accent, style = MaterialTheme.typography.bodySmall)
                }
            }
            DetailBox("던지신 질문", concern.ifBlank { book?.concernText.orEmpty().ifBlank { "지금 마음속에서 가장 자주 떠오르는 고민" } }, identity.accentDeep)
            DetailBox("주의할 장면", chapter?.highlightQuote ?: book?.summary.orEmpty(), Rose)
            DetailBox(
                "이번 주 실천 지침",
                chapter?.actionTip?.joinToString("\n") { "• $it" } ?: "• 하루 10분 마음 상태를 확인하기\n• 중요한 선택은 하루 뒤 다시 보기\n• 반복되는 패턴을 짧게 기록하기",
                identity.accent
            )
            Spacer(Modifier.weight(1f))
            Text("15", color = identity.accent.copy(alpha = 0.48f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("목차보기", onBack, Modifier.weight(1f))
            GradientButton("보관함에 보관", onArchive, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BookStageScaffold(content: @Composable ColumnScope.() -> Unit) {
    MysticBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

private fun Modifier.bookTurnGestures(
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?
): Modifier = this
    .pointerInput(onPrevious, onNext) {
        detectTapGestures { offset ->
            when {
                offset.x > size.width * 0.62f -> onNext?.invoke()
                offset.x < size.width * 0.38f -> onPrevious?.invoke()
            }
        }
    }
    .pointerInput(onPrevious, onNext) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragStart = { totalDrag = 0f },
            onHorizontalDrag = { change, dragAmount ->
                totalDrag += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    totalDrag < -72f -> onNext?.invoke()
                    totalDrag > 72f -> onPrevious?.invoke()
                }
            }
        )
    }

@Composable
private fun PremiumBookCover(book: FortuneBook?, modifier: Modifier = Modifier) {
    val identity = bookIdentityFor(book)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.985f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(identity.coverTop, identity.coverMid, identity.coverBottom)
                    )
                )
                .border(1.dp, Color.Black.copy(alpha = 0.32f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                repeat(22) { index ->
                    val y = size.height * (index + 1) / 23f
                    val wave = if (index % 2 == 0) 18f else -10f
                    drawLine(
                        Color.White.copy(alpha = 0.025f),
                        Offset(18f, y),
                        Offset(size.width - 18f, y + wave),
                        strokeWidth = 1f
                    )
                }
                repeat(18) { index ->
                    val y = size.height * (index + 1) / 19f
                    drawLine(
                        Color.Black.copy(alpha = 0.18f),
                        Offset(22f, y + 6f),
                        Offset(size.width - 20f, y - 5f),
                        strokeWidth = 1f
                    )
                }
                drawCircle(Color.White.copy(alpha = 0.035f), radius = size.minDimension * 0.22f, center = Offset(size.width * 0.18f, size.height * 0.12f))
            }
            Box(Modifier.fillMaxSize().border(1.dp, identity.foil.copy(alpha = 0.72f), RoundedCornerShape(8.dp)))
            Box(Modifier.fillMaxSize().padding(7.dp).border(1.dp, identity.foil.copy(alpha = 0.30f), RoundedCornerShape(6.dp)))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
                    .size(width = 1.dp, height = 420.dp)
                    .background(identity.foil.copy(alpha = 0.28f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                repeat(34) {
                    Box(
                        modifier = Modifier
                            .size(width = 2.dp, height = 2.dp)
                            .clip(CircleShape)
                            .background(identity.stitch.copy(alpha = 0.72f))
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(width = 18.dp, height = 380.dp)
                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 28.dp)
                    .size(width = 16.dp, height = 96.dp)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(Brush.verticalGradient(listOf(identity.ribbon, identity.accentDeep)))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 54.dp, end = 28.dp, top = 34.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(identity.caption, color = identity.foil, style = MaterialTheme.typography.labelMedium)
                    Text(
                        if (book?.bookType == FortuneBookType.COMPATIBILITY) "수리의\n궁합노트" else "수리의\n운세노트",
                        color = identity.foil,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Box(
                        modifier = Modifier
                            .width(132.dp)
                            .height(2.dp)
                            .background(identity.foil.copy(alpha = 0.86f))
                    )
                }
                Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(13.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(book?.coverTitle ?: identity.shortName, color = Color(0xFFF8FAFC), style = MaterialTheme.typography.labelLarge)
                    Text(book?.coverSubtitle ?: "나만의 맞춤 비책", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                    Text("운명수 ${book?.destiny ?: 7} · ${identity.shortName}", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.14f))
                            .border(2.dp, identity.foil.copy(alpha = 0.90f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((book?.destiny ?: 7).toString(), color = identity.foil, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 22.dp, bottom = 26.dp)
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFB91C1C).copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                Text("수리", color = Color.White, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PaperPage(
    identity: BookIdentityTheme,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.985f)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(identity.coverTop, identity.coverMid, identity.coverBottom)))
            .border(1.dp, Color.Black.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(18) { index ->
                val y = size.height * (index + 1) / 19f
                drawLine(Color.White.copy(alpha = 0.022f), Offset(16f, y), Offset(size.width - 16f, y - 4f), strokeWidth = 1f)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(identity.pageTop, identity.page, Color(0xFFF4EBD9))))
                .border(1.dp, identity.edge, RoundedCornerShape(8.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun TocLine(index: Int, title: String, page: String, identity: BookIdentityTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (index == 2) identity.tint.copy(alpha = 0.34f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(25.dp).background(identity.accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Text(index.toString(), color = identity.accent, style = MaterialTheme.typography.bodySmall)
            }
            Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        }
        Text(page, color = identity.accent.copy(alpha = 0.52f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DetailBox(title: String, body: String, color: Color) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Color.White.copy(alpha = 0.56f),
        borderColor = color.copy(alpha = 0.18f),
        contentPadding = 0
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(title, color = color, style = MaterialTheme.typography.labelLarge)
                Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private data class BookIdentityTheme(
    val shortName: String,
    val caption: String,
    val accent: Color,
    val accentDeep: Color,
    val ribbon: Color,
    val foil: Color,
    val stitch: Color,
    val coverTop: Color,
    val coverMid: Color,
    val coverBottom: Color,
    val tint: Color,
    val page: Color,
    val pageTop: Color,
    val edge: Color
)

private fun bookIdentityFor(book: FortuneBook?): BookIdentityTheme {
    val key = listOfNotNull(book?.coverTheme, book?.concernTopic, book?.coverTitle).joinToString(" ").lowercase()
    return when {
        "career" in key || "진로" in key || "일" in key -> BookIdentityTheme(
            shortName = "일과 방향",
            caption = "PREMIUM CAREER NOTE",
            accent = Color(0xFF2563EB),
            accentDeep = Color(0xFF1E3A8A),
            ribbon = Color(0xFF2563EB),
            foil = Color(0xFFF7D56A),
            stitch = Color(0xFFFDE68A),
            coverTop = Color(0xFF222633),
            coverMid = Color(0xFF10131B),
            coverBottom = Color(0xFF05070C),
            tint = Color(0xFFEFF6FF),
            page = Color(0xFFFFFDF8),
            pageTop = Color(0xFFFFFAF1),
            edge = Color(0xFFE6DAC9)
        )
        "money" in key || "돈" in key -> BookIdentityTheme(
            shortName = "돈의 흐름",
            caption = "PREMIUM MONEY NOTE",
            accent = Color(0xFF059669),
            accentDeep = Color(0xFF065F46),
            ribbon = Color(0xFF10B981),
            foil = Color(0xFFF7D56A),
            stitch = Color(0xFFFDE68A),
            coverTop = Color(0xFF1F5B4C),
            coverMid = Color(0xFF0E332C),
            coverBottom = Color(0xFF061C18),
            tint = Color(0xFFECFDF5),
            page = Color(0xFFFFFDF8),
            pageTop = Color(0xFFFFFAF1),
            edge = Color(0xFFE6DAC9)
        )
        "relationship" in key || "인간관계" in key || "관계" in key -> BookIdentityTheme(
            shortName = "관계 패턴",
            caption = "PREMIUM RELATION NOTE",
            accent = Color(0xFFA16207),
            accentDeep = Color(0xFF713F12),
            ribbon = Color(0xFFF59E0B),
            foil = Color(0xFFF8E3A3),
            stitch = Color(0xFFF8E3B0),
            coverTop = Color(0xFF9C5D32),
            coverMid = Color(0xFF673719),
            coverBottom = Color(0xFF30170C),
            tint = Color(0xFFFFF7ED),
            page = Color(0xFFFFFDF8),
            pageTop = Color(0xFFFFFAF1),
            edge = Color(0xFFE6DAC9)
        )
        "self_esteem" in key || "나 자신" in key || "자기" in key -> BookIdentityTheme(
            shortName = "자기 기준",
            caption = "PREMIUM SELF NOTE",
            accent = Color(0xFF7C3AED),
            accentDeep = Color(0xFF4C1D95),
            ribbon = Color(0xFF7C3AED),
            foil = Color(0xFFF7D56A),
            stitch = Color(0xFFFDE68A),
            coverTop = Color(0xFF171A2A),
            coverMid = Color(0xFF101225),
            coverBottom = Color(0xFF070813),
            tint = Color(0xFFF5F3FF),
            page = Color(0xFFFFFDF8),
            pageTop = Color(0xFFFFFAF1),
            edge = Color(0xFFE6DAC9)
        )
        "compatibility" in key || "궁합" in key -> BookIdentityTheme(
            shortName = "궁합노트",
            caption = "PREMIUM MATCH NOTE",
            accent = Color(0xFFDC2626),
            accentDeep = Color(0xFF991B1B),
            ribbon = Color(0xFFB91C1C),
            foil = Color(0xFFF7D56A),
            stitch = Color(0xFFFDE68A),
            coverTop = Color(0xFF222633),
            coverMid = Color(0xFF10131B),
            coverBottom = Color(0xFF05070C),
            tint = Color(0xFFFDF2F8),
            page = Color(0xFFFFFDF8),
            pageTop = Color(0xFFFFFAF1),
            edge = Color(0xFFE6DAC9)
        )
        else -> BookIdentityTheme(
            shortName = "연애 운세",
            caption = "PREMIUM ROMANCE NOTE",
            accent = Color(0xFFDC2626),
            accentDeep = Color(0xFF991B1B),
            ribbon = Color(0xFFB91C1C),
            foil = Color(0xFFF7D56A),
            stitch = Color(0xFFFDE68A),
            coverTop = Color(0xFF222633),
            coverMid = Color(0xFF10131B),
            coverBottom = Color(0xFF05070C),
            tint = Color(0xFFFFF1F2),
            page = Color(0xFFFFFDF8),
            pageTop = Color(0xFFFFFAF1),
            edge = Color(0xFFE6DAC9)
        )
    }
}

private fun PremiumTopic.bookLabel(): String = when (this) {
    PremiumTopic.ROMANCE -> "연애"
    PremiumTopic.CAREER -> "일 & 진로"
    PremiumTopic.MONEY -> "돈 & 경제"
    PremiumTopic.SELF_ESTEEM -> "나 자신"
    PremiumTopic.RELATIONSHIP -> "인간관계"
}
