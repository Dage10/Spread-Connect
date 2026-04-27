package models

data class ActivitatHistorial(
    val id: String,
    val id_usuari: String,
    val tipus: String,
    val id_post: String? = null,
    val id_presentacio: String? = null,
    val id_usuari_relacionat: String? = null,
    val created_at: String = "",
    val titol_post: String? = null,
    val titol_presentacio: String? = null,
    val contingut: String? = null,
    val num_posts: Int = 0,
    val num_presentacions: Int = 0,
    val num_comentaris: Int = 0,
    val num_seguidors: Int = 0,
    val num_seguint: Int = 0,
    val num_likes: Int = 0,
    val num_dislikes: Int = 0
)