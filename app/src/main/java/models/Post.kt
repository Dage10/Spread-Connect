package models

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String,
    val id_usuari: String,
    val titol: String,
    val descripcio: String,
    val area_id: String,
    val created_at: String,
    val updated_at: String,
    val nom_usuari: String? = null,
    val imatge_url: String? = null,
    val avatar_url: String? = null,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val reaccioActual: String? = null
)