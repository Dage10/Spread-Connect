package models

import util.UiText

data class HistorialUiState(
    val loading: Boolean = false,
    val historial: List<ActivitatHistorial> = emptyList(),
    val error: UiText? = null
)