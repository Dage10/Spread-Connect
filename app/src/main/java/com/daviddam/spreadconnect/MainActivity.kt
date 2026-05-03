package com.daviddam.spreadconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.daviddam.spreadconnect.databinding.ActivityMainBinding
import androidx.navigation.NavController
import conexio.SupabaseClient
import com.google.firebase.messaging.FirebaseMessaging
import daos.FcmTokenDao
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import repository.Repository
import sharedPreference.SharedPreference
import util.NotificationHelper
import util.PreferenciesApplier
import util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val repo = Repository()

    private val requestPermisLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { estaPermes: Boolean ->
        syncNotificacioPreferenciesAmbPermisos(estaPermes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.crearNotificacioCanal(this)
        demanarPermisNotificacions()
        sincronitzarTokenFcmSiCal()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        carregarIAplicarPreferencies()
        observarSessio()
    }

    override fun onResume() {
        super.onResume()
        obtenirNotificacionsNoLlegides()
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: android.content.Intent?) {
        val targetId = intent?.getStringExtra("targetId")
        val targetType = intent?.getStringExtra("targetType")
        if (!targetId.isNullOrBlank() && !targetType.isNullOrBlank()) {
            if (SharedPreference.obtenirUsuariLoguejat(this).isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.usuari_no_trobat), Toast.LENGTH_LONG).show()
                return
            }
            navegarAComentaris(targetId, targetType)
        }
    }

    private fun navegarAComentaris(targetId: String, targetType: String) {
        val args = Bundle().apply {
            putString("targetId", targetId)
            putString("targetType", targetType)
        }
        navController.navigate(R.id.comentarisFragment, args)
    }

    private fun demanarPermisNotificacions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permes = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (permes) {
                syncNotificacioPreferenciesAmbPermisos(true)
            } else {
                requestPermisLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun syncNotificacioPreferenciesAmbPermisos(estaPermes: Boolean) {
        if (!SharedPreference.estaLoguejat(this)) return

        val idUsuari = SharedPreference.obtenirUsuariLoguejat(this) ?: return
        lifecycleScope.launch {
            try {
                val prefs = repo.preferenciesDao.getPerUsuari(idUsuari)
                if (prefs == null) {
                    repo.preferenciesDao.insertPreferencies(
                        idUsuari,
                        "Español",
                        "Clar",
                        estaPermes
                    )
                }
            } catch (_: Exception) {

            }
        }
    }

    private fun sincronitzarTokenFcmSiCal() {
        val idUsuari = SharedPreference.obtenirUsuariLoguejat(this) ?: return
        val tokenLocal = SharedPreference.obtenirFcmToken(this)

        if (!tokenLocal.isNullOrBlank()) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    FcmTokenDao.guardarTokenFcm(idUsuari, tokenLocal)
                } catch (_: Exception) {

                }
            }
            return
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if (token.isNullOrBlank()) return@addOnSuccessListener
            SharedPreference.guardarFcmToken(this, token)
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    FcmTokenDao.guardarTokenFcm(idUsuari, token)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun obtenirNotificacionsNoLlegides() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val idUsuari = SharedPreference.obtenirUsuariLoguejat(this) ?: return

        lifecycleScope.launch {
            try {
                val prefs = repo.preferenciesDao.getPerUsuari(idUsuari)
                if (prefs?.rebre_notificacions == true) {
                    val notificacions = repo.notificacioDao.getNotificacionsSenseVeure(idUsuari)
                    notificacions.forEach { notification ->
                        val (title, text) = NotificationHelper.buildNotificacioContinngut(
                            this@MainActivity,
                            notification.tipus,
                            notification.missatge
                        )
                        NotificationHelper.mostrarNotificacio(
                            this@MainActivity,
                            title,
                            text,
                            notificacioId = notification.id.hashCode(),
                            idTarget = notification.id_target,
                            targetTipus = notification.target_type
                        )
                    }
                    repo.notificacioDao.marcarComLlegida(notificacions.map { it.id })
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun carregarIAplicarPreferencies() {
        val idUsuari = SharedPreference.obtenirUsuariLoguejat(this) ?: return
        lifecycleScope.launch {
            try {
                val prefs = repo.preferenciesDao.getPerUsuari(idUsuari)
                prefs?.let {
                    PreferenciesApplier.applyLanguage(it.llenguatge)
                    PreferenciesApplier.applyTheme(it.tema)
                }
            } catch (_: Exception) {}
        }
    }

    private fun observarSessio() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SupabaseClient.client.auth.sessionStatus.collect { status ->
                    if (status is SessionStatus.NotAuthenticated) {
                        if (SharedPreference.obtenirUsuariLoguejat(this@MainActivity) != null) {
                            tancarSessioIAnarInici()
                        }
                    }
                }
            }
        }
    }

    private fun tancarSessioIAnarInici() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                SessionManager.tancarSessio(this@MainActivity)
            }
            navController.navigate(R.id.iniciFragment)
            Toast.makeText(this@MainActivity, getString(R.string.usuari_no_trobat), Toast.LENGTH_LONG).show()
        }
    }
}
