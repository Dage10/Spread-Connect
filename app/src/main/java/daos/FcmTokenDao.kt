package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object FcmTokenDao {

    suspend fun guardarTokenFcm(idUsuari: String, token: String): JsonObject? {
        if (idUsuari.isBlank() || token.isBlank()) return null

        val existingToken = SupabaseClient.client
            .from("fcm_tokens")
            .select { filter {
                eq("id_usuari", idUsuari)
                eq("token", token)
            } }
            .decodeSingleOrNull<JsonObject>()

        return if (existingToken != null) {
            val updateBody = buildJsonObject {
                put("activa", true)
                put("plataforma", "android")
            }
            SupabaseClient.client
                .from("fcm_tokens")
                .update(updateBody) {
                    filter {
                        eq("id_usuari", idUsuari)
                        eq("token", token)
                    }
                }
                .decodeSingleOrNull<JsonObject>()
        } else {
            val insertBody = buildJsonObject {
                put("id_usuari", idUsuari)
                put("token", token)
                put("plataforma", "android")
                put("activa", true)
            }
            SupabaseClient.client
                .from("fcm_tokens")
                .insert(insertBody) { select() }
                .decodeList<JsonObject>()
                .firstOrNull()
        }
    }

    suspend fun desactivarTokenFcm(idUsuari: String, token: String): Boolean {
        if (idUsuari.isBlank() || token.isBlank()) return false

        val body = buildJsonObject {
            put("activa", false)
        }

        return SupabaseClient.client
            .from("fcm_tokens")
            .update(body) {
                filter {
                    eq("id_usuari", idUsuari)
                    eq("token", token)
                }
            }
            .decodeList<JsonObject>()
            .isNotEmpty()
    }
}
