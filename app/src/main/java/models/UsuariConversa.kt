package models

import kotlinx.serialization.Serializable

@Serializable
data class UsuariConversa(
    val id_usuari: String? = null,
    val nom_usuari: String? = null,
    val avatar_url: String? = null
)