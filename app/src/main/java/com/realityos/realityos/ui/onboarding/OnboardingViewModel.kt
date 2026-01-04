package com.realityos.realityos.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityos.realityos.data.local.entity.HistoryEventEntity
import com.realityos.realityos.data.local.entity.UserEntity
import com.realityos.realityos.data.repository.RealityOSRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: RealityOSRepository) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            // Create the initial user
            repository.insertUser(
                UserEntity(
                    commitmentLevel = "INITIATE",
                    xp = 0,
                    streak = 0,
                    hasBrokenCommitment = false,
                    installTimestamp = timestamp
                )
            )
            // Create the first history event
            repository.logHistoryEvent(
                HistoryEventEntity(
                    timestamp = timestamp,
                    eventDescription = "Welcome to Reality OS. Commitment initiated."
                )
            )
        }
    }
}
