package com.realityos.realityos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityos.realityos.data.local.entity.HistoryEventEntity
import com.realityos.realityos.data.repository.RealityOSRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(repository: RealityOSRepository) : ViewModel() {
    val historyUiState: StateFlow<List<HistoryEventEntity>> =
        repository.getHistory()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}
