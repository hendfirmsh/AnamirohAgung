package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.Pembayaran
import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

class JamaahRepository {
    private val firestore = FirebaseModule.firestore
    private val auth = FirebaseAuth.getInstance()
    private val collection = firestore.collection("jamaah")
    private val activityCollection = firestore.collection("activity")

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
                    trySend(Result.success(jamaahs))
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun addJamaah(jamaah: Jamaah): Result<String> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = collection.document()
                val jamaahWithId = jamaah.copy(id = docRef.id)
                transaction.set(docRef, jamaahWithId)
                transaction.update(docRef, "createdAt", FieldValue.serverTimestamp())

                val agentEmail = jamaah.input_by.ifBlank { auth.currentUser?.email ?: "unknown" }
                val agentUid = auth.currentUser?.uid ?: ""

                val activityRef = activityCollection.document()
                val activity = mapOf<String, Any>(
                    "activityId" to activityRef.id,
                    "type" to "JAMAAH_CREATED",
                    "title" to "Pendaftaran Baru",
                    "description" to "${jamaah.nama} • ${jamaah.program}",
                    "jamaahId" to docRef.id,
                    "jamaahName" to jamaah.nama,
                    "paymentId" to "",
                    "invoiceId" to "",
                    "agentUid" to agentUid,
                    "agentEmail" to agentEmail,
                    "amount" to 0L,
                    "status" to "pending",
                    "createdAt" to Timestamp.now(),
                    "metadata" to mapOf<String, Any>(
                        "program" to jamaah.program,
                        "dp" to jamaah.dp
                    )
                )
                transaction.set(activityRef, activity)

                docRef.id
            }.await().let { id ->
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteJamaah(id: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val jamaahRef = collection.document(id)
                val jamaahSnapshot = transaction.get(jamaahRef)
                val jamaah = jamaahSnapshot.toObject(Jamaah::class.java)

                transaction.delete(jamaahRef)

                val agentEmail = auth.currentUser?.email ?: "unknown"
                val agentUid = auth.currentUser?.uid ?: ""

                val activityRef = activityCollection.document()
                val activity = mapOf<String, Any>(
                    "activityId" to activityRef.id,
                    "type" to "JAMAAH_DELETED",
                    "title" to "Data Jamaah Dihapus",
                    "description" to (jamaah?.nama ?: "Unknown"),
                    "jamaahId" to id,
                    "jamaahName" to (jamaah?.nama ?: ""),
                    "paymentId" to "",
                    "invoiceId" to "",
                    "agentUid" to agentUid,
                    "agentEmail" to agentEmail,
                    "amount" to 0L,
                    "status" to "deleted",
                    "createdAt" to Timestamp.now(),
                    "metadata" to emptyMap<String, Any>()
                )
                transaction.set(activityRef, activity)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJamaah(id: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val jamaahRef = collection.document(id)
                val jamaahSnapshot = transaction.get(jamaahRef)
                val jamaah = jamaahSnapshot.toObject(Jamaah::class.java)

                transaction.update(jamaahRef, updates)

                val agentEmail = auth.currentUser?.email ?: "unknown"
                val agentUid = auth.currentUser?.uid ?: ""

                val activityRef = activityCollection.document()
                val changedFields = updates.keys.joinToString(", ")
                val activity = mapOf<String, Any>(
                    "activityId" to activityRef.id,
                    "type" to "JAMAAH_UPDATED",
                    "title" to "Data Jamaah Diperbarui",
                    "description" to "${jamaah?.nama ?: "Unknown"} • $changedFields",
                    "jamaahId" to id,
                    "jamaahName" to (jamaah?.nama ?: ""),
                    "paymentId" to "",
                    "invoiceId" to "",
                    "agentUid" to agentUid,
                    "agentEmail" to agentEmail,
                    "amount" to 0L,
                    "status" to (updates["status"] as? String ?: jamaah?.status ?: ""),
                    "createdAt" to Timestamp.now(),
                    "metadata" to mapOf<String, Any>(
                        "changedFields" to changedFields
                    )
                )
                transaction.set(activityRef, activity)
            }.await()
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

    fun getPaymentHistory(jamaahId: String): Flow<Result<List<com.agunganamiroh.data.model.Pembayaran>>> = callbackFlow {
        val subscription = firestore.collection("pembayaran")
            .whereEqualTo("jamaahId", jamaahId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
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

    fun getAllPaymentsByAgent(agentEmail: String): Flow<Result<List<Pembayaran>>> = callbackFlow {
        val subscription = firestore.collection("pembayaran")
            .whereEqualTo("agentEmail", agentEmail)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val payments = snapshot.toObjects(Pembayaran::class.java)
                    trySend(Result.success(payments))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getGlobalPaymentsByAgent(agentEmail: String): Flow<Result<List<Pembayaran>>> = callbackFlow {
        val subscription = firestore.collection("pembayaran")
            .whereEqualTo("agentEmail", agentEmail)
            .orderBy("createdAt", Query.Direction.DESCENDING)
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
