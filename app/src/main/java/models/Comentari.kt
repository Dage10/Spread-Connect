package models

import kotlinx.serialization.Serializable

@Serializable
data class Comentari(
    val id: String,
    val id_post: String? = null,
    val id_presentacio: String? = null,
    val id_comentari_pare: String? = null,
    val id_usuari: String,
    val contingut: String,
    val imatge_url: String? = null,
    val created_at: String,
    val updated_at: String,
    val nom_usuari: String? = null,
    val avatar_url: String? = null,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val reaccioActual: String? = null
)
