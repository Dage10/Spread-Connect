package models

import util.UiText

data class CrearPresentacioUiState(
    val loading: Boolean = false,
    val presentacioCreada: Presentacio? = null,
    val error: UiText? = null
)
