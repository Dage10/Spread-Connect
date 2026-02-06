package conexio

import android.net.Uri
import io.github.jan.supabase.storage.storage
import java.util.UUID

object SupabaseStorage {

    private const val SUPABASE_URL = "https://fvoouemimuhvwnzbetrl.supabase.co"
    private const val BUCKET_AVATARS = "avatars"
    private const val BUCKET_POSTS = "posts"
    private const val BUCKET_PRESENTACIONS = "presentacions"

    private fun buildPublicUrl(bucket: String, path: String): String {
        return "$SUPABASE_URL/storage/v1/object/public/$bucket/$path"
    }

    private suspend fun penjarArxiu(
        bucket: String,
        path: String,
        bytes: ByteArray,
        upsert: Boolean = false
    ): String {
        return try {
            SupabaseClient.client.storage.from(bucket).upload(path, bytes) { 
                this.upsert = upsert 
            }
            buildPublicUrl(bucket, path)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun penjarAvatar(idUsuari: String, bytes: ByteArray): String {
        return penjarArxiu(BUCKET_AVATARS, "$idUsuari.jpg", bytes, upsert = true)
    }

    suspend fun penjarPostImatge(uri: Uri, readBytes: (Uri) -> ByteArray): String {
        val bytes = readBytes(uri)
        return penjarArxiu(BUCKET_POSTS, "${UUID.randomUUID()}.jpg", bytes)
    }

    suspend fun penjarPresentacioImatge(uri: Uri, readBytes: (Uri) -> ByteArray): String {
        val bytes = readBytes(uri)
        return penjarArxiu(BUCKET_PRESENTACIONS, "${UUID.randomUUID()}.jpg", bytes)
    }
}
