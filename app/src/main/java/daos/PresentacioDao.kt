package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Presentacio
import java.time.Instant

class PresentacioDao {

    @Serializable
    private data class PresentacioAmbUsuari(
        val id: String,
        val id_usuari: String,
        val titol: String,
        val contingut_presentacio: String,
        val imatge_url: String? = null,
        val area_id: String,
        val created_at: String,
        val updated_at: String,
        val usuaris: UsuariInfo? = null
    ) {
        @Serializable
        data class UsuariInfo(
            val nom_usuari: String? = null,
            val avatar_url: String? = null
        )

        fun toPresentacio() = Presentacio(
            id = id, id_usuari = id_usuari, titol = titol,
            contingut_presentacio = contingut_presentacio, imatge_url = imatge_url,
            area_id = area_id, created_at = created_at, updated_at = updated_at,
            nom_usuari = usuaris?.nom_usuari,
            avatar_url = usuaris?.avatar_url
        )
    }

    suspend fun getPresentacionsAmbUsuari(areaId: String): List<Presentacio> =
        SupabaseClient.client.from("presentacions")
            .select(Columns.raw("*, usuaris(nom_usuari, avatar_url)")) {
                filter { eq("area_id", areaId) }
            }
            .decodeList<PresentacioAmbUsuari>()
            .map { it.toPresentacio() }

    suspend fun getPresentacioPerId(id: String): Presentacio =
        SupabaseClient.client.from("presentacions").select { filter { eq("id", id) } }.decodeSingle()

    suspend fun getPresentacionsPerUsuari(idUsuari: String): List<Presentacio> =
        SupabaseClient.client.from("presentacions")
            .select(Columns.raw("id, id_usuari, titol, contingut_presentacio, imatge_url, area_id, created_at, updated_at, usuaris(nom_usuari, avatar_url)")) {
                filter { eq("id_usuari", idUsuari) }
            }
            .decodeList<PresentacioAmbUsuari>()
            .map { it.toPresentacio() }

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
        SupabaseClient.client
            .from("presentacions")
            .update(data) {
                filter { eq("id", id) }
            }
        return getPresentacioPerId(id)
    }

    suspend fun eliminarPresentacio(id: String) = SupabaseClient.client.from("presentacions").delete { filter { eq("id", id) } }
}
