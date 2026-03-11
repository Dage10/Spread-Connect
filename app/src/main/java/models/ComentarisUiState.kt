package models

import util.UiText

data class ComentarisUiState(
    val loading: Boolean = false,
    val post: Post? = null,
    val presentacio: Presentacio? = null,
    val comentariPare: Comentari? = null,
    val comentaris: List<Comentari> = emptyList(),
    val error: UiText? = null
)
