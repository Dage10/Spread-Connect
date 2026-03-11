package models

import kotlinx.serialization.Serializable

@Serializable
data class Etiqueta(
    val id: String,
    val nom: String,
    val idPost: String = ""
)