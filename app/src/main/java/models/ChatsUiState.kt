package models
import util.UiText

data class ChatsUiState(
    val loading: Boolean = false,
    val converses: List<Conversa> = emptyList(),
    val error: UiText? = null
)
