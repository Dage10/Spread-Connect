package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import models.Reaccio
import java.time.Instant
import java.util.UUID

class ReaccioDao {

    private suspend fun getLikesGeneric(taula: String, columna: String, id: String): Int = try {
        SupabaseClient.client.from(taula)
            .select {
                filter {
                    eq(columna, id)
                    eq("tipus", "like")
                }
            }
            .decodeList<Reaccio>()
            .size
    } catch (_: Exception) {
        0
    }

    private suspend fun getDislikesGeneric(taula: String, columna: String, id: String): Int = try {
        SupabaseClient.client.from(taula)
            .select {
                filter {
                    eq(columna, id)
                    eq("tipus", "dislike")
                }
            }
            .decodeList<Reaccio>()
            .size
    } catch (_: Exception) {
        0
    }

    private suspend fun getReaccioUsuariGeneric(taula: String, columna: String, id: String,
        idUsuari: String): String? = try {
        SupabaseClient.client.from(taula)
            .select {
                filter {
                    eq(columna, id)
                    eq("id_usuari", idUsuari)
                }
            }
            .decodeSingleOrNull<Reaccio>()
            ?.tipus
    } catch (_: Exception) {
        null
    }

    private suspend fun canviarReaccioGeneric(taula: String, columna: String,
        id: String, idUsuari: String,
        tipus: String, buildReaccio: (String) -> Reaccio) {
        try {
            val existent = SupabaseClient.client.from(taula)
                .select {
                    filter {
                        eq(columna, id)
                        eq("id_usuari", idUsuari)
                    }
                }
                .decodeSingleOrNull<Reaccio>()

            when {
                existent == null ->
                    SupabaseClient.client.from(taula).insert(buildReaccio(id))

                existent.tipus == tipus ->
                    SupabaseClient.client.from(taula).delete {
                        filter { eq("id", existent.id) }
                    }

                else ->
                    SupabaseClient.client.from(taula).update({
                        set("tipus", tipus)
                        set("created_at", Instant.now().toString())
                    }) {
                        filter { eq("id", existent.id) }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getLikes(idPost: String) =
        getLikesGeneric("reaccions_posts", "id_post", idPost)

    suspend fun getDislikes(idPost: String) =
        getDislikesGeneric("reaccions_posts", "id_post", idPost)

    suspend fun getReaccioUsuari(idPost: String, idUsuari: String) =
        getReaccioUsuariGeneric("reaccions_posts", "id_post", idPost, idUsuari)

    suspend fun canviarReaccio(idPost: String, idUsuari: String, tipus: String) =
        canviarReaccioGeneric("reaccions_posts", "id_post", idPost, idUsuari, tipus) {
            Reaccio(
                id = UUID.randomUUID().toString(),
                id_post = it,
                id_usuari = idUsuari,
                tipus = tipus,
                created_at = Instant.now().toString()
            )
        }


    suspend fun getLikesPresentacio(idPresentacio: String) =
        getLikesGeneric("reaccions_presentacions", "id_presentacio", idPresentacio)

    suspend fun getDislikesPresentacio(idPresentacio: String) =
        getDislikesGeneric("reaccions_presentacions", "id_presentacio", idPresentacio)

    suspend fun getReaccioUsuariPresentacio(idPresentacio: String, idUsuari: String) =
        getReaccioUsuariGeneric("reaccions_presentacions", "id_presentacio", idPresentacio, idUsuari)

    suspend fun canviarReaccioPresentacio(idPresentacio: String, idUsuari: String, tipus: String) =
        canviarReaccioGeneric("reaccions_presentacions", "id_presentacio", idPresentacio, idUsuari, tipus) {
            Reaccio(
                id = UUID.randomUUID().toString(),
                id_presentacio = it,
                id_usuari = idUsuari,
                tipus = tipus,
                created_at = Instant.now().toString()
            )
        }

    suspend fun getLikesComentari(idComentari: String) =
        getLikesGeneric("reaccions_comentaris", "id_comentari", idComentari)

    suspend fun getDislikesComentari(idComentari: String) =
        getDislikesGeneric("reaccions_comentaris", "id_comentari", idComentari)

    suspend fun getReaccioUsuariComentari(idComentari: String, idUsuari: String) =
        getReaccioUsuariGeneric("reaccions_comentaris", "id_comentari", idComentari, idUsuari)

    suspend fun canviarReaccioComentari(idComentari: String, idUsuari: String, tipus: String) =
        canviarReaccioGeneric("reaccions_comentaris", "id_comentari", idComentari, idUsuari, tipus) {
            Reaccio(
                id = UUID.randomUUID().toString(),
                id_comentari = it,
                id_usuari = idUsuari,
                tipus = tipus,
                created_at = Instant.now().toString()
            )
        }
}
