package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.ComentarisUiState
import repository.Repository


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
                        val postRaw = repo.postDao.getPostPerId(targetId)
                        val usuariPost = repo.usuariDao.getUsuariPerId(postRaw.id_usuari)
                        val likesPost = repo.reaccioDao.getLikes(targetId)
                        val dislikesPost = repo.reaccioDao.getDislikes(targetId)
                        val reaccioPost = idUsuariLoguejat?.let { repo.reaccioDao.getReaccioUsuari(targetId, it) }

                        val postDetallat = postRaw.copy(
                            nom_usuari = usuariPost.nom_usuari,
                            avatar_url = usuariPost.avatar_url,
                            likes = likesPost,
                            dislikes = dislikesPost,
                            reaccioActual = reaccioPost
                        )

                        val comentarisRaw = repo.comentarisDao.getComentarisPost(targetId)
                        val comentarisDetallats = carregarDetallsComentaris(comentarisRaw, idUsuariLoguejat)

                        _uiState.value = ComentarisUiState(
                            post = postDetallat,
                            comentaris = comentarisDetallats,
                            loading = false
                        )
                    }
                    "presentacio" -> {
                        val presRaw = repo.presentacioDao.getPresentacioPerId(targetId)
                        val usuariPres = repo.usuariDao.getUsuariPerId(presRaw.id_usuari)
                        val likesPres = repo.reaccioDao.getLikesPresentacio(targetId)
                        val dislikesPres = repo.reaccioDao.getDislikesPresentacio(targetId)
                        val reaccioPres = idUsuariLoguejat?.let { repo.reaccioDao.getReaccioUsuariPresentacio(targetId, it) }

                        val presDetallada = presRaw.copy(
                            nom_usuari = usuariPres.nom_usuari,
                            avatar_url = usuariPres.avatar_url,
                            likes = likesPres,
                            dislikes = dislikesPres,
                            reaccioActual = reaccioPres
                        )

                        val comentarisRaw = repo.comentarisDao.getComentarisPresentacio(targetId)
                        val comentarisDetallats = carregarDetallsComentaris(comentarisRaw, idUsuariLoguejat)

                        _uiState.value = ComentarisUiState(
                            presentacio = presDetallada,
                            comentaris = comentarisDetallats,
                            loading = false
                        )
                    }
                    "comment" -> {
                        val pareRaw = repo.comentarisDao.getComentariPerId(targetId)
                        val usuariPare = repo.usuariDao.getUsuariPerId(pareRaw.id_usuari)
                        val likesPare = repo.reaccioDao.getLikesComentari(targetId)
                        val dislikesPare = repo.reaccioDao.getDislikesComentari(targetId)
                        val reaccioPare = idUsuariLoguejat?.let { repo.reaccioDao.getReaccioUsuariComentari(targetId, it) }

                        val pareDetallat = pareRaw.copy(
                            nom_usuari = usuariPare.nom_usuari,
                            avatar_url = usuariPare.avatar_url,
                            likes = likesPare,
                            dislikes = dislikesPare,
                            reaccioActual = reaccioPare
                        )

                        val comentarisRaw = repo.comentarisDao.getComentarisRespostes(targetId)
                        val comentarisDetallats = carregarDetallsComentaris(comentarisRaw, idUsuariLoguejat)


                        _uiState.value = ComentarisUiState(
                            comentaris = comentarisDetallats,
                            loading = false
                        ).copy(comentariPare = pareDetallat)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.message)
            }
        }
    }

    private suspend fun carregarDetallsComentaris(raw: List<models.Comentari>, idUsuariLoguejat: String?): List<models.Comentari> {
        return raw.map { comentari ->
            try {
                val usuari = repo.usuariDao.getUsuariPerId(comentari.id_usuari)
                val likes = repo.reaccioDao.getLikesComentari(comentari.id)
                val dislikes = repo.reaccioDao.getDislikesComentari(comentari.id)
                val reaccioActual = idUsuariLoguejat?.let { repo.reaccioDao.getReaccioUsuariComentari(comentari.id, it) }
                
                comentari.copy(
                    nom_usuari = usuari.nom_usuari, 
                    avatar_url = usuari.avatar_url,
                    likes = likes,
                    dislikes = dislikes,
                    reaccioActual = reaccioActual
                )
            } catch (e: Exception) {
                comentari
            }
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
                _uiState.value = _uiState.value.copy(error = e.message)
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
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun eliminarComentari(id: String, targetId: String, idUsuari: String?, targetType: String) {
        viewModelScope.launch {
            try {
                repo.comentarisDao.eliminarComentari(id)
                carregarDades(targetId, idUsuari, targetType)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun reaccionarComentari(comentariId: String, targetId: String, idUsuari: String, tipus: String, targetType: String) {
        viewModelScope.launch {
            try {
                repo.reaccioDao.canviarReaccioComentari(comentariId, idUsuari, tipus)
                carregarDades(targetId, idUsuari, targetType)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun reaccionarPost(postId: String, idUsuari: String, tipus: String) {
        viewModelScope.launch {
            try {
                repo.reaccioDao.canviarReaccio(postId, idUsuari, tipus)
                carregarDades(postId, idUsuari, "post")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun reaccionarPresentacio(presId: String, idUsuari: String, tipus: String) {
        viewModelScope.launch {
            try {
                repo.reaccioDao.canviarReaccioPresentacio(presId, idUsuari, tipus)
                carregarDades(presId, idUsuari, "presentacio")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
