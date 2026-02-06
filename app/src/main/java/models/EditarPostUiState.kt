package models

data class EditarPostUiState(
    val loading: Boolean = false,
    val post: Post? = null,
    val postActualitzat: Post? = null,
    val postEliminat: Boolean = false,
    val error: String? = null
)