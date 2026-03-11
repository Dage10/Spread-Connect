package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.clickconnect.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.EditarPostUiState
import models.Etiqueta
import models.Post
import repository.Repository
import util.UiText


class EditarPostViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPostUiState())
    val uiState: StateFlow<EditarPostUiState> = _uiState

    private val _etiquetes = MutableStateFlow<List<Etiqueta>>(emptyList())
    val etiquetes: StateFlow<List<Etiqueta>> = _etiquetes

    fun carregarPost(idPost: String) {
        _uiState.value = EditarPostUiState(loading = true)
        viewModelScope.launch {
            try {
                val post = repo.postDao.getPostPerId(idPost)
                val tags = repo.postDao.getEtiquetesPost(idPost)
                _uiState.value = EditarPostUiState(post = post)
                _etiquetes.value = tags
            } catch (e: Exception) {
                _uiState.value = EditarPostUiState(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }

    fun editarPost(idPost: String, titol: String, descripcio: String, imatgeUrl: String? = null) {
        if (titol.isBlank() || descripcio.isBlank()) {
            _uiState.value = _uiState.value.copy(error = UiText.StringResource(R.string.omple_tots_camps))
            return
        }

        _uiState.value = _uiState.value.copy(loading = true)

        viewModelScope.launch {
            try {
                val post = repo.postDao.editarPost(idPost, titol, descripcio, imatgeUrl)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    postActualitzat = post,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = UiText.DynamicString(e.message ?: "Error")
                )
            }
        }
    }

    fun eliminarPost(post: Post) {
        _uiState.value = _uiState.value.copy(loading = true, postEliminat = false, error = null)
        viewModelScope.launch {
            try {
                repo.postDao.eliminarPost(post.id)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    postEliminat = true,
                    postActualitzat = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = UiText.DynamicString(e.message ?: "Error")
                )
            }
        }
    }

    fun resetPostEliminat() {
        _uiState.value = _uiState.value.copy(postEliminat = false)
    }

    fun afegirEtiqueta(postId: String, nom: String) {
        val neta = nom.trim().lowercase()
        if (neta.isEmpty()) return

        viewModelScope.launch {
            try {
                val nova = repo.postDao.crearEtiqueta(neta, postId)
                _etiquetes.value += nova
            } catch (_: Exception) {
            }
        }
    }

    fun editarEtiqueta(postId: String, nomAntic: String, nouNom: String) {
        val neta = nouNom.trim().lowercase()
        if (neta.isEmpty()) return

        viewModelScope.launch {
            try {
                val etiquetaActual =
                    _etiquetes.value.firstOrNull { it.nom.equals(nomAntic, ignoreCase = true) }
                if (etiquetaActual != null) {
                    val actualitzada = repo.postDao.editarEtiqueta(etiquetaActual.id, neta)
                    _etiquetes.value = _etiquetes.value.map {
                        if (it.id == etiquetaActual.id) it.copy(nom = actualitzada.nom) else it
                    }
                } else {
                    val nova = repo.postDao.crearEtiqueta(neta, postId)
                    _etiquetes.value += nova
                }
            } catch (_: Exception) {
            }
        }
    }

    fun eliminarEtiqueta(postId: String, nom: String) {
        viewModelScope.launch {
            try {
                val etiqueta =
                    _etiquetes.value.firstOrNull { it.nom.equals(nom, ignoreCase = true) }
                if (etiqueta != null) {
                    repo.postDao.eliminarEtiqueta(etiqueta.id, postId)
                    _etiquetes.value = _etiquetes.value.filterNot { it.id == etiqueta.id }
                }
            } catch (_: Exception) {
            }
        }
    }
}
