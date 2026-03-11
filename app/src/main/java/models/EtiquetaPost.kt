package models

import kotlinx.serialization.Serializable

@Serializable
data class EtiquetaPost(
    val id_post: String,
    val id_etiqueta: String,
    val etiquetes: Etiqueta
)