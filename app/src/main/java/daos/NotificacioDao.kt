package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Notificacio
import java.net.HttpURLConnection
import java.net.URL

private const val NOTIFICACIONS_FUNCIO_URL = "https://fvoouemimuhvwnzbetrl.supabase.co/functions/v1/notificacions-funcio"

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
        if (ids.isEmpty()) return

        SupabaseClient.client
            .from("notificacions")
            .update(buildJsonObject {
                put("llegida", true)
            }) {
            filter {
                isIn("id", ids)
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

    suspend fun notificarSeguidors(autorId: String, tipus: String, missatge: String, titolPost: String? = null, idTarget: String? = null, targetType: String? = null){
        val seguidors = SeguimentDao().getSeguidors(autorId)
        val seguidorsFiltrats = seguidors.filter { followerId ->
            followerId != autorId &&
                    PreferenciesDao().getPerUsuari(followerId)?.rebre_notificacions != false
        }

        seguidorsFiltrats.forEach { followerId ->
            guardarNotificacio(
                idUsuari = followerId,
                tipus = tipus,
                missatge = missatge,
                idTarget = idTarget,
                targetType = targetType
            )
        }

        if (seguidorsFiltrats.isNotEmpty()) {
            try {
                invocarFuncioNotificacions(autorId, titolPost ?: missatge, missatge, idTarget.orEmpty())
            } catch (_: Exception) {

            }
        }
    }

    private suspend fun invocarFuncioNotificacions(autorId: String, titol: String, missatge: String, postId: String) = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("idUsuariPublicador", autorId)
            put("titol", titol)
            put("missatge", missatge)
            put("postId", postId)
        }

        val url = URL(NOTIFICACIONS_FUNCIO_URL)
        (url.openConnection() as HttpURLConnection).run {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            outputStream.use { it.write(payload.toString().encodeToByteArray()) }

            if (responseCode !in 200..299) {
                error("Edge function returned HTTP $responseCode")
            }
        }
    }

    suspend fun marcarNotificacioFcmComLlegida(idUsuari: String, idTarget: String) {
        SupabaseClient.client
            .from("notificacions")
            .update(buildJsonObject { put("llegida", true) }) {
                filter {
                    eq("id_usuari", idUsuari)
                    eq("id_target", idTarget)
                    eq("llegida", false)
                }
            }
    }
}
