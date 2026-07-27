package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PaketRepository {
    private val firestore = FirebaseModule.firestore
    private val collection = firestore.collection("paket_umroh")

    fun getPaketRealtime(): Flow<Result<List<Paket>>> = callbackFlow {
        val subscription = collection
            .orderBy("tanggal", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val pakets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Paket::class.java)?.copy(id = doc.id)
                    }
                    trySend(Result.success(pakets))
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun getAllPaket(): Result<List<Paket>> {
        return try {
            val snapshot = collection.get().await()
            val pakets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Paket::class.java)?.copy(id = doc.id)
            }
            Result.success(pakets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaketById(id: String): Result<Paket?> {
        return try {
            val document = collection.document(id).get().await()
            val paket = document.toObject(Paket::class.java)
            Result.success(paket)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
