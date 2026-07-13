package com.eventquote.app.ui.screens.saved

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventquote.app.model.EstimateStatus
import com.eventquote.app.ui.components.*
import com.eventquote.app.ui.theme.SuccessGreen
import com.eventquote.app.viewmodel.SavedEstimatesViewModel
import com.eventquote.app.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedEstimatesScreen(
    viewModel: SavedEstimatesViewModel,
    onNavigateBack: () -> Unit,
    onEstimateClick: (String) -> Unit,
    onNewEstimate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show feedback messages
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            snackbarHostState.showSnackbar("Estimate deleted")
            viewModel.clearDeleteSuccess()
        }
    }
    LaunchedEffect(uiState.duplicateSuccess) {
        uiState.duplicateSuccess?.let {
            snackbarHostState.showSnackbar("Estimate duplicated!")
            viewModel.clearDuplicateSuccess()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearError()
        }
    }

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            PrimaryFab(onClick = onNewEstimate, label = "New Estimate")
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Header
            GradientHeader(
                title = "Saved Estimates",
                subtitle = "${uiState.estimates.size} estimate(s) found",
                onBack = onNavigateBack,
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(option.displayName,
                                            fontWeight = if (uiState.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.sortOption == option) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface)
                                    },
                                    onClick = { viewModel.sort(option); showSortMenu = false },
                                    leadingIcon = if (uiState.sortOption == option) {
                                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            )

            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::search,
                placeholder = { Text("Search by name, number, phone, venue...") },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.search("") }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Status filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == null,
                        onClick = { viewModel.filterByStatus(null) },
                        label = { Text("All") }
                    )
                }
                items(EstimateStatus.entries) { status ->
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = {
                            viewModel.filterByStatus(if (uiState.selectedStatus == status) null else status)
                        },
                        label = { Text(status.displayName) }
                    )
                }
            }

            // Estimates list
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.estimates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.FolderOpen,
                        title = "No estimates found",
                        subtitle = if (uiState.searchQuery.isNotBlank())
                            "Try a different search term"
                        else "Tap the button below to create your first estimate",
                        actionLabel = "Create Estimate",
                        onAction = onNewEstimate
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.estimates, key = { it.id }) { estimate ->
                        EstimateListItem(
                            estimate = estimate,
                            onClick = { onEstimateClick(estimate.id) },
                            onDuplicate = { viewModel.duplicateEstimate(estimate.id) },
                            onDelete = { viewModel.deleteEstimate(estimate.id) },
                            onStatusChange = { newStatus ->
                                viewModel.updateStatus(estimate.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}
