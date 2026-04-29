package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.spreadconnect.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.Area
import models.AreesUiState
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
                        
                        val prefs = repo.preferenciesDao.getPerUsuari(idUsuari)
                        if (prefs != null) {
                            idiomaUsuari = prefs.llenguatge
                        }
                    } catch (e: Exception) {
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
                
                if (primera != null) {
                    carregarPerArea(primera.id)
                }

                viewModelScope.launch {
                    try {
                        val nomsOriginals = llistaOriginal.map { it.nom }
                        val nomsTraduits = TranslationUtil.translateList(nomsOriginals, idiomaUsuari)
                        
                        val llistaTraduida = llistaOriginal.mapIndexed { index, area ->
                            area.copy(nom = nomsTraduits.getOrElse(index) { area.nom })
                        }

                        val seleccionadaActual = _uiState.value.areaSeleccionada
                        val novaSeleccionada = llistaTraduida.find { it.id == seleccionadaActual?.id }

                        _uiState.value = _uiState.value.copy(
                            areas = llistaTraduida,
                            areaSeleccionada = novaSeleccionada ?: llistaTraduida.firstOrNull()
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            error = UiText.StringResource(R.string.error_traduccio)
                        )
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

    fun refrescarArea() {
        val area = _uiState.value.areaSeleccionada
        if (area != null) {
            carregarPerArea(area.id)
        }
    }

    fun seleccionarArea(area: Area) {
        _uiState.value = _uiState.value.copy(areaSeleccionada = area)
        carregarPerArea(area.id)
    }

    private fun carregarPerArea(areaId: String) {
        viewModelScope.launch {
            try {
                val postsDetallats = repo.postDao.getPostsAmbUsuari(areaId).map { post ->
                    val likes = repo.reaccioDao.getLikes(post.id)
                    val dislikes = repo.reaccioDao.getDislikes(post.id)
                    val reaccioActual = idUsuariActual?.let { repo.reaccioDao.getReaccioUsuari(post.id, it) }
                    val tags = try { repo.postDao.getEtiquetesPost(post.id).map { it.nom } } catch (_: Exception) { emptyList() }
                    
                    post.copy(
                        likes = likes,
                        dislikes = dislikes,
                        reaccioActual = reaccioActual,
                        etiquetes = tags
                    )
                }

                val presentacionsDetallades = repo.presentacioDao.getPresentacionsAmbUsuari(areaId).map { pres ->
                    val likes = repo.reaccioDao.getLikesPresentacio(pres.id)
                    val dislikes = repo.reaccioDao.getDislikesPresentacio(pres.id)
                    val reaccioActual = idUsuariActual?.let { repo.reaccioDao.getReaccioUsuariPresentacio(pres.id, it) }
                    
                    pres.copy(
                        likes = likes,
                        dislikes = dislikes,
                        reaccioActual = reaccioActual
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
        idUsuariActual?.let { usuari ->
            viewModelScope.launch {
                try {
                    repo.reaccioDao.canviarReaccio(post.id, usuari, tipus)
                    refrescarArea()
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = UiText.DynamicString("Error: ${e.message}"))
                }
            }
        }
    }

    fun reaccionarPresentacio(presentacio: models.Presentacio, tipus: String) {
        idUsuariActual?.let { usuari ->
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
}
