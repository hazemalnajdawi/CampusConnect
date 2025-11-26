package week11.st573015.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st573015.finalproject.models.Task

class TaskRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun userTasksCollection() =
        db.collection("users").document(auth.currentUser?.uid ?: "__NO_UID__").collection("tasks")

    /**
     * Real-time flow of tasks for current user.
     */
    fun tasksFlow(): Flow<List<Task>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val coll = db.collection("users").document(uid).collection("tasks")
        val registration: ListenerRegistration = coll.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snap?.documents
                ?.mapNotNull { it.toObject(Task::class.java) }
                ?.sortedWith(compareBy(nullsLast()) { it.dueTimestamp }) ?: emptyList()
            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    suspend fun createTask(task: Task): Task {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        val docRef = db.collection("users").document(uid).collection("tasks").document()
        val withId = task.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(withId).await()
        return withId
    }

    suspend fun updateTask(task: Task) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        if (task.id.isBlank()) throw IllegalArgumentException("Task id required")
        db.collection("users").document(uid).collection("tasks").document(task.id).set(task.copy(updatedAt = System.currentTimeMillis())).await()
    }

    suspend fun deleteTask(taskId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        if (taskId.isBlank()) return
        db.collection("users").document(uid).collection("tasks").document(taskId).delete().await()
    }
}