package com.realityos.realityos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityos.realityos.data.local.entity.UserEntity
import com.realityos.realityos.data.repository.RealityOSRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(repository: RealityOSRepository) : ViewModel() {
    val homeUiState: StateFlow<HomeUiState> =
        repository.getUser().map { user ->
            if (user != null) {
                HomeUiState.Success(user)
            } else {
                HomeUiState.Loading // Or an error state
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )
}

sealed interface HomeUiState {
    data class Success(val user: UserEntity) : HomeUiState
    object Loading : HomeUiState
}
