package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.model.AlertType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreAlertRepository {

    private val db = FirebaseFirestore.getInstance()
    private val alertsCollection = db.collection("alerts")
    private var listenerRegistration: ListenerRegistration? = null

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
        return snapshot.documents.mapNotNull { doc ->
            try {
                Alert(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    message = doc.getString("message") ?: "",
                    time = doc.getString("time") ?: "",
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                    type = try {
                        AlertType.valueOf(doc.getString("type") ?: "PRICE")
                    } catch (e: Exception) {
                        AlertType.PRICE
                    },
                    isRead = doc.getBoolean("isRead") ?: false,
                    actionRoute = doc.getString("actionRoute") ?: "",
                    location = doc.getString("location") ?: ""
                )
            } catch (e: Exception) {
                null
            }
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
                val alerts = snapshot.documents.mapNotNull { doc ->
                    try {
                        Alert(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            time = doc.getString("time") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            type = try {
                                AlertType.valueOf(doc.getString("type") ?: "PRICE")
                            } catch (e: Exception) {
                                AlertType.PRICE
                            },
                            isRead = doc.getBoolean("isRead") ?: false,
                            actionRoute = doc.getString("actionRoute") ?: "",
                            location = doc.getString("location") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onAlertsReceived(alerts)
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    suspend fun markAsRead(alertId: String) {
        alertsCollection.document(alertId).update("isRead", true).await()
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
