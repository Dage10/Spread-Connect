package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import models.Conversa
import models.Missatge
import models.UsuariConversa

class MissatgeriaDao {

    suspend fun getConversesUsuari(idUsuari: String): List<Conversa> {
        val conversesIds = SupabaseClient.client
            .from("conversa_usuaris")
            .select(Columns.list("id_conversa")) {
                filter {
                    eq("id_usuari", idUsuari)
                }
            }
            .decodeList<JsonObject>()
            .mapNotNull { it["id_conversa"]?.jsonPrimitive?.content }

        if (conversesIds.isEmpty()) return emptyList()

        val totsElsUsuaris = SupabaseClient.client
            .from("conversa_usuaris")
            .select(Columns.list("id_conversa", "id_usuari", "usuaris(nom_usuari, avatar_url)")) {
                filter {
                    isIn("id_conversa", conversesIds)
                }
            }
            .decodeList<JsonObject>()

        val totsMissatges = SupabaseClient.client
            .from("missatges")
            .select(Columns.list("id", "id_conversa", "id_usuari", "contingut", "imatge_url", "created_at")) {
                filter { isIn("id_conversa", conversesIds) }
            }
            .decodeList<Missatge>()

        val usuarisPerConversa = totsElsUsuaris.groupBy(
            keySelector = { it["id_conversa"]?.jsonPrimitive?.content ?: "" },
            valueTransform = { row ->
                val usuariObj = row["usuaris"] as? JsonObject
                UsuariConversa(
                    id_usuari = row["id_usuari"]?.jsonPrimitive?.content ?: "",
                    nom_usuari = usuariObj?.get("nom_usuari")?.jsonPrimitive?.content,
                    avatar_url = usuariObj?.get("avatar_url")?.jsonPrimitive?.content
                )
            }
        )

        val ultimMissatgePerConversa = totsMissatges
            .groupBy { it.id_conversa }
            .mapValues { (_, missatges) -> missatges.maxByOrNull { it.created_at } }

        return conversesIds.map { id ->
            Conversa(
                id = id,
                created_at = "",
                usuaris = usuarisPerConversa[id] ?: emptyList(),
                ultim_missatge = ultimMissatgePerConversa[id]
            )
        }.sortedByDescending { it.ultim_missatge?.created_at }
    }

    suspend fun getMissatgesConversa(idConversa: String): List<Missatge> {
        return try {
            SupabaseClient.client
                .from("missatges")
                .select(Columns.list("id", "id_conversa", "id_usuari", "contingut", "imatge_url", "created_at")) {
                    filter {
                        eq("id_conversa", idConversa)
                    }
                }
                .decodeList<Missatge>()
                .sortedBy { it.created_at }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun crearConversa(idUsuari1: String, idUsuari2: String): Conversa {
        val idConversa = java.util.UUID.randomUUID().toString()

        SupabaseClient.client.from("converses").insert(buildJsonObject {
            put("id", idConversa)
        })

        SupabaseClient.client.from("conversa_usuaris").insert(buildJsonObject {
            put("id_conversa", idConversa)
            put("id_usuari", idUsuari1)
        })
        SupabaseClient.client.from("conversa_usuaris").insert(buildJsonObject {
            put("id_conversa", idConversa)
            put("id_usuari", idUsuari2)
        })

        return Conversa(id = idConversa, created_at = java.time.Instant.now().toString())
    }

    suspend fun enviarMissatge(idConversa: String, idUsuari: String, contingut: String?, imatgeUrl: String? = null): Missatge {
        val missatge = buildJsonObject {
            put("id_conversa", idConversa); put("id_usuari", idUsuari)
            contingut?.let { put("contingut", it) }
            imatgeUrl?.let { put("imatge_url", it) }
        }
        return SupabaseClient.client
            .from("missatges")
            .insert(missatge) {
                select()
            }.decodeSingle()
    }

    suspend fun getAltreUsuariConversa(idConversa: String, idUsuariActual: String): UsuariConversa? {
        return try {
            val usuarisEnConversa = SupabaseClient.client
                .from("conversa_usuaris")
                .select(Columns.list("id_usuari")) {
                    filter {
                        eq("id_conversa", idConversa)
                    }
                }
                .decodeList<JsonObject>()
                .mapNotNull { it["id_usuari"]?.jsonPrimitive?.content }
                .filter { it != idUsuariActual }

            val idAltre = usuarisEnConversa.firstOrNull() ?: return null

            val altreUsuari = SupabaseClient.client
                .from("usuaris")
                .select(Columns.list("nom_usuari, avatar_url")) {
                    filter {
                        eq("id", idAltre)
                    }
                }
                .decodeSingleOrNull<JsonObject>()

            UsuariConversa(
                id_usuari = idAltre,
                nom_usuari = altreUsuari?.get("nom_usuari")?.jsonPrimitive?.content,
                avatar_url = altreUsuari?.get("avatar_url")?.jsonPrimitive?.content
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun trobarConversaExistents(idUsuari1: String, idUsuari2: String): Conversa? {
        return try {
            val usuaris1 = SupabaseClient.client
                .from("conversa_usuaris")
                .select(Columns.list("id_conversa")) {
                    filter {
                        eq("id_usuari", idUsuari1)
                    }
                }
                .decodeList<JsonObject>()
                .mapNotNull { it["id_conversa"]?.jsonPrimitive?.content }

            val usuaris2 = SupabaseClient.client
                .from("conversa_usuaris")
                .select(Columns.list("id_conversa")) {
                    filter {
                        eq("id_usuari", idUsuari2)
                    }
                }
                .decodeList<JsonObject>()
                .mapNotNull { it["id_conversa"]?.jsonPrimitive?.content }

            val idComu = usuaris1.intersect(usuaris2.toSet()).firstOrNull()

            idComu?.let {
                SupabaseClient.client
                    .from("converses")
                    .select {
                        filter { eq("id", it) }
                    }
                    .decodeSingleOrNull<Conversa>()
            }
        } catch (e: Exception) { null }
    }
}