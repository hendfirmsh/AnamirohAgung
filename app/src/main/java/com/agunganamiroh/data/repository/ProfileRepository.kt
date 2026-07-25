package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Invoice
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.User
import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val auth = FirebaseModule.auth
    private val firestore = FirebaseModule.firestore

    fun getCurrentUserUid(): String? = auth.currentUser?.uid
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun observeUser(uid: String): Flow<Result<User>> = callbackFlow {
        val subscription = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(Result.success(user))
                    }
                } else {
                    trySend(Result.failure(Exception("User not found")))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun observeJamaahCount(agentEmail: String): Flow<Result<List<Jamaah>>> = callbackFlow {
        val subscription = firestore.collection("jamaah")
            .whereEqualTo("input_by", agentEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Jamaah::class.java)
                    trySend(Result.success(list))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun observeInvoiceCount(agentEmail: String): Flow<Result<List<Invoice>>> = callbackFlow {
        val subscription = firestore.collection("invoices")
            .whereEqualTo("agentEmail", agentEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Invoice::class.java)
                    trySend(Result.success(list))
                }
            }
        awaitClose { subscription.remove() }
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
