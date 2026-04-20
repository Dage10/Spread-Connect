package models

import models.Post
import models.Presentacio
import util.UiText

data class PerfilUiState(
    val loading: Boolean = false,
    val usuari: Usuari? = null,
    val numSeguidors: Int? = null,
    val numSeguint: Int? = null,
    val isSeguint: Boolean = false,
    val posts: List<Post> = emptyList(),
    val presentacions: List<Presentacio> = emptyList(),
    val error: UiText? = null
)
