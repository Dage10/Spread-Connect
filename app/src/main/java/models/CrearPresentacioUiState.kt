package models

data class CrearPresentacioUiState(
    val loading: Boolean = false,
    val presentacioCreada: Presentacio? = null,
    val error: String? = null
)