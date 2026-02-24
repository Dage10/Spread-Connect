package viewmodel

import models.ResetContrasenyaUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import repository.Repository

class ResetContrasenyaViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetContrasenyaUiState())
    val uiState: StateFlow<ResetContrasenyaUiState> = _uiState


    fun enviarOtp(email: String) {
        if (email.isBlank()) {
            _uiState.value = ResetContrasenyaUiState(error = "Introdueix un correu vàlid")
            return
        }
        _uiState.value = ResetContrasenyaUiState(loading = true)
        viewModelScope.launch {
            try {
                repo.usuariDao.enviarOtp(email)
                _uiState.value = ResetContrasenyaUiState(success = true, step = 2, email = email)
            } catch (e: Exception) {
                _uiState.value = ResetContrasenyaUiState(error = "Error ${e.message}")
            }
        }
    }


    fun verificarCodi(codi: String, novaContrasenya: String) {
        val emailActual = _uiState.value.email
        if (codi.isBlank() || novaContrasenya.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Omple tots els camps")
            return
        }
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                repo.usuariDao.verificarOtpICanviar(emailActual, codi, novaContrasenya)
                _uiState.value = ResetContrasenyaUiState(success = true, step = 3)
            } catch (e: Exception) {
                _uiState.value = ResetContrasenyaUiState(error = e.message ?: "Error desconegut")
            }
        }
    }
}