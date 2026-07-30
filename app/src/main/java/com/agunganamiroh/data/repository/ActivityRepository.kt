package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ActivityRepository {
    private val firestore = FirebaseModule.firestore
    private val collection = firestore.collection("activity")

    suspend fun createActivity(activity: Activity): Result<Unit> {
        return try {
            val docRef = collection.document()
            val activityWithId = activity.copy(
                id = docRef.id,
                activityId = docRef.id,
                createdAt = activity.createdAt ?: Timestamp.now()
            )
            docRef.set(activityWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRecentActivities(agentEmail: String, limit: Int = 20): Flow<Result<List<Activity>>> = callbackFlow {
        var query = collection
            .whereEqualTo("agentEmail", agentEmail)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        if (limit > 0) {
            query = query.limit(limit.toLong())
        }
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val activities = snapshot.toObjects(Activity::class.java)
                trySend(Result.success(activities))
            }
        }
        awaitClose { subscription.remove() }
    }

    fun getActivityById(activityId: String): Flow<Result<Activity>> = callbackFlow {
        val subscription = collection.document(activityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val activity = snapshot.toObject(Activity::class.java)
                    if (activity != null) {
                        trySend(Result.success(activity))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteActivity(activityId: String): Result<Unit> {
        return try {
            collection.document(activityId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
