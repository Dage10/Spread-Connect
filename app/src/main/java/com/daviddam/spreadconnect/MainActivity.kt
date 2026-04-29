package com.daviddam.spreadconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.daviddam.spreadconnect.databinding.ActivityMainBinding
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import conexio.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import repository.Repository
import sharedPreference.SharedPreference
import util.NotificationHelper

private const val REQUEST_CODE_POST_NOTIFICATIONS = 1001

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val repo = Repository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.crearNotificacioCanal(this)
        demanarPermisNotificacions()

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
        if (targetId != null && targetType != null) {
            navegarAComentaris(targetId, targetType)
        }
    }

    private fun navegarAComentaris(targetId: String, targetType: String) {
        val action: NavDirections = when (targetType) {
            "post" -> AreesFragmentsDirections.actionAreesFragmentsToComentarisFragment(targetId, "post")
            "presentacio" -> AreesFragmentsDirections.actionAreesFragmentsToComentarisFragment(targetId, "presentacio")
            else -> AreesFragmentsDirections.actionAreesFragmentsToComentarisFragment(targetId, "post")
        }
        navController.navigate(action)
    }

    private fun demanarPermisNotificacions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
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
                        NotificationHelper.mostrarNotificacio(
                            this@MainActivity,
                            getString(R.string.notificacions),
                            notification.missatge,
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
                    val tag = when (it.llenguatge) {
                        "Català" -> "ca"
                        "Anglès" -> "en"
                        else -> "es"
                    }
                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                        androidx.core.os.LocaleListCompat.forLanguageTags(tag)
                    )
                    val mode = if (it.tema == "Fosc") 
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES 
                    else 
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
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
        SharedPreference.tancarSessio(this@MainActivity)
        navController.navigate(R.id.iniciFragment)
        Toast.makeText(this, getString(R.string.usuari_no_trobat), Toast.LENGTH_LONG).show()
    }
}
