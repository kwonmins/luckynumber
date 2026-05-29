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
import com.example.unum.data.model.NumerologyNumbers
import com.example.unum.data.model.PartnerBirthFormState
import com.example.unum.data.model.PremiumMode
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.ReaderFontScale
import com.example.unum.data.model.RecentSearch
import com.example.unum.data.model.SuriSpeechScript
import com.example.unum.domain.NumerologyCalculator
import com.example.unum.domain.ServiceLocator
import com.example.unum.domain.usecase.PremiumMonthPlanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen-facing orchestration for the app.
 *
 * One UiState is shared across bottom-tab screens so the free report, premium
 * note creation, reader, and archive can hand context to each other. Heavy
 * business rules stay in use cases; this class coordinates validation, loading
 * flags, local persistence, and navigation-ready state.
 */
class AppViewModel : ViewModel() {
    // Grouped here to make the eventual move from ServiceLocator to DI simple.
    private val repository = ServiceLocator.numerologyRepository
    private val buildNumerologyResultBundle = ServiceLocator.buildNumerologyResultBundleUseCase
    private val buildFortuneBook = ServiceLocator.buildFortuneBookUseCase
    private val buildSuriSpeechScript = ServiceLocator.buildSuriSpeechScriptUseCase
    private val buildPremiumDummyConsultation = ServiceLocator.buildPremiumDummyConsultationUseCase
    private val generatePremiumConsultation = ServiceLocator.generatePremiumConsultationUseCase
    private val generateCompatibilityConsultation = ServiceLocator.generateCompatibilityConsultationUseCase
    private val premiumAccessGate = ServiceLocator.premiumAccessGate
    private val fortuneBookStore = ServiceLocator.fortuneBookStore

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        val loadedBooks = fortuneBookStore.loadBooks()
        val books = sortBooks(loadedBooks.map(::refreshStoredMonthInsights))
        if (books != loadedBooks) {
            fortuneBookStore.saveBooks(books)
        }
        _uiState.update { it.copy(savedBooks = books, selectedBookId = books.firstOrNull()?.bookId) }
        observeRecentSearches()
        calculateAndStore(isInitial = true)
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            repository.observeRecentSearches().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    // Birth input and free report -------------------------------------------------

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

    // Premium note input ---------------------------------------------------------

    fun selectPremiumTopic(topic: PremiumTopic) {
        _uiState.update { it.copy(premiumTopic = topic, premiumEssentialQuestion = "", inputError = null) }
    }

    fun setPremiumMode(mode: PremiumMode) {
        _uiState.update { it.copy(premiumMode = mode, inputError = null) }
    }

    fun updatePremiumConcern(value: String) {
        _uiState.update { it.copy(premiumConcern = value, premiumEssentialQuestion = "", inputError = null) }
    }

    fun setPremiumFlowStep(step: PremiumFlowStep) {
        _uiState.update {
            if (step == PremiumFlowStep.LOADING) {
                it.copy(
                    premiumFlowStep = step,
                    premiumResult = null,
                    compatibilityResult = null,
                    inputError = null
                )
            } else {
                it.copy(premiumFlowStep = step, inputError = null)
            }
        }
    }

    fun resetPremiumFlow() {
        _uiState.update { it.copy(premiumFlowStep = PremiumFlowStep.FORM, premiumEssentialQuestion = "", inputError = null) }
    }

