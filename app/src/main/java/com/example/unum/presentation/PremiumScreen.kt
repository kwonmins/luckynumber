package com.example.unum.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.R
import com.example.unum.data.model.BookSpecs
import com.example.unum.data.model.BookThemeId
import com.example.unum.data.model.BookThemeSpecs
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumMode
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.resolvedThemeId
import com.example.unum.presentation.spec.FeatureSpecs
import com.example.unum.ui.components.DateInputRow
import com.example.unum.ui.components.GradientButton
import com.example.unum.ui.components.MysticBackground
import com.example.unum.ui.components.SecondaryButton
import com.example.unum.ui.components.SurfaceCard
import com.example.unum.ui.components.ToggleSegment
import com.example.unum.ui.components.premiumTopicMascot
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
    onOpenBook: (FortuneBook) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenPayment: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val shareBook = rememberFortuneBookShareHandler()
    val activeBookType = if (uiState.premiumMode == PremiumMode.COMPATIBILITY) {
        FortuneBookType.COMPATIBILITY
    } else {
        FortuneBookType.PERSONAL
    }
    val currentBook = uiState.savedBooks.firstOrNull {
        it.bookId == uiState.selectedBookId && it.bookType == activeBookType
    } ?: uiState.savedBooks.firstOrNull { it.bookType == activeBookType }
    val hasGeneratedBook = when (uiState.premiumMode) {
        PremiumMode.PERSONAL -> uiState.premiumResult != null && currentBook != null
        PremiumMode.COMPATIBILITY -> uiState.compatibilityResult != null && currentBook != null
    }
    val bookSteps = remember { setOf(PremiumFlowStep.COVER, PremiumFlowStep.TOC, PremiumFlowStep.DETAIL) }
    val flipRotation = remember { Animatable(0f) }
    var previousBookStep by remember { mutableStateOf(uiState.premiumFlowStep) }
    val openCurrentBook: () -> Unit = {
        currentBook?.let { book ->
            viewModel.setPremiumFlowStep(PremiumFlowStep.COVER)
            onOpenBook(book)
        } ?: viewModel.setPremiumFlowStep(PremiumFlowStep.COVER)
    }

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
            PremiumFlowStep.FORM -> PremiumEntryBackground {
                PremiumFormScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onStart = { viewModel.preparePremiumQuestionConfirmation() }
                )
            }
            PremiumFlowStep.CONFIRM_QUESTION -> QuestionConfirmScreen(
                topicLabel = if (uiState.premiumMode == PremiumMode.COMPATIBILITY) {
                    uiState.compatibilityForm.relationshipStatus.label
                } else {
                    uiState.premiumTopic.label
                },
                question = uiState.premiumEssentialQuestion,
                originalConcern = if (uiState.premiumMode == PremiumMode.COMPATIBILITY) {
                    uiState.compatibilityConcern
                } else {
                    uiState.premiumConcern
                },
                onEdit = { viewModel.setPremiumFlowStep(PremiumFlowStep.FORM) },
                onConfirm = onOpenPayment
            )
            PremiumFlowStep.LOADING -> PremiumLoadingScreen(
                isLoading = uiState.isPremiumLoading,
                hasBook = hasGeneratedBook,
                mode = uiState.premiumMode,
                onDone = openCurrentBook
            )
            PremiumFlowStep.VOICE_CHOICE -> VoiceChoiceScreen(
                onRead = { viewModel.setPremiumFlowStep(PremiumFlowStep.HANOK_READING) },
                onSkip = openCurrentBook
            )
            PremiumFlowStep.HANOK_READING -> HanokReadingScreen(
                advice = viewModel.buildCurrentPremiumSpeechScript()?.segments?.joinToString(" ") { it.body }
                    ?: currentBook?.summary
                    ?: "지금의 고민은 조급하게 결론내리기보다, 마음의 온도를 먼저 확인할 때 더 선명하게 풀립니다.",
                onSkip = openCurrentBook,
                onOpenBook = openCurrentBook
            )
            PremiumFlowStep.COVER -> BookCoverScreen(
                book = currentBook,
                onReset = viewModel::resetPremiumFlow,
                onOpen = openCurrentBook,
                flipModifier = flipModifier
            )
            PremiumFlowStep.TOC -> BookTocScreen(
                book = currentBook,
                onBack = { viewModel.setPremiumFlowStep(PremiumFlowStep.COVER) },
                onRead = { viewModel.setPremiumFlowStep(PremiumFlowStep.DETAIL) },
                flipModifier = flipModifier
            )
            PremiumFlowStep.DETAIL -> BookDetailScreen(
                book = currentBook,
                concern = if (uiState.premiumMode == PremiumMode.COMPATIBILITY) {
                    uiState.compatibilityConcern
                } else {
                    uiState.premiumEssentialQuestion.ifBlank { uiState.premiumConcern }
                },
                onBack = { viewModel.setPremiumFlowStep(PremiumFlowStep.TOC) },
                onShare = { book -> shareBook(book) },
                onArchive = { currentBook?.let(onOpenBook) ?: onOpenLibrary() },
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
    val isCompatibility = uiState.premiumMode == PremiumMode.COMPATIBILITY
    val partner = uiState.compatibilityForm.partner
    var personalStep by remember { mutableStateOf(1) }
    val compatibilityReady = uiState.latestBundle != null &&
        partner.year.length == 4 &&
        partner.month.isNotBlank() &&
        partner.day.isNotBlank() &&
        uiState.compatibilityConcern.trim().length >= 6
    val canStart = if (isCompatibility) {
        compatibilityReady
    } else {
        uiState.latestBundle != null && uiState.premiumConcern.trim().length >= 6
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PremiumEntryHeader()
        PremiumEntryModeSelector(
            selected = uiState.premiumMode,
            onSelected = { mode ->
                personalStep = 1
                viewModel.setPremiumMode(mode)
            }
        )

        if (isCompatibility) {
            PremiumCompatibilityEntry(
                uiState = uiState,
                viewModel = viewModel,
                canStart = canStart,
                onStart = onStart
            )
        } else if (personalStep == 1) {
            PremiumPersonalIntro(
                uiState = uiState,
                onTopicSelected = viewModel::selectPremiumTopic,
                onContinue = { personalStep = 2 }
            )
        } else {
            PremiumPersonalDetails(
                uiState = uiState,
                onConcernChange = viewModel::updatePremiumConcern,
                onBack = { personalStep = 1 },
                canStart = canStart,
                onStart = onStart
            )
        }

        uiState.inputError?.let {
            Text(it, color = Color(0xFFFDA4AF), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
    }
}

private val PremiumEntryNavy = Color(0xFF030409)
private val PremiumEntryBlue = Color(0xFF5B8AF5)
private val PremiumEntryIndigo = Color(0xFF7B9AF8)
private val PremiumEntryPink = Color(0xFFFF4D6D)
private val PremiumEntryViolet = Color(0xFFA78BFA)
private val PremiumEntryGold = Color(0xFFF0A84A)

@Composable
private fun PremiumEntryBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PremiumEntryNavy, Color(0xFF0A0B1A), Color(0xFF060710))
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PremiumEntryGold.copy(alpha = 0.19f), Color.Transparent),
                    center = Offset(size.width * 0.04f, size.height * 0.02f),
                    radius = size.width * 0.58f
                ),
                radius = size.width * 0.58f,
                center = Offset(size.width * 0.04f, size.height * 0.02f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PremiumEntryViolet.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.98f, size.height * 0.25f),
                    radius = size.width * 0.46f
                ),
                radius = size.width * 0.46f,
                center = Offset(size.width * 0.98f, size.height * 0.25f)
            )
        }
        content()
    }
}

