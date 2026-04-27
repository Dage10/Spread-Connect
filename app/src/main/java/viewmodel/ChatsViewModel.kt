package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.ChatsUiState
import repository.Repository
import util.UiText

class ChatsViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState

    fun carregarConverses(idUsuari: String) {
        _uiState.value = ChatsUiState(loading = true)
        viewModelScope.launch {
            try {
                val converses = repo.missatgeriaDao.getConversesUsuari(idUsuari)
                _uiState.value = ChatsUiState(converses = converses, loading = false)
            } catch (e: Exception) {
                _uiState.value = ChatsUiState(error = UiText.DynamicString(e.message ?: "Error"), loading = false)
            }
        }
    }
}