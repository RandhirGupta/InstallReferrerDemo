package dev.anthropic.installreferrerdemo.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.anthropic.installreferrerdemo.data.ReferrerInfo
import dev.anthropic.installreferrerdemo.data.ReferrerRepository
import dev.anthropic.installreferrerdemo.data.ReferrerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReferrerUiState {
    data object Idle : ReferrerUiState()
    data object Loading : ReferrerUiState()
    data class Success(val info: ReferrerInfo) : ReferrerUiState()
    data class Error(val message: String) : ReferrerUiState()
}

class ReferrerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReferrerRepository(application)

    private val _uiState = MutableStateFlow<ReferrerUiState>(ReferrerUiState.Idle)
    val uiState: StateFlow<ReferrerUiState> = _uiState.asStateFlow()

    fun fetchReferrer() {
        _uiState.value = ReferrerUiState.Loading
        viewModelScope.launch {
            when (val result = repository.fetchReferrerInfo()) {
                is ReferrerResult.Success -> {
                    _uiState.value = ReferrerUiState.Success(result.info)
                }
                is ReferrerResult.Error -> {
                    _uiState.value = ReferrerUiState.Error(result.message)
                }
            }
        }
    }
}
