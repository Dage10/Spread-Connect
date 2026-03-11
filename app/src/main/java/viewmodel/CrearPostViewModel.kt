package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.clickconnect.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.CrearPostUiState
import repository.Repository
import util.UiText

class CrearPostViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearPostUiState())
    val uiState: StateFlow<CrearPostUiState> = _uiState

    private val _etiquetes = MutableStateFlow<List<String>>(emptyList())
    val etiquetes: StateFlow<List<String>> = _etiquetes

    fun afegirEtiqueta(nom: String) {
        val neta = nom.trim().lowercase()
        if (neta.isNotEmpty() && !_etiquetes.value.contains(neta)) {
            _etiquetes.value += neta
        }
    }

    fun treureEtiqueta(nom: String) {
        _etiquetes.value -= nom
    }

    fun crearPost(idUsuari: String, areaId: String, titol: String, descripcio: String, imatgeUrl: String? = null) {
        if (titol.isBlank() || descripcio.isBlank()) {
            _uiState.value = CrearPostUiState(error = UiText.StringResource(R.string.omple_tots_camps))
            return
        }

        _uiState.value = CrearPostUiState(loading = true)

        viewModelScope.launch {
            try {
                val post = repo.postDao.crearPost(idUsuari, titol, descripcio, areaId, imatgeUrl)
                _etiquetes.value.forEach { nomEtiqueta ->
                    repo.postDao.crearEtiqueta(nomEtiqueta, post.id)
                }
                _uiState.value = CrearPostUiState(postCreat = post)
            } catch (e: Exception) {
                _uiState.value = CrearPostUiState(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }
}
