package models

import util.UiText

data class RegistreUiState(
    val loading: Boolean = false,
    val usuariCreat: Usuari? = null,
    val error: UiText? = null
)
