package models

import util.UiText

data class ChatUiState(
    val loading: Boolean = false,
    val conversaId: String? = null,
    val missatges: List<Missatge> = emptyList(),
    val altreUsuari: UsuariConversa? = null,
    val idUsuariLoguejat: String? = null,
    val error: UiText? = null
)