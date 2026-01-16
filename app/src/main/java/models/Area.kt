package models

import kotlinx.serialization.Serializable

@Serializable
data class Area(
    val id: String,
    val nom: String
)