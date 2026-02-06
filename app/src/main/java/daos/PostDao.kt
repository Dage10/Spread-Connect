package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Post

class PostDao {

    suspend fun getPostsPerArea(areaId: String): List<Post> =
        SupabaseClient.client
            .from("posts")
            .select { filter { eq("area_id", areaId) } }
            .decodeList()

    suspend fun getPostPerId(id: String): Post =
        SupabaseClient.client
            .from("posts")
            .select { filter { eq("id", id) } }
            .decodeList<Post>()
            .first()

    suspend fun crearPost(idUsuari: String, titol: String, descripcio: String, areaId: String, imatgeUrl: String?): Post {
        val nouPost = buildJsonObject {
            put("id_usuari", idUsuari)
            put("titol", titol)
            put("descripcio", descripcio)
            put("area_id", areaId)
            if (imatgeUrl != null) {
                put("imatge_url", imatgeUrl)
            }
        }

        return try {
            SupabaseClient.client
                .from("posts")
                .insert(nouPost) { select() }
                .decodeList<Post>()
                .firstOrNull() ?: throw Exception("Error en crear el post")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun editarPost(idPost: String, titol: String, descripcio: String, imatgeUrl: String?): Post {
        val data = buildJsonObject {
            put("titol", titol)
            put("descripcio", descripcio)
            if (imatgeUrl != null) {
                put("imatge_url", imatgeUrl)
            }
        }
        return try {
            SupabaseClient.client
                .from("posts")
                .update(data) {
                    filter { eq("id", idPost) }
                    select()
                }
                .decodeList<Post>()
                .firstOrNull() ?: throw Exception("Error en editar el post")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun eliminarPost(idPost: String) {
        try {
            SupabaseClient.client
                .from("posts")
                .delete { filter { eq("id", idPost) } }
        } catch (e: Exception) {
            throw e
        }
    }
}
