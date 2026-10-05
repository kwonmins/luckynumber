package com.example.unum.di

import com.example.unum.domain.PremiumAccessGate

import android.content.Context
import com.example.unum.data.remote.ai.OpenAiChatClient
import com.example.unum.domain.service.JsonChatClient
import com.example.unum.BuildConfig
import com.example.unum.data.repository.FortuneBookStore
import com.example.unum.data.repository.books.FortuneBookRepository
import com.example.unum.data.repository.books.DefaultFortuneBookRepository
import com.example.unum.data.repository.LocalAssetNumerologyRepository
import com.example.unum.data.repository.NumerologyRepository
import com.example.unum.data.repository.ReaderSettingsStore
import com.example.unum.data.repository.StarWalletStore
import com.example.unum.data.repository.UserPreferencesStore
import com.example.unum.data.repository.auth.AuthRepository
import com.example.unum.data.repository.auth.SocialAuthRepository
import com.example.unum.data.repository.user.SupabaseRestUserDatabase
import com.example.unum.data.repository.user.UserDataRepository
import com.example.unum.domain.usecase.BuildPremiumDummyConsultationUseCase
import com.example.unum.domain.usecase.BuildFortuneBookUseCase
import com.example.unum.domain.usecase.BuildDailyFortuneUseCase
import com.example.unum.domain.usecase.BuildNumerologyResultBundleUseCase
import com.example.unum.domain.usecase.BuildSuriSpeechScriptUseCase
import com.example.unum.domain.usecase.CalculateNumerologyUseCase
import com.example.unum.domain.usecase.GenerateCompatibilityConsultationUseCase
import com.example.unum.domain.usecase.GeneratePremiumConsultationUseCase

object AppContainer {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val numerologyRepository: NumerologyRepository by lazy {
        check(::appContext.isInitialized) { "AppContainer.init(context) must be called first." }
        LocalAssetNumerologyRepository(appContext)
    }

    val fortuneBookStore: FortuneBookStore by lazy {
        check(::appContext.isInitialized) { "AppContainer.init(context) must be called first." }
        FortuneBookStore(appContext)
    }

    val fortuneBookRepository: FortuneBookRepository by lazy {
        DefaultFortuneBookRepository(fortuneBookStore, userDataRepository)
    }

    val readerSettingsStore: ReaderSettingsStore by lazy {
        check(::appContext.isInitialized) { "AppContainer.init(context) must be called first." }
        ReaderSettingsStore(appContext)
    }

    val starWalletStore: StarWalletStore by lazy {
        check(::appContext.isInitialized) { "AppContainer.init(context) must be called first." }
        StarWalletStore(appContext)
    }

    val userPreferencesStore: UserPreferencesStore by lazy {
        check(::appContext.isInitialized) { "AppContainer.init(context) must be called first." }
        UserPreferencesStore(appContext)
    }

    val authRepository: AuthRepository by lazy {
        check(::appContext.isInitialized) { "AppContainer.init(context) must be called first." }
        SocialAuthRepository(appContext)
    }

    val userDataRepository: UserDataRepository by lazy {
        UserDataRepository(
            SupabaseRestUserDatabase(
                context = appContext,
                supabaseUrl = BuildConfig.SUPABASE_URL,
                anonKey = BuildConfig.SUPABASE_ANON_KEY
            )
        )
    }

    val calculateNumerologyUseCase: CalculateNumerologyUseCase by lazy { CalculateNumerologyUseCase() }
    val buildNumerologyResultBundleUseCase: BuildNumerologyResultBundleUseCase by lazy {
        BuildNumerologyResultBundleUseCase(numerologyRepository, calculateNumerologyUseCase)
    }
    val buildDailyFortuneUseCase: BuildDailyFortuneUseCase by lazy { BuildDailyFortuneUseCase() }
    val buildFortuneBookUseCase: BuildFortuneBookUseCase by lazy { BuildFortuneBookUseCase() }
    val buildSuriSpeechScriptUseCase: BuildSuriSpeechScriptUseCase by lazy { BuildSuriSpeechScriptUseCase() }
    val buildPremiumDummyConsultationUseCase: BuildPremiumDummyConsultationUseCase by lazy { BuildPremiumDummyConsultationUseCase() }
    private val chatClient: JsonChatClient by lazy { OpenAiChatClient() }
    val generatePremiumConsultationUseCase: GeneratePremiumConsultationUseCase by lazy { GeneratePremiumConsultationUseCase(chatClient) }
    val generateCompatibilityConsultationUseCase: GenerateCompatibilityConsultationUseCase by lazy { GenerateCompatibilityConsultationUseCase(chatClient) }
    val premiumAccessGate: PremiumAccessGate by lazy { PremiumAccessGate() }
}
