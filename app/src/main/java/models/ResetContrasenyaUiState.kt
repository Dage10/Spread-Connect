package models

import util.UiText

data class ResetContrasenyaUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: UiText? = null,
    val step: Int = 1,
    val email: String = "",
    val codi: String = "",
    val novaContrasenya: String = ""
)
