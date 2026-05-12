package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Etiqueta
import models.EtiquetaPost
import models.Post
import java.time.Instant

class PostDao {

    @Serializable
    private data class PostAmbUsuari(
        val id: String, val id_usuari: String,
        val titol: String, val descripcio: String,
        val area_id: String, val created_at: String,
        val updated_at: String, val imatge_url: String? = null,
        val usuaris: UsuariInfo? = null) {
        @Serializable
        data class UsuariInfo(
            val nom_usuari: String? = null,
            val avatar_url: String? = null
        )

        fun toPost() = Post(
            id = id, id_usuari = id_usuari,
            titol = titol, descripcio = descripcio,
            area_id = area_id, created_at = created_at,
            updated_at = updated_at, imatge_url = imatge_url,
            nom_usuari = usuaris?.nom_usuari, avatar_url = usuaris?.avatar_url
        )
    }

    suspend fun getPostsAmbUsuari(areaId: String): List<Post> =
        SupabaseClient.client.from("posts")
            .select(Columns.raw("id, id_usuari, titol, descripcio, area_id, created_at, updated_at, imatge_url, usuaris(nom_usuari, avatar_url)")) {
                filter { eq("area_id", areaId) }
            }
            .decodeList<PostAmbUsuari>()
            .map { it.toPost() }


    suspend fun getPostPerId(id: String): Post =
        SupabaseClient.client.from("posts")
            .select { filter { eq("id", id) } }
            .decodeSingle()

    suspend fun getPostsPerUsuari(idUsuari: String): List<Post> =
        SupabaseClient.client.from("posts")
            .select(Columns.raw("id, id_usuari, titol, descripcio, area_id, created_at, updated_at, imatge_url, usuaris(nom_usuari, avatar_url)")) {
                filter { eq("id_usuari", idUsuari) }
            }
            .decodeList<PostAmbUsuari>()
            .map { it.toPost() }


    suspend fun crearPost(idUsuari: String, titol: String, desc: String, areaId: String, img: String?): Post {
        val nouPost = buildJsonObject {
            put("id_usuari", idUsuari); put("titol", titol); put("descripcio", desc); put("area_id", areaId)
            img?.let { put("imatge_url", it) }
        }
        return SupabaseClient.client.from("posts").insert(nouPost) { select() }.decodeSingle()
    }

    suspend fun editarPost(id: String, titol: String, desc: String, img: String?): Post {
        val data = buildJsonObject {
            put("titol", titol); put("descripcio", desc); put("updated_at", Instant.now().toString())
            put("imatge_url", img)
        }
        SupabaseClient.client
            .from("posts")
            .update(data) {
                filter { eq("id", id) }
            }
        return getPostPerId(id)
    }

    suspend fun eliminarPost(id: String) {
        SupabaseClient.client.from("posts_etiquetes").delete { filter { eq("id_post", id) } }
        SupabaseClient.client.from("posts").delete { filter { eq("id", id) } }
    }

    suspend fun getEtiquetesPost(postId: String): List<Etiqueta> =
        SupabaseClient.client.from("posts_etiquetes")
            .select(Columns.list("id_post", "id_etiqueta", "etiquetes(id,nom)")) { filter { eq("id_post", postId) } }
            .decodeList<EtiquetaPost>()
            .map { Etiqueta(it.etiquetes.id, it.etiquetes.nom, it.id_post) }

    suspend fun crearEtiqueta(nom: String, postId: String): Etiqueta {
        val existent = SupabaseClient.client.from("etiquetes").select { filter { eq("nom", nom) } }.decodeSingleOrNull<Etiqueta>()
        val tag = existent ?: SupabaseClient.client.from("etiquetes").insert(buildJsonObject { put("nom", nom) }) { select() }.decodeSingle()
        SupabaseClient.client.from("posts_etiquetes").insert(buildJsonObject { put("id_post", postId); put("id_etiqueta", tag.id) })
        return Etiqueta(tag.id, tag.nom, postId)
    }

    suspend fun editarEtiqueta(id: String, nom: String): Etiqueta {
        SupabaseClient.client.from("etiquetes")
            .update(buildJsonObject { put("nom", nom) }) {
                filter { eq("id", id) }
            }
        return SupabaseClient.client.from("etiquetes")
            .select {
                filter { eq("id", id) }
            }
            .decodeSingle<Etiqueta>()
    }

    suspend fun eliminarEtiqueta(idTag: String, idPost: String) =
        SupabaseClient.client.from("posts_etiquetes").delete { filter { eq("id_etiqueta", idTag); eq("id_post", idPost) } }
}
