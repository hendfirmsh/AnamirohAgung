package com.agunganamiroh.ui.screen.riwayat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.motion.animateEntrance
import com.agunganamiroh.viewmodel.ActivityViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(
    navController: NavController,
    activityViewModel: ActivityViewModel = viewModel()
) {
    val uiState by activityViewModel.uiState.collectAsStateWithLifecycle()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Riwayat Aktivitas", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 10 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when {
                        uiState.loading -> LoadingState()
                        uiState.error != null -> ErrorState(
                            message = uiState.error!!,
                            onRetry = { activityViewModel.observeActivities() }
                        )
                        uiState.activities.isEmpty() -> EmptyState()
                        else -> ActivityList(
                            activities = uiState.activities.sortedByDescending { it.createdAt?.toDate() ?: Date() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityList(activities: List<Activity>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(activities) { activity ->
            ActivityCard(activity = activity)
        }
    }
}

@Composable
private fun ActivityCard(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth().animateEntrance(50),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(activityIconColor(activity.type).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activityIcon(activity.type),
                    contentDescription = null,
                    tint = activityIconColor(activity.type),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (activity.description.isNotBlank()) {
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(activity.createdAt?.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (activity.amount > 0) {
                Text(
                    text = "Rp${String.format("%,d", activity.amount)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Memuat riwayat...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Belum Ada Aktivitas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Riwayat aktivitas akan muncul di sini",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Gagal Memuat Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun IslamicPatternOverlay() {
    val patternColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val opacity = if (backgroundColor.luminance() < 0.5f) 0.05f else 0.03f

    Canvas(modifier = Modifier.fillMaxSize().alpha(opacity)) {
        val sizePx = 60.dp.toPx()
        val strokeWidth = 1.dp.toPx()
        for (x in 0..(size.width / sizePx).toInt()) {
            for (y in 0..(size.height / sizePx).toInt()) {
                val cx = x * sizePx
                val cy = y * sizePx
                drawPath(path = Path().apply {
                    moveTo(cx, cy - sizePx * 0.5f)
                    lineTo(cx + sizePx * 0.15f, cy - sizePx * 0.15f)
                    lineTo(cx + sizePx * 0.5f, cy)
                    lineTo(cx + sizePx * 0.15f, cy + sizePx * 0.15f)
                    lineTo(cx, cy + sizePx * 0.5f)
                    lineTo(cx - sizePx * 0.15f, cy + sizePx * 0.15f)
                    lineTo(cx - sizePx * 0.5f, cy)
                    lineTo(cx - sizePx * 0.15f, cy - sizePx * 0.15f)
                    close()
                }, color = patternColor, style = Stroke(width = strokeWidth))
            }
        }
    }
}

private fun activityIcon(type: String): ImageVector = when (type.lowercase()) {
    "create", "registration", "register" -> Icons.Default.PersonAdd
    "update", "edit" -> Icons.Default.Edit
    "payment" -> Icons.Default.Payments
    "invoice" -> Icons.Default.Receipt
    "delete" -> Icons.Default.Delete
    "login" -> Icons.Default.Login
    else -> Icons.Default.Notifications
}

@Composable
private fun activityIconColor(type: String): Color = when (type.lowercase()) {
    "create", "registration", "register" -> Color(0xFF4CAF50)
    "update", "edit" -> Color(0xFF2196F3)
    "payment" -> Color(0xFFFF9800)
    "invoice" -> Color(0xFF9C27B0)
    "delete" -> Color(0xFFF44336)
    "login" -> Color(0xFF607D8B)
    else -> MaterialTheme.colorScheme.primary
}

private fun formatTimestamp(date: Date?): String {
    if (date == null) return ""
    val now = Date()
    val diff = now.time - date.time
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Baru saja"
        minutes < 60 -> "${minutes}m yang lalu"
        hours < 24 -> "${hours}j yang lalu"
        days < 7 -> "${days}h yang lalu"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(date)
    }
}