@Composable
private fun PremiumEntryHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("♛", color = PremiumEntryGold, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text(
                "PREMIUM",
                color = PremiumEntryGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        Text(
            "나만의 노트 제작",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
        )
    }
}

@Composable
private fun PremiumEntryModeSelector(selected: PremiumMode, onSelected: (PremiumMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(PremiumMode.PERSONAL to "🔮 운세노트", PremiumMode.COMPATIBILITY to "💑 궁합노트").forEach { (mode, label) ->
            val isSelected = selected == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.11f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) Color.White.copy(alpha = 0.14f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelected(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.42f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun PremiumPersonalIntro(
    uiState: AppUiState,
    onTopicSelected: (PremiumTopic) -> Unit,
    onContinue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PremiumPersonalPreview(topic = uiState.premiumTopic)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "어떤 고민이 있으신가요?",
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            PremiumTopicGrid(selected = uiState.premiumTopic, onSelected = onTopicSelected)
        }
        PremiumIncludedCard()
        if (uiState.latestBundle == null) {
            PremiumEntryHint("생년월일 분석을 먼저 완료하면 맞춤 운세노트를 제작할 수 있어요.")
        }
        PremiumPrimaryButton(
            text = "✦  맞춤 비책 제작하기",
            onClick = onContinue,
            colors = listOf(PremiumEntryGold, Color(0xFFD4884A)),
            contentColor = PremiumEntryNavy
        )
    }
}

@Composable
private fun PremiumPersonalPreview(topic: PremiumTopic) {
    val topicLabel = topic.bookLabel()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(PremiumEntryBlue.copy(alpha = 0.34f), PremiumEntryViolet.copy(alpha = 0.34f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(18.dp))
    ) {
        Image(
            painter = painterResource(premiumTopicMascot(topic)),
            contentDescription = "운세노트를 쓰는 수리",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(118.dp)
                .fillMaxHeight()
                .padding(top = 8.dp, end = 4.dp),
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.72f)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "미리보기 · $topicLabel 리포트",
                    color = Color(0xFF93C5FD),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "“숫자 속 흐름을 읽어,\n지금 필요한 답을 찾아드려요”",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("에너지 분석", "타이밍", "비책 3가지").forEach { label ->
                    Text(
                        label,
                        color = Color.White.copy(alpha = 0.68f),
                        fontSize = 9.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumTopicGrid(selected: PremiumTopic, onSelected: (PremiumTopic) -> Unit) {
    val topics = FeatureSpecs.premiumTopicPlans.map { it.topic }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        topics.chunked(2).forEach { rowTopics ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                rowTopics.forEach { topic ->
                    val isSelected = selected == topic
                    val accent = topicAccent(topic)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (isSelected) accent.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.055f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) accent.copy(alpha = 0.62f)
                                else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(13.dp)
                            )
                            .clickable { onSelected(topic) }
                            .padding(horizontal = 5.dp, vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(topicEmoji(topic), fontSize = 20.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            topic.bookLabel(),
                            color = if (isSelected) accent else Color.White.copy(alpha = 0.50f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                repeat(2 - rowTopics.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun topicAccent(topic: PremiumTopic): Color = when (topic) {
    PremiumTopic.ROMANCE -> PremiumEntryPink
    PremiumTopic.CAREER -> PremiumEntryBlue
    PremiumTopic.MONEY -> Color(0xFF2DD4BF)
    PremiumTopic.STUDY -> PremiumEntryViolet
    PremiumTopic.HEALTH -> Color(0xFF2DD4BF)
    PremiumTopic.BUSINESS -> PremiumEntryGold
    PremiumTopic.GENERAL -> PremiumEntryViolet
    PremiumTopic.SELF_ESTEEM -> PremiumEntryGold
    PremiumTopic.RELATIONSHIP -> PremiumEntryPink
}

private fun topicEmoji(topic: PremiumTopic): String = when (topic) {
    PremiumTopic.ROMANCE -> "💘"
    PremiumTopic.CAREER -> "💼"
    PremiumTopic.MONEY -> "💰"
    PremiumTopic.STUDY -> "📚"
    PremiumTopic.HEALTH -> "🌿"
    PremiumTopic.BUSINESS -> "📈"
    PremiumTopic.GENERAL -> "🔮"
    PremiumTopic.SELF_ESTEEM -> "✨"
    PremiumTopic.RELATIONSHIP -> "🤝"
}

@Composable
private fun PremiumIncludedCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PremiumEntryGold.copy(alpha = 0.09f))
            .border(1.dp, PremiumEntryGold.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            "이 리포트에 포함돼요",
            color = Color.White.copy(alpha = 0.74f),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        listOf(
            "수비학 기반 에너지 심층 분석 (20p+)",
            "분야별 타이밍 캘린더",
            "지금 당장 쓸 수 있는 비책 3가지",
            "피해야 할 패턴 경고 카드"
        ).forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.Top) {
                Text("✦", color = PremiumEntryGold, fontSize = 11.sp)
                Text(item, color = Color.White.copy(alpha = 0.60f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PremiumPersonalDetails(
    uiState: AppUiState,
    onConcernChange: (String) -> Unit,
    onBack: () -> Unit,
    canStart: Boolean,
    onStart: () -> Unit
) {
    var selectedTone by remember { mutableStateOf("전략적") }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 5.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = Color.White.copy(alpha = 0.55f), fontSize = 24.sp)
            Text(uiState.premiumTopic.bookLabel(), color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(15.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("구체적인 고민을 알려주세요", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            PremiumDarkConcernField(
                value = uiState.premiumConcern,
                onValueChange = onConcernChange,
                placeholder = "예) 오래 만난 연인과 결혼을 고민 중인데, 지금이 좋은 시기인지 알고 싶어요..."
            )
            Text("상세할수록 더 정확한 비책이 나와요 · 6자 이상", color = Color.White.copy(alpha = 0.30f), fontSize = 10.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("리포트 톤 선택", color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("⚡" to "직설적", "🌸" to "부드럽게", "🎯" to "전략적").forEach { (emoji, label) ->
                    val selected = selectedTone == label
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (selected) PremiumEntryBlue.copy(alpha = 0.31f) else Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                if (selected) PremiumEntryIndigo.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.07f),
                                RoundedCornerShape(13.dp)
                            )
                            .clickable { selectedTone = label }
                            .padding(vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 18.sp)
                        Text(
                            label,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.42f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        if (uiState.latestBundle == null) {
            PremiumEntryHint("입력 화면에서 생년월일을 저장한 뒤 제작할 수 있어요.")
        }
        PremiumPrimaryButton(
            text = "♛  프리미엄 비책 생성하기",
            onClick = onStart,
            enabled = canStart,
            colors = listOf(PremiumEntryGold, Color(0xFFD4884A)),
            contentColor = PremiumEntryNavy
        )
        PremiumTrustLine("🔒  안전하게 제작 · 평균 22페이지")
    }
}

@Composable
private fun PremiumCompatibilityEntry(
    uiState: AppUiState,
    viewModel: AppViewModel,
    canStart: Boolean,
    onStart: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PremiumEntryPink.copy(alpha = 0.27f), PremiumEntryViolet.copy(alpha = 0.30f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.11f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.66f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("두 사람의 숫자 흐름", color = Color(0xFFF9A8D4), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Text("마음의 거리와\n관계의 타이밍을 읽어요", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 21.sp))
                Text("궁합 분석 · 관계 비책 · 타이밍", color = Color.White.copy(alpha = 0.52f), fontSize = 10.sp)
            }
            Image(
                painter = painterResource(R.drawable.suri_reader_compatibility),
                contentDescription = "궁합을 읽는 수리",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(126.dp)
                    .fillMaxHeight()
                    .padding(top = 5.dp, end = 5.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("관계 유형", color = Color.White.copy(alpha = 0.80f), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                CompatibilityRelationshipStatus.entries.forEach { status ->
                    val selected = uiState.compatibilityForm.relationshipStatus == status
                    val (emoji, label) = relationshipPresentation(status)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (selected) PremiumEntryPink.copy(alpha = 0.31f) else Color.White.copy(alpha = 0.055f))
                            .border(
                                1.dp,
                                if (selected) PremiumEntryPink.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(13.dp)
                            )
                            .clickable { viewModel.setCompatibilityRelationshipStatus(status) }
                            .padding(vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 20.sp)
                        Text(label, color = if (selected) Color.White else Color.White.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        PremiumPartnerInputCard(uiState = uiState, viewModel = viewModel)
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("관계에서 가장 궁금한 점", color = Color.White.copy(alpha = 0.80f), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            PremiumDarkConcernField(
                value = uiState.compatibilityConcern,
                onValueChange = viewModel::updateCompatibilityConcern,
                placeholder = "예) 서로 마음은 있는데 자꾸 어긋나요. 관계가 좋아질 시기를 알고 싶어요..."
            )
            Text("상황과 궁금한 점을 6자 이상 적어주세요.", color = Color.White.copy(alpha = 0.30f), fontSize = 10.sp)
        }
        if (uiState.latestBundle == null) {
            PremiumEntryHint("먼저 내 생년월일 분석을 완료해 주세요.")
        }
        PremiumPrimaryButton(
            text = "✦  궁합 리포트 제작하기",
            onClick = onStart,
            enabled = canStart,
            colors = listOf(PremiumEntryPink, PremiumEntryViolet)
        )
        PremiumTrustLine("🔒  상대방 정보는 분석에만 안전하게 사용돼요")
    }
}

private fun relationshipPresentation(status: CompatibilityRelationshipStatus): Pair<String, String> = when (status) {
    CompatibilityRelationshipStatus.COUPLE -> "💑" to "연인·배우자"
    CompatibilityRelationshipStatus.CRUSH -> "💌" to "짝사랑"
    CompatibilityRelationshipStatus.REUNION -> "🔁" to "재회"
}

@Composable
private fun PremiumPartnerInputCard(uiState: AppUiState, viewModel: AppViewModel) {
    val partner = uiState.compatibilityForm.partner
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(15.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("상대방 정보", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Text(
                uiState.latestBundle?.displayInput?.let { "내 정보 ${it.year}.${it.month}.${it.day}" } ?: "내 정보 미입력",
                color = Color.White.copy(alpha = 0.34f),
                fontSize = 10.sp
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(GenderOption.MALE to "남성", GenderOption.FEMALE to "여성").forEach { (gender, label) ->
                val selected = uiState.compatibilityForm.partnerGender == gender
                PremiumCompactChoice(
                    text = label,
                    selected = selected,
                    accent = PremiumEntryPink,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setCompatibilityPartnerGender(gender) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(CalendarType.SOLAR to "양력", CalendarType.LUNAR to "음력").forEach { (calendar, label) ->
                PremiumCompactChoice(
                    text = label,
                    selected = partner.calendarType == calendar,
                    accent = PremiumEntryViolet,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setCompatibilityPartnerCalendarType(calendar) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            PremiumDarkDateField(
                value = partner.year,
                onValueChange = viewModel::updateCompatibilityPartnerYear,
                placeholder = "연도",
                maxLength = 4,
                modifier = Modifier.weight(1.5f)
            )
            PremiumDarkDateField(
                value = partner.month,
                onValueChange = viewModel::updateCompatibilityPartnerMonth,
                placeholder = "월",
                maxLength = 2,
                modifier = Modifier.weight(1f)
            )
            PremiumDarkDateField(
                value = partner.day,
                onValueChange = viewModel::updateCompatibilityPartnerDay,
                placeholder = "일",
                maxLength = 2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PremiumCompactChoice(
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
private fun PremiumDarkDateField(
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
private fun PremiumDarkConcernField(
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
private fun PremiumPrimaryButton(
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
private fun PremiumEntryHint(text: String) {
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
private fun PremiumTrustLine(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.30f),
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PremiumModeSelector(selected: PremiumMode, onSelected: (PremiumMode) -> Unit) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface2, borderColor = Border, contentPadding = 4) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PremiumModePill(
                title = "운세노트",
                body = "내 생년월일로 제작",
                selected = selected == PremiumMode.PERSONAL,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(PremiumMode.PERSONAL) }
            )
            PremiumModePill(
                title = "궁합노트",
                body = "두 사람의 숫자 흐름 비교",
                selected = selected == PremiumMode.COMPATIBILITY,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(PremiumMode.COMPATIBILITY) }
            )
        }
    }
}

@Composable
private fun PremiumModePill(
    title: String,
    body: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "premiumModeScale"
    )
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Accent.copy(alpha = 0.16f) else Surface)
            .border(1.dp, if (selected) Accent.copy(alpha = 0.74f) else Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = if (selected) Accent else TextPrimary, style = MaterialTheme.typography.labelLarge)
        Text(body, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CompatibilityGreetingCard() {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 16) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Rose.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text("궁", color = Rose, style = MaterialTheme.typography.titleMedium)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text("안녕하세요. 관계를 알려주세요.", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                Text("두 사람의 흐름을 책자 형태로 정리해드릴게요.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CompatibilityFormSection(uiState: AppUiState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("관계 유형 선택", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        CompatibilityStatusSelector(
            selected = uiState.compatibilityForm.relationshipStatus,
            onSelected = viewModel::setCompatibilityRelationshipStatus
        )
        Text("상대방 정보 입력", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        MyBirthFixedCard(bundle = uiState.latestBundle)
        PartnerBirthInputCard(
            title = "상대방 정보",
            subtitle = "성별과 생년월일만 입력해 주세요.",
            selectedGender = uiState.compatibilityForm.partnerGender,
            onGenderSelected = viewModel::setCompatibilityPartnerGender,
            calendarType = uiState.compatibilityForm.partner.calendarType,
            year = uiState.compatibilityForm.partner.year,
            month = uiState.compatibilityForm.partner.month,
            day = uiState.compatibilityForm.partner.day,
            onCalendarSelected = viewModel::setCompatibilityPartnerCalendarType,
            onYearChange = viewModel::updateCompatibilityPartnerYear,
            onMonthChange = viewModel::updateCompatibilityPartnerMonth,
            onDayChange = viewModel::updateCompatibilityPartnerDay,
            accentColor = Rose
        )
    }
}

@Composable
private fun CompatibilityStatusSelector(
    selected: CompatibilityRelationshipStatus,
    onSelected: (CompatibilityRelationshipStatus) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(CompatibilityRelationshipStatus.entries) { status ->
            CompatibilityStatusCard(
                status = status,
                selected = selected == status,
                onClick = { onSelected(status) }
            )
        }
    }
}

@Composable
private fun CompatibilityStatusCard(
    status: CompatibilityRelationshipStatus,
    selected: Boolean,
    onClick: () -> Unit
) {
    val lift by animateFloatAsState(
        targetValue = if (selected) -10f else 0f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "compatibilityStatusLift"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 0.96f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "compatibilityStatusScale"
    )
    val glow by animateFloatAsState(
        targetValue = if (selected) 0.38f else 0.08f,
        animationSpec = tween(durationMillis = 360),
        label = "compatibilityStatusGlow"
    )

    Box(
        modifier = Modifier
            .width(128.dp)
            .height(82.dp)
            .graphicsLayer {
                translationY = lift
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (selected) 14.dp else 3.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    if (selected) {
                        listOf(Rose.copy(alpha = 0.24f), Surface.copy(alpha = 0.94f))
                    } else {
                        listOf(Surface2.copy(alpha = 0.86f), Surface.copy(alpha = 0.94f))
                    }
                )
            )
            .border(1.dp, if (selected) Rose.copy(alpha = 0.72f) else Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Rose.copy(alpha = glow),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.86f, size.height * 0.18f)
            )
            drawCircle(
                color = Gold.copy(alpha = glow * 0.55f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.12f, size.height * 0.84f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                status.label,
                color = if (selected) Rose else TextSecondary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(if (selected) "궁합 활성" else "밀어서 선택", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MyBirthFixedCard(bundle: NumerologyResultBundle?) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        tonalColor = Surface2.copy(alpha = 0.58f),
        borderColor = Border.copy(alpha = 0.52f),
        contentPadding = 12
    ) {
        val input = bundle?.displayInput
        val dateText = input?.let {
            "${calendarLabel(it.calendarType)} ${it.year}.${it.month}.${it.day} · ${genderLabel(it.gender)}"
        } ?: "생년월일을 먼저 입력해 주세요"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("내 정보", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
            Text(dateText, color = TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(TextMuted.copy(alpha = 0.08f))
                    .border(1.dp, TextMuted.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text("고정", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PartnerBirthInputCard(
    title: String,
    subtitle: String,
    selectedGender: GenderOption,
    onGenderSelected: (GenderOption) -> Unit,
    calendarType: CalendarType,
    year: String,
    month: String,
    day: String,
    onCalendarSelected: (CalendarType) -> Unit,
    onYearChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    accentColor: Color
) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), tonalColor = Surface, borderColor = Border, contentPadding = 12) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PartnerGenderSelector(
                    selected = selectedGender,
                    onSelected = onGenderSelected,
                    accentColor = accentColor,
                    modifier = Modifier.weight(0.95f)
                )
                ToggleSegment(
                    selected = calendarType,
                    onSelected = onCalendarSelected,
                    modifier = Modifier.weight(1.05f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("생년월일", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                DateInputRow(
                    year = year,
                    month = month,
                    day = day,
                    onYearChange = onYearChange,
                    onMonthChange = onMonthChange,
                    onDayChange = onDayChange
                )
            }
        }
    }
}

@Composable
private fun PartnerGenderSelector(
    selected: GenderOption,
    onSelected: (GenderOption) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier.fillMaxWidth()) {
        listOf(GenderOption.MALE, GenderOption.FEMALE).forEach { gender ->
            val isSelected = selected == gender
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) accentColor.copy(alpha = 0.12f) else Surface2.copy(alpha = 0.72f))
                    .border(1.dp, if (isSelected) accentColor.copy(alpha = 0.68f) else Border, RoundedCornerShape(8.dp))
                    .clickable { onSelected(gender) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(genderLabel(gender), color = if (isSelected) accentColor else TextSecondary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun calendarLabel(type: CalendarType): String = when (type) {
    CalendarType.SOLAR -> "양력"
    CalendarType.LUNAR -> "음력"
}

private fun genderLabel(gender: GenderOption): String = when (gender) {
    GenderOption.MALE -> "남성"
    GenderOption.FEMALE -> "여성"
    GenderOption.NONE -> "미선택"
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
    val topics = FeatureSpecs.premiumTopicPlans.map { it.topic }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(topics) { topic ->
            MagneticTopicCard(
                topic = topic,
                selected = selected == topic,
                onClick = { onSelected(topic) }
            )
        }
    }
}

@Composable
private fun MagneticTopicCard(topic: PremiumTopic, selected: Boolean, onClick: () -> Unit) {
    val lift by animateFloatAsState(
        targetValue = if (selected) -10f else 0f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "topicLift"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 0.96f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "topicScale"
    )
    val glow by animateFloatAsState(
        targetValue = if (selected) 0.38f else 0.08f,
        animationSpec = tween(durationMillis = 360),
        label = "topicGlow"
    )
    Box(
        modifier = Modifier
            .width(128.dp)
            .height(82.dp)
            .graphicsLayer {
                translationY = lift
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (selected) 14.dp else 3.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    if (selected) {
                        listOf(Accent.copy(alpha = 0.24f), Surface.copy(alpha = 0.94f))
                    } else {
                        listOf(Surface2.copy(alpha = 0.86f), Surface.copy(alpha = 0.94f))
                    }
                )
            )
            .border(1.dp, if (selected) Accent.copy(alpha = 0.72f) else Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Accent.copy(alpha = glow),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.86f, size.height * 0.18f)
            )
            drawCircle(
                color = Gold.copy(alpha = glow * 0.55f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.12f, size.height * 0.84f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(topic.bookLabel(), color = if (selected) Accent else TextSecondary, style = MaterialTheme.typography.labelLarge)
            Text(if (selected) "끌림 활성" else "밀어서 선택", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PremiumConcernField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "해답을 찾고 싶은 상세한 사연이나 사건을 자유롭게 적어주세요."
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
        placeholder = {
            Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodySmall)
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
private fun QuestionConfirmScreen(
    topicLabel: String,
    question: String,
    originalConcern: String,
    onEdit: () -> Unit,
    onConfirm: () -> Unit
) {
    MysticBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("이 질문이 맞나요?", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(
                    "적어주신 고민을 AI가 명확하게 답할 수 있는 상담 질문으로 다듬었어요.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                SurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    tonalColor = Surface,
                    borderColor = Accent.copy(alpha = 0.28f),
                    contentPadding = 18
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("AI에 전달할 질문 · $topicLabel", color = Accent, style = MaterialTheme.typography.labelLarge)
                        Text(
                            question.ifBlank { originalConcern },
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                SurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    tonalColor = Surface2,
                    borderColor = Border,
                    contentPadding = 14
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("원문", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                        Text(originalConcern, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton("네, 이 질문이 맞아요", onConfirm, Modifier.fillMaxWidth())
                SecondaryButton("고민 다시 수정하기", onEdit, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun PremiumLoadingScreen(
    isLoading: Boolean,
    hasBook: Boolean,
    mode: PremiumMode,
    onDone: () -> Unit
) {
    val stages = remember(mode) {
        if (mode == PremiumMode.COMPATIBILITY) {
            listOf(
                "두 사람의 숫자를 비교 중",
                "궁합수를 계산하는 중",
                "프리미엄 궁합노트 제본 중"
            )
        } else {
            listOf(
                "수리가 숫자를 해석 중",
                "고민의 질문을 정리 중",
                "프리미엄 책자 제본 중"
            )
        }
    }
    var stageIndex by remember { mutableStateOf(0) }
    LaunchedEffect(isLoading) {
        while (isLoading) {
            delay(1_050)
            stageIndex = (stageIndex + 1) % stages.size
        }
        if (!isLoading && hasBook) {
            stageIndex = stages.lastIndex
        }
    }
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
                ConsultingStageTicker(
                    stage = stages[stageIndex],
                    labels = stages,
                    caption = if (!isLoading && hasBook) {
                        "책자 준비가 끝났어요. 표지를 여는 중입니다."
                    } else if (mode == PremiumMode.COMPATIBILITY) {
                        "남자와 여자 각각의 흐름을 관계 문장과 책자 구조로 엮고 있어요."
                    } else {
                        "입력한 흐름을 상담 문장과 책자 구조로 엮고 있어요."
                    }
                )
                Text(
                    if (mode == PremiumMode.COMPATIBILITY) {
                        "수리가 두 사람 사이의 숫자 흐름과\n관계 질문을 조용히 풀고 있어요"
                    } else {
                        "수리가 당신의 숫자 흐름과\n적어주신 사연을 조용히 풀고 있어요"
                    },
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    if (mode == PremiumMode.COMPATIBILITY) {
                        "궁합수와 생활 흐름에 맞는 관계 비책을 제작 중입니다."
                    } else {
                        "고민 분야에 맞는 깊이 있는 비책을 제작 중입니다."
                    },
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
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
private fun ConsultingStageTicker(stage: String, labels: List<String>, caption: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(initialScale = 0.96f)) togetherWith
                    (fadeOut(tween(160)) + scaleOut(targetScale = 1.02f))
            },
            label = "consultingStage"
        ) { text ->
            Text(
                text,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(caption, color = TextMuted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            labels.forEach { label ->
                val active = label == stage
                Box(
                    modifier = Modifier
                        .size(width = if (active) 22.dp else 7.dp, height = 7.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) Accent else Surface2)
                )
            }
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
    var revealStarted by remember(book?.bookId) { mutableStateOf(false) }
    LaunchedEffect(book?.bookId) { revealStarted = true }
    val reveal by animateFloatAsState(
        targetValue = if (revealStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 820, easing = FastOutSlowInEasing),
        label = "bookCoverReveal"
    )
    BookStageScaffold {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = reveal
                    scaleX = 0.94f + reveal * 0.06f
                    scaleY = 0.94f + reveal * 0.06f
                    translationY = (1f - reveal) * 28f
                }
                .bookTurnGestures(onPrevious = null, onNext = onOpen)
                .then(flipModifier),
            contentAlignment = Alignment.Center
        ) {
            PremiumBookCover(book = book, modifier = Modifier.fillMaxSize())
            CoverRevealAura(reveal = reveal, identity = bookIdentityFor(book), modifier = Modifier.fillMaxSize())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("처음으로", onReset, Modifier.weight(1f))
            GradientButton("노트 펼치기", onOpen, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CoverRevealAura(reveal: Float, identity: BookIdentityTheme, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val shadowAlpha = (1f - reveal).coerceIn(0f, 1f)
        drawRect(Color.Black.copy(alpha = shadowAlpha * 0.42f))
        val lineProgress = reveal.coerceIn(0f, 1f)
        val left = size.width * 0.08f
        val top = size.height * 0.03f
        val right = size.width * (0.08f + 0.84f * lineProgress)
        val bottom = size.height * 0.97f
        drawLine(identity.foil.copy(alpha = 0.60f * lineProgress), Offset(left, top), Offset(right, top), strokeWidth = 2.2f)
        drawLine(identity.foil.copy(alpha = 0.42f * lineProgress), Offset(left, bottom), Offset(right, bottom), strokeWidth = 1.6f)
        drawCircle(
            identity.foil.copy(alpha = 0.18f * (1f - kotlin.math.abs(0.55f - reveal)).coerceIn(0f, 1f)),
            radius = size.minDimension * 0.42f,
            center = Offset(size.width * 0.50f, size.height * 0.40f)
        )
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
    val tocTitles = if (book?.bookType == FortuneBookType.COMPATIBILITY) {
        listOf("남자 성향", "여자 성향", "둘의 궁합수", "생활 흐름")
    } else {
        listOf("지혜의 본질", "상황별 해석", "주의할 장면", "이번 달 행동 지침")
    }
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
            tocTitles.forEachIndexed { index, title ->
                TocLine(index + 1, title, "p.${4 + index * 10}", identity, onClick = onRead)
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
    onShare: (FortuneBook) -> Unit,
    onArchive: () -> Unit,
    flipModifier: Modifier
) {
    val chapter = book?.chapters?.firstOrNull()
    val identity = bookIdentityFor(book)
    val isCompatibility = book?.bookType == FortuneBookType.COMPATIBILITY
    BookStageScaffold {
        PaperPage(
            identity = identity,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .bookTurnGestures(onPrevious = onBack, onNext = null)
                .then(flipModifier)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(24.dp).background(identity.accent, CircleShape), contentAlignment = Alignment.Center) {
                    Text("2", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        if (isCompatibility) "두 사람 궁합 해석 비책" else "상황별 고민 해결 비책",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        book?.concernTopic ?: if (isCompatibility) "궁합 특화 카운슬링" else "연애 특화 카운슬링",
                        color = identity.accent,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = { book?.let(onShare) }) {
                    Icon(Icons.Rounded.Share, contentDescription = "공유", tint = identity.accent)
                }
            }
            DetailBox(
                if (isCompatibility) "궁금한 관계 질문" else "던지신 질문",
                concern.ifBlank {
                    book?.concernText.orEmpty().ifBlank {
                        if (isCompatibility) "두 사람 사이의 전반적인 궁합 흐름" else "지금 마음속에서 가장 자주 떠오르는 고민"
                    }
                },
                identity.accentDeep
            )
            DetailBox(if (isCompatibility) "마찰이 생기는 장면" else "주의할 장면", chapter?.highlightQuote ?: book?.summary.orEmpty(), Rose)
            DetailBox(
                if (isCompatibility) "관계 흐름 포인트" else "읽는 포인트",
                chapter?.actionTip?.joinToString("\n") { "• $it" } ?: if (isCompatibility) {
                    "서로의 속도와 말의 온도가 관계의 편안함을 좌우하는 흐름입니다."
                } else {
                    "지금 반복되는 분위기를 차분히 읽는 것이 리포트의 핵심입니다."
                },
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
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
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
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(18.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f))))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(10.dp)
                .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)))
        )
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
private fun TocLine(index: Int, title: String, page: String, identity: BookIdentityTheme, onClick: (() -> Unit)? = null) {
    val focus by animateFloatAsState(
        targetValue = if (index == 2) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "tocFocus"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = 1f + focus * 0.025f
                scaleY = 1f + focus * 0.025f
                translationX = focus * 6f
            }
            .clip(RoundedCornerShape(8.dp))
            .background(if (index == 2) identity.tint.copy(alpha = 0.34f) else Color.Transparent)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
    val themeId = book?.resolvedThemeId() ?: BookThemeId.ROMANCE
    val theme = BookThemeSpecs.get(themeId)
    val spec = book?.let(BookSpecs::forBook) ?: BookSpecs.forTheme(themeId)
    return BookIdentityTheme(
        shortName = theme.displayName,
        caption = spec?.coverKicker ?: "PREMIUM FORTUNE NOTE",
        accent = theme.readerAccentColor.toComposeColor(),
        accentDeep = theme.readerAccentDeepColor.toComposeColor(),
        ribbon = theme.pdfRibbonColor.toComposeColor(),
        foil = theme.pdfFoilColor.toComposeColor(),
        stitch = theme.readerStitchColor.toComposeColor(),
        coverTop = theme.readerCoverTopColor.toComposeColor(),
        coverMid = theme.readerCoverMidColor.toComposeColor(),
        coverBottom = theme.readerCoverBottomColor.toComposeColor(),
        tint = theme.readerTintColor.toComposeColor(),
        page = theme.readerPageColor.toComposeColor(),
        pageTop = theme.readerPageTopColor.toComposeColor(),
        edge = theme.readerEdgeColor.toComposeColor()
    )
}

private fun Long.toComposeColor(): Color = Color(this)

private fun Int.toComposeColor(): Color = Color(toLong() and 0xFFFFFFFF)

private fun PremiumTopic.bookLabel(): String = FeatureSpecs.planFor(this).bookLabel
