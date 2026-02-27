package models

import kotlinx.serialization.Serializable

@Serializable
data class Reaccio(
    val id: String,
    val id_post: String? = null,
    val id_presentacio: String? = null,
    val id_usuari: String,
    val tipus: String,
    val created_at: String
)