package week11.st573015.finalproject.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import week11.st573015.finalproject.data.TaskRepository
import week11.st573015.finalproject.models.Task
import week11.st573015.finalproject.notifications.ReminderScheduler

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Error(val message: String) : UiState()
}

class TasksViewModel(
    private val repo: TaskRepository = TaskRepository()
) : ViewModel() {

    // Tasks stream exposed as StateFlow
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.tasksFlow().collect { list ->
                _tasks.value = list
                _uiState.value = UiState.Idle
            }
        }
    }

    fun createTask(title: String, description: String, dueTs: Long?, reminderTs: Long?) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val created = repo.createTask(Task(title = title, description = description, dueTimestamp = dueTs, reminderTimestamp = reminderTs))
                // schedule reminder if provided
                if (created.reminderTimestamp != null) {
                    ReminderScheduler.scheduleReminderForTask(created)
                }
                _uiState.value = UiState.Idle
            } catch (t: Throwable) {
                _uiState.value = UiState.Error(t.localizedMessage ?: "Create failed")
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.updateTask(task)
                // schedule or cancel based on reminderTimestamp
                if (task.reminderTimestamp != null) {
                    ReminderScheduler.scheduleReminderForTask(task)
                } else {
                    ReminderScheduler.cancelReminderForTask(task)
                }
                _uiState.value = UiState.Idle
            } catch (t: Throwable) {
                _uiState.value = UiState.Error(t.localizedMessage ?: "Update failed")
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.deleteTask(task.id)
                ReminderScheduler.cancelReminderForTask(task)
                _uiState.value = UiState.Idle
            } catch (t: Throwable) {
                _uiState.value = UiState.Error(t.localizedMessage ?: "Delete failed")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) _uiState.value = UiState.Idle
    }
}