package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daos.HistorialDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.HistorialUiState
import repository.Repository
import util.UiText
class HistorialViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState

    fun carregarHistorial(idUsuari: String) {
        _uiState.value = HistorialUiState(loading = true)
        viewModelScope.launch {
            try {
                val historial = repo.historialDao.getHistorialUsuari(idUsuari)
                _uiState.value = HistorialUiState(historial = historial, loading = false)
            } catch (e: Exception) {
                _uiState.value = HistorialUiState(error = UiText.DynamicString(e.message ?: "Error"), loading = false)
            }
        }
    }
}