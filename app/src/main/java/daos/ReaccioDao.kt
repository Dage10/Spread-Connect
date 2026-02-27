package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import models.Reaccio
import java.util.UUID

class ReaccioDao {

    suspend fun getLikes(idPost: String): Int {
        return try {
            SupabaseClient.client
                .from("reaccions_posts")
                .select {
                    filter {
                        eq("id_post", idPost)
                        eq("tipus", "like")
                    }
                }
                .decodeList<Reaccio>()
                .size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getDislikes(idPost: String): Int {
        return try {
            SupabaseClient.client
                .from("reaccions_posts")
                .select {
                    filter {
                        eq("id_post", idPost)
                        eq("tipus", "dislike")
                    }
                }
                .decodeList<Reaccio>()
                .size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getReaccioUsuari(idPost: String, idUsuari: String): String? {
        return try {
            SupabaseClient.client
                .from("reaccions_posts")
                .select {
                    filter {
                        eq("id_post", idPost)
                        eq("id_usuari", idUsuari)
                    }
                }
                .decodeList<Reaccio>()
                .firstOrNull()
                ?.tipus
        } catch (e: Exception) {
            null
        }
    }

    suspend fun canviarReaccio(idPost: String, idUsuari: String, tipus: String) {
        try {
            val reaccioExistent = SupabaseClient.client
                .from("reaccions_posts")
                .select {
                    filter {
                        eq("id_post", idPost)
                        eq("id_usuari", idUsuari)
                    }
                }
                .decodeList<Reaccio>()
                .firstOrNull()

            if (reaccioExistent != null) {
                if (reaccioExistent.tipus == tipus) {
                    SupabaseClient.client
                        .from("reaccions_posts")
                        .delete {
                            filter { eq("id", reaccioExistent.id) }
                        }
                } else {
                    SupabaseClient.client
                        .from("reaccions_posts")
                        .update({ set("tipus", tipus);set("created_at",java.time.Instant.now().toString())}) {
                            filter { eq("id", reaccioExistent.id) }
                        }
                }
            } else {
                val horaActual = java.time.Instant.now().toString()
                SupabaseClient.client
                    .from("reaccions_posts")
                    .insert(
                        Reaccio(
                            id = UUID.randomUUID().toString(),
                            id_post = idPost,
                            id_usuari = idUsuari,
                            tipus = tipus,
                            created_at = horaActual
                        )
                    )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun getLikesPresentacio(idPresentacio: String): Int {
        return try {
            SupabaseClient.client
                .from("reaccions_presentacions")
                .select {
                    filter {
                        eq("id_presentacio", idPresentacio)
                        eq("tipus", "like")
                    }
                }
                .decodeList<Reaccio>()
                .size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getDislikesPresentacio(idPresentacio: String): Int {
        return try {
            SupabaseClient.client
                .from("reaccions_presentacions")
                .select {
                    filter {
                        eq("id_presentacio", idPresentacio)
                        eq("tipus", "dislike")
                    }
                }
                .decodeList<Reaccio>()
                .size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getReaccioUsuariPresentacio(idPresentacio: String, idUsuari: String): String? {
        return try {
            SupabaseClient.client
                .from("reaccions_presentacions")
                .select {
                    filter {
                        eq("id_presentacio", idPresentacio)
                        eq("id_usuari", idUsuari)
                    }
                }
                .decodeList<Reaccio>()
                .firstOrNull()
                ?.tipus
        } catch (e: Exception) {
            null
        }
    }

    suspend fun canviarReaccioPresentacio(idPresentacio: String, idUsuari: String, tipus: String) {
        try {
            val reaccioExistent = SupabaseClient.client
                .from("reaccions_presentacions")
                .select {
                    filter {
                        eq("id_presentacio", idPresentacio)
                        eq("id_usuari", idUsuari)
                    }
                }
                .decodeList<Reaccio>()
                .firstOrNull()

            if (reaccioExistent != null) {
                if (reaccioExistent.tipus == tipus) {
                    SupabaseClient.client
                        .from("reaccions_presentacions")
                        .delete {
                            filter { eq("id", reaccioExistent.id) }
                        }
                } else {
                    SupabaseClient.client
                        .from("reaccions_presentacions")
                        .update({ set("tipus", tipus) }) {
                            filter { eq("id", reaccioExistent.id) }
                        }
                }
            } else {
                val horaActual = java.time.Instant.now().toString()
                SupabaseClient.client
                    .from("reaccions_presentacions")
                    .insert(
                            Reaccio(
                                id = UUID.randomUUID().toString(),
                                id_presentacio = idPresentacio,
                                id_usuari = idUsuari,
                                tipus = tipus,
                                created_at = horaActual
                            )
                    )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
