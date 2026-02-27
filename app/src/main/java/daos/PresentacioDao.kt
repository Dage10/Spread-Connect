package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Presentacio

class PresentacioDao {

    suspend fun getPresentacionsPerArea(areaId: String): List<Presentacio> =
        SupabaseClient.client
            .from("presentacions")
            .select { filter { eq("area_id", areaId) } }
            .decodeList()

    suspend fun getPresentacioPerId(id: String): Presentacio =
        SupabaseClient.client
            .from("presentacions")
            .select { filter { eq("id", id) } }
            .decodeList<Presentacio>()
            .first()

    suspend fun crearPresentacio(
        idUsuari: String,
        titol: String,
        contingut: String,
        areaId: String,
        imatgeUrl: String?
    ): Presentacio {
        val nova = buildJsonObject {
            put("id_usuari", idUsuari)
            put("titol", titol)
            put("contingut_presentacio", contingut)
            put("area_id", areaId)
            if (imatgeUrl != null) {
                put("imatge_url", imatgeUrl)
            }
        }

        return try {
            SupabaseClient.client
                .from("presentacions")
                .insert(nova) {
                    select()
                }
                .decodeList<Presentacio>()
                .firstOrNull() ?: throw Exception("Error en crear la presentació")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun editarPresentacio(
        id: String,
        titol: String,
        contingut: String,
        imatgeUrl: String?
    ): Presentacio {
        val data = buildJsonObject {
            put("titol", titol)
            put("contingut_presentacio", contingut)
            if (imatgeUrl != null) {
                put("imatge_url", imatgeUrl)
            }
        }
        return try {
            SupabaseClient.client
                .from("presentacions")
                .update(data) {
                    filter { eq("id", id) }
                    select()
                }
                .decodeList<Presentacio>()
                .firstOrNull() ?: throw Exception("Error en editar la presentació")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun eliminarPresentacio(id: String) {
        try {
            SupabaseClient.client
                .from("presentacions")
                .delete { filter { eq("id", id) } }
        } catch (e: Exception) {
            throw e
        }
    }
}
