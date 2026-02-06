package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.EditarPostUiState
import models.Post
import repository.Repository


class EditarPostViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPostUiState())
    val uiState: StateFlow<EditarPostUiState> = _uiState

    fun carregarPost(idPost: String) {
        _uiState.value = EditarPostUiState(loading = true)
        viewModelScope.launch {
            try {
                val post = repo.postDao.getPostPerId(idPost)
                _uiState.value = EditarPostUiState(post = post)
            } catch (e: Exception) {
                _uiState.value = EditarPostUiState(error = e.message)
            }
        }
    }

    fun editarPost(idPost: String, titol: String, descripcio: String, imatgeUrl: String? = null) {
        if (titol.isBlank() || descripcio.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Omple tots els camps")
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
                    error = e.message
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
                    error = e.message
                )
            }
        }
    }

    fun resetPostEliminat() {
        _uiState.value = _uiState.value.copy(postEliminat = false)
    }
}
