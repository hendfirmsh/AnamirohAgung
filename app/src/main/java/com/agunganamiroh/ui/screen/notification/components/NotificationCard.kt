package com.agunganamiroh.ui.screen.notification.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.agunganamiroh.data.model.NotificationCategory
import com.agunganamiroh.data.model.NotificationItem
import com.agunganamiroh.data.model.NotificationPriority
import com.agunganamiroh.motion.animateEntrance
import com.agunganamiroh.motion.bounceClick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCard(
    notification: NotificationItem,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = ""
    )

    val cardColors = if (notification.isRead)
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    else
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
        )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(animScale)
            .bounceClick()
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .animateEntrance(index * 50),
        shape = RoundedCornerShape(20.dp),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                    content = {}
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(notification.category).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(notification.category),
                    contentDescription = null,
                    tint = getCategoryColor(notification.category),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (notification.priority == NotificationPriority.HIGH && !notification.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = "Prioritas",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatRelativeTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = notification.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
}

private fun getCategoryIcon(category: NotificationCategory): ImageVector = when (category) {
    NotificationCategory.PAYMENT -> Icons.Default.Payments
    NotificationCategory.INVOICE -> Icons.Default.Receipt
    NotificationCategory.JAMAAH -> Icons.Default.Person
    NotificationCategory.SECURITY -> Icons.Default.Security
    NotificationCategory.SYSTEM -> Icons.Default.Info
    NotificationCategory.ANNOUNCEMENT -> Icons.Default.Campaign
}

private fun getCategoryColor(category: NotificationCategory): Color = when (category) {
    NotificationCategory.PAYMENT -> Color(0xFF4CAF50)
    NotificationCategory.INVOICE -> Color(0xFFFFA726)
    NotificationCategory.JAMAAH -> Color(0xFF42A5F5)
    NotificationCategory.SECURITY -> Color(0xFFEF5350)
    NotificationCategory.SYSTEM -> Color(0xFF78909C)
    NotificationCategory.ANNOUNCEMENT -> Color(0xFFAB47BC)
}

private fun formatRelativeTime(timeMillis: Long): String {
    if (timeMillis == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - timeMillis
    return when {
        diff < 60_000 -> "Baru saja"
        diff < 3600_000 -> "${diff / 60_000} menit lalu"
        diff < 86400_000 -> "${diff / 3600_000} jam lalu"
        diff < 604800_000 -> "${diff / 86400_000} hari lalu"
        else -> SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID")).format(Date(timeMillis))
    }
}
