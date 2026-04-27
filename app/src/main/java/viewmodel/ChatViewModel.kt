package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.ChatUiState
import repository.Repository
import util.UiText


class ChatViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun carregarConversa(idConversa: String, idUsuariLoguejat: String) {
        _uiState.value = ChatUiState(loading = true)
        viewModelScope.launch {
            try {
                val missatges = repo.missatgeriaDao.getMissatgesConversa(idConversa)
                val altreUsuari =
                    repo.missatgeriaDao.getAltreUsuariConversa(idConversa, idUsuariLoguejat)
                _uiState.value = ChatUiState(
                    missatges = missatges,
                    loading = false,
                    idUsuariLoguejat = idUsuariLoguejat,
                    altreUsuari = altreUsuari
                )
            } catch (e: Exception) {
                _uiState.value = ChatUiState(
                    error = UiText.DynamicString(
                        e.message ?: "Error carregant conversa"
                    ), loading = false
                )
            }
        }
    }

    fun enviarMissatge(idConversa: String, idUsuari: String, contingut: String?,
    imatgeUrl: String? = null) {
        viewModelScope.launch {
            try {
                repo.missatgeriaDao.enviarMissatge(idConversa, idUsuari, contingut, imatgeUrl)
                carregarConversa(idConversa, idUsuari)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = UiText.DynamicString(
                        e.message ?: "Error enviant missatge"
                    )
                )
            }
        }
    }
}
