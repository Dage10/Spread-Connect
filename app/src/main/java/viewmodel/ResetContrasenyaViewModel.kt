package viewmodel

import models.ResetContrasenyaUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.clickconnect.R
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
                repo.usuariDao.enviarOtp(email)
                _uiState.value = ResetContrasenyaUiState(success = true, step = 2, email = email)
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains(R.string.usuari_no_trobat.toString()) == true ->
                        UiText.StringResource(R.string.usuari_no_trobat)
                    else -> UiText.DynamicString(e.message ?: "Error")
                }
                _uiState.value = ResetContrasenyaUiState(error = errorMsg)
            }
        }
    }


    fun verificarCodi(codi: String, novaContrasenya: String) {
        val emailActual = _uiState.value.email
        if (codi.isBlank() || novaContrasenya.isBlank()) {
            _uiState.value = _uiState.value.copy(error = UiText.StringResource(R.string.omple_tots_camps))
            return
        }
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                repo.usuariDao.verificarOtpICanviar(emailActual, codi, novaContrasenya)
                _uiState.value = ResetContrasenyaUiState(success = true, step = 3)
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains(R.string.codi_no_solicitat.toString()) == true ->
                        UiText.StringResource(R.string.codi_no_solicitat)
                    e.message?.contains(R.string.codi_incorrecte.toString()) == true ->
                        UiText.StringResource(R.string.codi_incorrecte)
                    e.message?.contains(R.string.error_verificar_otp.toString()) == true ->
                        UiText.StringResource(R.string.error_verificar_otp)
                    else -> UiText.DynamicString(e.message ?: "Error desconegut")
                }
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = errorMsg
                )
            }
        }
    }
}
