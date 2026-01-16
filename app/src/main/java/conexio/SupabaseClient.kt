package conexio

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://fvoouemimuhvwnzbetrl.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZ2b291ZW1pbXVodnduemJldHJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU0NTM2NjcsImV4cCI6MjA4MTAyOTY2N30.IZrgoKYzdHRUJBw3vRfh24O3YkJfkFgLTULFzo2t2Zc"
    ) {
        install(Postgrest)
    }
}