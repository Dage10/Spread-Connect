package models

data class EditarPresentacioUiState(
    val loading: Boolean = false,
    val presentacio: Presentacio? = null,
    val presentacioActualitzada: Presentacio? = null,
    val presentacioEliminada: Boolean = false,
    val error: String? = null
)