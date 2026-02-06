package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.CrearPostUiState
import repository.Repository


class CrearPostViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearPostUiState())
    val uiState: StateFlow<CrearPostUiState> = _uiState

    fun crearPost(idUsuari: String, areaId: String, titol: String, descripcio: String, imatgeUrl: String? = null) {
        if (titol.isBlank() || descripcio.isBlank()) {
            _uiState.value = CrearPostUiState(error = "Omple tots els camps")
            return
        }

        _uiState.value = CrearPostUiState(loading = true)

        viewModelScope.launch {
            try {
                val post = repo.postDao.crearPost(idUsuari, titol, descripcio, areaId, imatgeUrl)
                _uiState.value = CrearPostUiState(postCreat = post)
            } catch (e: Exception) {
                _uiState.value = CrearPostUiState(error = e.message)
            }
        }
    }
}
