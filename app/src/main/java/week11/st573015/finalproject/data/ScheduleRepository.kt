package week11.st573015.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st573015.finalproject.models.ScheduleItem

class ScheduleRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun scheduleCollectionPath(uid: String) = db.collection("users").document(uid).collection("schedule")

    /**
     * Real-time flow of schedule items for the signed-in user.
     * Emits a list sorted by dayOfWeek then startMinutes.
     */
    fun scheduleFlow(): Flow<List<ScheduleItem>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val coll = scheduleCollectionPath(uid)
        val registration: ListenerRegistration = coll.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snap?.documents
                ?.mapNotNull { it.toObject(ScheduleItem::class.java) }
                ?.sortedWith(compareBy({ it.dayOfWeek }, { it.startMinutes })) ?: emptyList()
            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    suspend fun addItem(item: ScheduleItem): ScheduleItem {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        val docRef = scheduleCollectionPath(uid).document()
        val withId = item.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(withId).await()
        return withId
    }

    suspend fun updateItem(item: ScheduleItem) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        if (item.id.isBlank()) throw IllegalArgumentException("Item id required")
        scheduleCollectionPath(uid).document(item.id).set(item.copy(updatedAt = System.currentTimeMillis())).await()
    }

    suspend fun deleteItem(itemId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        if (itemId.isBlank()) return
        scheduleCollectionPath(uid).document(itemId).delete().await()
    }

    // One-time fetch (optional)
    suspend fun getAllOnce(): List<ScheduleItem> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return scheduleCollectionPath(uid).get().await().toObjects(ScheduleItem::class.java)
            .sortedWith(compareBy({ it.dayOfWeek }, { it.startMinutes }))
    }
}
