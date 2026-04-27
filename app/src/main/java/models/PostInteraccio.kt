package models

data class PostInteraccio(
    val id: String,
    val titol: String?,
    val contingut: String?,
    val createdAt: String,
    val likes: Int,
    val dislikes: Int,
    val totalInteraccions: Int
)