package com.agunganamiroh.data.repository

import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.data.model.ActivityType
import com.agunganamiroh.data.model.NotificationCategory
import com.agunganamiroh.data.model.NotificationItem
import com.agunganamiroh.data.model.NotificationPriority

class NotificationRepository {

    fun mapToNotification(activity: Activity, isRead: Boolean): NotificationItem {
        val category = resolveCategory(activity.type)
        val priority = resolvePriority(activity.type)
        val timestamp = activity.timestamp.toDate().time

        return NotificationItem(
            id = activity.id,
            type = activity.type.name,
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

    fun resolveCategory(type: ActivityType): NotificationCategory = when (type) {
        ActivityType.PAYMENT -> NotificationCategory.PAYMENT
        ActivityType.INVOICE -> NotificationCategory.INVOICE
        ActivityType.REGISTRATION -> NotificationCategory.JAMAAH
        ActivityType.STATUS_UPDATE -> NotificationCategory.SYSTEM
        ActivityType.PROFILE_UPDATE -> NotificationCategory.SYSTEM
        ActivityType.OTHER -> NotificationCategory.SYSTEM
    }

    fun resolvePriority(type: ActivityType): NotificationPriority = when (type) {
        ActivityType.PAYMENT -> NotificationPriority.HIGH
        ActivityType.INVOICE -> NotificationPriority.HIGH
        ActivityType.REGISTRATION -> NotificationPriority.HIGH
        ActivityType.STATUS_UPDATE -> NotificationPriority.NORMAL
        ActivityType.PROFILE_UPDATE -> NotificationPriority.NORMAL
        ActivityType.OTHER -> NotificationPriority.LOW
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
