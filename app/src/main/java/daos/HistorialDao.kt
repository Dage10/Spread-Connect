package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import models.ActivitatHistorial
import models.PostInteraccio

class HistorialDao {

    private fun JsonObject.getString(key: String): String? {
        return (this[key] as? JsonPrimitive)?.content
    }

    suspend fun getHistorialUsuari(idUsuari: String): List<ActivitatHistorial> {
        val resultat = mutableListOf<ActivitatHistorial>()

        val posts = SupabaseClient.client
            .from("posts")
            .select(Columns.list("id", "id_usuari", "titol", "descripcio", "created_at")) {
                filter {
                    eq("id_usuari", idUsuari)
                }
            }
            .decodeList<JsonObject>()

        resultat.add(
            ActivitatHistorial(
                id = "total_posts",
                id_usuari = idUsuari,
                tipus = "total_posts",
                num_posts = posts.size
            )
        )

        val postIds = posts.mapNotNull { it.getString("id") }
        val reaccions = if (postIds.isNotEmpty()) {
            SupabaseClient.client
                .from("reaccions_posts")
                .select(Columns.list("id_post", "tipus")) {
                    filter {
                        isIn("id_post", postIds)
                    }
                }
                .decodeList<JsonObject>()
        } else emptyList()

        val reaccionsPerPost = reaccions.groupBy { it.getString("id_post") }

        val postMesInteraccions = posts.mapNotNull { post ->
            val postId = post.getString("id") ?: return@mapNotNull null
            val titol = post.getString("titol")
            val contingut = post.getString("descripcio")
            val createdAt = post.getString("created_at") ?: ""

            val r = reaccionsPerPost[postId].orEmpty()
            val likes = r.count { it.getString("tipus") == "like" }
            val dislikes = r.count { it.getString("tipus") == "dislike" }
            val total = likes + dislikes

            PostInteraccio(postId, titol, contingut, createdAt, likes, dislikes, total)
        }.maxWithOrNull(compareBy({ it.totalInteraccions }, { it.createdAt }))

        if (postMesInteraccions != null) {
            resultat.add(
                ActivitatHistorial(
                    id = postMesInteraccions.id,
                    id_usuari = idUsuari,
                    tipus = "post_mes_interaccions",
                    titol_post = postMesInteraccions.titol,
                    contingut = postMesInteraccions.contingut,
                    created_at = postMesInteraccions.createdAt,
                    num_likes = postMesInteraccions.likes,
                    num_dislikes = postMesInteraccions.dislikes
                )
            )
        }

        posts.maxByOrNull { it.getString("created_at") ?: "" }?.let { ultimPost ->
            resultat.add(
                ActivitatHistorial(
                    id = ultimPost.getString("id") ?: "",
                    id_usuari = idUsuari,
                    tipus = "ultim_post",
                    titol_post = ultimPost.getString("titol"),
                    contingut = ultimPost.getString("descripcio"),
                    created_at = ultimPost.getString("created_at") ?: ""
                )
            )
        }

        val presentacions = SupabaseClient.client
            .from("presentacions")
            .select(Columns.list("id", "id_usuari", "titol", "contingut_presentacio", "created_at")) {
                filter {
                    eq("id_usuari", idUsuari)
                }
            }
            .decodeList<JsonObject>()

        resultat.add(
            ActivitatHistorial(
                id = "total_presentacions",
                id_usuari = idUsuari,
                tipus = "total_presentacions",
                num_presentacions = presentacions.size
            )
        )

        presentacions.maxByOrNull { it.getString("created_at") ?: "" }?.let { ultimaPresentacio ->
            resultat.add(
                ActivitatHistorial(
                    id = ultimaPresentacio.getString("id") ?: "",
                    id_usuari = idUsuari,
                    tipus = "ultim_presentacio",
                    titol_presentacio = ultimaPresentacio.getString("titol"),
                    contingut = ultimaPresentacio.getString("contingut_presentacio"),
                    created_at = ultimaPresentacio.getString("created_at") ?: ""
                )
            )
        }

        val numComentaris = SupabaseClient.client
            .from("comentaris")
            .select(Columns.list("id")) {
                filter {
                    eq("id_usuari", idUsuari)
                }
            }
            .decodeList<JsonObject>()
            .size

        resultat.add(
            ActivitatHistorial(
                id = "comentaris",
                id_usuari = idUsuari,
                tipus = "comentaris",
                num_comentaris = numComentaris
            )
        )

        val numSeguidors = SupabaseClient.client
            .from("seguiments")
            .select(Columns.list("id")) {
                filter {
                    eq("id_seguit", idUsuari)
                }
            }
            .decodeList<JsonObject>()
            .size

        val numSeguint = SupabaseClient.client
            .from("seguiments")
            .select(Columns.list("id")) {
                filter {
                    eq("id_seguidor", idUsuari)
                }
            }
            .decodeList<JsonObject>()
            .size

        resultat.add(
            ActivitatHistorial(
                id = "estadistiques",
                id_usuari = idUsuari,
                tipus = "estadistiques",
                num_seguidors = numSeguidors,
                num_seguint = numSeguint
            )
        )

        return resultat
    }
}