package br.com.movva.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
)

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$")

class AuthViewModel(
    private val authRepository: AuthRepository = FakeAuthRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun login() {
        val current = _uiState.value
        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = try {
                authRepository.login(current.email.trim(), current.password)
            } catch (e: Exception) {
                AuthResult.Error(AuthError.NetworkError)
            }

            _uiState.update {
                when (result) {
                    is AuthResult.Success -> it.copy(isLoading = false, isLoginSuccessful = true)
                    is AuthResult.Error -> it.copy(
                        isLoading = false,
                        errorMessage = mapErrorToMessage(result.error),
                    )
                }
            }
        }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Informe seu e-mail"
        !EMAIL_REGEX.matches(email.trim()) -> "E-mail inválido"
        else -> null
    }

    private fun validatePassword(password: String): String? =
        if (password.isBlank()) "Informe sua senha" else null

    private fun mapErrorToMessage(error: AuthError): String = when (error) {
        is AuthError.InvalidCredentials -> "E-mail ou senha inválidos"
        is AuthError.NetworkError -> "Falha de conexão. Verifique sua internet e tente novamente"
        is AuthError.Unknown -> error.message.ifBlank { "Não foi possível entrar. Tente novamente" }
    }
}
