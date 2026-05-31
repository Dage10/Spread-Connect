package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.Comentari
import models.ComentarisUiState
import models.Post
import models.Presentacio
import models.ReaccioStats
import repository.Repository
import util.UiText

class ComentarisViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComentarisUiState())
    val uiState: StateFlow<ComentarisUiState> = _uiState

    fun carregarDades(targetId: String, idUsuariLoguejat: String?, targetType: String) {
        _uiState.value = _uiState.value.copy(loading = true)
        viewModelScope.launch {
            try {
                when (targetType) {
                    "post" -> {
                        val post = enriquirPost(repo.postDao.getPostPerId(targetId), idUsuariLoguejat)
                        val comentaris = enriquirComentaris(
                            repo.comentarisDao.getComentarisPost(targetId),
                            idUsuariLoguejat
                        )
                        _uiState.value = ComentarisUiState(
                            post = post,
                            comentaris = comentaris,
                            loading = false
                        )
                    }
                    "presentacio" -> {
                        val presentacio = enriquirPresentacio(
                            repo.presentacioDao.getPresentacioPerId(targetId),
                            idUsuariLoguejat
                        )
                        val comentaris = enriquirComentaris(
                            repo.comentarisDao.getComentarisPresentacio(targetId),
                            idUsuariLoguejat
                        )
                        _uiState.value = ComentarisUiState(
                            presentacio = presentacio,
                            comentaris = comentaris,
                            loading = false
                        )
                    }
                    "comment" -> {
                        val comentariPare = enriquirComentariPare(
                            repo.comentarisDao.getComentariPerId(targetId),
                            idUsuariLoguejat
                        )
                        val comentaris = enriquirComentaris(
                            repo.comentarisDao.getComentarisRespostes(targetId),
                            idUsuariLoguejat
                        )
                        _uiState.value = ComentarisUiState(
                            comentaris = comentaris,
                            loading = false
                        ).copy(comentariPare = comentariPare)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = UiText.DynamicString(e.message ?: "Error")
                )
            }
        }
    }

    private suspend fun enriquirPost(post: Post, idUsuariLoguejat: String?): Post {
        val usuari = repo.usuariDao.getUsuariPerId(post.id_usuari)
        val reaccions = repo.reaccioDao.getStatsPosts(listOf(post.id), idUsuariLoguejat)[post.id] ?: ReaccioStats()
        val numeroComentaris = repo.comentarisDao.getNumComentarisPerPosts(listOf(post.id))[post.id] ?: 0
        return post.copy(
            nom_usuari = usuari.nom_usuari,
            avatar_url = usuari.avatar_url,
            likes = reaccions.likes,
            dislikes = reaccions.dislikes,
            reaccioActual = reaccions.reaccioActual,
            numComentaris = numeroComentaris
        )
    }

    private suspend fun enriquirPresentacio(presentacio: Presentacio, idUsuariLoguejat: String?): Presentacio {
        val usuari = repo.usuariDao.getUsuariPerId(presentacio.id_usuari)
        val reaccions = repo.reaccioDao.getStatsPresentacions(listOf(presentacio.id), idUsuariLoguejat)
            .getOrDefault(presentacio.id, ReaccioStats())
        val numeroComentaris = repo.comentarisDao.getNumComentarisPerPresentacions(listOf(presentacio.id))
            .getOrDefault(presentacio.id, 0)
        return presentacio.copy(
            nom_usuari = usuari.nom_usuari,
            avatar_url = usuari.avatar_url,
            likes = reaccions.likes,
            dislikes = reaccions.dislikes,
            reaccioActual = reaccions.reaccioActual,
            numComentaris = numeroComentaris
        )
    }

    private suspend fun enriquirComentariPare(comentari: Comentari, idUsuariLoguejat: String?): Comentari {
        val usuari = repo.usuariDao.getUsuariPerId(comentari.id_usuari)
        val reaccions = repo.reaccioDao.getStatsComentaris(listOf(comentari.id), idUsuariLoguejat)
            .getOrDefault(comentari.id, ReaccioStats())
        return comentari.copy(
            nom_usuari = usuari.nom_usuari,
            avatar_url = usuari.avatar_url,
            likes = reaccions.likes,
            dislikes = reaccions.dislikes,
            reaccioActual = reaccions.reaccioActual
        )
    }

    private suspend fun enriquirComentaris(
        comentaris: List<Comentari>,
        idUsuariLoguejat: String?
    ): List<Comentari> {
        if (comentaris.isEmpty()) return emptyList()
        val comentarisIds = comentaris.map { it.id }
        val reaccionsPerComentari = repo.reaccioDao.getStatsComentaris(comentarisIds, idUsuariLoguejat)
        val numeroRespostesPerComentari = repo.comentarisDao.getNumRespostesPerComentaris(comentarisIds)

        return comentaris.map { comentari ->
            val reaccions = reaccionsPerComentari[comentari.id] ?: ReaccioStats()
            comentari.copy(
                likes = reaccions.likes,
                dislikes = reaccions.dislikes,
                reaccioActual = reaccions.reaccioActual,
                numRespostes = numeroRespostesPerComentari[comentari.id] ?: 0
            )
        }
    }

    fun enviarComentari(targetId: String, idUsuari: String, contingut: String, imatgeUrl: String? = null, targetType: String) {
        if (contingut.isBlank()) return
        viewModelScope.launch {
            try {
                when (targetType) {
                    "post" -> repo.comentarisDao.crearComentari(targetId, idUsuari, contingut, imatgeUrl)
                    "presentacio" -> repo.comentarisDao.crearComentariPresentacio(targetId, idUsuari, contingut, imatgeUrl)
                    "comment" -> repo.comentarisDao.crearComentariResposta(targetId, idUsuari, contingut, imatgeUrl)
                }
                carregarDades(targetId, idUsuari, targetType)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }

    fun editarComentari(id: String, contingut: String, targetId: String, idUsuari: String?, targetType: String) {
        if (contingut.isBlank()) return
        viewModelScope.launch {
            try {
                repo.comentarisDao.editarComentari(id, contingut, null)
                carregarDades(targetId, idUsuari, targetType)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }

    fun eliminarComentari(id: String, targetId: String, idUsuari: String?, targetType: String) {
        viewModelScope.launch {
            try {
                repo.comentarisDao.eliminarComentari(id)
                carregarDades(targetId, idUsuari, targetType)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }

    fun reaccionarComentari(comentariId: String, targetId: String, idUsuari: String, tipus: String, targetType: String) {
        viewModelScope.launch {
            try {
                repo.reaccioDao.canviarReaccioComentari(comentariId, idUsuari, tipus)
                carregarDades(targetId, idUsuari, targetType)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }

    fun reaccionarPost(postId: String, idUsuari: String, tipus: String) {
        viewModelScope.launch {
            try {
                repo.reaccioDao.canviarReaccio(postId, idUsuari, tipus)
                carregarDades(postId, idUsuari, "post")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }

    fun reaccionarPresentacio(presentacioId: String, idUsuari: String, tipus: String) {
        viewModelScope.launch {
            try {
                repo.reaccioDao.canviarReaccioPresentacio(presentacioId, idUsuari, tipus)
                carregarDades(presentacioId, idUsuari, "presentacio")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = UiText.DynamicString(e.message ?: "Error"))
            }
        }
    }
}
