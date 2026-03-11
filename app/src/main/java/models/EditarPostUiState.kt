package models

import util.UiText

data class EditarPostUiState(
    val loading: Boolean = false,
    val post: Post? = null,
    val postActualitzat: Post? = null,
    val postEliminat: Boolean = false,
    val error: UiText? = null
)
