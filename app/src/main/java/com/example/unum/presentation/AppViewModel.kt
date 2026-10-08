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
import com.example.unum.data.model.PartnerBirthFormState
import com.example.unum.data.model.PremiumMode
import com.example.unum.data.model.PremiumTopic
import com.example.unum.data.model.ReaderFontScale
import com.example.unum.data.model.RecentSearch
import com.example.unum.data.model.SuriSpeechScript
import com.example.unum.data.model.UserSyncState
import com.example.unum.domain.NumerologyCalculator
import com.example.unum.di.AppContainer
import com.example.unum.domain.books.FortuneBookPolicy
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
    // Dependencies are composed centrally; screens coordinate UI state only.
    private val repository = AppContainer.numerologyRepository
    private val freeFortune by lazy { AppContainer.freeFortuneEngine }
    private val buildFortuneBook = AppContainer.buildFortuneBookUseCase
    private val buildSuriSpeechScript = AppContainer.buildSuriSpeechScriptUseCase
    private val buildPremiumDummyConsultation = AppContainer.buildPremiumDummyConsultationUseCase
    private val aiConsultation = AppContainer.aiConsultationService
    private val premiumAccessGate = AppContainer.premiumAccessGate
    private val fortuneBookRepository = AppContainer.fortuneBookRepository
    private val readerSettingsStore = AppContainer.readerSettingsStore
    private val userPreferencesStore = AppContainer.userPreferencesStore
    private val authRepository = AppContainer.authRepository
    private val userDataRepository = AppContainer.userDataRepository

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val discoveryRepository = AppContainer.discoveryRepository
    private val _discovery = MutableStateFlow(discoveryRepository.cached(null))
    val discovery: StateFlow<com.example.unum.data.repository.DiscoveryState> = _discovery.asStateFlow()
    fun refreshDiscovery() = runDiscovery { discoveryRepository.refresh(it) }
    fun drawTarot(theme: com.example.unum.data.content.TarotTheme) = runDiscovery { discoveryRepository.draw(it, theme) }
    fun unlockTarot() = runDiscovery { discoveryRepository.unlock(it) }
    fun claimAttendance() = runDiscovery { discoveryRepository.attendance(it) }
    private fun runDiscovery(action: suspend (com.example.unum.data.model.AuthUser?) -> com.example.unum.data.repository.DiscoveryState) {
        if(_discovery.value.busy) return
        val user=(_uiState.value.authState as? AuthState.SignedIn)?.user
        _discovery.update { it.copy(busy=true,message=null) }
        viewModelScope.launch {
            val result=runCatching { action(user) }
            if(((_uiState.value.authState as? AuthState.SignedIn)?.user?.id) != user?.id) return@launch
            result.onSuccess { _discovery.value=it.copy(busy=false) }.onFailure {
                _discovery.update { s -> s.copy(busy=false,message="연결을 확인한 뒤 다시 시도해주세요. 기존 카드는 유지돼요.") }
            }
        }
    }

    fun dailyFortune(date: LocalDate = com.example.unum.data.content.TarotCatalog.today()): DailyFortuneResult? {
        return _uiState.value.latestBundle?.numbers?.let { numbers ->
            freeFortune.daily(numbers, date)
        }
    }

    init {
        val savedBooks = FortuneBookPolicy.sortBooks(fortuneBookRepository.loadBooks())
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
        viewModelScope.launch {
            while(true) {
                delay(30_000)
                val today=com.example.unum.data.content.TarotCatalog.today()
                if(_uiState.value.today!=today) {
                    _uiState.update { it.copy(today=today) }
                    refreshDiscovery()
                }
            }
        }
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
                _discovery.value=discoveryRepository.cached(user)
                if (user != null) {
                    syncSignedInUser(user.id)
                    refreshDiscovery()
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
            fortuneBookRepository.saveBooks(emptyList())
            repository.clearRecentSearches()
            userPreferencesStore.clearBirthFormState()
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
            runCatching { freeFortune.bundle(userBirthInput) }
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
                val bundle = freeFortune.bundle(userBirthInput)
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
                    aiConsultation.personal(
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
                val myBundle = current.latestBundle ?: freeFortune.bundle(myInput)
                val partnerBundle = freeFortune.bundle(partnerInput)
                val maleBundle = if (myInput.gender == GenderOption.MALE) myBundle else partnerBundle
                val femaleBundle = if (myInput.gender == GenderOption.MALE) partnerBundle else myBundle
                val consultation = aiConsultation.compatibility(
                    apiKey = BuildConfig.OPENAI_API_KEY,
                    maleBundle = maleBundle,
                    femaleBundle = femaleBundle,
                    concern = confirmedConcern,
                    relationshipStatus = current.compatibilityForm.relationshipStatus,
                    requesterIsPersonA = myInput.gender == GenderOption.MALE
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
        val nextBooks = FortuneBookPolicy.sortBooks(
            _uiState.value.savedBooks.map { saved ->
                if (saved.bookId == book.bookId) saved.copy(lastOpenedAt = now) else saved
            }
        )
        fortuneBookRepository.saveBooks(nextBooks)
        _uiState.update { it.copy(savedBooks = nextBooks, selectedBookId = book.bookId) }
    }

    fun selectSavedBookById(bookId: String) {
        _uiState.value.savedBooks.firstOrNull { it.bookId == bookId }?.let(::selectSavedBook)
    }

    fun toggleBookmark(book: FortuneBook) {
        val nextBooks = FortuneBookPolicy.sortBooks(
            _uiState.value.savedBooks.map { saved ->
                if (saved.bookId == book.bookId) saved.copy(isBookmarked = !saved.isBookmarked) else saved
            }
        )
        fortuneBookRepository.saveBooks(nextBooks)
        _uiState.update { it.copy(savedBooks = nextBooks) }
        pushBooksForCurrentUser(nextBooks)
    }

    fun deleteSavedBook(book: FortuneBook) {
        val current = _uiState.value
        val nextBooks = FortuneBookPolicy.sortBooks(current.savedBooks.filterNot { it.bookId == book.bookId })
        val nextSelectedBookId = if (current.selectedBookId == book.bookId) {
            nextBooks.firstOrNull()?.bookId
        } else {
            current.selectedBookId
        }

        fortuneBookRepository.saveBooks(nextBooks)
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
                fortuneBookRepository.deleteRemoteBook(userId, book.bookId)
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
        val nextBooks = FortuneBookPolicy.sortBooks(listOf(ownedBook) + _uiState.value.savedBooks)
        fortuneBookRepository.saveBooks(nextBooks)
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
            fortuneBookRepository.synchronize(user, _uiState.value.savedBooks)
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
                fortuneBookRepository.pushBooks(userId, books)
            }.onSuccess {
                _uiState.update { it.copy(userSyncState = UserSyncState.Synced("로그인되었습니다.")) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(userSyncState = UserSyncState.Failed(error.message ?: "책자 동기화에 실패했습니다."))
                }
            }
        }
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
