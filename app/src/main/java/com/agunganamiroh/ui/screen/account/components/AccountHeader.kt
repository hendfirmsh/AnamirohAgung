package com.agunganamiroh.ui.screen.account.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agunganamiroh.data.model.User
import com.agunganamiroh.motion.animateEntrance
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AccountHeader(
    user: User?,
    jamaahCount: Int,
    invoiceCount: Int,
    onEditPhotoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .animateEntrance(0),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.10f),
                            primaryContainer.copy(alpha = 0.05f),
                            surfaceColor
                        )
                    )
                )
                .drawBehind {
                    val s = size
                    val path = Path().apply {
                        moveTo(s.width * 0.7f, 0f)
                        cubicTo(s.width * 0.8f, s.height * 0.3f, s.width * 0.95f, s.height * 0.4f, s.width, s.height * 0.5f)
                        lineTo(s.width, 0f)
                        close()
                    }
                    drawPath(
                        path,
                        color = primaryColor.copy(alpha = 0.04f)
                    )
                    val path2 = Path().apply {
                        moveTo(s.width * 0.85f, 0f)
                        cubicTo(s.width * 0.9f, s.height * 0.4f, s.width, s.height * 0.6f, s.width, s.height * 0.7f)
                        lineTo(s.width, 0f)
                        close()
                    }
                    drawPath(
                        path2,
                        color = primaryColor.copy(alpha = 0.03f)
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.03f),
                        radius = 120.dp.toPx(),
                        center = Offset(s.width * 0.9f, s.height * 0.15f)
                    )
                }
                .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InitialsAvatar(
                    name = user?.fullName ?: user?.companyName ?: "User",
                    size = 88.dp,
                    textSize = 30,
                    onEditClick = onEditPhotoClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.fullName ?: user?.companyName ?: "Pengguna",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = (user?.role ?: "AGENT").uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (user?.accountStatus == "active") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .drawBehind {
                                            drawCircle(Color(0xFF4CAF50))
                                        }
                                )
                                Text(
                                    text = "Aktif",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (user?.companyName?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = user.companyName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (user?.agentCode?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Kode Agen: ${user.agentCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (user?.email?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStatCard(
                    label = "Jamaah",
                    value = jamaahCount.toString(),
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    label = "Invoice",
                    value = invoiceCount.toString(),
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    label = "Anggota Sejak",
                    value = user?.createdAt?.let {
                        try {
                            SimpleDateFormat("yyyy", Locale.forLanguageTag("id-ID")).format(it.toDate())
                        } catch (e: Exception) { "-" }
                    } ?: "-",
                    icon = Icons.Default.Event,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
