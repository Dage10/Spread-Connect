package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.CrearPresentacioUiState
import repository.Repository


class CrearPresentacioViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearPresentacioUiState())
    val uiState: StateFlow<CrearPresentacioUiState> = _uiState

    fun crearPresentacio(
        idUsuari: String,
        areaId: String,
        titol: String,
        contingut: String,
        imatgeUrl: String?
    ) {
        if (titol.isBlank() || contingut.isBlank()) {
            _uiState.value = CrearPresentacioUiState(error = "Omple tots els camps")
            return
        }

        _uiState.value = CrearPresentacioUiState(loading = true)

        viewModelScope.launch {
            try {
                val p = repo.presentacioDao.crearPresentacio(idUsuari, titol, contingut, areaId, imatgeUrl)
                _uiState.value = CrearPresentacioUiState(presentacioCreada = p)
            } catch (e: Exception) {
                _uiState.value = CrearPresentacioUiState(error = e.message)
            }
        }
    }
}
