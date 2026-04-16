package viewmodel

import models.RegistreUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.clickconnect.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import repository.Repository
import util.UiText

class RegistreViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistreUiState())
    val uiState: StateFlow<RegistreUiState> = _uiState

    private fun isUsuariExistentError(e: Exception): Boolean {
        val msg = (e.message ?: "").lowercase()
        return msg.contains("already registered") ||
            msg.contains("user already") ||
            msg.contains("already exists") ||
            msg.contains("duplicate key") ||
            msg.contains("23505") ||
            msg.contains("unique constraint")
    }

    fun registre(nom: String, email: String, pass: String, repetir: String) {

        if (pass != repetir) {
            _uiState.value = RegistreUiState(error = UiText.StringResource(R.string.contrasenyes_no_conceidexen))
            return
        }

        _uiState.value = RegistreUiState(loading = true)

        viewModelScope.launch {
            try {
                repo.usuariDao.registreUsuari(nom, email, pass)
                _uiState.value = RegistreUiState(isSuccess = true)
            } catch (e: Exception) {
                val uiError = if (isUsuariExistentError(e)) {
                    UiText.StringResource(R.string.usuari_existent)
                } else {
                    UiText.StringResource(R.string.error_al_registrar)
                }
                _uiState.value = RegistreUiState(error = uiError)
            }
        }
    }
}
