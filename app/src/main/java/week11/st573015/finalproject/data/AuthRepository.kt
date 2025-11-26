package week11.st573015.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import week11.st573015.finalproject.models.UserProfile

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun currentUserUid(): String? = auth.currentUser?.uid
    fun currentUser() = auth.currentUser

    /**
     * Register with email & password, create basic user doc in Firestore, update displayName.
     * Throws exceptions on failure (propagated to caller).
     */
    suspend fun register(email: String, password: String, displayName: String): UserProfile {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Registration failed")
        // Update displayName on FirebaseUser
        val profileUpdates = userProfileChangeRequest {
            this.displayName = displayName
        }
        firebaseUser.updateProfile(profileUpdates).await()

        // Create user doc in Firestore under /users/{uid}
        val profile = UserProfile(
            uid = firebaseUser.uid,
            displayName = displayName,
            email = firebaseUser.email ?: ""
        )
        db.collection("users").document(firebaseUser.uid).set(profile).await()
        return profile
    }

    /**
     * Sign in with email & password. Returns firebase user uid on success.
     */
    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    /**
     * Send password reset email.
     */
    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun signOut() {
        auth.signOut()
    }
}
