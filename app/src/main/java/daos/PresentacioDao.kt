package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import models.Presentacio
import java.time.Instant

class PresentacioDao {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getPresentacionsAmbUsuari(areaId: String): List<Presentacio> {
        val rows = SupabaseClient.client
            .from("presentacions")
            .select(Columns.raw("*, usuaris(nom_usuari, avatar_url)")) {
                filter { eq("area_id", areaId) }
            }

        return rows.decodeList<JsonObject>().map { row ->
            val usuari = row["usuaris"] as? JsonObject
            val base = json.decodeFromJsonElement(Presentacio.serializer(), row)
            base.copy(
                nom_usuari = usuari?.get("nom_usuari")?.jsonPrimitive?.content,
                avatar_url = usuari?.get("avatar_url")?.jsonPrimitive?.content
            )
        }
    }

    suspend fun getPresentacioPerId(id: String): Presentacio =
        SupabaseClient.client.from("presentacions").select { filter { eq("id", id) } }.decodeSingle()

    suspend fun getPresentacionsPerUsuari(idUsuari: String): List<Presentacio> {
        val rows = SupabaseClient.client
            .from("presentacions")
            .select(Columns.list("id", "id_usuari", "titol", "contingut_presentacio", "imatge_url", "area_id", "created_at", "updated_at", "usuaris(nom_usuari, avatar_url)")) {
                filter { eq("id_usuari", idUsuari) }
            }

        return rows.decodeList<JsonObject>().map { row ->
            val usuari = row["usuaris"] as? JsonObject
            val base = json.decodeFromJsonElement(Presentacio.serializer(), row)
            base.copy(
                nom_usuari = usuari?.get("nom_usuari")?.jsonPrimitive?.content,
                avatar_url = usuari?.get("avatar_url")?.jsonPrimitive?.content
            )
        }
    }

    suspend fun crearPresentacio(idUsuari: String, titol: String, contingut: String, areaId: String, img: String?): Presentacio {
        val nova = buildJsonObject {
            put("id_usuari", idUsuari); put("titol", titol); put("contingut_presentacio", contingut); put("area_id", areaId)
            img?.let { put("imatge_url", it) }
        }
        return SupabaseClient.client.from("presentacions").insert(nova) { select() }.decodeSingle()
    }

    suspend fun editarPresentacio(id: String, titol: String, contingut: String, img: String?): Presentacio {
        val data = buildJsonObject {
            put("titol", titol); put("contingut_presentacio", contingut); put("updated_at", Instant.now().toString())
            img?.let { put("imatge_url", it) }
        }
        return SupabaseClient.client.from("presentacions").update(data) { filter { eq("id", id) }; select() }.decodeSingle()
    }

    suspend fun eliminarPresentacio(id: String) = SupabaseClient.client.from("presentacions").delete { filter { eq("id", id) } }
}
