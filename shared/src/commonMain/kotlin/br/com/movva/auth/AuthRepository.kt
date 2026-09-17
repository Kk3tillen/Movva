package br.com.movva.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    suspend fun login(email: String, senha: String): Result<Unit>
    suspend fun logout()
    fun currentSession(): UserSession?
    val sessionStatus: StateFlow<SessionStatus>
}

class AuthRepositoryImpl(
    private val client: SupabaseClient
) : AuthRepository {

    override suspend fun login(email: String, senha: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = senha
        }
    }

    override suspend fun logout() {
        client.auth.signOut()
    }

    override fun currentSession(): UserSession? =
        client.auth.currentSessionOrNull()

    override val sessionStatus: StateFlow<SessionStatus>
        get() = client.auth.sessionStatus
}