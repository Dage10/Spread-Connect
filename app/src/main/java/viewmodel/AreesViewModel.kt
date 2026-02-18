package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.Area
import models.AreesUiState
import repository.Repository
import util.TranslationUtil

class AreesViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AreesUiState())
    val uiState: StateFlow<AreesUiState> = _uiState

    fun carregarArees(idUsuari: String?) {
        _uiState.value = _uiState.value.copy(loading = true)
        viewModelScope.launch {
            try {
                val llistaOriginal = repo.areaDao.getArees()
                val primera = llistaOriginal.firstOrNull()

                var nomUsuari: String? = null
                var avatarUrl: String? = null
                var idiomaUsuari = "Castellà" 

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
                        _uiState.value = _uiState.value.copy(error = "Error en carregar l'usuari: ${e.message}")
                    }
                }

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
                        _uiState.value = _uiState.value.copy(error = "Error en la traducció: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
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
                val postsRaw = repo.postDao.getPostsPerArea(areaId)
                val presentacionsRaw = repo.presentacioDao.getPresentacionsPerArea(areaId)

                val ids = (postsRaw.map {
                    it.id_usuari
                } + presentacionsRaw.map {
                    it.id_usuari
                }).distinct()

                val mapaUsuaris = ids.associateWith { id ->
                    try {
                        repo.usuariDao.getUsuariPerId(id).nom_usuari
                    } catch (e: Exception) {
                        null
                    }
                }
                val mapaAvatars = ids.associateWith { id ->
                    try {
                        repo.usuariDao.getUsuariPerId(id).avatar_url
                    } catch (e: Exception) {
                        null
                    }
                }

                _uiState.value = _uiState.value.copy(
                    posts = postsRaw.map {
                        it.copy(
                            nom_usuari = mapaUsuaris[it.id_usuari] ?: "Usuari",
                            avatar_url = mapaAvatars[it.id_usuari]
                        )
                    },
                    presentacions = presentacionsRaw.map {
                        it.copy(nom_usuari = mapaUsuaris[it.id_usuari] ?: "Usuari")
                    },
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
