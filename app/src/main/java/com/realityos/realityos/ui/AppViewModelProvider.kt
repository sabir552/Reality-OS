package com.realityos.realityos.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.realityos.realityos.RealityOSApplication
import com.realityos.realityos.ui.elite.EliteViewModel
import com.realityos.realityos.ui.history.HistoryViewModel
import com.realityos.realityos.ui.home.HomeViewModel
import com.realityos.realityos.ui.onboarding.OnboardingViewModel
import com.realityos.realityos.ui.rules.RulesViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Reality OS app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(realityOSApplication().container.realityOSRepository)
        }
        // Initializer for OnboardingViewModel
        initializer {
            OnboardingViewModel(realityOSApplication().container.realityOSRepository)
        }
        // Initializer for RulesViewModel
        initializer {
            RulesViewModel(realityOSApplication().container.realityOSRepository)
        }
        // Initializer for HistoryViewModel
        initializer {
            HistoryViewModel(realityOSApplication().container.realityOSRepository)
        }
        // Initializer for EliteViewModel
        initializer {
            EliteViewModel(
                realityOSApplication(), // Pass the application context
                realityOSApplication().container.realityOSRepository,
                this.createSavedStateHandle()
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [RealityOSApplication].
 */
fun CreationExtras.realityOSApplication(): RealityOSApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RealityOSApplication)
    
