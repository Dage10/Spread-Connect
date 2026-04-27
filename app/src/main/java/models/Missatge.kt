package models

import kotlinx.serialization.Serializable

@Serializable
data class Missatge(
    val id: String,
    val id_conversa: String,
    val id_usuari: String,
    val contingut: String? = null,
    val imatge_url: String? = null,
    val created_at: String,
    val nom_usuari: String? = null,
    val avatar_url: String? = null
)