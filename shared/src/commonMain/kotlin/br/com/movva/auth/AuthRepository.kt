package br.com.movva.auth

import kotlinx.coroutines.delay

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object NetworkError : AuthError
    data class Unknown(val message: String) : AuthError
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val error: AuthError) : AuthResult
}

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
}

class FakeAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): AuthResult {
        delay(1200)
        return if (email == "erro@movva.com") {
            AuthResult.Error(AuthError.InvalidCredentials)
        } else {
            AuthResult.Success
        }
    }
}
