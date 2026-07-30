package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.data.model.NotificationCategory
import com.agunganamiroh.data.model.NotificationItem
import com.agunganamiroh.data.model.NotificationPriority

class NotificationRepository {

    fun mapToNotification(activity: Activity, isRead: Boolean): NotificationItem {
        val category = resolveCategory(activity.type)
        val priority = resolvePriority(activity.type)
        val timestamp = activity.createdAt?.toDate()?.time ?: System.currentTimeMillis()

        return NotificationItem(
            id = activity.id,
            type = activity.type,
            category = category,
            priority = priority,
            title = activity.title,
            description = activity.description,
            timestamp = timestamp,
            isRead = isRead,
            jamaahId = activity.jamaahId,
            jamaahName = activity.jamaahName,
            paymentId = activity.paymentId,
            invoiceId = activity.invoiceId,
            amount = activity.amount,
            status = activity.status
        )
    }

    fun resolveCategory(type: String): NotificationCategory = when {
        type.startsWith("PAYMENT") -> NotificationCategory.PAYMENT
        type.startsWith("INVOICE") -> NotificationCategory.INVOICE
        type.startsWith("JAMAAH") -> NotificationCategory.JAMAAH
        else -> NotificationCategory.SYSTEM
    }

    fun resolvePriority(type: String): NotificationPriority = when {
        type.startsWith("PAYMENT") || type.startsWith("INVOICE") || type.startsWith("REGIST") -> NotificationPriority.HIGH
        type.startsWith("JAMAAH") -> NotificationPriority.HIGH
        type == "PROFILE_UPDATED" -> NotificationPriority.NORMAL
        else -> NotificationPriority.LOW
    }

    fun resolveNavigationRoute(notification: NotificationItem): String? = when (notification.category) {
        NotificationCategory.JAMAAH -> if (notification.jamaahId.isNotBlank()) "detail_jamaah/${notification.jamaahId}" else null
        NotificationCategory.PAYMENT -> if (notification.jamaahId.isNotBlank()) "payment_detail/${notification.jamaahId}" else null
        NotificationCategory.INVOICE -> if (notification.invoiceId.isNotBlank()) "agent_invoice_detail/${notification.invoiceId}" else null
        NotificationCategory.SECURITY -> "account_center"
        NotificationCategory.SYSTEM -> "account_center"
        NotificationCategory.ANNOUNCEMENT -> null
    }

    fun getCategoryDisplayName(category: NotificationCategory): String = category.displayName

    companion object {
        @Volatile
        private var instance: NotificationRepository? = null

        fun getInstance(): NotificationRepository {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepository().also { instance = it }
            }
        }
    }
}
