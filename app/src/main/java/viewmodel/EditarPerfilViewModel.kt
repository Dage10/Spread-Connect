package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.clickconnect.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.EditarPerfilUiState
import conexio.SupabaseStorage
import repository.Repository
import util.UiText

class EditarPerfilViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState

    fun carregarUsuari(idUsuari: String) {
        _uiState.value = EditarPerfilUiState(loading = true)
        viewModelScope.launch {
            try {
                val usuari = repo.usuariDao.getUsuariPerId(idUsuari)
                val preferencies = repo.preferenciesDao.getPerUsuari(idUsuari)

                _uiState.value = EditarPerfilUiState(
                    usuari = usuari,
                    preferencies = preferencies,
                    loading = false
                )
            } catch (e: Exception) {
                _uiState.value = EditarPerfilUiState(
                    error = UiText.DynamicString(e.message ?: "Error"),
                    loading = false
                )
            }
        }
    }

    fun guardarCanvis(
        idUsuari: String,
        nom: String,
        descripcio: String?,
        contrasenyaAntiga: String?,
        novaContrasenya: String?,
        llenguatge: String,
        tema: String,
        rebreNotificacions: Boolean,
        avatarImageBytes: ByteArray? = null
    ) {
        if (nom.isBlank()) {
            _uiState.value = _uiState.value.copy(error = UiText.StringResource(R.string.email_nom_obligatoris))
            return
        }

        _uiState.value = _uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                var avatarUrl: String? = null

                if (avatarImageBytes != null && avatarImageBytes.isNotEmpty()) {
                    avatarUrl = SupabaseStorage.penjarAvatar(idUsuari, avatarImageBytes)
                }

                val usuari = repo.usuariDao.actualitzarPerfil(
                    idUsuari, nom, descripcio, novaContrasenya, avatarUrl,contrasenyaAntiga
                )

                val prefs = if (_uiState.value.preferencies != null) {
                    repo.preferenciesDao.updatePreferencies(idUsuari, llenguatge, tema, rebreNotificacions)
                } else {
                    repo.preferenciesDao.insertPreferencies(idUsuari, llenguatge, tema, rebreNotificacions)
                }

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    usuariActualitzat = usuari,
                    preferenciesActualitzades = prefs
                )

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    usuariActualitzat = usuari,
                    preferenciesActualitzades = prefs,
                    error = null
                )
            } catch (e: Exception) {

                val uiError = when (e.message) {

                    "INTRODUIR_CONTRASENYA_ANTIGA" ->
                        UiText.StringResource(R.string.introdueix_contrasenya_antiga)

                    "SESSIO_NO_TROBADA" ->
                        UiText.StringResource(R.string.sessio_no_trobada)

                    "EMAIL_NO_DISPONIBLE" ->
                        UiText.StringResource(R.string.email_no_disponible)

                    "CONTRASENYA_ANTIGA_INCORRECTA" ->
                        UiText.StringResource(R.string.error_contrasenya_antiga_incorrecta)

                    else ->
                        UiText.DynamicString(e.message ?: "Error")
                }

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = uiError
                )
            }
        }
    }
}
