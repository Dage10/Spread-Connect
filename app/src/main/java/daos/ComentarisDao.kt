package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Comentari
import java.time.Instant

class ComentarisDao {

    suspend fun getComentarisPost(idPost: String): List<Comentari> =
        SupabaseClient.client
            .from("comentaris")
            .select { filter { eq("id_post", idPost) } }
            .decodeList<Comentari>()

    suspend fun getComentarisPresentacio(idPresentacio: String): List<Comentari> =
        SupabaseClient.client
            .from("comentaris")
            .select { filter { eq("id_presentacio", idPresentacio) } }
            .decodeList<Comentari>()

    suspend fun getComentarisRespostes(idPare: String): List<Comentari> =
        SupabaseClient.client
            .from("comentaris")
            .select { filter { eq("id_comentari_pare", idPare) } }
            .decodeList<Comentari>()

    suspend fun getComentariPerId(id: String): Comentari =
        SupabaseClient.client
            .from("comentaris")
            .select { filter { eq("id", id) } }
            .decodeSingle<Comentari>()

    suspend fun crearComentari(idPost: String, idUsuari: String, contingut: String, imatgeUrl: String? = null): Comentari {
        val dades = buildJsonObject {
            put("id_post", idPost)
            put("id_usuari", idUsuari)
            put("contingut", contingut)
            if (imatgeUrl != null) {
                put("imatge_url", imatgeUrl)
            }
        }
        return SupabaseClient.client
            .from("comentaris")
            .insert(dades) { select() }
            .decodeSingle<Comentari>()
    }

    suspend fun crearComentariPresentacio(idPresentacio: String, idUsuari: String, contingut: String, imatgeUrl: String? = null): Comentari {
        val dades = buildJsonObject {
            put("id_presentacio", idPresentacio)
            put("id_usuari", idUsuari)
            put("contingut", contingut)
            if (imatgeUrl != null) {
                put("imatge_url", imatgeUrl)
            }
        }
        return SupabaseClient.client
            .from("comentaris")
            .insert(dades) { select() }
            .decodeSingle<Comentari>()
    }

    suspend fun crearComentariResposta(idPare: String, idUsuari: String, contingut: String, imatgeUrl: String? = null): Comentari {
        val dades = buildJsonObject {
            put("id_comentari_pare", idPare)
            put("id_usuari", idUsuari)
            put("contingut", contingut)
            if (imatgeUrl != null) {
                put("imatge_url", imatgeUrl)
            }
        }
        return SupabaseClient.client
            .from("comentaris")
            .insert(dades) { select() }
            .decodeSingle<Comentari>()
    }

    suspend fun eliminarComentari(id_comentari: String){
        SupabaseClient.client
            .from("comentaris")
            .delete{filter{eq("id",id_comentari)}}
    }

    suspend fun editarComentari(
        id: String,
        contingut: String,
        imatgeUrl: String?
    ): Comentari {
        val dades = buildJsonObject {
            put("contingut", contingut)
            put("updated_at", Instant.now().toString())
            if (imatgeUrl != null) {
                put("imatge_url", imatgeUrl)
            }
        }
        return SupabaseClient.client
            .from("comentaris")
            .update(dades) {
                filter { eq("id", id) }
                select()
            }
            .decodeList<Comentari>()
            .firstOrNull() ?: throw Exception("Error en editar el comentari")
    }
}
