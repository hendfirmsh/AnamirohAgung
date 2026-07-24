package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.Kwitansi
import com.agunganamiroh.data.model.Pembayaran
import com.agunganamiroh.data.remote.FirebaseModule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PembayaranRepository {
    private val firestore = FirebaseModule.firestore
    private val auth = FirebaseAuth.getInstance()
    private val pembayaranCollection = firestore.collection("pembayaran")
    private val jamaahCollection = firestore.collection("jamaah")
    private val activityCollection = firestore.collection("activity")
    private val kwitansiCollection = firestore.collection("kwitansi")

    suspend fun addPayment(payment: Pembayaran): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val jamaahRef = jamaahCollection.document(payment.jamaahId)
                val jamaahSnapshot = transaction.get(jamaahRef)
                val jamaah = jamaahSnapshot.toObject(Jamaah::class.java)
                    ?: throw Exception("Jamaah not found")

                val paymentRef = pembayaranCollection.document()
                val paymentWithId = payment.copy(id = paymentRef.id)
                transaction.set(paymentRef, paymentWithId)

                val newTotalPaid = jamaah.dp + payment.nominal
                val isLunas = newTotalPaid >= jamaah.hargaPaket

                transaction.update(jamaahRef, "dp", newTotalPaid)
                transaction.update(jamaahRef, "pelunasan", isLunas)

                val now = Timestamp.now()
                val agentEmail = payment.agentEmail.ifBlank { auth.currentUser?.email ?: "unknown" }
                val agentUid = auth.currentUser?.uid ?: ""

                val activityRef = activityCollection.document()
                val paymentActivity = mapOf<String, Any>(
                    "activityId" to activityRef.id,
                    "type" to "PAYMENT_ADDED",
                    "title" to "Pembayaran Diterima",
                    "description" to "${jamaah.nama} • +Rp${String.format("%,d", payment.nominal).replace(',', '.')}",
                    "jamaahId" to jamaah.id,
                    "jamaahName" to jamaah.nama,
                    "paymentId" to paymentRef.id,
                    "invoiceId" to "",
                    "agentUid" to agentUid,
                    "agentEmail" to agentEmail,
                    "amount" to payment.nominal,
                    "status" to if (isLunas) "completed" else "partial",
                    "createdAt" to now,
                    "metadata" to mapOf(
                        "metode" to payment.metode,
                        "catatan" to payment.catatan
                    )
                )
                transaction.set(activityRef, paymentActivity)

                val invoiceNo = "INV-${jamaah.id}-${System.currentTimeMillis()}"
                val kwitansiRef = kwitansiCollection.document()
                val kwitansi = Kwitansi(
                    id = kwitansiRef.id,
                    jamaahId = jamaah.id,
                    noInvoice = invoiceNo,
                    tanggalInvoice = payment.tanggal,
                    subtotal = payment.nominal,
                    totalTagihan = jamaah.hargaPaket,
                    dp = newTotalPaid,
                    upgradeDouble = 0,
                    upgradeTriple = 0,
                    koper = 0,
                    paspor = 0,
                    vaksin = 0
                )
                transaction.set(kwitansiRef, kwitansi)

                val invoiceActivityRef = activityCollection.document()
                val invoiceActivity = mapOf<String, Any>(
                    "activityId" to invoiceActivityRef.id,
                    "type" to "INVOICE_CREATED",
                    "title" to "Invoice Diterbitkan",
                    "description" to "${jamaah.nama} • $invoiceNo",
                    "jamaahId" to jamaah.id,
                    "jamaahName" to jamaah.nama,
                    "paymentId" to paymentRef.id,
                    "invoiceId" to kwitansiRef.id,
                    "agentUid" to agentUid,
                    "agentEmail" to agentEmail,
                    "amount" to payment.nominal,
                    "status" to "active",
                    "createdAt" to Timestamp.now(),
                    "metadata" to mapOf(
                        "noInvoice" to invoiceNo,
                        "metode" to payment.metode
                    )
                )
                transaction.set(invoiceActivityRef, invoiceActivity)

                if (isLunas) {
                    val completedActivityRef = activityCollection.document()
                    val completedActivity = mapOf<String, Any>(
                        "activityId" to completedActivityRef.id,
                        "type" to "PAYMENT_COMPLETED",
                        "title" to "Pembayaran Lunas",
                        "description" to "${jamaah.nama} • Pembayaran telah lunas",
                        "jamaahId" to jamaah.id,
                        "jamaahName" to jamaah.nama,
                        "paymentId" to paymentRef.id,
                        "invoiceId" to kwitansiRef.id,
                        "agentUid" to agentUid,
                        "agentEmail" to agentEmail,
                        "amount" to newTotalPaid,
                        "status" to "completed",
                        "createdAt" to Timestamp.now(),
                        "metadata" to emptyMap<String, Any>()
                    )
                    transaction.set(completedActivityRef, completedActivity)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPaymentsByJamaah(jamaahId: String): Flow<Result<List<Pembayaran>>> = callbackFlow {
        val subscription = pembayaranCollection
            .whereEqualTo("jamaahId", jamaahId)
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

    fun getJamaahForAgent(agentEmail: String): Flow<Result<List<Jamaah>>> = callbackFlow {
        val subscription = jamaahCollection
            .whereEqualTo("input_by", agentEmail)
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
}
