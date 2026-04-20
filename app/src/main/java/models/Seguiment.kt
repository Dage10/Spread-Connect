package models

import kotlinx.serialization.Serializable

@Serializable
data class Seguiment(
    val id: String,
    val id_seguidor: String,
    val id_seguit: String,
    val created_at: String
)
