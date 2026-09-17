package br.com.movva.auth

import br.com.movva.shared.db.MovvaDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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

class AuthRepositoryImpl(
    private val client: SupabaseClient,
    private val database: MovvaDatabase,
) : AuthRepository {

    override suspend fun login(email: String, senha: String): Result<Unit> = runCatching {
        val usuario = client.postgrest.rpc(
            "login_user",
            buildJsonObject {
                put("p_email", email)
                put("p_password", senha)
            }
        ).decodeList<LoginRpcResult>().firstOrNull() ?: throw InvalidCredentialsException()

        withContext(Dispatchers.Default) {
            database.sessaoQueries.upsertSessao(
                userId = usuario.id,
                nome = usuario.name,
                userTag = usuario.userTag,
            )
        }
    }

    override suspend fun logout() {
        withContext(Dispatchers.Default) {
            database.sessaoQueries.limparSessao()
        }
    }

    override suspend fun currentUser(): LoggedInUser? = withContext(Dispatchers.Default) {
        database.sessaoQueries.selectSessao().executeAsOneOrNull()?.let {
            LoggedInUser(id = it.userId, nome = it.nome, userTag = it.userTag)
        }
    }
}

@Serializable
private data class LoginRpcResult(
    val id: String,
    val name: String,
    @SerialName("user_tag") val userTag: String,
)
