package com.example.unum.presentation.premium

import com.example.unum.presentation.library.rememberFortuneBookShareHandler
import com.example.unum.presentation.*

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.PremiumMode

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

