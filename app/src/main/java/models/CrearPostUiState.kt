package models

data class CrearPostUiState(
    val loading: Boolean = false,
    val postCreat: Post? = null,
    val error: String? = null
)