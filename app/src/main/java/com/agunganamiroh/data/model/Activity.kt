package com.agunganamiroh.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Activity(
    @DocumentId
    val id: String = "",
    val activityId: String = "",
    val type: String = "",
    val title: String = "",
    val description: String = "",
    val jamaahId: String = "",
    val jamaahName: String = "",
    val paymentId: String = "",
    val invoiceId: String = "",
    val agentUid: String = "",
    val agentEmail: String = "",
    val amount: Long = 0,
    val status: String = "",
    val createdAt: Timestamp? = null,
    val metadata: Map<String, Any> = emptyMap()
)
