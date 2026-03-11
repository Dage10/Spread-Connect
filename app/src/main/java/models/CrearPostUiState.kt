package models

import util.UiText

data class CrearPostUiState(
    val loading: Boolean = false,
    val postCreat: Post? = null,
    val error: UiText? = null
)
