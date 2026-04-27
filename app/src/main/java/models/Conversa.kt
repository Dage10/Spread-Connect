package models

import kotlinx.serialization.Serializable

@Serializable
data class Conversa(
    val id: String,
    val created_at: String,
    val usuaris: List<UsuariConversa>? = null,
    val ultim_missatge: Missatge? = null
)
