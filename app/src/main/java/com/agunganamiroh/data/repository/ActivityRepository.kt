package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.data.remote.FirebaseModule
import kotlinx.coroutines.tasks.await

class ActivityRepository {

    private val firestore = FirebaseModule.firestore

    suspend fun getActivities(): Result<List<Activity>> {

        return try {

            val currentUser = FirebaseModule.auth.currentUser
            if (currentUser == null) {
                return Result.success(emptyList())
            }

            val snapshot = firestore
                .collection("users")
                .document(currentUser.uid)
                .collection("activities")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val activities = snapshot.documents
                .mapNotNull { it.toObject(Activity::class.java) }
                .sortedByDescending { it.timestamp.seconds }

            Result.success(activities)

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    suspend fun createActivity(activity: Activity): Result<Unit> {
        return try {
            val currentUser = FirebaseModule.auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val docRef = if (activity.id.isBlank()) {
                firestore
                    .collection("users")
                    .document(currentUser.uid)
                    .collection("activities")
                    .document()
            } else {
                firestore
                    .collection("users")
                    .document(currentUser.uid)
                    .collection("activities")
                    .document(activity.id)
            }

            val data = activity.copy(id = docRef.id).let { act ->
                mapOf(
                    "id" to act.id,
                    "type" to act.type.name,
                    "title" to act.title,
                    "subtitle" to act.subtitle,
                    "description" to act.description,
                    "jamaahId" to act.jamaahId,
                    "jamaahName" to act.jamaahName,
                    "paymentId" to act.paymentId,
                    "invoiceId" to act.invoiceId,
                    "amount" to act.amount,
                    "status" to act.status,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
            }

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
