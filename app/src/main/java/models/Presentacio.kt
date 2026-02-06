package models

import kotlinx.serialization.Serializable

@Serializable
data class Presentacio(
    val id: String,
    val id_usuari: String,
    val titol: String,
    val contingut_presentacio: String,
    val imatge_url: String?,
    val area_id: String,
    val created_at: String,
    val updated_at: String,
    val nom_usuari: String? = null
)