package com.example.unum.data.model

enum class AuthProvider(val label: String) {
    KAKAO("카카오")
}

data class AuthUser(
    val id: String,
    val provider: AuthProvider,
    val providerUserId: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val accessToken: String? = null
)

sealed interface AuthState {
    data object SignedOut : AuthState
    data class SignedIn(val user: AuthUser) : AuthState
}

sealed interface UserSyncState {
    data object Idle : UserSyncState
    data object Syncing : UserSyncState
    data class Synced(val message: String) : UserSyncState
    data class Failed(val message: String) : UserSyncState
}
