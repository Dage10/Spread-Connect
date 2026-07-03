package conexio

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://fvoouemimuhvwnzbetrl.supabase.co",
        supabaseKey = "sb_publishable_z-u5sCNuoK_cU7yEuDpdGw_pZ2F_lRc"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}