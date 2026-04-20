package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import models.Comentari
import java.time.Instant

class ComentarisDao {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getComentarisPost(idPost: String): List<Comentari> =
        getComentarisAmbUsuari("id_post", idPost)

    suspend fun getComentarisPresentacio(idPresentacio: String): List<Comentari> =
        getComentarisAmbUsuari("id_presentacio", idPresentacio)

    suspend fun getComentarisRespostes(idPare: String): List<Comentari> =
        getComentarisAmbUsuari("id_comentari_pare", idPare)

    private suspend fun getComentarisAmbUsuari(columna: String, valor: String): List<Comentari> {
        val rows = SupabaseClient.client
            .from("comentaris")
            .select(Columns.list("id", "id_post", "id_presentacio", "id_comentari_pare", "id_usuari", "contingut", "imatge_url", "created_at", "updated_at", "usuaris(nom_usuari, avatar_url)")) {
                filter { eq(columna, valor) }
            }

        return rows.decodeList<JsonObject>().map { row ->
            val usuari = row["usuaris"] as? JsonObject
            val base = json.decodeFromJsonElement(Comentari.serializer(), row)
            base.copy(
                nom_usuari = usuari?.get("nom_usuari")?.jsonPrimitive?.content,
                avatar_url = usuari?.get("avatar_url")?.jsonPrimitive?.content
            )
        }
    }

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
        return SupabaseClient.client
            .from("comentaris")
            .update(dades) {
                filter { eq("id", id)}
                select()
            }.decodeSingle()
    }
}
