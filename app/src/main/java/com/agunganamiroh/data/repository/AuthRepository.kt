package com.agunganamiroh.data.repository

import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseModule.auth
    private val firestore = FirebaseModule.firestore

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> {

        return try {

            val result = auth
                .signInWithEmailAndPassword(
                    email,
                    password
                )
                .await()

            val user = result.user

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(
                    Exception("User tidak ditemukan")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun getUserRole(
        uid: String
    ): String {

        return try {

            val document = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            document.getString("role")
                ?: "agent"

        } catch (e: Exception) {

            "agent"

        }
    }
}