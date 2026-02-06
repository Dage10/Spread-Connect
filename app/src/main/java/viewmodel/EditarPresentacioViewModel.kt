package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.EditarPresentacioUiState
import models.Presentacio
import repository.Repository


class EditarPresentacioViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPresentacioUiState())
    val uiState: StateFlow<EditarPresentacioUiState> = _uiState

    fun carregarPresentacio(idPresentacio: String) {
        _uiState.value = EditarPresentacioUiState(loading = true)
        viewModelScope.launch {
            try {
                val p = repo.presentacioDao.getPresentacioPerId(idPresentacio)
                _uiState.value = EditarPresentacioUiState(presentacio = p)
            } catch (e: Exception) {
                _uiState.value = EditarPresentacioUiState(error = e.message)
            }
        }
    }

    fun editarPresentacio(
        idPresentacio: String,
        titol: String,
        contingut: String,
        imatgeUrl: String?
    ) {
        if (titol.isBlank() || contingut.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Omple tots els camps")
            return
        }

        _uiState.value = _uiState.value.copy(loading = true)

        viewModelScope.launch {
            try {
                val p = repo.presentacioDao.editarPresentacio(idPresentacio, titol, contingut, imatgeUrl)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    presentacioActualitzada = p,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }

    fun eliminarPresentacio(presentacio: Presentacio) {
        _uiState.value = _uiState.value.copy(loading = true, presentacioEliminada = false, error = null)
        viewModelScope.launch {
            try {
                repo.presentacioDao.eliminarPresentacio(presentacio.id)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    presentacioEliminada = true,
                    presentacioActualitzada = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }

    fun resetPresentacioEliminada() {
        _uiState.value = _uiState.value.copy(presentacioEliminada = false)
    }
}
