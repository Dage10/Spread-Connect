package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object FcmTokenDao {

    suspend fun guardarTokenFcm(idUsuari: String, token: String) {
        if (idUsuari.isBlank() || token.isBlank()) return

        val existeix = SupabaseClient.client.from("fcm_tokens")
            .select {
                filter {
                    eq("id_usuari", idUsuari)
                    eq("token", token)
                }
            }
            .decodeSingleOrNull<JsonObject>() != null

        if (existeix) {
            SupabaseClient.client.from("fcm_tokens")
                .update(buildJsonObject {
                    put("activa", true)
                    put("plataforma", "android")
                }) {
                    filter {
                        eq("id_usuari", idUsuari)
                        eq("token", token)
                    }
                }
        } else {
            SupabaseClient.client.from("fcm_tokens")
                .insert(buildJsonObject {
                    put("id_usuari", idUsuari)
                    put("token", token)
                    put("plataforma", "android")
                    put("activa", true)
                })
        }
    }

    suspend fun desactivarTokenFcm(idUsuari: String, token: String) {
        if (idUsuari.isBlank() || token.isBlank()) return
        SupabaseClient.client.from("fcm_tokens")
            .update(buildJsonObject {
                put("activa", false)
            }) {
                filter {
                    eq("id_usuari", idUsuari)
                    eq("token", token) }
            }
    }
}
