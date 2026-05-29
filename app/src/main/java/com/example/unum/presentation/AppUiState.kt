package com.example.unum.presentation

import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.CompatibilityConsultation
import com.example.unum.data.model.CompatibilityFormState
import com.example.unum.data.model.HomeFormState
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumMode
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.RecentSearch
import com.example.unum.data.model.ReaderFontScale

enum class PremiumFlowStep {
    FORM,
    CONFIRM_QUESTION,
    LOADING,
    VOICE_CHOICE,
    HANOK_READING,
    COVER,
    TOC,
    DETAIL
}

data class AppUiState(
    val formState: HomeFormState = HomeFormState(),
    val latestBundle: NumerologyResultBundle? = null,
    val recentSearches: List<RecentSearch> = emptyList(),
    val premiumMode: PremiumMode = PremiumMode.PERSONAL,
    val premiumTopic: PremiumTopic = PremiumTopic.ROMANCE,
    val premiumConcern: String = "",
    val premiumEssentialQuestion: String = "",
    val premiumFlowStep: PremiumFlowStep = PremiumFlowStep.FORM,
    val compatibilityForm: CompatibilityFormState = CompatibilityFormState(),
    val compatibilityConcern: String = "",
    val premiumResult: PremiumConsultation? = null,
    val compatibilityResult: CompatibilityConsultation? = null,
    val savedBooks: List<FortuneBook> = emptyList(),
    val selectedBookId: String? = null,
    val isPremiumLoading: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val readerFontScale: ReaderFontScale = ReaderFontScale.MEDIUM,
    val inputError: String? = null,
    val isLoading: Boolean = false
)
