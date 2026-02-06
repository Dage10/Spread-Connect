package viewmodel
import models.RegistreUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import repository.Repository

class RegistreViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistreUiState())
    val uiState: StateFlow<RegistreUiState> = _uiState

    fun registre(nom: String, email: String, pass: String, repetir: String) {

        if (pass != repetir) {
            _uiState.value = RegistreUiState(error = "Les contrasenyes no coincideixen")
            return
        }

        _uiState.value = RegistreUiState(loading = true)

        viewModelScope.launch {
            try {
                val usuari = repo.usuariDao.registreUsuari(nom, email, pass)
                repo.preferenciesDao.insertPreferenciesPerDefecte(usuari.id)
                _uiState.value = RegistreUiState(usuariCreat = usuari)
            } catch (e: Exception) {
                _uiState.value = RegistreUiState(error = e.message ?: "Error en el registre")
            }
        }
    }
}
