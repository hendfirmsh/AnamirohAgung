package com.agunganamiroh.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

data class NotificationPrefs(
    val pushEnabled: Boolean = true,
    val paymentEnabled: Boolean = true,
    val invoiceEnabled: Boolean = true,
    val approvalEnabled: Boolean = true
)

class NotificationPreferenceManager(private val context: Context) {
    companion object {
        private val PUSH_KEY = booleanPreferencesKey("push_notifications")
        private val PAYMENT_KEY = booleanPreferencesKey("payment_notifications")
        private val INVOICE_KEY = booleanPreferencesKey("invoice_notifications")
        private val APPROVAL_KEY = booleanPreferencesKey("approval_notifications")
    }

    val notificationPrefs: Flow<NotificationPrefs> = context.notificationDataStore.data
        .map { prefs ->
            NotificationPrefs(
                pushEnabled = prefs[PUSH_KEY] ?: true,
                paymentEnabled = prefs[PAYMENT_KEY] ?: true,
                invoiceEnabled = prefs[INVOICE_KEY] ?: true,
                approvalEnabled = prefs[APPROVAL_KEY] ?: true
            )
        }

    suspend fun setPushEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[PUSH_KEY] = enabled }
    }

    suspend fun setPaymentEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[PAYMENT_KEY] = enabled }
    }

    suspend fun setInvoiceEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[INVOICE_KEY] = enabled }
    }

    suspend fun setApprovalEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[APPROVAL_KEY] = enabled }
    }
}
