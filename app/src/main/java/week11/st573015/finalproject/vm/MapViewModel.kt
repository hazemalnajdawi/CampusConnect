package week11.st573015.finalproject.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import week11.st573015.finalproject.data.LocationRepository
import week11.st573015.finalproject.models.LocationPin

class MapViewModel(
    private val repo: LocationRepository = LocationRepository()
) : ViewModel() {

    private val _pins = MutableStateFlow<List<LocationPin>>(emptyList())
    val pins: StateFlow<List<LocationPin>> = _pins.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            repo.locationsFlow().collect { list ->
                _pins.value = list
            }
        }
    }

    fun addPin(title: String, desc: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repo.addPin(
                    LocationPin(
                        title = title,
                        description = desc,
                        lat = lat,
                        lng = lng
                    )
                )
            } catch (_: Throwable) {
            } finally {
                _loading.value = false
            }
        }
    }

    fun deletePin(id: String) {
        viewModelScope.launch {
            repo.deletePin(id)
        }
    }
}