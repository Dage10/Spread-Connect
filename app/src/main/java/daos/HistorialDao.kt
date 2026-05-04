package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.Serializable
import models.ActivitatHistorial
import models.PostInteraccio

class HistorialDao {

    @Serializable
    private data class PostRow(
        val id: String,
        val titol: String? = null,
        val descripcio: String? = null,
        val created_at: String = ""
    )

    @Serializable
    private data class ReaccioRow(
        val id_post: String? = null,
        val tipus: String
    )

    @Serializable
    private data class PresentacioRow(
        val id: String,
        val titol: String? = null,
        val contingut_presentacio: String? = null,
        val created_at: String = ""
    )

    suspend fun getHistorialUsuari(idUsuari: String): List<ActivitatHistorial> {
        val resultat = mutableListOf<ActivitatHistorial>()

        val posts = SupabaseClient.client.from("posts")
            .select(Columns.list("id", "titol", "descripcio", "created_at")) {
                filter { eq("id_usuari", idUsuari) }
            }
            .decodeList<PostRow>()

        resultat.add(
            ActivitatHistorial(id = "total_posts", id_usuari = idUsuari, tipus = "total_posts", num_posts = posts.size)
        )

        val postIds = posts.map { it.id }

        val reaccions = if (postIds.isNotEmpty()) {
            SupabaseClient.client.from("reaccions_posts")
                .select(Columns.list("id_post", "tipus")) { filter { isIn("id_post", postIds) } }
                .decodeList<ReaccioRow>()
        } else emptyList()

        val reaccionsPerPost = reaccions.groupBy { it.id_post }

        posts.mapNotNull { post ->
            val r = reaccionsPerPost[post.id].orEmpty()
            val likes = r.count { it.tipus == "like" }
            val dislikes = r.count { it.tipus == "dislike" }
            PostInteraccio(post.id, post.titol, post.descripcio, post.created_at, likes, dislikes, likes + dislikes)
        }.maxWithOrNull(compareBy({ it.totalInteraccions }, { it.createdAt }))?.let {
            resultat.add(
                ActivitatHistorial(
                    id = it.id, id_usuari = idUsuari, tipus = "post_mes_interaccions",
                    titol_post = it.titol, contingut = it.contingut, created_at = it.createdAt,
                    num_likes = it.likes, num_dislikes = it.dislikes
                )
            )
        }

        posts.maxByOrNull { it.created_at }?.let {
            resultat.add(
                ActivitatHistorial(
                    id = it.id, id_usuari = idUsuari, tipus = "ultim_post",
                    titol_post = it.titol, contingut = it.descripcio, created_at = it.created_at
                )
            )
        }

        val presentacions = SupabaseClient.client.from("presentacions")
            .select(Columns.list("id", "titol", "contingut_presentacio", "created_at")) {
                filter { eq("id_usuari", idUsuari) }
            }
            .decodeList<PresentacioRow>()

        resultat.add(
            ActivitatHistorial(
                id = "total_presentacions",
                id_usuari = idUsuari,
                tipus = "total_presentacions",
                num_presentacions = presentacions.size
            )
        )

        presentacions.maxByOrNull { it.created_at }?.let {
            resultat.add(
                ActivitatHistorial(
                    id = it.id,
                    id_usuari = idUsuari,
                    tipus = "ultim_presentacio",
                    titol_presentacio = it.titol,
                    contingut = it.contingut_presentacio,
                    created_at = it.created_at
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