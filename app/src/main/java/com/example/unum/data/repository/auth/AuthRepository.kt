package com.example.unum.data.repository.auth

import android.app.Activity
import com.example.unum.data.model.AuthState
import com.example.unum.data.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun signInWithKakao(activity: Activity): Result<AuthUser>
    suspend fun refreshKakaoSession(): Result<AuthUser>
    suspend fun signOut()
}
