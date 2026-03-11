package models

import util.UiText

data class EditarPerfilUiState(
    val loading: Boolean = false,
    val usuari: Usuari? = null,
    val usuariActualitzat: Usuari? = null,
    val preferencies: PreferenciesUsuari? = null,
    val preferenciesActualitzades: PreferenciesUsuari? = null,
    val error: UiText? = null
)
