package week11.st573015.finalproject.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st573015.finalproject.data.AuthRepository
import kotlin.Exception

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    init {
        // If already signed in, emit success
        _state.value = AuthState.Success(repo.currentUser())
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                repo.register(email, password, displayName)
                _state.value = AuthState.Success(repo.currentUser())
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                repo.signIn(email, password)
                _state.value = AuthState.Success(repo.currentUser())
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    fun sendPasswordReset(email: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                repo.sendPasswordReset(email)
                onComplete(Result.success(Unit))
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
        }
    }

    fun logout() {
        repo.signOut()
        _state.value = AuthState.Success(null)
    }

    fun clearError() {
        if (_state.value is AuthState.Error) {
            _state.value = AuthState.Idle
        }
    }
}