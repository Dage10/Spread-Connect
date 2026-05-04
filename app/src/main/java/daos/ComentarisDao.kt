package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Comentari
import java.time.Instant

class ComentarisDao {

    @Serializable
    private data class ComentariAmbUsuari(
        val id: String, val id_post: String? = null,
        val id_presentacio: String? = null, val id_comentari_pare: String? = null,
        val id_usuari: String, val contingut: String,
        val imatge_url: String? = null, val created_at: String,
        val updated_at: String, val usuaris: UsuariInfo? = null) {
        @Serializable
        data class UsuariInfo(
            val nom_usuari: String? = null,
            val avatar_url: String? = null
        )

        fun toComentari() = Comentari(
            id = id, id_post = id_post, id_presentacio = id_presentacio,
            id_comentari_pare = id_comentari_pare, id_usuari = id_usuari,
            contingut = contingut, imatge_url = imatge_url, created_at = created_at,
            updated_at = updated_at, nom_usuari = usuaris?.nom_usuari,
            avatar_url = usuaris?.avatar_url
        )
    }

    suspend fun getNumComentarisPost(idPost: String): Int {
        return try {
            SupabaseClient.client
                .from("comentaris")
                .select {
                    filter {
                        eq("id_post", idPost)
                    }
                }
                .decodeList<Comentari>()
                .size
        } catch (_: Exception) {
            0
        }
    }

    suspend fun getNumComentarisPresentacio(idPresentacio: String): Int {
        return try {
            SupabaseClient.client
                .from("comentaris")
                .select {
                    filter {
                        eq("id_presentacio", idPresentacio)
                    }
                }
                .decodeList<Comentari>()
                .size
        } catch (_: Exception) {
            0
        }
    }

    suspend fun getNumRespostes(comentariId: String): Int {
        return SupabaseClient.client
            .from("comentaris")
            .select(Columns.list("id")) { filter { eq("id_comentari_pare", comentariId) } }
            .decodeList<JsonObject>()
            .size
    }

    suspend fun getComentarisPost(idPost: String): List<Comentari> =
        getComentarisAmbUsuari("id_post", idPost)

    suspend fun getComentarisPresentacio(idPresentacio: String): List<Comentari> =
        getComentarisAmbUsuari("id_presentacio", idPresentacio)

    suspend fun getComentarisRespostes(idPare: String): List<Comentari> =
        getComentarisAmbUsuari("id_comentari_pare", idPare)

    private suspend fun getComentarisAmbUsuari(columna: String, valor: String): List<Comentari> =
        SupabaseClient.client.from("comentaris")
            .select(Columns.raw("id, id_post, id_presentacio, id_comentari_pare, id_usuari, contingut, imatge_url, created_at, updated_at, usuaris(nom_usuari, avatar_url)")) {
                filter { eq(columna, valor) }
            }
            .decodeList<ComentariAmbUsuari>()
            .map { it.toComentari() }

    suspend fun getComentariPerId(id: String): Comentari =
        SupabaseClient.client.from("comentaris")
            .select(Columns.list("id", "id_post", "id_presentacio", "id_comentari_pare", "id_usuari", "contingut", "imatge_url", "created_at", "updated_at")) {
                filter { eq("id", id) } }
            .decodeSingle()

    suspend fun crearComentari(idPost: String, idUsuari: String, contingut: String, img: String? = null): Comentari {
        val dades = buildJsonObject {
            put("id_post", idPost); put("id_usuari", idUsuari); put("contingut", contingut)
            img?.let { put("imatge_url", it) }
        }
        return SupabaseClient.client
            .from("comentaris").insert(dades) {
                select()
            }.decodeSingle()
    }

    suspend fun crearComentariPresentacio(idPres: String, idUsuari: String, cont: String, img: String? = null): Comentari {
        val dades = buildJsonObject {
            put("id_presentacio", idPres); put("id_usuari", idUsuari); put("contingut", cont)
            img?.let { put("imatge_url", it) }
        }
        return SupabaseClient.client
            .from("comentaris")
            .insert(dades) {
                select()
            }.decodeSingle()
    }

    suspend fun crearComentariResposta(idPare: String, idUsuari: String, cont: String, img: String? = null): Comentari {
        val dades = buildJsonObject {
            put("id_comentari_pare", idPare); put("id_usuari", idUsuari); put("contingut", cont)
            img?.let { put("imatge_url", it) }
        }
        return SupabaseClient.client
            .from("comentaris")
            .insert(dades) {
                select()
            }.decodeSingle()
    }

    suspend fun eliminarComentari(id: String) = SupabaseClient.client
        .from("comentaris")
        .delete {
            filter { eq("id", id) }
        }

    suspend fun editarComentari(id: String, contingut: String, imatgeUrl: String?): Comentari {
        val dades = buildJsonObject {
            put("contingut", contingut); put("updated_at", Instant.now().toString())
            imatgeUrl?.let { put("imatge_url", it) }
        }
        SupabaseClient.client.from("comentaris")
            .update(dades) {
                filter { eq("id", id) }
            }
        return getComentariPerId(id)
    }
}
