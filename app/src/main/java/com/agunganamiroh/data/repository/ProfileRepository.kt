package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.User
import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val auth = FirebaseModule.auth
    private val firestore = FirebaseModule.firestore

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    suspend fun loadProfile(uid: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found in database"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
