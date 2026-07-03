package com.example.unum.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unum.data.model.AuthState
import com.example.unum.BuildConfig
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.CompatibilityFormState
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.DailyFortuneResult
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    private val buildDailyFortune = ServiceLocator.buildDailyFortuneUseCase
    private val buildFortuneBook = ServiceLocator.buildFortuneBookUseCase
    private val buildSuriSpeechScript = ServiceLocator.buildSuriSpeechScriptUseCase
    private val buildPremiumDummyConsultation = ServiceLocator.buildPremiumDummyConsultationUseCase
    private val generatePremiumConsultation = ServiceLocator.generatePremiumConsultationUseCase
    private val generateCompatibilityConsultation = ServiceLocator.generateCompatibilityConsultationUseCase
    private val premiumAccessGate = ServiceLocator.premiumAccessGate
    private val fortuneBookStore = ServiceLocator.fortuneBookStore
    private val readerSettingsStore = ServiceLocator.readerSettingsStore
    private val userPreferencesStore = ServiceLocator.userPreferencesStore
    private val authRepository = ServiceLocator.authRepository
    private val userDataRepository = ServiceLocator.userDataRepository

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun dailyFortune(date: LocalDate = LocalDate.now()): DailyFortuneResult? {
        return _uiState.value.latestBundle?.numbers?.let { numbers ->
            buildDailyFortune(numbers, date)
        }
    }

    init {
        val savedBooks = sortBooks(fortuneBookStore.loadBooks())
        val savedForm = userPreferencesStore.loadBirthFormState()
        _uiState.update {
            it.copy(
                formState = savedForm ?: it.formState,
                savedBooks = savedBooks,
                selectedBookId = savedBooks.firstOrNull()?.bookId,
                notificationsEnabled = userPreferencesStore.loadNotificationsEnabled(),
                notificationOnboardingSeen = userPreferencesStore.loadNotificationOnboardingSeen(),
                readerFontScale = readerSettingsStore.loadFontScale()
            )
        }
        observeRecentSearches()
        observeAuthState()
        savedForm?.let(::restoreSavedBirthResult)
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
                            userSyncState = UserSyncState.Failed(error.message ?: "카카오 로그인에 실패했습니다."),
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
        readerSettingsStore.saveFontScale(scale)
        _uiState.update { it.copy(readerFontScale = scale) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        userPreferencesStore.saveNotificationsEnabled(enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun completeNotificationOnboarding(enabled: Boolean) {
        userPreferencesStore.saveNotificationsEnabled(enabled)
        userPreferencesStore.saveNotificationOnboardingSeen(true)
        _uiState.update {
            it.copy(
                notificationsEnabled = enabled,
                notificationOnboardingSeen = true
            )
        }
    }

    private fun updateForm(block: HomeFormState.() -> HomeFormState) {
        val nextForm = _uiState.value.formState.block()
        userPreferencesStore.saveBirthFormState(nextForm)
        _uiState.update { current -> current.copy(formState = nextForm, inputError = null) }
    }

    private fun updateCompatibilityForm(block: CompatibilityFormState.() -> CompatibilityFormState) {
        _uiState.update { current ->
            current.copy(
                compatibilityForm = current.compatibilityForm.block(),
                inputError = null
            )
        }
    }

    private fun updateCompatibilityPartner(block: PartnerBirthFormState.() -> PartnerBirthFormState) = updateCompatibilityForm {
        copy(partner = partner.block())
    }

    private fun restoreSavedBirthResult(formState: HomeFormState) {
        val userBirthInput = NumerologyCalculator.toBirthInput(formState) ?: return
        viewModelScope.launch {
            runCatching { buildNumerologyResultBundle(userBirthInput) }
                .onSuccess { bundle ->
                    _uiState.update { it.copy(latestBundle = bundle) }
                }
        }
    }

    fun calculateAndStore(isInitial: Boolean = false, onSuccess: (() -> Unit)? = null) {
        val userBirthInput = NumerologyCalculator.toBirthInput(_uiState.value.formState)
        if (userBirthInput == null) {
            _uiState.update { it.copy(inputError = "생년월일을 다시 확인해 주세요. 예: 1999 / 03 / 13") }
            return
        }
        userPreferencesStore.saveBirthFormState(_uiState.value.formState)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, inputError = null) }
            runCatching {
                val bundle = buildNumerologyResultBundle(userBirthInput)
                delay(900)
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
                        inputError = error.message ?: "리포트를 불러오지 못했어요. 잠시 후 다시 시도해 주세요."
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
        _uiState.update {
            it.copy(
                premiumMode = mode,
                premiumEssentialQuestion = "",
                inputError = null
            )
        }
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
            _uiState.update { it.copy(inputError = "먼저 생년월일을 입력해 기본 결과를 만들어 주세요.") }
            return false
        }

        if (current.premiumMode == PremiumMode.COMPATIBILITY) {
            val partner = current.compatibilityForm.partner
            if (partner.year.length != 4 || partner.month.isBlank() || partner.day.isBlank()) {
                _uiState.update { it.copy(inputError = "상대방의 생년월일을 모두 입력해 주세요.") }
                return false
            }
        }

        val originalConcern = if (current.premiumMode == PremiumMode.COMPATIBILITY) {
            current.compatibilityConcern
        } else {
            current.premiumConcern
        }
        val normalizedConcern = originalConcern.trim().replace(Regex("\\s+"), " ")
        if (normalizedConcern.length < 6) {
            _uiState.update { it.copy(inputError = "AI가 질문을 명확하게 이해할 수 있도록 고민을 6자 이상 적어주세요.") }
            return false
        }

        val essentialQuestion = if (current.premiumMode == PremiumMode.COMPATIBILITY) {
            buildCompatibilityEssentialQuestion(
                status = current.compatibilityForm.relationshipStatus,
                concern = normalizedConcern
            )
        } else {
            buildEssentialQuestion(current.premiumTopic, normalizedConcern)
        }
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
        _uiState.update {
            it.copy(
                compatibilityConcern = value,
                premiumEssentialQuestion = "",
                inputError = null
            )
        }
    }

    fun setCompatibilityRelationshipStatus(status: CompatibilityRelationshipStatus) = updateCompatibilityForm {
        copy(relationshipStatus = status)
    }

    fun setCompatibilityPartnerGender(gender: GenderOption) = updateCompatibilityForm {
        copy(partnerGender = if (gender == GenderOption.NONE) GenderOption.FEMALE else gender)
    }

    fun setCompatibilityPartnerCalendarType(type: CalendarType) = updateCompatibilityPartner { copy(calendarType = type) }

    fun updateCompatibilityPartnerYear(value: String) = updateCompatibilityPartner { copy(year = value.filter(Char::isDigit).take(4)) }

    fun updateCompatibilityPartnerMonth(value: String) = updateCompatibilityPartner { copy(month = value.filter(Char::isDigit).take(2)) }

    fun updateCompatibilityPartnerDay(value: String) = updateCompatibilityPartner { copy(day = value.filter(Char::isDigit).take(2)) }

    fun clearBirthInput() {
        userPreferencesStore.clearBirthFormState()
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
                        inputError = error.message ?: "운세노트를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
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
        val confirmedConcern = current.premiumEssentialQuestion.ifBlank { current.compatibilityConcern }
        val myInput = current.latestBundle?.displayInput
        val partnerInput = current.compatibilityForm.partner.toBirthInput(current.compatibilityForm.partnerGender)

        if (myInput == null || partnerInput == null) {
            _uiState.update { it.copy(inputError = "내 생년월일 저장 여부와 상대방 생년월일을 다시 확인해 주세요.") }
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
                val myBundle = current.latestBundle ?: buildNumerologyResultBundle(myInput)
                val partnerBundle = buildNumerologyResultBundle(partnerInput)
                val maleBundle = if (myInput.gender == GenderOption.MALE) myBundle else partnerBundle
                val femaleBundle = if (myInput.gender == GenderOption.MALE) partnerBundle else myBundle
                val consultation = generateCompatibilityConsultation(
                    apiKey = BuildConfig.OPENAI_API_KEY,
                    maleBundle = maleBundle,
                    femaleBundle = femaleBundle,
                    concern = confirmedConcern,
                    relationshipStatus = current.compatibilityForm.relationshipStatus
                )
                Triple(maleBundle, femaleBundle, consultation)
            }.onSuccess { (maleBundle, femaleBundle, consultation) ->
                val book = buildFortuneBook.buildCompatibilityBook(
                    consultation = consultation,
                    maleBundle = maleBundle,
                    femaleBundle = femaleBundle,
                    concern = confirmedConcern,
                    relationshipStatus = current.compatibilityForm.relationshipStatus
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
                        inputError = error.message ?: "궁합노트를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
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
                it.copy(userSyncState = UserSyncState.Synced("로그인되었습니다."))
            }
            return
        }

        _uiState.update { it.copy(userSyncState = UserSyncState.Syncing) }
        runCatching {
            val user = (_uiState.value.authState as? AuthState.SignedIn)?.user ?: return
            val localBooks = _uiState.value.savedBooks
            userDataRepository.prepareUser(user)
            val remoteBooks = sortBooks(userDataRepository.loadBooks(userId).map {
                refreshStoredMonthInsights(it.copy(userId = userId))
            })
            val mergedBooks = mergeBooks(localBooks, remoteBooks).map { it.copy(userId = userId) }
            fortuneBookStore.saveBooks(mergedBooks)
            userDataRepository.saveBooks(userId, mergedBooks)
            mergedBooks
        }.onSuccess { books ->
            _uiState.update {
                it.copy(
                    savedBooks = books,
                    selectedBookId = books.firstOrNull()?.bookId,
                    userSyncState = UserSyncState.Synced("로그인되었습니다.")
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(userSyncState = UserSyncState.Failed(error.message ?: "계정 동기화에 실패했습니다."))
            }
        }
    }

    private fun pushBooksForCurrentUser(books: List<FortuneBook>) {
        val userId = (_uiState.value.authState as? AuthState.SignedIn)?.user?.id ?: return
        if (!userDataRepository.isRemoteConfigured) return
        viewModelScope.launch {
            runCatching {
                userDataRepository.saveBooks(userId, books.map { it.copy(userId = userId) })
            }.onSuccess {
                _uiState.update { it.copy(userSyncState = UserSyncState.Synced("로그인되었습니다.")) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(userSyncState = UserSyncState.Failed(error.message ?: "책자 동기화에 실패했습니다."))
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
            PremiumTopic.ROMANCE -> "${monthText}에는 마음을 편안하게 열고 관계의 온도를 다시 맞추기 좋습니다. 부담스러운 확인보다 구체적인 만남 제안이 자연스럽게 이어집니다."
            PremiumTopic.CAREER -> "${monthText}에는 준비한 내용을 지원, 제안, 면접으로 옮기기 좋습니다. 조건과 역할을 선명하게 정리하면 기회가 더 분명해집니다."
            PremiumTopic.MONEY -> "${monthText}에는 수입과 지출 구조를 다시 세우기 좋습니다. 큰 결정보다 기준을 세우는 행동이 돈의 흐름을 안정시킵니다."
            PremiumTopic.STUDY -> "${monthText}에는 집중 범위를 정리하고 취약 영역을 보완하기 좋습니다. 반복 학습이 시험과 과제의 실수를 줄여줍니다."
            PremiumTopic.HEALTH -> "${monthText}에는 수면과 활동 리듬을 회복하기 좋습니다. 무리한 변화보다 꾸준한 생활 시간이 컨디션을 안정시킵니다."
            PremiumTopic.BUSINESS -> "${monthText}에는 거래 조건과 수익 구조를 점검하기 좋습니다. 제안과 계약의 책임 범위를 분명히 하면 기회가 안정적으로 이어집니다."
            PremiumTopic.GENERAL -> "${monthText}에는 일, 돈, 관계의 우선순위가 선명해집니다. 가장 영향이 큰 한 영역에 힘을 모으기 좋습니다."
            PremiumTopic.SELF_ESTEEM -> "${monthText}에는 스스로를 다시 세우는 힘이 살아납니다. 작은 약속을 지키는 경험이 마음의 중심을 단단하게 합니다."
            PremiumTopic.RELATIONSHIP -> "${monthText}에는 사람들과의 접점이 자연스럽게 열립니다. 미뤄둔 대화나 관계 회복을 부드럽게 시작하기 좋습니다."
        }
        val passedMonth = selection.replacedPastMonth ?: return base
        return "올해 가장 추천 흐름이 강했던 ${passedMonth}월은 이미 지났습니다. 지금 이후에는 ${monthText}을 다음 추천 구간으로 보세요. $base"
    }

    private fun buildStoredRiskyMonthReason(
        topic: PremiumTopic,
        selection: PremiumMonthPlanner.MonthSelection
    ): String {
        val monthText = selection.toDisplayText()
        val base = when (topic) {
            PremiumTopic.ROMANCE -> "${monthText}에는 마음이 앞서 결론을 재촉하기 쉽습니다. 상대의 반응 속도와 여백을 함께 살피는 편이 안전합니다."
            PremiumTopic.CAREER -> "${monthText}에는 변화 욕구가 커져 성급한 결정을 내리기 쉽습니다. 큰 선택은 조건을 다시 확인한 뒤 움직이는 편이 안전합니다."
            PremiumTopic.MONEY -> "${monthText}에는 빠른 이익을 좇고 싶은 마음이 커질 수 있습니다. 확인되지 않은 제안과 충동 지출은 거리를 두는 편이 안전합니다."
            PremiumTopic.STUDY -> "${monthText}에는 불안 때문에 계획만 늘리거나 밤샘으로 밀어붙이기 쉽습니다. 범위를 줄이고 수면을 지키는 편이 안전합니다."
            PremiumTopic.HEALTH -> "${monthText}에는 피로 신호를 무시하기 쉽습니다. 불편함이 지속되면 운세보다 의료진의 진료를 우선하세요."
            PremiumTopic.BUSINESS -> "${monthText}에는 확장 욕구가 커져 비용과 계약 조건을 낙관적으로 보기 쉽습니다. 문서와 숫자를 확인하기 전에는 큰 결정을 미루는 편이 안전합니다."
            PremiumTopic.GENERAL -> "${monthText}에는 여러 문제를 한 번에 해결하려다 판단이 흐려질 수 있습니다. 큰 결정을 겹치지 않는 편이 안전합니다."
            PremiumTopic.SELF_ESTEEM -> "${monthText}에는 비교와 조급함이 커지기 쉽습니다. 몸과 마음의 리듬을 먼저 회복하는 데 집중하세요."
            PremiumTopic.RELATIONSHIP -> "${monthText}에는 오해가 빠르게 번질 수 있습니다. 중요한 대화는 단정하지 말고 시간을 두는 편이 좋습니다."
        }
        val passedMonth = selection.replacedPastMonth ?: return base
        return if (selection.isNextYear) {
            "올해 가장 강하게 조심할 달인 ${passedMonth}월은 이미 지났습니다. 다음 해 ${selection.month}월을 다음 주의 구간으로 봅니다. $base"
        } else {
            "올해 가장 강하게 조심할 달인 ${passedMonth}월은 이미 지났습니다. 지금 이후에는 ${monthText}을 다음 주의 구간으로 보세요. $base"
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
        val shortened = concernForQuestion(concern)
        val topicHint = when (topic) {
            PremiumTopic.ROMANCE -> "연애"
            PremiumTopic.CAREER -> "일과 진로"
            PremiumTopic.MONEY -> "재물"
            PremiumTopic.STUDY -> "학업과 시험"
            PremiumTopic.HEALTH -> "건강과 생활 리듬"
            PremiumTopic.BUSINESS -> "사업과 계약"
            PremiumTopic.GENERAL -> "이번 달 종합운"
            PremiumTopic.SELF_ESTEEM -> "마음과 자존감"
            PremiumTopic.RELATIONSHIP -> "인간관계"
        }
        return "$topicHint 상담에서 '$shortened' 상황을 중심으로 현재 흐름과 주의점, 현실적인 대응 방법은 무엇인가요?"
    }

    private fun buildCompatibilityEssentialQuestion(
        status: CompatibilityRelationshipStatus,
        concern: String
    ): String {
        val shortened = concernForQuestion(concern)
        return when (status) {
            CompatibilityRelationshipStatus.COUPLE ->
                "현재 연인 관계에서 '$shortened' 상황을 어떻게 해석하고 조율하면 좋을까요?"
            CompatibilityRelationshipStatus.CRUSH ->
                "짝사랑 관계에서 '$shortened' 상황을 어떻게 해석하고, 부담 없이 관계를 발전시키려면 어떻게 해야 할까요?"
            CompatibilityRelationshipStatus.REUNION ->
                "재회를 고민하는 관계에서 '$shortened' 상황을 어떻게 해석하고, 다시 연락하기 전에 무엇을 살펴야 할까요?"
        }
    }

    private fun concernForQuestion(concern: String): String {
        val firstSentence = concern
            .split(Regex("[.!?。！？\\n]+"))
            .map(String::trim)
            .firstOrNull(String::isNotBlank)
            ?: concern.trim()
        return firstSentence.take(90).trim().trim('"', '\'', '“', '”')
    }

    private fun CompatibilityFormState.hasAnyInput(): Boolean {
        return partner.year.isNotBlank() ||
            partner.month.isNotBlank() ||
            partner.day.isNotBlank()
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
