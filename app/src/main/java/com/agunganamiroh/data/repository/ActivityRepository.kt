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
}