package week11.st573015.finalproject.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import week11.st573015.finalproject.data.ScheduleRepository
import week11.st573015.finalproject.models.ScheduleItem

sealed class ScheduleUiState {
    object Idle : ScheduleUiState()
    object Loading : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}

class ScheduleViewModel(
    private val repo: ScheduleRepository = ScheduleRepository()
) : ViewModel() {

    private val _items = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val items: StateFlow<List<ScheduleItem>> = _items.asStateFlow()

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            repo.scheduleFlow().collect { list ->
                _items.value = list
                _uiState.value = ScheduleUiState.Idle
            }
        }
    }

    fun addItem(item: ScheduleItem, onResult: (Result<ScheduleItem>) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                val created = repo.addItem(item)
                _uiState.value = ScheduleUiState.Idle
                onResult(Result.success(created))
            } catch (t: Throwable) {
                _uiState.value = ScheduleUiState.Error(t.localizedMessage ?: "Add failed")
                onResult(Result.failure(t))
            }
        }
    }

    fun updateItem(item: ScheduleItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                repo.updateItem(item)
                _uiState.value = ScheduleUiState.Idle
                onResult(Result.success(Unit))
            } catch (t: Throwable) {
                _uiState.value = ScheduleUiState.Error(t.localizedMessage ?: "Update failed")
                onResult(Result.failure(t))
            }
        }
    }

    fun deleteItem(itemId: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                repo.deleteItem(itemId)
                _uiState.value = ScheduleUiState.Idle
                onResult(Result.success(Unit))
            } catch (t: Throwable) {
                _uiState.value = ScheduleUiState.Error(t.localizedMessage ?: "Delete failed")
                onResult(Result.failure(t))
            }
        }
    }

    fun clearError() {
        if (_uiState.value is ScheduleUiState.Error) _uiState.value = ScheduleUiState.Idle
    }
}