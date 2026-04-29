package models

import kotlinx.serialization.Serializable

@Serializable
data class Notificacio(
    val id: String,
    val id_usuari: String,
    val tipus: String,
    val missatge: String,
    val llegida: Boolean = false,
    val created_at: String,
    val id_target: String? = null,
    val target_type: String? = null
)
