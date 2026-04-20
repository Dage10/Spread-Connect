package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.PerfilUiState
import repository.Repository
import util.UiText
import com.daviddam.clickconnect.R

class PerfilViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    fun carregarPerfil(idUsuari: String, idUsuariLoguejat: String?) {
        _uiState.value = PerfilUiState(loading = true)
        viewModelScope.launch {
            try {
                val usuari = repo.usuariDao.getUsuariPerId(idUsuari)
                val numSeguidors = repo.seguimentDao.getNumSeguidors(idUsuari)
                val numSeguint = repo.seguimentDao.getNumSeguint(idUsuari)
                val isSeguint = if (idUsuariLoguejat != null && idUsuariLoguejat != idUsuari) {
                    repo.seguimentDao.isSeguint(idUsuariLoguejat, idUsuari)
                } else {
                    false
                }

                val posts = try { repo.postDao.getPostsPerUsuari(idUsuari) } catch (e: Exception) { emptyList() }
                val presentacions = try { repo.presentacioDao.getPresentacionsPerUsuari(idUsuari) } catch (e: Exception) { emptyList() }

                _uiState.value = PerfilUiState(
                    usuari = usuari,
                    numSeguidors = numSeguidors,
                    numSeguint = numSeguint,
                    isSeguint = isSeguint,
                    posts = posts,
                    presentacions = presentacions,
                    loading = false
                )
            } catch (e: Exception) {
                _uiState.value = PerfilUiState(
                    error = UiText.StringResource(R.string.error_carregar_dades),
                    loading = false
                )
            }
        }
    }

    fun toggleSeguir(idUsuariLoguejat: String) {
        val estatActual = _uiState.value
        val usuariAperfil = estatActual.usuari ?: return
        
        if (idUsuariLoguejat == usuariAperfil.id) return

        viewModelScope.launch {
            try {
                if (estatActual.isSeguint) {
                    repo.seguimentDao.deixarDeSeguirUsuari(idUsuariLoguejat, usuariAperfil.id)
                } else {
                    repo.seguimentDao.seguirUsuari(idUsuariLoguejat, usuariAperfil.id)
                }

                val nousSeguidors = if (estatActual.isSeguint) (estatActual.numSeguidors ?: 0) - 1 else (estatActual.numSeguidors ?: 0) + 1
                _uiState.value = estatActual.copy(
                    isSeguint = !estatActual.isSeguint,
                    numSeguidors = nousSeguidors
                )
            } catch (e: Exception) {
                _uiState.value = estatActual.copy(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }
}
