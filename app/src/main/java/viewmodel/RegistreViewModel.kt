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

    fun registre(nom: String, email: String, pass: String, repetir: String) {

        if (pass != repetir) {
            _uiState.value = RegistreUiState(error = UiText.StringResource(R.string.contrasenyes_no_conceidexen))
            return
        }

        _uiState.value = RegistreUiState(loading = true)

        viewModelScope.launch {
            try {
                val usuari = repo.usuariDao.registreUsuari(nom, email, pass)
                repo.preferenciesDao.insertPreferenciesPerDefecte(usuari.id)
                _uiState.value = RegistreUiState(usuariCreat = usuari)
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains(R.string.usuari_existent.toString()) == true -> 
                        UiText.StringResource(R.string.usuari_existent)
                    else -> UiText.DynamicString(e.message ?: "Error en el registre")
                }
                _uiState.value = RegistreUiState(error = errorMsg)
            }
        }
    }
}
