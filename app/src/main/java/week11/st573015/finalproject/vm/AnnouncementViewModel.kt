package week11.st573015.finalproject.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import week11.st573015.finalproject.data.AnnouncementRepository
import week11.st573015.finalproject.models.Announcement

sealed class AnnouncementUiState {
    object Idle : AnnouncementUiState()
    object Loading : AnnouncementUiState()
    data class Error(val message: String) : AnnouncementUiState()
}

class AnnouncementViewModel(
    private val repo: AnnouncementRepository = AnnouncementRepository()
) : ViewModel() {

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _uiState = MutableStateFlow<AnnouncementUiState>(AnnouncementUiState.Idle)
    val uiState: StateFlow<AnnouncementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = AnnouncementUiState.Loading
            repo.announcementsFlow().collect { list ->
                _announcements.value = list
                _uiState.value = AnnouncementUiState.Idle
            }
        }
    }

    fun addAnnouncement(title: String, message: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AnnouncementUiState.Loading
                repo.addAnnouncement(Announcement(title = title, message = message))
                _uiState.value = AnnouncementUiState.Idle
            } catch (t: Throwable) {
                _uiState.value = AnnouncementUiState.Error(t.localizedMessage ?: "Add failed")
            }
        }
    }

    fun deleteAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            try {
                _uiState.value = AnnouncementUiState.Loading
                repo.deleteAnnouncement(announcement.id)
                _uiState.value = AnnouncementUiState.Idle
            } catch (t: Throwable) {
                _uiState.value = AnnouncementUiState.Error(t.localizedMessage ?: "Delete failed")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is AnnouncementUiState.Error) _uiState.value = AnnouncementUiState.Idle
    }
}