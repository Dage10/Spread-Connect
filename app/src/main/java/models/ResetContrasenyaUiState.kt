package models

data class ResetContrasenyaUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val step: Int = 1,
    val email: String = "",
    val codi: String = "",
    val novaContrasenya: String = ""
)