    fun preparePremiumQuestionConfirmation(): Boolean {
        val current = _uiState.value
        if (current.latestBundle == null) {
            _uiState.update { it.copy(inputError = "먼저 생년월일 결과를 만든 뒤 프리미엄 책자를 신청해 주세요.") }
            return false
        }

        val normalizedConcern = current.premiumConcern.trim().replace(Regex("\\s+"), " ")
        if (normalizedConcern.length < 6) {
            _uiState.update { it.copy(inputError = "수리가 질문의 본질을 잡을 수 있도록 고민을 한 문장 이상 적어주세요.") }
            return false
        }

        val essentialQuestion = buildEssentialQuestion(current.premiumTopic, normalizedConcern)
        _uiState.update {
            it.copy(
                premiumEssentialQuestion = essentialQuestion,
                premiumFlowStep = PremiumFlowStep.CONFIRM_QUESTION,
                inputError = null
            )
        }
        return true
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

    // Premium note generation ----------------------------------------------------

    fun runPremiumConsultation() {
        val bundle = _uiState.value.latestBundle ?: return
        if (!premiumAccessGate.canUsePremiumForTest()) {
            _uiState.update { it.copy(inputError = "프리미엄 이용 권한을 확인할 수 없습니다.") }
            return
        }

        viewModelScope.launch {
            val current = _uiState.value
            val confirmedConcern = current.premiumEssentialQuestion.ifBlank { current.premiumConcern }
            _uiState.update {
                it.copy(
                    isPremiumLoading = true,
                    inputError = null,
                    premiumResult = null,
                    compatibilityResult = null
                )
            }
            runCatching {
                if (BuildConfig.OPENAI_API_KEY.isBlank()) {
                    buildPremiumDummyConsultation(
                        topic = current.premiumTopic,
                        concern = confirmedConcern,
                        bundle = bundle
                    )
                } else {
                    generatePremiumConsultation(
                        apiKey = BuildConfig.OPENAI_API_KEY,
                        topic = current.premiumTopic,
                        concern = confirmedConcern,
                        bundle = bundle
                    )
                }
            }.onSuccess { consultation ->
                val book = buildFortuneBook.buildPersonalBook(
                    consultation = consultation,
                    bundle = bundle,
                    topic = current.premiumTopic,
                    concern = confirmedConcern
                )
                val nextBooks = saveNewBook(book)
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
                        premiumFlowStep = PremiumFlowStep.FORM,
                        inputError = error.message ?: "운세노트를 불러오지 못했습니다."
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
                val nextBooks = saveNewBook(book)
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
                        inputError = error.message ?: "궁합노트를 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    // Archive and reader ---------------------------------------------------------

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

    // Speech scene ---------------------------------------------------------------

    fun buildCurrentPremiumSpeechScript(): SuriSpeechScript? {
        val current = _uiState.value
        return when (current.premiumMode) {
            PremiumMode.PERSONAL -> {
                val bundle = current.latestBundle ?: return null
                val latestBook = current.selectedPersonalBook()
                val confirmedConcern = current.premiumEssentialQuestion.ifBlank { current.premiumConcern }
                current.premiumResult?.let { consultation ->
                    buildSuriSpeechScript.buildPersonalResult(
                        bundle = bundle,
                        consultation = consultation,
                        topic = current.premiumTopic,
                        concern = confirmedConcern,
                        book = latestBook
                    )
                } ?: buildSuriSpeechScript.buildPersonalPreview(
                    bundle = bundle,
                    topic = current.premiumTopic,
                    concern = confirmedConcern
                )
            }

            PremiumMode.COMPATIBILITY -> {
                val latestBook = current.selectedCompatibilityBook()
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

    private fun saveNewBook(book: FortuneBook): List<FortuneBook> {
        val nextBooks = sortBooks(listOf(book) + _uiState.value.savedBooks)
        fortuneBookStore.saveBooks(nextBooks)
        return nextBooks
    }

    private fun refreshStoredMonthInsights(book: FortuneBook): FortuneBook {
        if (book.bookType != FortuneBookType.PERSONAL) return book
        val topic = PremiumMonthPlanner.topicFromThemeOrLabel(book.coverTheme, book.concernTopic) ?: return book
        val currentMonth = PremiumMonthPlanner.currentMonth()
        val bookNumbers = NumerologyNumbers(
            destiny = book.destiny,
            early = book.early,
            middle = book.middle,
            late = book.late,
            code = book.code
        )
        val bestSelection = PremiumMonthPlanner.pickBestMonth(topic, bookNumbers, currentMonth)
        val riskySelection = PremiumMonthPlanner.pickRiskyMonth(topic, bookNumbers, currentMonth)
        val bestMonth = bestSelection.toDisplayText()
        val riskyMonth = riskySelection.toDisplayText()
        val shouldRefreshBest = book.bestMonth != bestMonth ||
            PremiumMonthPlanner.isPastMonthText(book.bestMonth, currentMonth)
        val shouldRefreshRisky = book.riskyMonth != riskyMonth ||
            PremiumMonthPlanner.isPastMonthText(book.riskyMonth, currentMonth)

        if (!shouldRefreshBest && !shouldRefreshRisky) return book

        return book.copy(
            bestMonth = if (shouldRefreshBest) bestMonth else book.bestMonth,
            bestMonthReason = if (shouldRefreshBest) buildStoredBestMonthReason(topic, bestSelection) else book.bestMonthReason,
            riskyMonth = if (shouldRefreshRisky) riskyMonth else book.riskyMonth,
            riskyMonthReason = if (shouldRefreshRisky) buildStoredRiskyMonthReason(topic, riskySelection) else book.riskyMonthReason
        )
    }

    private fun buildStoredBestMonthReason(
        topic: PremiumTopic,
        selection: PremiumMonthPlanner.MonthSelection
    ): String {
        val monthText = selection.toDisplayText()
        val base = when (topic) {
            PremiumTopic.ROMANCE -> "${monthText}에는 마음을 새롭게 열고 관계의 온도를 다시 맞추기 좋습니다. 무거운 확인보다 구체적인 만남 제안이 흐름을 부드럽게 만듭니다."
            PremiumTopic.CAREER -> "${monthText}에는 준비한 것을 실제 제안, 지원, 면담으로 옮기기 좋습니다. 조건과 역할을 선명하게 정리하면 기회가 더 분명해집니다."
            PremiumTopic.MONEY -> "${monthText}에는 수입과 지출 구조를 다시 잡기 좋습니다. 큰 욕심보다 기준을 세우는 행동이 돈의 흐름을 안정시킵니다."
            PremiumTopic.SELF_ESTEEM -> "${monthText}에는 스스로를 다시 세우는 힘이 살아납니다. 작은 약속을 지키는 경험을 반복하면 마음의 중심이 단단해집니다."
            PremiumTopic.RELATIONSHIP -> "${monthText}에는 사람들과의 접점이 자연스럽게 열립니다. 오래 미뤄둔 대화나 관계 회복을 부드럽게 시작하기 좋습니다."
        }
        val passedMonth = selection.replacedPastMonth ?: return base
        return "올해 가장 추천 흐름이 강했던 ${passedMonth}월은 이미 지났습니다. 지금 이후에는 ${monthText}을 추천 구간으로 보고 움직여보세요. $base"
    }

    private fun buildStoredRiskyMonthReason(
        topic: PremiumTopic,
        selection: PremiumMonthPlanner.MonthSelection
    ): String {
        val monthText = selection.toDisplayText()
        val base = when (topic) {
            PremiumTopic.ROMANCE -> "${monthText}에는 마음이 앞서 결론을 재촉하기 쉽습니다. 상대의 속도와 여백을 각별히 조심하세요."
            PremiumTopic.CAREER -> "${monthText}에는 변화 욕구가 커져 성급한 결정으로 흐르기 쉽습니다. 큰 선택은 한 번 더 검토한 뒤 움직이는 편이 안전합니다."
            PremiumTopic.MONEY -> "${monthText}에는 빠른 이익을 좇는 마음이 강해질 수 있습니다. 확인되지 않은 제안과 충동 지출은 반드시 거리를 두세요."
            PremiumTopic.SELF_ESTEEM -> "${monthText}에는 비교와 조급함이 커지기 쉽습니다. 몸과 마음의 리듬을 먼저 회복하는 데 집중하세요."
            PremiumTopic.RELATIONSHIP -> "${monthText}에는 사람 사이의 오해가 빨리 번질 수 있습니다. 중요한 대화는 차분히 시간을 두는 편이 좋습니다."
        }
        val passedMonth = selection.replacedPastMonth ?: return base
        return if (selection.isNextYear) {
            "올해 가장 강하게 조심할 달인 ${passedMonth}월은 이미 지났고, 올해 남은 구간에는 같은 결이 약하게 지나갑니다. 그래서 다음 해 ${selection.month}월을 다음 주의 구간으로 봅니다. $base"
        } else {
            "올해 가장 강하게 조심할 달인 ${passedMonth}월은 이미 지났으니, 지금 이후에는 ${monthText}을 다음 주의 구간으로 보세요. $base"
        }
    }

    private fun sortBooks(books: List<FortuneBook>): List<FortuneBook> {
        return books.sortedWith(
            compareByDescending<FortuneBook> { it.lastOpenedAt ?: it.createdAt }
                .thenByDescending { it.createdAt }
        )
    }

    private fun AppUiState.selectedPersonalBook(): FortuneBook? {
        return savedBooks.firstOrNull { it.bookId == selectedBookId && it.bookType == FortuneBookType.PERSONAL }
            ?: savedBooks.firstOrNull { it.bookType == FortuneBookType.PERSONAL }
    }

    private fun AppUiState.selectedCompatibilityBook(): FortuneBook? {
        return savedBooks.firstOrNull { it.bookId == selectedBookId && it.bookType == FortuneBookType.COMPATIBILITY }
            ?: savedBooks.firstOrNull { it.bookType == FortuneBookType.COMPATIBILITY }
    }

    private fun buildEssentialQuestion(topic: PremiumTopic, concern: String): String {
        val firstSentence = concern
            .split(".", "?", "!", "\n")
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            ?: concern
        val shortened = firstSentence.take(72).trim()
        val topicHint = when (topic) {
            PremiumTopic.ROMANCE -> "연애에서"
            PremiumTopic.CAREER -> "일과 진로에서"
            PremiumTopic.MONEY -> "돈의 흐름에서"
            PremiumTopic.SELF_ESTEEM -> "나 자신을 대하는 방식에서"
            PremiumTopic.RELATIONSHIP -> "인간관계에서"
        }
        val sentence = if (shortened.endsWith("?")) shortened.dropLast(1) else shortened
        return "$topicHint 내가 지금 가장 조정해야 할 핵심은 '$sentence'가 맞나요?"
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
