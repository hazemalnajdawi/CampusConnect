package week11.st573015.finalproject.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st573015.finalproject.models.Announcement

class AnnouncementRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun announcementsCollection() = db.collection("announcements")

    /**
     * Real-time flow of announcements.
     */
    fun announcementsFlow(): Flow<List<Announcement>> = callbackFlow {
        val registration: ListenerRegistration = announcementsCollection()
            .orderBy("createdAt")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(Announcement::class.java) }
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }

    suspend fun addAnnouncement(announcement: Announcement): Announcement {
        val doc = announcementsCollection().document()
        val withId = announcement.copy(id = doc.id)
        doc.set(withId).await()
        return withId
    }

    suspend fun deleteAnnouncement(id: String) {
        if (id.isBlank()) return
        announcementsCollection().document(id).delete().await()
    }
}