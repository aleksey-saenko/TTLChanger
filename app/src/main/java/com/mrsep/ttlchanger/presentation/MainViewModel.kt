package com.mrsep.ttlchanger.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mrsep.ttlchanger.data.TtlManager
import com.mrsep.ttlchanger.data.TtlManagerImpl
import com.mrsep.ttlchanger.data.TtlOperationResult
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val ttlManager: TtlManager
) : ViewModel() {

    private val _userInput = savedStateHandle.getStateFlow<Int?>(KEY_USER_INPUT, 64)
    val userInput = _userInput.map { value -> value?.let { "$value" } ?: "" }

    val currentTtl = savedStateHandle.getStateFlow<Int?>(KEY_CURRENT_TTL, null)

    val lastOperationResult = savedStateHandle.getStateFlow(KEY_LAST_OPERATION, "")

    fun updateUserInput(value: String) {
        if (value.length > 3) return
        if (value.isBlank()) savedStateHandle[KEY_USER_INPUT] = null
        value.toIntOrNull().takeIf { it in 1..255 }?.let { ttl ->
            savedStateHandle[KEY_USER_INPUT] = ttl
        }
    }

    fun decUserInput() {
        _userInput.value?.dec()?.run { savedStateHandle[KEY_USER_INPUT] = this }
    }

    fun incUserInput() {
        _userInput.value?.inc()?.run { savedStateHandle[KEY_USER_INPUT] = this }
    }

    fun applyTtl() {
        val currentTtl = _userInput.value ?: return
        viewModelScope.launch {
            val result = ttlManager.writeValue(currentTtl)
            savedStateHandle[KEY_LAST_OPERATION] = result.toString()
        }
    }

    fun readTtl() {
        viewModelScope.launch {
            val result = ttlManager.readValue()
            if (result is TtlOperationResult.Success) {
                savedStateHandle[KEY_CURRENT_TTL] = result.value
            }
            savedStateHandle[KEY_LAST_OPERATION] = result.toString()
        }
    }

    companion object {
        private const val KEY_USER_INPUT = "key_input"
        private const val KEY_CURRENT_TTL = "key_current"
        private const val KEY_LAST_OPERATION = "key_operation"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    ttlManager = TtlManagerImpl()
                )
            }
        }
    }


}