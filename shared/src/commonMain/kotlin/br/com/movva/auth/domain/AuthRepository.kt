package br.com.movva.auth.domain

data class LoggedInUser(
    val id: String,
    val nome: String,
    val userTag: String,
)

class InvalidCredentialsException : Exception("invalid_credentials")

interface AuthRepository {
    suspend fun login(email: String, senha: String): Result<Unit>
    suspend fun logout()
    suspend fun currentUser(): LoggedInUser?
}
