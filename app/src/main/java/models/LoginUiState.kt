package models

import util.UiText

data class LoginUiState(
    val loading: Boolean = false,
    val usuari: Usuari? = null,
    val error: UiText? = null
)
