package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.model.AlertType
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreAlertRepository {

    private val db = FirebaseFirestore.getInstance()
    private val alertsCollection = db.collection("alerts")
    private var listenerRegistration: ListenerRegistration? = null
    // Separate slot so the home-screen unread badge can coexist with the
    // alerts screen's own realtime listener.
    private var unreadListenerRegistration: ListenerRegistration? = null

    private fun DocumentSnapshot.toAlert(): Alert? = try {
        Alert(
            id = id,
            title = getString("title") ?: "",
            message = getString("message") ?: "",
            time = getString("time") ?: "",
            timestamp = getLong("timestamp") ?: System.currentTimeMillis(),
            type = try {
                AlertType.valueOf(getString("type") ?: "PRICE")
            } catch (e: Exception) {
                AlertType.PRICE
            },
            isRead = getBoolean("isRead") ?: false,
            actionRoute = getString("actionRoute") ?: "",
            location = getString("location") ?: ""
        )
    } catch (e: Exception) {
        null
    }

    suspend fun getAlerts(
        location: String? = null,
        type: AlertType? = null,
        limit: Long = 20
    ): List<Alert> {
        var query: Query = alertsCollection.orderBy("timestamp", Query.Direction.DESCENDING)

        if (location != null) {
            query = query.whereEqualTo("location", location)
        }
        if (type != null) {
            query = query.whereEqualTo("type", type.name)
        }

        query = query.limit(limit)

        val snapshot = query.get().await()
        return dedupe(snapshot.documents.mapNotNull { it.toAlert() })
    }

    /** Collapse alerts that share the same content (duplicate protection). */
    private fun dedupe(alerts: List<Alert>): List<Alert> {
        val seen = HashSet<String>()
        return alerts.filter { alert ->
            val key = "${alert.type}_${alert.title}_${alert.message}".hashCode().toString()
            seen.add(key)
        }
    }

    fun listenForAlerts(
        limit: Long = 30,
        onAlertsReceived: (List<Alert>) -> Unit
    ) {
        listenerRegistration?.remove()
        listenerRegistration = alertsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val alerts = dedupe(snapshot.documents.mapNotNull { it.toAlert() })
                onAlertsReceived(alerts)
            }
    }

    /** Realtime count of unread alerts, mirroring the list query's shape. */
    fun listenForUnreadCount(
        limit: Long = 30,
        onCountChanged: (Int) -> Unit
    ) {
        unreadListenerRegistration?.remove()
        unreadListenerRegistration = alertsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val unread = dedupe(snapshot.documents.mapNotNull { it.toAlert() })
                    .count { !it.isRead }
                onCountChanged(unread)
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun stopUnreadListening() {
        unreadListenerRegistration?.remove()
        unreadListenerRegistration = null
    }

    suspend fun markAsRead(alertId: String) {
        alertsCollection.document(alertId).update("isRead", true).await()
    }

    suspend fun markAllAsRead(alertIds: List<String>) {
        val batch = db.batch()
        alertIds.forEach { id ->
            batch.update(alertsCollection.document(id), "isRead", true)
        }
        batch.commit().await()
    }

    suspend fun saveToken(token: String, location: String, crops: List<String>) {
        val deviceDoc = db.collection("user_tokens").document(token)
        deviceDoc.set(
            mapOf(
                "token" to token,
                "location" to location,
                "preferredCrops" to crops,
                "lastActive" to System.currentTimeMillis()
            )
        ).await()
    }

    companion object {
        private var instance: FirestoreAlertRepository? = null

        fun getInstance(): FirestoreAlertRepository {
            return instance ?: synchronized(this) {
                instance ?: FirestoreAlertRepository().also { instance = it }
            }
        }
    }
}
