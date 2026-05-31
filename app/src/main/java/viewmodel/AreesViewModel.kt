package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.spreadconnect.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.Area
import models.AreesUiState
import models.ReaccioStats
import repository.Repository
import util.TranslationUtil
import util.UiText

class AreesViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AreesUiState())
    val uiState: StateFlow<AreesUiState> = _uiState
    private var idUsuariActual: String? = null

    fun carregarArees(idUsuari: String?) {
        idUsuariActual = idUsuari
        _uiState.value = _uiState.value.copy(loading = true)
        viewModelScope.launch {
            try {
                var nomUsuari: String? = null
                var avatarUrl: String? = null
                var idiomaUsuari = "Español"

                if (idUsuari != null) {
                    try {
                        val usuari = repo.usuariDao.getUsuariPerId(idUsuari)
                        nomUsuari = usuari.nom_usuari
                        avatarUrl = usuari.avatar_url
                        repo.preferenciesDao.getPerUsuari(idUsuari)?.let { idiomaUsuari = it.llenguatge }
                    } catch (_: Exception) {
                        _uiState.value = _uiState.value.copy(
                            error = UiText.StringResource(R.string.usuari_no_trobat),
                            loading = false
                        )
                        return@launch
                    }
                }

                val llistaOriginal = repo.areaDao.getArees()
                val primera = llistaOriginal.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    areas = llistaOriginal,
                    areaSeleccionada = primera,
                    nomUsuari = nomUsuari,
                    avatarUrl = avatarUrl
                )

                primera?.let { carregarPerArea(it.id) }

                try {
                    val nomsTraduits = TranslationUtil.translateList(
                        llistaOriginal.map { it.nom },
                        idiomaUsuari
                    )
                    val llistaTraduida = llistaOriginal.mapIndexed { i, area ->
                        area.copy(nom = nomsTraduits.getOrElse(i) { area.nom })
                    }
                    val seleccionadaId = _uiState.value.areaSeleccionada?.id
                    _uiState.value = _uiState.value.copy(
                        areas = llistaTraduida,
                        areaSeleccionada = llistaTraduida.find { it.id == seleccionadaId }
                            ?: llistaTraduida.firstOrNull()
                    )
                } catch (_: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = UiText.StringResource(R.string.error_traduccio)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = UiText.DynamicString(e.message ?: "Error")
                )
            }
        }
    }

    fun refrescarArea() {
        _uiState.value.areaSeleccionada?.id?.let { carregarPerArea(it) }
    }

    fun seleccionarArea(area: Area) {
        _uiState.value = _uiState.value.copy(areaSeleccionada = area)
        carregarPerArea(area.id)
    }

    private fun carregarPerArea(areaId: String) {
        viewModelScope.launch {
            try {
                val posts = repo.postDao.getPostsAmbUsuari(areaId)
                val postsIds = posts.map { it.id }

                val likesIDislikesPerPost = repo.reaccioDao.getStatsPosts(postsIds, idUsuariActual)
                val numeroComentarisPosts = repo.comentarisDao.getNumComentarisPerPosts(postsIds)
                val etiquetesPerPost = try {
                    repo.postDao.getEtiquetesNomsPerPosts(postsIds)
                } catch (_: Exception) {
                    emptyMap()
                }

                val postsDetallats = posts.map { post ->
                    val reaccions = likesIDislikesPerPost[post.id] ?: ReaccioStats()
                    val likes = reaccions.likes
                    val dislikes = reaccions.dislikes
                    val reaccioActual = reaccions.reaccioActual
                    val numComentaris = numeroComentarisPosts[post.id] ?: 0
                    val etiquetes = etiquetesPerPost[post.id] ?: emptyList()

                    post.copy(
                        likes = likes,
                        dislikes = dislikes,
                        reaccioActual = reaccioActual,
                        numComentaris = numComentaris,
                        etiquetes = etiquetes
                    )
                }

                val presentacions = repo.presentacioDao.getPresentacionsAmbUsuari(areaId)
                val presentacionsIds = presentacions.map { it.id }

                val likesIDislikesPerPresentacio = repo.reaccioDao.getStatsPresentacions(presentacionsIds, idUsuariActual)
                val numeroComentarisPresentacio = repo.comentarisDao.getNumComentarisPerPresentacions(presentacionsIds)

                val presentacionsDetallades = presentacions.map { presentacio ->
                    val reaccions = likesIDislikesPerPresentacio[presentacio.id] ?: ReaccioStats()
                    val likes = reaccions.likes
                    val dislikes = reaccions.dislikes
                    val reaccioActual = reaccions.reaccioActual
                    val numComentaris = numeroComentarisPresentacio[presentacio.id] ?: 0
                    presentacio.copy(
                        likes = likes,
                        dislikes = dislikes,
                        reaccioActual = reaccioActual,
                        numComentaris = numComentaris
                    )
                }

                _uiState.value = _uiState.value.copy(
                    posts = postsDetallats,
                    presentacions = presentacionsDetallades,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = UiText.DynamicString(e.message ?: "Error")
                )
            }
        }
    }

    fun reaccionarPost(post: models.Post, tipus: String) {
        val usuari = idUsuariActual ?: return
        viewModelScope.launch {
            try {
                repo.reaccioDao.canviarReaccio(post.id, usuari, tipus)
                refrescarArea()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = UiText.DynamicString("Error: ${e.message}"))
            }
        }
    }

    fun reaccionarPresentacio(presentacio: models.Presentacio, tipus: String) {
        val usuari = idUsuariActual ?: return
        viewModelScope.launch {
            try {
                repo.reaccioDao.canviarReaccioPresentacio(presentacio.id, usuari, tipus)
                refrescarArea()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = UiText.DynamicString("Error: ${e.message}"))
            }
        }
    }
}
