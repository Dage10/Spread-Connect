package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.PreferenciesUsuari

@Serializable
private data class PreferenciesUpsertBody(
    val id_usuari: String,
    val llenguatge: String,
    val tema: String,
    @kotlinx.serialization.SerialName("rebre_notificacions") val rebre_notificacions: Boolean
)

class PreferenciesDao {

    suspend fun getPerUsuari(idUsuari: String): PreferenciesUsuari? =
        SupabaseClient.client
            .from("preferencies_usuari")
            .select {
                filter { eq("id_usuari", idUsuari) }
            }
            .decodeSingleOrNull<PreferenciesUsuari>()

    suspend fun insertPreferencies(
        idUsuari: String,
        llenguatge: String,
        tema: String,
        rebreNotificacions: Boolean
    ): PreferenciesUsuari {
        val body = PreferenciesUpsertBody(id_usuari = idUsuari, llenguatge = llenguatge, tema = tema, rebre_notificacions = rebreNotificacions)
        return SupabaseClient.client
            .from("preferencies_usuari")
            .insert(body) { select() }
            .decodeSingle<PreferenciesUsuari>()
    }

    suspend fun updatePreferencies(
        idUsuari: String,
        llenguatge: String,
        tema: String,
        rebreNotificacions: Boolean
    ): PreferenciesUsuari {
        val data = buildJsonObject {
            put("llenguatge", llenguatge)
            put("tema", tema)
            put("rebre_notificacions", rebreNotificacions)
        }
        return try {
            SupabaseClient.client
                .from("preferencies_usuari")
                .update(data) {
                    filter { eq("id_usuari", idUsuari) }
                    select()
                }
                .decodeSingle<PreferenciesUsuari>()
        } catch (e: Exception) {
            throw e
        }
    }
}
