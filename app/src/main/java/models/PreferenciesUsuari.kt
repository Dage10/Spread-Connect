package models

import kotlinx.serialization.Serializable

@Serializable
data class PreferenciesUsuari(
    val id: String,
    val id_usuari: String,
    val llenguatge: String = "Español",
    val tema: String = "Clar",
    val rebre_notificacions: Boolean = true
)