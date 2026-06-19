package com.example.unum.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unum.data.model.AuthState
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
import com.example.unum.data.model.UserSyncState
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
    private val authRepository = ServiceLocator.authRepository
    private val userDataRepository = ServiceLocator.userDataRepository

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        fortuneBookStore.saveBooks(emptyList())
        _uiState.update { it.copy(savedBooks = emptyList(), selectedBookId = null) }
        observeRecentSearches()
        observeAuthState()
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            repository.observeRecentSearches().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.update { it.copy(authState = authState) }
                val user = (authState as? AuthState.SignedIn)?.user
                if (user != null) {
                    syncSignedInUser(user.id)
                }
            }
        }
    }

    fun signInWithKakao(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(userSyncState = UserSyncState.Syncing, inputError = null) }
            authRepository.signInWithKakao(activity)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            userSyncState = UserSyncState.Failed(error.message ?: "移댁뭅??濡쒓렇?몄뿉 ?ㅽ뙣?덉뒿?덈떎."),
                            inputError = error.message
                        )
                    }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            userDataRepository.clearLocalSession()
            fortuneBookStore.saveBooks(emptyList())
            _uiState.value = AppUiState(
                readerFontScale = _uiState.value.readerFontScale,
                notificationsEnabled = _uiState.value.notificationsEnabled,
                authState = AuthState.SignedOut
            )
        }
    }

    fun syncCurrentUserBooks() {
        val userId = (_uiState.value.authState as? AuthState.SignedIn)?.user?.id ?: return
        viewModelScope.launch { syncSignedInUser(userId) }
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
            _uiState.update { it.copy(inputError = "?앸뀈?붿씪???ㅼ떆 ?뺤씤?댁＜?몄슂.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, inputError = null) }
            runCatching {
                val bundle = buildNumerologyResultBundle(userBirthInput)
                val genderPrefix = when (userBirthInput.gender) {
                    GenderOption.MALE -> "?⑥꽦 쨌 "
                    GenderOption.FEMALE -> "?ъ꽦 쨌 "
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
                        subtitle = "${genderPrefix}?대챸??${numbers.destiny} 쨌 肄붾뱶 ${numbers.code}",
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
                        inputError = error.message ?: "寃곌낵瑜?遺덈윭?ㅼ? 紐삵뻽?듬땲??"
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
            _uiState.update { it.copy(inputError = "癒쇱? ?앸뀈?붿씪 寃곌낵瑜?留뚮뱺 ???꾨━誘몄뾼 梨낆옄瑜??좎껌??二쇱꽭??") }
            return false
        }

        val normalizedConcern = current.premiumConcern.trim().replace(Regex("\\s+"), " ")
        if (normalizedConcern.length < 6) {
            _uiState.update { it.copy(inputError = "?섎━媛 吏덈Ц??蹂몄쭏???≪쓣 ???덈룄濡?怨좊?????臾몄옣 ?댁긽 ?곸뼱二쇱꽭??") }
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
            _uiState.update { it.copy(inputError = "?꾨━誘몄뾼 ?댁슜 沅뚰븳???뺤씤?????놁뒿?덈떎.") }
            return
        }

        viewModelScope.launch {
            val current = _uiState.value
            val confirmedConcern = current.premiumEssentialQuestion.ifBlank { current.premiumConcern }
            _uiState.update {
                it.copy(
                    isPremiumLoading = true,
                    premiumFlowStep = PremiumFlowStep.LOADING,
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
                        inputError = error.message ?: "?댁꽭?명듃瑜?遺덈윭?ㅼ? 紐삵뻽?듬땲??"
                    )
                }
            }
        }
    }

    fun runCompatibilityConsultation() {
        if (!premiumAccessGate.canUsePremiumForTest()) {
            _uiState.update { it.copy(inputError = "?꾨━誘몄뾼 ?댁슜 沅뚰븳???뺤씤?????놁뒿?덈떎.") }
            return
        }

        val current = _uiState.value
        val maleInput = current.compatibilityForm.male.toBirthInput(GenderOption.MALE)
        val femaleInput = current.compatibilityForm.female.toBirthInput(GenderOption.FEMALE)

        if (maleInput == null || femaleInput == null) {
            _uiState.update { it.copy(inputError = "?⑥옄? ?ъ옄 ?앸뀈?붿씪???ㅼ떆 ?뺤씤?댁＜?몄슂.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPremiumLoading = true,
                    premiumFlowStep = PremiumFlowStep.LOADING,
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
                        premiumFlowStep = PremiumFlowStep.FORM,
                        inputError = error.message ?: "沅곹빀?명듃瑜?遺덈윭?ㅼ? 紐삵뻽?듬땲??"
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
        pushBooksForCurrentUser(nextBooks)
    }

    fun deleteSavedBook(book: FortuneBook) {
        val current = _uiState.value
        val nextBooks = sortBooks(current.savedBooks.filterNot { it.bookId == book.bookId })
        val nextSelectedBookId = if (current.selectedBookId == book.bookId) {
            nextBooks.firstOrNull()?.bookId
        } else {
            current.selectedBookId
        }

        fortuneBookStore.saveBooks(nextBooks)
        _uiState.update {
            it.copy(
                savedBooks = nextBooks,
                selectedBookId = nextSelectedBookId,
                userSyncState = UserSyncState.Synced("책자를 삭제했습니다.")
            )
        }

        val userId = (current.authState as? AuthState.SignedIn)?.user?.id ?: return
        if (!userDataRepository.isRemoteConfigured) return
        viewModelScope.launch {
            runCatching {
                userDataRepository.deleteBook(userId, book.bookId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(userSyncState = UserSyncState.Failed(error.message ?: "책자 삭제 동기화에 실패했습니다."))
                }
            }
        }
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
        val userId = (_uiState.value.authState as? AuthState.SignedIn)?.user?.id
        val ownedBook = if (userId == null) book else book.copy(userId = userId)
        val nextBooks = sortBooks(listOf(ownedBook) + _uiState.value.savedBooks)
        fortuneBookStore.saveBooks(nextBooks)
        pushBooksForCurrentUser(nextBooks)
        return nextBooks
    }

    private suspend fun syncSignedInUser(userId: String) {
        if (!userDataRepository.isRemoteConfigured) {
            _uiState.update {
                it.copy(userSyncState = UserSyncState.Synced("濡쒓렇?몃맖. Supabase ?ㅼ젙??異붽??섎㈃ 梨낆옄媛 怨꾩젙蹂꾨줈 ?숆린?붾맗?덈떎."))
            }
            return
        }

        _uiState.update { it.copy(userSyncState = UserSyncState.Syncing) }
        runCatching {
            val user = (_uiState.value.authState as? AuthState.SignedIn)?.user ?: return
            userDataRepository.prepareUser(user)
            val remoteBooks = sortBooks(userDataRepository.loadBooks(userId).map {
                refreshStoredMonthInsights(it.copy(userId = userId))
            })
            fortuneBookStore.saveBooks(remoteBooks)
            remoteBooks
        }.onSuccess { books ->
            _uiState.update {
                it.copy(
                    savedBooks = books,
                    selectedBookId = books.firstOrNull()?.bookId,
                    userSyncState = UserSyncState.Synced("怨꾩젙 梨낆옄 ${books.size}媛쒕? ?숆린?뷀뻽?듬땲??")
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(userSyncState = UserSyncState.Failed(error.message ?: "怨꾩젙 ?숆린?붿뿉 ?ㅽ뙣?덉뒿?덈떎."))
            }
        }
    }

    private fun pushBooksForCurrentUser(books: List<FortuneBook>) {
        val userId = (_uiState.value.authState as? AuthState.SignedIn)?.user?.id ?: return
        if (!userDataRepository.isRemoteConfigured) return
        viewModelScope.launch {
            runCatching {
                userDataRepository.saveBooks(userId, books.map { it.copy(userId = userId) })
            }.onFailure { error ->
                _uiState.update {
                    it.copy(userSyncState = UserSyncState.Failed(error.message ?: "梨낆옄 ?숆린?붿뿉 ?ㅽ뙣?덉뒿?덈떎."))
                }
            }
        }
    }

    private fun mergeBooks(localBooks: List<FortuneBook>, remoteBooks: List<FortuneBook>): List<FortuneBook> {
        val merged = linkedMapOf<String, FortuneBook>()
        (remoteBooks + localBooks).forEach { book ->
            val current = merged[book.bookId]
            if (current == null || (book.lastOpenedAt ?: book.createdAt) >= (current.lastOpenedAt ?: current.createdAt)) {
                merged[book.bookId] = book
            }
        }
        return sortBooks(merged.values.toList())
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
            PremiumTopic.ROMANCE -> "${monthText}?먮뒗 留덉쓬???덈∼寃??닿퀬 愿怨꾩쓽 ?⑤룄瑜??ㅼ떆 留욎텛湲?醫뗭뒿?덈떎. 臾닿굅???뺤씤蹂대떎 援ъ껜?곸씤 留뚮궓 ?쒖븞???먮쫫??遺?쒕읇寃?留뚮벊?덈떎."
            PremiumTopic.CAREER -> "${monthText}?먮뒗 以鍮꾪븳 寃껋쓣 ?ㅼ젣 ?쒖븞, 吏?? 硫대떞?쇰줈 ??린湲?醫뗭뒿?덈떎. 議곌굔怨???븷???좊챸?섍쾶 ?뺣━?섎㈃ 湲고쉶媛 ??遺꾨챸?댁쭛?덈떎."
            PremiumTopic.MONEY -> "${monthText}?먮뒗 ?섏엯怨?吏異?援ъ“瑜??ㅼ떆 ?↔린 醫뗭뒿?덈떎. ???뺤떖蹂대떎 湲곗????몄슦???됰룞???덉쓽 ?먮쫫???덉젙?쒗궢?덈떎."
            PremiumTopic.SELF_ESTEEM -> "${monthText}?먮뒗 ?ㅼ뒪濡쒕? ?ㅼ떆 ?몄슦???섏씠 ?댁븘?⑸땲?? ?묒? ?쎌냽??吏?ㅻ뒗 寃쏀뿕??諛섎났?섎㈃ 留덉쓬??以묒떖???⑤떒?댁쭛?덈떎."
            PremiumTopic.RELATIONSHIP -> "${monthText}?먮뒗 ?щ엺?ㅺ낵???묒젏???먯뿰?ㅻ읇寃??대┰?덈떎. ?ㅻ옒 誘몃쨪????붾굹 愿怨??뚮났??遺?쒕읇寃??쒖옉?섍린 醫뗭뒿?덈떎."
        }
        val passedMonth = selection.replacedPastMonth ?: return base
        return "?ы빐 媛??異붿쿇 ?먮쫫??媛뺥뻽??${passedMonth}?붿? ?대? 吏?ъ뒿?덈떎. 吏湲??댄썑?먮뒗 ${monthText}??異붿쿇 援ш컙?쇰줈 蹂닿퀬 ?吏곸뿬蹂댁꽭?? $base"
    }

    private fun buildStoredRiskyMonthReason(
        topic: PremiumTopic,
        selection: PremiumMonthPlanner.MonthSelection
    ): String {
        val monthText = selection.toDisplayText()
        val base = when (topic) {
            PremiumTopic.ROMANCE -> "${monthText}?먮뒗 留덉쓬???욎꽌 寃곕줎???ъ큺?섍린 ?쎌뒿?덈떎. ?곷????띾룄? ?щ갚??媛곷퀎??議곗떖?섏꽭??"
            PremiumTopic.CAREER -> "${monthText}?먮뒗 蹂???뺢뎄媛 而ㅼ졇 ?깃툒??寃곗젙?쇰줈 ?먮Ⅴ湲??쎌뒿?덈떎. ???좏깮? ??踰???寃?좏븳 ???吏곸씠???몄씠 ?덉쟾?⑸땲??"
            PremiumTopic.MONEY -> "${monthText}?먮뒗 鍮좊Ⅸ ?댁씡??醫뉖뒗 留덉쓬??媛뺥빐吏????덉뒿?덈떎. ?뺤씤?섏? ?딆? ?쒖븞怨?異⑸룞 吏異쒖? 諛섎뱶??嫄곕━瑜??먯꽭??"
            PremiumTopic.SELF_ESTEEM -> "${monthText}?먮뒗 鍮꾧탳? 議곌툒?⑥씠 而ㅼ?湲??쎌뒿?덈떎. 紐멸낵 留덉쓬??由щ벉??癒쇱? ?뚮났?섎뒗 ??吏묒쨷?섏꽭??"
            PremiumTopic.RELATIONSHIP -> "${monthText}?먮뒗 ?щ엺 ?ъ씠???ㅽ빐媛 鍮⑤━ 踰덉쭏 ???덉뒿?덈떎. 以묒슂????붾뒗 李⑤텇???쒓컙???먮뒗 ?몄씠 醫뗭뒿?덈떎."
        }
        val passedMonth = selection.replacedPastMonth ?: return base
        return if (selection.isNextYear) {
            "?ы빐 媛??媛뺥븯寃?議곗떖???ъ씤 ${passedMonth}?붿? ?대? 吏?ш퀬, ?ы빐 ?⑥? 援ш컙?먮뒗 媛숈? 寃곗씠 ?쏀븯寃?吏?섍컩?덈떎. 洹몃옒???ㅼ쓬 ??${selection.month}?붿쓣 ?ㅼ쓬 二쇱쓽 援ш컙?쇰줈 遊낅땲?? $base"
        } else {
            "?ы빐 媛??媛뺥븯寃?議곗떖???ъ씤 ${passedMonth}?붿? ?대? 吏?ъ쑝?? 吏湲??댄썑?먮뒗 ${monthText}???ㅼ쓬 二쇱쓽 援ш컙?쇰줈 蹂댁꽭?? $base"
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
            PremiumTopic.ROMANCE -> "?곗븷?먯꽌"
            PremiumTopic.CAREER -> "?쇨낵 吏꾨줈?먯꽌"
            PremiumTopic.MONEY -> "?덉쓽 ?먮쫫?먯꽌"
            PremiumTopic.SELF_ESTEEM -> "???먯떊????섎뒗 諛⑹떇?먯꽌"
            PremiumTopic.RELATIONSHIP -> "인간관계에서"
        }
        val sentence = if (shortened.endsWith("?")) shortened.dropLast(1) else shortened
        return "$topicHint ?닿? 吏湲?媛??議곗젙?댁빞 ???듭떖? '$sentence'媛 留욌굹??"
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
