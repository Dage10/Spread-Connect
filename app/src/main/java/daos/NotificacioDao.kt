package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Notificacio

@Serializable
private data class NotificacioUpsertBody(
    val id_usuari: String,
    val tipus: String,
    val missatge: String,
    val llegida: Boolean = false,
    val id_target: String? = null,
    val target_type: String? = null
)

class NotificacioDao {

    suspend fun getNotificacionsSenseVeure(idUsuari: String): List<Notificacio> {
        return SupabaseClient.client
            .from("notificacions")
            .select {
                filter {
                    eq("id_usuari", idUsuari)
                    eq("llegida", false)
                }
            }
            .decodeList<Notificacio>()
    }

    suspend fun marcarComLlegida(ids: List<String>) {
        ids.forEach { id ->
            SupabaseClient.client.from("notificacions").update(buildJsonObject { put("llegida", true) }) {
                filter { eq("id", id) }
            }
        }
    }

    suspend fun guardarNotificacio(idUsuari: String, tipus: String, missatge: String, idTarget: String? = null, targetType: String? = null) {
        val body = NotificacioUpsertBody(
            id_usuari = idUsuari,
            tipus = tipus,
            missatge = missatge,
            id_target = idTarget,
            target_type = targetType
        )
        SupabaseClient.client.from("notificacions").insert(body)
    }

    suspend fun notificarSeguidors(autorId: String, tipus: String, missatge: String, idTarget: String? = null, targetType: String? = null) {
        val seguidors = SeguimentDao().getSeguidors(autorId)
        seguidors.forEach { followerId ->
            val prefs = PreferenciesDao().getPerUsuari(followerId)
            if (prefs?.rebre_notificacions != false) {
                guardarNotificacio(
                    idUsuari = followerId,
                    tipus = tipus,
                    missatge = missatge,
                    idTarget = idTarget,
                    targetType = targetType
                )
            }
        }
    }
}
