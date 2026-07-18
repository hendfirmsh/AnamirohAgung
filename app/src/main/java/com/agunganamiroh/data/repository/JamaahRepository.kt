package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

class JamaahRepository {
    private val firestore = FirebaseModule.firestore
    private val collection = firestore.collection("jamaah")

    fun getJamaahRealtime(agentEmail: String): Flow<Result<List<Jamaah>>> = callbackFlow {
        Log.d("JamaahRepository", "Querying Firestore for input_by: $agentEmail")
        
        val subscription = collection
            .whereEqualTo("input_by", agentEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("JamaahRepository", "Firestore error: ${error.message}", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val jamaahs = snapshot.toObjects(Jamaah::class.java)
                    Log.d("JamaahRepository", "Found ${jamaahs.size} documents")
                    
                    if (jamaahs.isEmpty()) {
                        Log.d("JamaahRepository", "No documents found for $agentEmail")
                    } else {
                        jamaahs.forEach { jamaah ->
                            Log.d("JamaahRepository", "Document: ID=${jamaah.id}, Nama=${jamaah.nama}, Status=${jamaah.status}")
                        }
                    }

                    trySend(Result.success(jamaahs))
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun addJamaah(jamaah: Jamaah): Result<String> {
        return try {
            val docRef = collection.add(jamaah).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteJamaah(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateJamaah(id: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            collection.document(id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getJamaahById(id: String): Flow<Result<Jamaah>> = callbackFlow {
        val subscription = collection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val jamaah = snapshot.toObject(Jamaah::class.java)
                    if (jamaah != null) {
                        trySend(Result.success(jamaah))
                    } else {
                        trySend(Result.failure(Exception("Data jamaah tidak valid")))
                    }
                } else {
                    trySend(Result.failure(Exception("Data jamaah tidak ditemukan")))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addPayment(
        jamaahId: String,
        pembayaran: com.agunganamiroh.data.model.Pembayaran,
        newTotalDp: Long,
        isLunas: Boolean
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. Add to payments subcollection
                val paymentRef = collection.document(jamaahId).collection("payments").document()
                transaction.set(paymentRef, pembayaran.copy(id = paymentRef.id))

                // 2. Update main jamaah document
                val jamaahRef = collection.document(jamaahId)
                transaction.update(jamaahRef, "dp", newTotalDp)
                transaction.update(jamaahRef, "pelunasan", isLunas)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPaymentHistory(jamaahId: String): Flow<Result<List<com.agunganamiroh.data.model.Pembayaran>>> = callbackFlow {
        val subscription = collection.document(jamaahId).collection("payments")
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val payments = snapshot.toObjects(com.agunganamiroh.data.model.Pembayaran::class.java)
                    trySend(Result.success(payments))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getPaketById(paketId: String): Flow<Result<com.agunganamiroh.data.model.Paket>> = callbackFlow {
        val subscription = firestore.collection("paket_umroh").document(paketId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val paket = snapshot.toObject(com.agunganamiroh.data.model.Paket::class.java)
                    if (paket != null) {
                        trySend(Result.success(paket))
                    } else {
                        trySend(Result.failure(Exception("Data paket tidak valid")))
                    }
                } else {
                    trySend(Result.failure(Exception("Data paket tidak ditemukan")))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getGlobalPaymentsByAgent(agentEmail: String): Flow<Result<List<com.agunganamiroh.data.model.Pembayaran>>> = callbackFlow {
        val subscription = firestore.collectionGroup("payments")
            .whereEqualTo("dibuatOleh", agentEmail)
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val payments = snapshot.toObjects(com.agunganamiroh.data.model.Pembayaran::class.java)
                    trySend(Result.success(payments))
                }
            }
        awaitClose { subscription.remove() }
    }
}
