package daos

import com.daviddam.clickconnect.R
import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import models.Etiqueta
import models.EtiquetaPost
import models.Post
import java.time.Instant

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
                .firstOrNull() ?: throw Exception(R.string.error_en_crear_post.toString())
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun editarPost(idPost: String, titol: String, descripcio: String, imatgeUrl: String?): Post {
        val data = buildJsonObject {
            put("titol", titol)
            put("descripcio", descripcio)
            put("updated_at", Instant.now().toString())
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
                .firstOrNull() ?: throw Exception(R.string.error_en_editar_post.toString())
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

    suspend fun getEtiquetesPost(postId: String): List<Etiqueta> =
        SupabaseClient.client
            .from("posts_etiquetes")
            .select(Columns.list("id_post", "id_etiqueta", "etiquetes(id,nom)")) {
                filter { eq("id_post", postId) }
            }
            .decodeList<EtiquetaPost>()
            .map { row ->
                Etiqueta(
                    id = row.etiquetes.id,
                    nom = row.etiquetes.nom,
                    idPost = row.id_post
                )
            }

    suspend fun crearEtiqueta(nom: String, postId: String): Etiqueta {
        val existent = SupabaseClient.client
            .from("etiquetes")
            .select {
                filter { eq("nom", nom) }
            }
            .decodeList<Etiqueta>()
            .firstOrNull()

        val etiquetaNoua = existent ?: SupabaseClient.client
            .from("etiquetes")
            .insert(
                buildJsonObject {
                    put("nom", nom)
                }
            ) {
                select()
            }
            .decodeList<Etiqueta>()
            .first()

        SupabaseClient.client
            .from("posts_etiquetes")
            .insert(
                buildJsonObject {
                    put("id_post", postId)
                    put("id_etiqueta", etiquetaNoua.id)
                }
            )

        return Etiqueta(
            id = etiquetaNoua.id,
            nom = etiquetaNoua.nom,
            idPost = postId
        )
    }

    suspend fun editarEtiqueta(id: String, nom: String): Etiqueta {
        val data = buildJsonObject { put("nom", nom) }
        val updatedTag = SupabaseClient.client.from("etiquetes")
            .update(data) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle<JsonObject>()

        return Etiqueta(
            id = updatedTag["id"]?.jsonPrimitive?.content ?: "",
            nom = updatedTag["nom"]?.jsonPrimitive?.content ?: "",
            idPost = ""
        )
    }

    suspend fun eliminarEtiqueta(idEtiqueta: String, postId: String) {
        SupabaseClient.client
            .from("posts_etiquetes")
            .delete {
                filter {
                    eq("id_etiqueta", idEtiqueta)
                    eq("id_post", postId)
                }
            }
    }
}
