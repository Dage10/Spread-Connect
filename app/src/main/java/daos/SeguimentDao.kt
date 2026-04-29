package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import models.Seguiment
import java.util.UUID
import java.time.Instant

class SeguimentDao {

    suspend fun getNumSeguidors(idUsuari: String): Int = try {
        SupabaseClient.client
            .from("seguiments")
            .select { filter { eq("id_seguit", idUsuari) } }
            .decodeList<Seguiment>()
            .size
    } catch (e: Exception) {
        0
    }

    suspend fun getNumSeguint(idUsuari: String): Int = try {
        SupabaseClient.client
            .from("seguiments")
            .select { filter { eq("id_seguidor", idUsuari) } }
            .decodeList<Seguiment>()
            .size
    } catch (e: Exception) {
        0
    }

    suspend fun getSeguidors(idUsuari: String): List<String> = try {
        SupabaseClient.client
            .from("seguiments")
            .select { filter { eq("id_seguit", idUsuari) } }
            .decodeList<Seguiment>()
            .map { it.id_seguidor }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun isSeguint(idSeguidor: String, idSeguit: String): Boolean = try {
        SupabaseClient.client
            .from("seguiments")
            .select {
                filter {
                    eq("id_seguidor", idSeguidor)
                    eq("id_seguit", idSeguit)
                }
            }.decodeSingleOrNull<Seguiment>() != null
    } catch (e: Exception) {
        false
    }

    suspend fun seguirUsuari(idSeguidor: String, idSeguit: String) {
        val seguiment = Seguiment(
            id = UUID.randomUUID().toString(),
            id_seguidor = idSeguidor,
            id_seguit = idSeguit,
            created_at = Instant.now().toString()
        )
        SupabaseClient.client.from("seguiments").insert(seguiment)
    }

    suspend fun deixarDeSeguirUsuari(idSeguidor: String, idSeguit: String) {
        SupabaseClient.client
            .from("seguiments")
            .delete {
                filter {
                    eq("id_seguidor", idSeguidor)
                    eq("id_seguit", idSeguit)
                }
            }
    }
}
