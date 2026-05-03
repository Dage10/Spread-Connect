package com.daviddam.spreadconnect

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import daos.FcmTokenDao
import daos.PreferenciesDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import sharedPreference.SharedPreference
import util.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val ambitServei = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearNotificacioCanal(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ambitServei.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SharedPreference.guardarFcmToken(applicationContext, token)

        ambitServei.launch {
            try {
                val idUsuari = SharedPreference.obtenirUsuariLoguejat(applicationContext)
                if (idUsuari.isNullOrBlank()) {
                    return@launch
                }
                FcmTokenDao.guardarTokenFcm(idUsuari, token)
            } catch (_: Exception) {
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        ambitServei.launch {
            try {
                val idUsuari = SharedPreference.obtenirUsuariLoguejat(applicationContext)
                if (idUsuari.isNullOrBlank()) return@launch

                val prefs = PreferenciesDao().getPerUsuari(idUsuari)
                if (prefs?.rebre_notificacions != true) return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MyFirebaseMessagingService,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        return@launch
                    }
                }

                NotificationHelper.crearNotificacioCanal(this@MyFirebaseMessagingService)

                val titolPost = remoteMessage.data["titolPost"] ?: ""
                val title = getString(R.string.tipus_notificacio_post)
                val body = getString(R.string.missatge_notificacio_post, titolPost)

                val idTarget = remoteMessage.data["targetId"]
                val targetType = remoteMessage.data["targetType"]

                NotificationHelper.mostrarNotificacio(
                    this@MyFirebaseMessagingService,
                    title,
                    body,
                    notificacioId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                    idTarget = idTarget,
                    targetTipus = targetType
                )

                if (!idTarget.isNullOrBlank()) {
                    daos.NotificacioDao().marcarNotificacioFcmComLlegida(idUsuari, idTarget)
                }
            } catch (_: Exception) { }
        }
    }
}