package week11.st573015.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st573015.finalproject.models.LocationPin

class LocationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun userLocations() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "__NO_UID__")
            .collection("locations")

    fun locationsFlow(): Flow<List<LocationPin>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val reg: ListenerRegistration = userLocations().addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snap?.documents
                ?.mapNotNull { it.toObject(LocationPin::class.java) }
                ?.sortedBy { it.title } ?: emptyList()

            trySend(list)
        }

        awaitClose { reg.remove() }
    }

    suspend fun addPin(pin: LocationPin): LocationPin {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        val doc = userLocations().document()
        val item = pin.copy(id = doc.id)
        doc.set(item).await()
        return item
    }

    suspend fun deletePin(id: String) {
        userLocations().document(id).delete().await()
    }
}