package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Conversa
import models.Missatge
import models.UsuariConversa

class MissatgeriaDao {

    @Serializable
    private data class ConversaIdRow(val id_conversa: String)

    @Serializable
    private data class ConversaIdUsuariRow(val id_usuari: String)

    @Serializable
    private data class ConversaUsuariRow(
        val id_conversa: String,
        val id_usuari: String,
        val usuaris: UsuariConversa? = null
    )

    suspend fun getConversesUsuari(idUsuari: String): List<Conversa> {
        val conversesIds = SupabaseClient.client.from("conversa_usuaris")
            .select(Columns.list("id_conversa")) { filter {
                eq("id_usuari", idUsuari) }
            }
            .decodeList<ConversaIdRow>()
            .map { it.id_conversa }

        if (conversesIds.isEmpty()) return emptyList()

        val totsElsUsuaris = SupabaseClient.client.from("conversa_usuaris")
            .select(Columns.raw("id_conversa, id_usuari, usuaris(nom_usuari, avatar_url)")) {
                filter {
                    isIn("id_conversa", conversesIds)
                }
            }
            .decodeList<ConversaUsuariRow>()

        val totsMissatges = SupabaseClient.client.from("missatges")
            .select(Columns.list("id", "id_conversa", "id_usuari", "contingut", "imatge_url", "created_at")) {
                filter { isIn("id_conversa", conversesIds) }
            }
            .decodeList<Missatge>()

        val usuarisPerConversa = totsElsUsuaris.groupBy(
            keySelector = { it.id_conversa },
            valueTransform = { UsuariConversa(it.id_usuari, it.usuaris?.nom_usuari, it.usuaris?.avatar_url) }
        )

        val ultimMissatgePerConversa = totsMissatges
            .groupBy { it.id_conversa }
            .mapValues { (_, m) -> m.maxByOrNull { it.created_at } }

        return conversesIds.map { id ->
            Conversa(id = id, created_at = "", usuaris = usuarisPerConversa[id] ?: emptyList(), ultim_missatge = ultimMissatgePerConversa[id])
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
        } catch (_: Exception) {
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

    suspend fun getAltreUsuariConversa(idConversa: String, idUsuariActual: String): UsuariConversa? = try {
        val idAltre = SupabaseClient.client.from("conversa_usuaris")
            .select(Columns.list("id_usuari")) {
                filter {
                    eq("id_conversa", idConversa)
                }
            }
            .decodeList<ConversaIdUsuariRow>()
            .map { it.id_usuari }
            .firstOrNull { it != idUsuariActual } ?: return null

        SupabaseClient.client.from("usuaris")
            .select(Columns.raw("id, nom_usuari, avatar_url")) {
                filter {
                    eq("id", idAltre)
                }
            }
            .decodeSingleOrNull<UsuariConversa>()
    } catch (_: Exception) {
        null
    }

    suspend fun trobarConversaExistents(idUsuari1: String, idUsuari2: String): Conversa? = try {
        val ids1 = SupabaseClient.client.from("conversa_usuaris")
            .select(Columns.list("id_conversa")) {
                filter {
                    eq("id_usuari", idUsuari1)
                }
            }
            .decodeList<ConversaIdRow>().map { it.id_conversa }.toSet()

        val ids2 = SupabaseClient.client.from("conversa_usuaris")
            .select(Columns.list("id_conversa")) {
                filter {
                    eq("id_usuari", idUsuari2)
                }
            }
            .decodeList<ConversaIdRow>().map { it.id_conversa }.toSet()

        val idComu = ids1.intersect(ids2).firstOrNull() ?: return null

        SupabaseClient.client.from("converses")
            .select { filter { eq("id", idComu) } }
            .decodeSingleOrNull()
    } catch (_: Exception) {
        null
    }

}