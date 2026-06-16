package com.example.unum.data.repository.auth

import android.app.Activity
import android.content.Context
import com.example.unum.BuildConfig
import com.example.unum.data.model.AuthProvider
import com.example.unum.data.model.AuthState
import com.example.unum.data.model.AuthUser
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class SocialAuthRepository(
    private val context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences("auth_session", Context.MODE_PRIVATE)
    private val _authState = MutableStateFlow(loadSession())
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun signInWithKakao(activity: Activity): Result<AuthUser> {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            return Result.failure(IllegalStateException("카카오 네이티브 앱 키가 local.properties에 없습니다."))
        }

        return runCatching {
            loginWithKakao(activity)
            val user = fetchKakaoUser()
            saveSession(user)
            user
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Result.failure(IllegalStateException(error.toKakaoLoginMessage(), error))
            }
        )
    }

    override suspend fun signOut() {
        runCatching {
            UserApiClient.instance.logout(callback = { })
        }
        prefs.edit().clear().apply()
        _authState.value = AuthState.SignedOut
    }

    private suspend fun loginWithKakao(activity: Activity): OAuthToken {
        return suspendCancellableCoroutine { continuation ->
            var resolved = false

            fun resumeSuccess(token: OAuthToken) {
                if (!resolved) {
                    resolved = true
                    continuation.resume(token)
                }
            }

            fun resumeFailure(error: Throwable) {
                if (!resolved) {
                    resolved = true
                    continuation.resumeWith(Result.failure(error))
                }
            }

            val accountCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                when {
                    token != null -> resumeSuccess(token)
                    error != null -> resumeFailure(error)
                    else -> resumeFailure(IllegalStateException("카카오 로그인 토큰을 받지 못했습니다."))
                }
            }

            val talkCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                when {
                    token != null -> resumeSuccess(token)
                    error != null && error.isUserCancelled() -> resumeFailure(error)
                    else -> UserApiClient.instance.loginWithKakaoAccount(activity, callback = accountCallback)
                }
            }

            continuation.invokeOnCancellation { resolved = true }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                UserApiClient.instance.loginWithKakaoTalk(activity, callback = talkCallback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(activity, callback = accountCallback)
            }
        }
    }

    private suspend fun fetchKakaoUser(): AuthUser {
        return suspendCancellableCoroutine { continuation ->
            UserApiClient.instance.me { kakaoUser, error ->
                when {
                    error != null -> continuation.resumeWith(Result.failure(error))
                    kakaoUser == null || kakaoUser.id == null -> {
                        continuation.resumeWith(Result.failure(IllegalStateException("카카오 사용자 정보를 받지 못했습니다.")))
                    }

                    else -> {
                        val account = kakaoUser.kakaoAccount
                        val profile = account?.profile
                        val providerUserId = kakaoUser.id.toString()
                        continuation.resume(
                            AuthUser(
                                id = stableUserId(providerUserId),
                                provider = AuthProvider.KAKAO,
                                providerUserId = providerUserId,
                                displayName = profile?.nickname ?: "카카오 사용자",
                                email = account?.email,
                                avatarUrl = profile?.thumbnailImageUrl
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadSession(): AuthState {
        val providerName = prefs.getString(KEY_PROVIDER, null) ?: return AuthState.SignedOut
        val provider = runCatching { AuthProvider.valueOf(providerName) }.getOrNull() ?: return AuthState.SignedOut
        val providerUserId = prefs.getString(KEY_PROVIDER_USER_ID, null) ?: return AuthState.SignedOut
        val user = AuthUser(
            id = prefs.getString(KEY_ID, stableUserId(providerUserId)) ?: stableUserId(providerUserId),
            provider = provider,
            providerUserId = providerUserId,
            displayName = prefs.getString(KEY_DISPLAY_NAME, null).orEmpty().ifBlank { "카카오 사용자" },
            email = prefs.getString(KEY_EMAIL, null),
            avatarUrl = prefs.getString(KEY_AVATAR_URL, null)
        )
        return AuthState.SignedIn(user)
    }

    private fun saveSession(user: AuthUser) {
        prefs.edit()
            .putString(KEY_ID, user.id)
            .putString(KEY_PROVIDER, user.provider.name)
            .putString(KEY_PROVIDER_USER_ID, user.providerUserId)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_AVATAR_URL, user.avatarUrl)
            .apply()
        _authState.value = AuthState.SignedIn(user)
    }

    private fun stableUserId(providerUserId: String): String {
        return "${AuthProvider.KAKAO.name.lowercase()}:$providerUserId"
    }

    private fun Throwable.isUserCancelled(): Boolean {
        val text = listOfNotNull(this::class.simpleName, message).joinToString(" ")
        return text.contains("cancel", ignoreCase = true) ||
            text.contains("cancelled", ignoreCase = true) ||
            text.contains("사용자 취소", ignoreCase = true)
    }

    private fun Throwable.toKakaoLoginMessage(): String {
        val raw = generateSequence(this) { it.cause }
            .mapNotNull { it.message }
            .joinToString(" / ")
            .ifBlank { this::class.simpleName.orEmpty() }
        val runtimeKeyHash = runCatching { Utility.getKeyHash(context) }.getOrDefault("확인 불가")
        val consoleHint = "카카오 Developers > 내 애플리케이션 > 앱 설정 > 플랫폼 > Android에 패키지명 com.example.unum 과 키 해시 $runtimeKeyHash 를 등록해주세요."

        return when {
            raw.contains("KOE006", ignoreCase = true) || raw.contains("redirect", ignoreCase = true) ->
                "카카오 로그인 리다이렉트 설정이 맞지 않습니다. $consoleHint 원인: $raw"

            raw.contains("keyhash", ignoreCase = true) ||
                raw.contains("key hash", ignoreCase = true) ||
                raw.contains("android keyhash validation failed", ignoreCase = true) ||
                raw.contains("package", ignoreCase = true) ->
                "카카오 Android 키 해시 검증에 실패했습니다. $consoleHint 원인: $raw"

            raw.contains("invalid_client", ignoreCase = true) || raw.contains("KOE101", ignoreCase = true) ->
                "카카오 네이티브 앱 키가 현재 앱과 맞지 않습니다. local.properties의 kakao.native.app.key 값을 확인해주세요. 원인: $raw"

            raw.contains("cancel", ignoreCase = true) ->
                "카카오 로그인이 취소되었습니다."

            else ->
                "카카오 로그인에 실패했습니다. 현재 앱 키 해시는 $runtimeKeyHash 입니다. 원인: $raw"
        }
    }

    private companion object {
        const val KEY_ID = "id"
        const val KEY_PROVIDER = "provider"
        const val KEY_PROVIDER_USER_ID = "provider_user_id"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_EMAIL = "email"
        const val KEY_AVATAR_URL = "avatar_url"
    }
}
