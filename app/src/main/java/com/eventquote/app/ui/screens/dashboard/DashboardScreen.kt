package com.eventquote.app.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventquote.app.model.Estimate
import com.eventquote.app.ui.components.EstimateListItem
import com.eventquote.app.ui.components.PrimaryFab
import com.eventquote.app.ui.theme.*
import com.eventquote.app.utils.CurrencyFormatter
import com.eventquote.app.utils.DateUtils
import com.eventquote.app.viewmodel.DashboardStats
import com.eventquote.app.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNewEstimate: () -> Unit,
    onSavedEstimates: () -> Unit,
    onCompanySettings: () -> Unit,
    onServicesMaster: () -> Unit,
    onBackup: () -> Unit,
    onAbout: () -> Unit,
    onEstimateClick: (String) -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val recentEstimates by viewModel.recentEstimates.collectAsState()

    Scaffold(
        floatingActionButton = {
            PrimaryFab(
                onClick = onNewEstimate,
                icon = Icons.Default.Add,
                label = "New Estimate"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ---- Hero Header ----
            item {
                DashboardHeader()
            }

            // ---- Statistics Cards ----
            item {
                StatsSection(stats = stats)
            }

            // ---- Quick Actions ----
            item {
                QuickActionsSection(
                    onNewEstimate = onNewEstimate,
                    onSavedEstimates = onSavedEstimates,
                    onCompanySettings = onCompanySettings,
                    onServicesMaster = onServicesMaster,
                    onBackup = onBackup,
                    onAbout = onAbout
                )
            }

            // ---- Recent Estimates ----
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Estimates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onSavedEstimates) {
                        Text("View All", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (recentEstimates.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.Description, contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            Spacer(Modifier.height(12.dp))
                            Text("No estimates yet", style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onNewEstimate) { Text("Create First Estimate") }
                        }
                    }
                }
            } else {
                items(recentEstimates, key = { it.id }) { estimate ->
                    EstimateListItem(
                        estimate = estimate,
                        onClick = { onEstimateClick(estimate.id) },
                        onDuplicate = {},
                        onDelete = {},
                        onStatusChange = {},
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientEnd,
                        PrimaryPurpleDark
                    )
                )
            )
            .padding(top = 48.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EventNote, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("EventQuote", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Professional Quotations", style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(currentDate, style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f))
            Text("Good to see you!", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun StatsSection(stats: DashboardStats) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Overview", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "Total Estimates",
                value = stats.totalEstimates.toString(),
                icon = Icons.Default.Description,
                color = PrimaryPurple,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Pending Balance",
                value = CurrencyFormatter.formatCompact(stats.pendingBalance),
                icon = Icons.Default.AccountBalance,
                color = WarningOrange,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "Today's Events",
                value = stats.todaysEvents.toString(),
                icon = Icons.Default.Today,
                color = AccentTeal,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "This Month",
                value = CurrencyFormatter.formatCompact(stats.thisMonthRevenue),
                icon = Icons.Default.TrendingUp,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNewEstimate: () -> Unit,
    onSavedEstimates: () -> Unit,
    onCompanySettings: () -> Unit,
    onServicesMaster: () -> Unit,
    onBackup: () -> Unit,
    onAbout: () -> Unit
) {
    val actions = listOf(
        QuickAction("New Estimate", Icons.Default.AddCircle, PrimaryPurple, onNewEstimate),
        QuickAction("Saved", Icons.Default.Folder, StatusSent, onSavedEstimates),
        QuickAction("Company", Icons.Default.Business, AccentTeal, onCompanySettings),
        QuickAction("Services", Icons.Default.Category, SuccessGreen, onServicesMaster),
        QuickAction("Backup", Icons.Default.CloudDownload, StatusCompleted, onBackup),
        QuickAction("About", Icons.Default.Info, WarningOrange, onAbout)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(actions) { action ->
                QuickActionItem(action = action)
            }
        }
    }
}

data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionItem(action: QuickAction) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = action.onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(action.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(action.icon, contentDescription = action.label,
                tint = action.color, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )
    }
}
