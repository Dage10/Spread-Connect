package models

data class LoginUiState(
    val loading: Boolean = false,
    val usuari: Usuari? = null,
    val error: String? = null
)
