package com.realityos.realityos.ui.rules

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityos.realityos.data.local.entity.RuleEntity
import com.realityos.realityos.data.repository.RealityOSRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RulesViewModel(private val repository: RealityOSRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RulesUiState())
    val uiState: StateFlow<RulesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRules().collect { rules ->
                _uiState.update { it.copy(rules = rules) }
            }
        }
    }

    fun addRule(rule: RuleEntity) {
        viewModelScope.launch {
            repository.addRule(rule)
        }
    }

    fun deleteRule(rule: RuleEntity) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 } // Filter out system apps
            .map { AppInfo(appName = it.loadLabel(pm).toString(), packageName = it.packageName) }
            .sortedBy { it.appName }
    }
}

data class RulesUiState(
    val rules: List<RuleEntity> = emptyList()
)

data class AppInfo(
    val appName: String,
    val packageName: String
)
