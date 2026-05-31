package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.spreadconnect.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.PerfilUiState
import models.Post
import models.Presentacio
import models.ReaccioStats
import repository.Repository
import util.UiText

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

                val posts = try {
                    enriquirPosts(repo.postDao.getPostsPerUsuari(idUsuari), idUsuariLoguejat)
                } catch (_: Exception) {
                    emptyList()
                }

                val presentacions = try {
                    enriquirPresentacions(
                        repo.presentacioDao.getPresentacionsPerUsuari(idUsuari),
                        idUsuariLoguejat
                    )
                } catch (_: Exception) {
                    emptyList()
                }

                _uiState.value = PerfilUiState(
                    usuari = usuari,
                    numSeguidors = numSeguidors,
                    numSeguint = numSeguint,
                    isSeguint = isSeguint,
                    posts = posts,
                    presentacions = presentacions,
                    loading = false
                )
            } catch (_: Exception) {
                _uiState.value = PerfilUiState(
                    error = UiText.StringResource(R.string.error_carregar_dades),
                    loading = false
                )
            }
        }
    }

    private suspend fun enriquirPosts(posts: List<Post>, idUsuariLoguejat: String?): List<Post> {
        if (posts.isEmpty()) return emptyList()
        val postsIds = posts.map { it.id }
        val reaccionsPerPost = repo.reaccioDao.getStatsPosts(postsIds, idUsuariLoguejat)
        val numeroComentarisPerPost = repo.comentarisDao.getNumComentarisPerPosts(postsIds)

        return posts.map { post ->
            val reaccions = reaccionsPerPost[post.id] ?: ReaccioStats()
            post.copy(
                likes = reaccions.likes,
                dislikes = reaccions.dislikes,
                reaccioActual = reaccions.reaccioActual,
                numComentaris = numeroComentarisPerPost[post.id] ?: 0
            )
        }
    }

    private suspend fun enriquirPresentacions(
        presentacions: List<Presentacio>,
        idUsuariLoguejat: String?
    ): List<Presentacio> {
        if (presentacions.isEmpty()) return emptyList()
        val presentacionsIds = presentacions.map { it.id }
        val reaccionsPerPresentacio = repo.reaccioDao.getStatsPresentacions(presentacionsIds, idUsuariLoguejat)
        val numeroComentarisPerPresentacio =
            repo.comentarisDao.getNumComentarisPerPresentacions(presentacionsIds)

        return presentacions.map { presentacio ->
            val reaccions = reaccionsPerPresentacio[presentacio.id] ?: ReaccioStats()
            presentacio.copy(
                likes = reaccions.likes,
                dislikes = reaccions.dislikes,
                reaccioActual = reaccions.reaccioActual,
                numComentaris = numeroComentarisPerPresentacio[presentacio.id] ?: 0
            )
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

                val nousSeguidors = if (estatActual.isSeguint) {
                    (estatActual.numSeguidors ?: 0) - 1
                } else {
                    (estatActual.numSeguidors ?: 0) + 1
                }
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
