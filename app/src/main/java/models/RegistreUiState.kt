package models

import util.UiText

data class RegistreUiState(
    val loading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: UiText? = null
)
