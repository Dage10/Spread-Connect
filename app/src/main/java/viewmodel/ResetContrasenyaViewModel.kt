package viewmodel

import models.ResetContrasenyaUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.spreadconnect.R
import conexio.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import repository.Repository
import util.UiText

class ResetContrasenyaViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetContrasenyaUiState())
    val uiState: StateFlow<ResetContrasenyaUiState> = _uiState

    fun enviarOtp(email: String) {
        if (email.isBlank()) {
            _uiState.value = ResetContrasenyaUiState(error = UiText.StringResource(R.string.email_no_valid))
            return
        }
        _uiState.value = ResetContrasenyaUiState(loading = true)
        viewModelScope.launch {
            try {
                repo.usuariDao.enviarResetPassword(email)
                _uiState.value = ResetContrasenyaUiState(success = true, step = 2, email = email)
            } catch (e: Exception) {
                _uiState.value = ResetContrasenyaUiState(error = UiText.StringResource(R.string.error))
            }
        }
    }

    fun verificarCodi(codi: String, novaContrasenya: String) {
        _uiState.value = _uiState.value.copy(loading = true)
        viewModelScope.launch {
            try {
                val emailActual = _uiState.value.email
                SupabaseClient.client.auth.verifyEmailOtp(
                    type = OtpType.Email.RECOVERY,
                    email = emailActual,
                    token = codi
                )

                SupabaseClient.client.auth.updateUser {
                    password = novaContrasenya
                }
                _uiState.value = ResetContrasenyaUiState(success = true, step = 3)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = UiText.StringResource(R.string.codi_incorrecte)
                )
            }
        }
    }
}
