package models

import kotlinx.serialization.Serializable

@Serializable
data class Usuari(
    val id: String,
    val nom_usuari: String,
    val email: String,
    val contrasenya_hash: String,
    val descripcio: String?,
    val created_at: String,
    val updated_at: String
)