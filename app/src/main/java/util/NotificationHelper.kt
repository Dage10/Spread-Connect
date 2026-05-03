package util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daviddam.spreadconnect.MainActivity
import com.daviddam.spreadconnect.R

object NotificationHelper {
    private const val CHANNEL_ID = "spreadconnect_updates"

    fun crearNotificacioCanal(context: Context) {

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notificacions),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notificacions)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.createNotificationChannel(channel)
    }

    fun buildNotificacioContinngut(context: Context, tipus: String, missatge: String?): Pair<String, String> {
        val normalizedType = when (tipus.lowercase()) {
            "post", "nou post", "nuevo post", "new post" -> "post"
            else -> tipus
        }

        val title = when (normalizedType) {
            "post" -> context.getString(R.string.tipus_notificacio_post)
            else -> tipus.ifBlank { context.getString(R.string.notificacions) }
        }

        val messageText = when (normalizedType) {
            "post" -> context.getString(R.string.missatge_notificacio_post, missatge.orEmpty())
            else -> missatge.orEmpty()
        }

        return title to messageText
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun mostrarNotificacio(context: Context, titol: String,
        text: String, notificacioId: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
        idTarget: String? = null, targetTipus: String? = null
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titol)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (idTarget != null && targetTipus != null) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("targetId", idTarget)
                putExtra("targetType", targetTipus)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificacioId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }

        NotificationManagerCompat.from(context).notify(notificacioId, builder.build())
    }
}
