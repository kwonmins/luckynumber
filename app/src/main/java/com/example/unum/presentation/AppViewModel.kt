package com.example.unum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unum.BuildConfig
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.HomeFormState
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.RecentSearch
import com.example.unum.domain.NumerologyCalculator
import com.example.unum.domain.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    private val repository = ServiceLocator.numerologyRepository
    private val calculateNumerology = ServiceLocator.calculateNumerologyUseCase
    private val generatePremiumConsultation = ServiceLocator.generatePremiumConsultationUseCase
    private val premiumAccessGate = ServiceLocator.premiumAccessGate

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        observeRecentSearches()
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            repository.observeRecentSearches().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    fun setCalendarType(type: CalendarType) = updateForm { copy(calendarType = type) }
    fun updateYear(value: String) = updateForm { copy(year = value.filter(Char::isDigit).take(4)) }
    fun updateMonth(value: String) = updateForm { copy(month = value.filter(Char::isDigit).take(2)) }
    fun updateDay(value: String) = updateForm { copy(day = value.filter(Char::isDigit).take(2)) }
    fun setGender(gender: GenderOption) = updateForm { copy(gender = gender) }

    private fun updateForm(block: HomeFormState.() -> HomeFormState) {
        _uiState.update { current -> current.copy(formState = current.formState.block(), inputError = null) }
    }

    fun calculateAndStore(isInitial: Boolean = false, onSuccess: (() -> Unit)? = null) {
        val solarBirthInput = NumerologyCalculator.toBirthInput(_uiState.value.formState)
        if (solarBirthInput == null) {
            _uiState.update { it.copy(inputError = "올바른 양력 생년월일을 입력해 주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, inputError = null) }
            runCatching {
                val lunarBirthInput = NumerologyCalculator.toLunarBirthInput(solarBirthInput)
                val numbers = calculateNumerology(lunarBirthInput)
                val content = repository.getContent(numbers.code)
                val bundle = NumerologyResultBundle(
                    input = lunarBirthInput,
                    numbers = numbers,
                    content = content,
                    displayInput = solarBirthInput
                )
                val recent = RecentSearch(
                    code = numbers.code,
                    dateLabel = NumerologyCalculator.formatDate(solarBirthInput.year, solarBirthInput.month, solarBirthInput.day),
                    subtitle = "음력 ${NumerologyCalculator.formatDate(lunarBirthInput.year, lunarBirthInput.month, lunarBirthInput.day)} 기준 · 운명수 ${numbers.destiny}"
                )
                repository.addRecentSearch(recent)
                bundle
            }.onSuccess { bundle ->
                _uiState.update {
                    it.copy(
                        latestBundle = bundle,
                        premiumResult = if (isInitial) it.premiumResult else null,
                        isLoading = false
                    )
                }
                onSuccess?.invoke()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, inputError = error.message ?: "데이터를 불러오지 못했습니다.") }
            }
        }
    }

    fun loadRecentSearch(search: RecentSearch, onSuccess: (() -> Unit)? = null) {
        val parts = search.dateLabel.split(".")
        if (parts.size != 3) return
        _uiState.update {
            it.copy(
                formState = it.formState.copy(
                    year = parts[0],
                    month = parts[1].trimStart('0').ifBlank { "0" },
                    day = parts[2].trimStart('0').ifBlank { "0" }
                )
            )
        }
        calculateAndStore(onSuccess = onSuccess)
    }

    fun selectPremiumTopic(topic: PremiumTopic) { _uiState.update { it.copy(premiumTopic = topic) } }
    fun updatePremiumConcern(value: String) { _uiState.update { it.copy(premiumConcern = value) } }
    fun clearBirthInput() {
        _uiState.update {
            it.copy(
                formState = HomeFormState(gender = it.formState.gender),
                inputError = null,
                latestBundle = null,
                premiumResult = null
            )
        }
    }

    fun removeRecentSearch(search: RecentSearch) {
        viewModelScope.launch {
            repository.removeRecentSearch(search)
        }
    }

    fun runPremiumConsultation() {
        val bundle = _uiState.value.latestBundle ?: return
        if (!premiumAccessGate.canUsePremiumForTest()) {
            _uiState.update { it.copy(inputError = "프리미엄 이용 권한을 확인할 수 없습니다.") }
            return
        }

        viewModelScope.launch {
            val current = _uiState.value
            _uiState.update { it.copy(isPremiumLoading = true, inputError = null, premiumResult = null) }
            runCatching {
                generatePremiumConsultation(
                    apiKey = BuildConfig.OPENAI_API_KEY,
                    topic = current.premiumTopic,
                    concern = current.premiumConcern,
                    bundle = bundle
                )
            }.onSuccess { consultation ->
                _uiState.update { it.copy(isPremiumLoading = false, premiumResult = consultation) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isPremiumLoading = false,
                        inputError = error.message ?: "프리미엄 상담을 불러오지 못했습니다."
                    )
                }
            }
        }
    }
}
