package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import models.Area

class AreaDao {

    suspend fun getArees(): List<Area> =
        SupabaseClient.client
            .from("areas")
            .select()
            .decodeList()
}
