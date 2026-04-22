package com.example.unum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unum.BuildConfig
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityFormState
import com.example.unum.data.model.FortuneBook
import com.example.unum.data.model.FortuneBookType
import com.example.unum.data.model.GenderOption
import com.example.unum.data.model.HomeFormState
import com.example.unum.data.model.PartnerBirthFormState
import com.example.unum.data.model.PremiumMode
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.ReaderFontScale
import com.example.unum.data.model.RecentSearch
import com.example.unum.data.model.SuriSpeechScript
import com.example.unum.domain.NumerologyCalculator
import com.example.unum.domain.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    private val repository = ServiceLocator.numerologyRepository
    private val buildNumerologyResultBundle = ServiceLocator.buildNumerologyResultBundleUseCase
    private val buildFortuneBook = ServiceLocator.buildFortuneBookUseCase
    private val buildSuriSpeechScript = ServiceLocator.buildSuriSpeechScriptUseCase
    private val generatePremiumConsultation = ServiceLocator.generatePremiumConsultationUseCase
    private val generateCompatibilityConsultation = ServiceLocator.generateCompatibilityConsultationUseCase
    private val premiumAccessGate = ServiceLocator.premiumAccessGate
    private val fortuneBookStore = ServiceLocator.fortuneBookStore

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        val books = sortBooks(fortuneBookStore.loadBooks())
        _uiState.update { it.copy(savedBooks = books, selectedBookId = books.firstOrNull()?.bookId) }
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

    fun setReaderFontScale(scale: ReaderFontScale) {
        _uiState.update { it.copy(readerFontScale = scale) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    private fun updateForm(block: HomeFormState.() -> HomeFormState) {
        _uiState.update { current -> current.copy(formState = current.formState.block(), inputError = null) }
    }

    private fun updateCompatibilityForm(block: CompatibilityFormState.() -> CompatibilityFormState) {
        _uiState.update { current ->
            current.copy(
                compatibilityForm = current.compatibilityForm.block(),
                inputError = null
            )
        }
    }

    private fun updateCompatibilityMale(block: PartnerBirthFormState.() -> PartnerBirthFormState) = updateCompatibilityForm {
        copy(male = male.block())
    }

    private fun updateCompatibilityFemale(block: PartnerBirthFormState.() -> PartnerBirthFormState) = updateCompatibilityForm {
        copy(female = female.block())
    }

    fun calculateAndStore(isInitial: Boolean = false, onSuccess: (() -> Unit)? = null) {
        val userBirthInput = NumerologyCalculator.toBirthInput(_uiState.value.formState)
        if (userBirthInput == null) {
            _uiState.update { it.copy(inputError = "생년월일을 다시 확인해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, inputError = null) }
            runCatching {
                val bundle = buildNumerologyResultBundle(userBirthInput)
                val genderPrefix = when (userBirthInput.gender) {
                    GenderOption.MALE -> "남성 · "
                    GenderOption.FEMALE -> "여성 · "
                    GenderOption.NONE -> ""
                }
                val displaySolarInput = bundle.displayInput
                val numbers = bundle.numbers
                repository.addRecentSearch(
                    RecentSearch(
                        code = numbers.code,
                        dateLabel = NumerologyCalculator.formatDate(
                            displaySolarInput.year,
                            displaySolarInput.month,
                            displaySolarInput.day
                        ),
                        subtitle = "${genderPrefix}운명수 ${numbers.destiny} · 코드 ${numbers.code}",
                        gender = userBirthInput.gender,
                        inputCalendarType = userBirthInput.calendarType,
                        inputYear = userBirthInput.year,
                        inputMonth = userBirthInput.month,
                        inputDay = userBirthInput.day
                    )
                )
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inputError = error.message ?: "결과를 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    fun loadRecentSearch(search: RecentSearch, onSuccess: (() -> Unit)? = null) {
        val fallbackParts = search.dateLabel.split(".")
        if (fallbackParts.size != 3) return
        _uiState.update {
            it.copy(
                formState = it.formState.copy(
                    calendarType = search.inputCalendarType,
                    year = (search.inputYear?.toString() ?: fallbackParts[0]),
                    month = (search.inputMonth?.toString() ?: fallbackParts[1].trimStart('0').ifBlank { "0" }),
                    day = (search.inputDay?.toString() ?: fallbackParts[2].trimStart('0').ifBlank { "0" }),
                    gender = search.gender
                )
            )
        }
        calculateAndStore(onSuccess = onSuccess)
    }

    fun selectPremiumTopic(topic: PremiumTopic) {
        _uiState.update { it.copy(premiumTopic = topic) }
    }

    fun setPremiumMode(mode: PremiumMode) {
        _uiState.update { it.copy(premiumMode = mode, inputError = null) }
    }

    fun updatePremiumConcern(value: String) {
        _uiState.update { it.copy(premiumConcern = value) }
    }

    fun updateCompatibilityConcern(value: String) {
        _uiState.update { it.copy(compatibilityConcern = value) }
    }

    fun setCompatibilityMaleCalendarType(type: CalendarType) = updateCompatibilityMale { copy(calendarType = type) }

    fun updateCompatibilityMaleYear(value: String) = updateCompatibilityMale { copy(year = value.filter(Char::isDigit).take(4)) }

    fun updateCompatibilityMaleMonth(value: String) = updateCompatibilityMale { copy(month = value.filter(Char::isDigit).take(2)) }

    fun updateCompatibilityMaleDay(value: String) = updateCompatibilityMale { copy(day = value.filter(Char::isDigit).take(2)) }

    fun setCompatibilityFemaleCalendarType(type: CalendarType) = updateCompatibilityFemale { copy(calendarType = type) }

    fun updateCompatibilityFemaleYear(value: String) = updateCompatibilityFemale { copy(year = value.filter(Char::isDigit).take(4)) }

    fun updateCompatibilityFemaleMonth(value: String) = updateCompatibilityFemale { copy(month = value.filter(Char::isDigit).take(2)) }

    fun updateCompatibilityFemaleDay(value: String) = updateCompatibilityFemale { copy(day = value.filter(Char::isDigit).take(2)) }

    fun clearBirthInput() {
        _uiState.update {
            it.copy(
                formState = HomeFormState(gender = it.formState.gender),
                inputError = null,
                latestBundle = null,
                premiumResult = null,
                compatibilityResult = null
            )
        }
    }

    fun clearCompatibilityInput() {
        _uiState.update {
            it.copy(
                compatibilityForm = CompatibilityFormState(),
                compatibilityConcern = "",
                compatibilityResult = null,
                inputError = null
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
            _uiState.update {
                it.copy(
                    isPremiumLoading = true,
                    inputError = null,
                    premiumResult = null,
                    compatibilityResult = null
                )
            }
            runCatching {
                generatePremiumConsultation(
                    apiKey = BuildConfig.OPENAI_API_KEY,
                    topic = current.premiumTopic,
                    concern = current.premiumConcern,
                    bundle = bundle
                )
            }.onSuccess { consultation ->
                val book = buildFortuneBook.buildPersonalBook(
                    consultation = consultation,
                    bundle = bundle,
                    topic = current.premiumTopic,
                    concern = current.premiumConcern
                )
                val nextBooks = sortBooks(listOf(book) + _uiState.value.savedBooks)
                fortuneBookStore.saveBooks(nextBooks)
                _uiState.update {
                    it.copy(
                        isPremiumLoading = false,
                        premiumResult = consultation,
                        savedBooks = nextBooks,
                        selectedBookId = book.bookId
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isPremiumLoading = false,
                        inputError = error.message ?: "AI 프리미엄 운세를 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    fun runCompatibilityConsultation() {
        if (!premiumAccessGate.canUsePremiumForTest()) {
            _uiState.update { it.copy(inputError = "프리미엄 이용 권한을 확인할 수 없습니다.") }
            return
        }

        val current = _uiState.value
        val maleInput = current.compatibilityForm.male.toBirthInput(GenderOption.MALE)
        val femaleInput = current.compatibilityForm.female.toBirthInput(GenderOption.FEMALE)

        if (maleInput == null || femaleInput == null) {
            _uiState.update { it.copy(inputError = "남자와 여자 생년월일을 다시 확인해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPremiumLoading = true,
                    inputError = null,
                    premiumResult = null,
                    compatibilityResult = null
                )
            }
            runCatching {
                val maleBundle = buildNumerologyResultBundle(maleInput)
                val femaleBundle = buildNumerologyResultBundle(femaleInput)
                val consultation = generateCompatibilityConsultation(
                    apiKey = BuildConfig.OPENAI_API_KEY,
                    maleBundle = maleBundle,
                    femaleBundle = femaleBundle,
                    concern = current.compatibilityConcern
                )
                Triple(maleBundle, femaleBundle, consultation)
            }.onSuccess { (maleBundle, femaleBundle, consultation) ->
                val book = buildFortuneBook.buildCompatibilityBook(
                    consultation = consultation,
                    maleBundle = maleBundle,
                    femaleBundle = femaleBundle,
                    concern = current.compatibilityConcern
                )
                val nextBooks = sortBooks(listOf(book) + _uiState.value.savedBooks)
                fortuneBookStore.saveBooks(nextBooks)
                _uiState.update {
                    it.copy(
                        isPremiumLoading = false,
                        compatibilityResult = consultation,
                        savedBooks = nextBooks,
                        selectedBookId = book.bookId
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isPremiumLoading = false,
                        inputError = error.message ?: "AI 궁합을 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    fun selectSavedBook(book: FortuneBook) {
        val now = System.currentTimeMillis()
        val nextBooks = sortBooks(
            _uiState.value.savedBooks.map { saved ->
                if (saved.bookId == book.bookId) saved.copy(lastOpenedAt = now) else saved
            }
        )
        fortuneBookStore.saveBooks(nextBooks)
        _uiState.update { it.copy(savedBooks = nextBooks, selectedBookId = book.bookId) }
    }

    fun selectSavedBookById(bookId: String) {
        _uiState.value.savedBooks.firstOrNull { it.bookId == bookId }?.let(::selectSavedBook)
    }

    fun toggleBookmark(book: FortuneBook) {
        val nextBooks = sortBooks(
            _uiState.value.savedBooks.map { saved ->
                if (saved.bookId == book.bookId) saved.copy(isBookmarked = !saved.isBookmarked) else saved
            }
        )
        fortuneBookStore.saveBooks(nextBooks)
        _uiState.update { it.copy(savedBooks = nextBooks) }
    }

    fun buildCurrentPremiumSpeechScript(): SuriSpeechScript? {
        val current = _uiState.value
        return when (current.premiumMode) {
            PremiumMode.PERSONAL -> {
                val bundle = current.latestBundle ?: return null
                val latestBook = current.savedBooks.firstOrNull { it.bookType == FortuneBookType.PERSONAL }
                current.premiumResult?.let { consultation ->
                    buildSuriSpeechScript.buildPersonalResult(
                        bundle = bundle,
                        consultation = consultation,
                        topic = current.premiumTopic,
                        concern = current.premiumConcern,
                        book = latestBook
                    )
                } ?: buildSuriSpeechScript.buildPersonalPreview(
                    bundle = bundle,
                    topic = current.premiumTopic,
                    concern = current.premiumConcern
                )
            }

            PremiumMode.COMPATIBILITY -> {
                val latestBook = current.savedBooks.firstOrNull { it.bookType == FortuneBookType.COMPATIBILITY }
                current.compatibilityResult?.let { consultation ->
                    buildSuriSpeechScript.buildCompatibilityResult(
                        consultation = consultation,
                        concern = current.compatibilityConcern,
                        book = latestBook
                    )
                } ?: if (current.compatibilityConcern.isNotBlank() || current.compatibilityForm.hasAnyInput()) {
                    buildSuriSpeechScript.buildCompatibilityPreview(current.compatibilityConcern)
                } else {
                    null
                }
            }
        }
    }

    private fun sortBooks(books: List<FortuneBook>): List<FortuneBook> {
        return books.sortedWith(
            compareByDescending<FortuneBook> { it.lastOpenedAt ?: it.createdAt }
                .thenByDescending { it.createdAt }
        )
    }

    private fun CompatibilityFormState.hasAnyInput(): Boolean {
        return male.year.isNotBlank() ||
            male.month.isNotBlank() ||
            male.day.isNotBlank() ||
            female.year.isNotBlank() ||
            female.month.isNotBlank() ||
            female.day.isNotBlank()
    }

    private fun PartnerBirthFormState.toBirthInput(gender: GenderOption) =
        NumerologyCalculator.toBirthInput(
            calendarType = calendarType,
            yearText = year,
            monthText = month,
            dayText = day,
            gender = gender
        )
}
