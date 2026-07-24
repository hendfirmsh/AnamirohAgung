package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Invoice
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceRepository {
    private val firestore = FirebaseModule.firestore
    private val auth = FirebaseAuth.getInstance()
    private val collection = firestore.collection("invoices")
    private val jamaahCollection = firestore.collection("jamaah")
    private val activityCollection = firestore.collection("activity")

    suspend fun createInvoice(invoice: Invoice): Result<String> {
        return try {
            val invoiceNo = generateInvoiceNumber()
            firestore.runTransaction { transaction ->
                val docRef = collection.document()
                val now = Timestamp.now()
                val invoiceWithId = invoice.copy(
                    id = docRef.id,
                    noInvoice = invoiceNo,
                    createdAt = now,
                    updatedAt = now
                )
                transaction.set(docRef, invoiceWithId)

                val agentUid = auth.currentUser?.uid ?: ""
                val agentEmail = auth.currentUser?.email ?: "unknown"

                val activityRef = activityCollection.document()
                val activity = mapOf<String, Any>(
                    "activityId" to activityRef.id,
                    "type" to "INVOICE_PUBLISHED",
                    "title" to "Invoice Diterbitkan",
                    "description" to "${invoice.jamaahName} • $invoiceNo",
                    "jamaahId" to invoice.jamaahId,
                    "jamaahName" to invoice.jamaahName,
                    "paymentId" to "",
                    "invoiceId" to docRef.id,
                    "agentUid" to agentUid,
                    "agentEmail" to agentEmail,
                    "amount" to invoice.totalTagihan,
                    "status" to "published",
                    "createdAt" to Timestamp.now(),
                    "metadata" to mapOf(
                        "noInvoice" to invoiceNo,
                        "totalTagihan" to invoice.totalTagihan
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

    suspend fun updateInvoice(id: String, updates: MutableMap<String, Any>): Result<Unit> {
        return try {
            updates["updatedAt"] = Timestamp.now()
            collection.document(id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelInvoice(id: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val invoiceRef = collection.document(id)
                val snapshot = transaction.get(invoiceRef)
                val invoice = snapshot.toObject(Invoice::class.java)
                    ?: throw Exception("Invoice not found")

                transaction.update(invoiceRef, "status", "cancelled")
                transaction.update(invoiceRef, "updatedAt", Timestamp.now())

                val agentUid = auth.currentUser?.uid ?: ""
                val agentEmail = auth.currentUser?.email ?: "unknown"

                val activityRef = activityCollection.document()
                val activity = mapOf<String, Any>(
                    "activityId" to activityRef.id,
                    "type" to "INVOICE_CANCELLED",
                    "title" to "Invoice Dibatalkan",
                    "description" to "${invoice.jamaahName} • ${invoice.noInvoice}",
                    "jamaahId" to invoice.jamaahId,
                    "jamaahName" to invoice.jamaahName,
                    "paymentId" to "",
                    "invoiceId" to id,
                    "agentUid" to agentUid,
                    "agentEmail" to agentEmail,
                    "amount" to 0L,
                    "status" to "cancelled",
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

    fun getInvoicesForAgent(agentEmail: String): Flow<Result<List<Invoice>>> = callbackFlow {
        val subscription = collection
            .whereEqualTo("agentEmail", agentEmail)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val invoices = snapshot.toObjects(Invoice::class.java)
                    trySend(Result.success(invoices))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getInvoiceById(id: String): Flow<Result<Invoice>> = callbackFlow {
        val subscription = collection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val invoice = snapshot.toObject(Invoice::class.java)
                    if (invoice != null) {
                        trySend(Result.success(invoice))
                    } else {
                        trySend(Result.failure(Exception("Data invoice tidak valid")))
                    }
                } else {
                    trySend(Result.failure(Exception("Data invoice tidak ditemukan")))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getInvoiceByJamaahId(jamaahId: String): Flow<Result<List<Invoice>>> = callbackFlow {
        val subscription = collection
            .whereEqualTo("jamaahId", jamaahId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val invoices = snapshot.toObjects(Invoice::class.java)
                    trySend(Result.success(invoices))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getAllInvoices(): Flow<Result<List<Invoice>>> = callbackFlow {
        val subscription = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val invoices = snapshot.toObjects(Invoice::class.java)
                    trySend(Result.success(invoices))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getApprovedJamaahs(): Flow<Result<List<Jamaah>>> = callbackFlow {
        val subscription = jamaahCollection
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val jamaahs = snapshot.toObjects(Jamaah::class.java)
                    trySend(Result.success(jamaahs))
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getJamaahById(jamaahId: String): Flow<Result<Jamaah>> = callbackFlow {
        val subscription = jamaahCollection.document(jamaahId)
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

    private suspend fun generateInvoiceNumber(): String {
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val prefix = "INV-$year-"
        return try {
            val lastInvoice = collection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            val lastNumber = if (!lastInvoice.isEmpty) {
                val lastNo = lastInvoice.toObjects(Invoice::class.java).first().noInvoice
                lastNo.substringAfterLast("-").toIntOrNull() ?: 0
            } else 0
            val nextNumber = lastNumber + 1
            "${prefix}${String.format("%04d", nextNumber)}"
        } catch (e: Exception) {
            "${prefix}0001"
        }
    }
}
