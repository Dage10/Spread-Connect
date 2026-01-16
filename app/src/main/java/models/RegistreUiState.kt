import models.Usuari

data class RegistreUiState(
    val loading: Boolean = false,
    val usuariCreat: Usuari? = null,
    val error: String? = null
)
