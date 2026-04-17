package com.example.unum.presentation

import com.example.unum.data.model.HomeFormState
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.RecentSearch

data class AppUiState(
    val formState: HomeFormState = HomeFormState(),
    val latestBundle: NumerologyResultBundle? = null,
    val recentSearches: List<RecentSearch> = emptyList(),
    val premiumTopic: PremiumTopic = PremiumTopic.ROMANCE,
    val premiumConcern: String = "",
    val premiumResult: PremiumConsultation? = null,
    val isPremiumLoading: Boolean = false,
    val inputError: String? = null,
    val isLoading: Boolean = false
)
