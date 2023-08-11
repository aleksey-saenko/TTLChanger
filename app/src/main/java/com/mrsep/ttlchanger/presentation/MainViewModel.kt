package com.mrsep.ttlchanger.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mrsep.ttlchanger.DiContainer
import com.mrsep.ttlchanger.data.AutostartManager
import com.mrsep.ttlchanger.data.TtlManager
import com.mrsep.ttlchanger.data.TtlOperationResult
import com.mrsep.ttlchanger.data.preferences.PreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val ttlManager: TtlManager,
    private val preferencesRepository: PreferencesRepository,
    private val autostartManager: AutostartManager
) : ViewModel() {

    private val userInputFlow = MutableStateFlow<Int?>(64)
    private val lastOperationFlow = MutableStateFlow<TtlOperation?>(null)
    private val inProgressFlow = MutableStateFlow(false)

    private var currentOperation: Job? = null

    val uiState = combine(
        preferencesRepository.userPreferencesFlow,
        userInputFlow.map { value -> value?.let { "$value" } ?: "" },
        lastOperationFlow,
        inProgressFlow
    ) { preferences, userInput, lastOperation, inProgress ->
        MainScreenUiState(
            userInput = userInput,
            inProgress = inProgress,
            lastOperation = lastOperation,
            autostartEnabled = preferences.autostartEnabled,
            ipv6Enabled = preferences.ipv6Enabled
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    fun updateUserInput(value: String) {
        if (value.length > 3) return
        if (value.isBlank()) userInputFlow.update { null }
        value.toIntOrNull().takeIf { it in 1..255 }?.let { ttl ->
            userInputFlow.update { ttl }
        }
    }

    fun decUserInput() {
        userInputFlow.update { lastValue -> lastValue?.dec()?.coerceIn(1..255) }
    }

    fun incUserInput() {
        userInputFlow.update { lastValue -> lastValue?.inc()?.coerceIn(1..255) }
    }

    fun writeTtl(ipv6Enabled: Boolean) {
        if (currentOperation?.isCompleted == false) return
        val selectedTtl = userInputFlow.value ?: return
        lastOperationFlow.update { null }
        currentOperation = viewModelScope.launch {
            inProgressFlow.update { true }
            val result = ttlManager.writeValue(selectedTtl, ipv6Enabled)
            if (result is TtlOperationResult.Success) {
                preferencesRepository.setSavedTtl(selectedTtl)
            }
            lastOperationFlow.update { TtlOperation(TtlOperationType.WRITE, result) }
            inProgressFlow.update { false }
        }
    }

    fun readTtl(ipv6Enabled: Boolean) {
        if (currentOperation?.isCompleted == false) return
        lastOperationFlow.update { null }
        currentOperation = viewModelScope.launch {
            inProgressFlow.update { true }
            val result = ttlManager.readValue(ipv6Enabled)
            lastOperationFlow.update { TtlOperation(TtlOperationType.READ, result) }
            inProgressFlow.update { false }
        }
    }

    fun resetLastOperation() {
        lastOperationFlow.update { null }
    }

    fun toggleAutoStart(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutostartEnabled(enabled)
            autostartManager.toggleAutostart(enabled)
        }
    }

    fun toggleIPv6(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setIPv6Enabled(enabled)
        }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    ttlManager = DiContainer.ttlManager,
                    preferencesRepository = DiContainer.preferencesRepository,
                    autostartManager = DiContainer.autostartManager
                )
            }
        }
    }

}

data class MainScreenUiState(
    val userInput: String,
    val inProgress: Boolean,
    val lastOperation: TtlOperation?,
    val autostartEnabled: Boolean,
    val ipv6Enabled: Boolean
)

data class TtlOperation(
    val type: TtlOperationType,
    val result: TtlOperationResult
)

enum class TtlOperationType {
    WRITE, READ
}