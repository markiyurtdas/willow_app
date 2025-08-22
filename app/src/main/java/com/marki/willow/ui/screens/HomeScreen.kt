package com.marki.willow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.marki.willow.navigation.Screen
import com.marki.willow.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Willow Health") }
            )
        }
    ) { paddingValues ->
        val summaryData by viewModel.summaryData.collectAsStateWithLifecycle()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Health Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                SummaryCard(summaryData = summaryData)
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthCard(
                        title = "Log Sleep",
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.SleepLog.route) }
                    )
                    
                    HealthCard(
                        title = "Log Exercise",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.ExerciseLog.route) }
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthCard(
                        title = "Sleep History",
                        icon = Icons.Default.List,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.SleepHistory.route) }
                    )
                    
                    HealthCard(
                        title = "Exercise History",
                        icon = Icons.Default.DateRange,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.ExerciseHistory.route) }
                    )
                }
            }
            
            item {
                HealthCard(
                    title = "Resolve Conflicts",
                    icon = Icons.Default.Warning,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Screen.Conflicts.route) }
                )
            }
            
            item {
                HealthCard(
                    title = "Settings & Sync",
                    icon = Icons.Default.Settings,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
    }
}

@Composable
fun HealthCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SummaryCard(summaryData: com.marki.willow.ui.viewmodel.HealthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Weekly Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = "Sleep Quality",
                    value = String.format("%.1f/5", summaryData.weeklyAverageSleepQuality),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                SummaryItem(
                    title = "Exercise",
                    value = "${summaryData.weeklyExerciseMinutes} min",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    title = "Calories Burned",
                    value = "${summaryData.weeklyCaloriesBurned} cal",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                SummaryItem(
                    title = "Conflicts",
                    value = summaryData.unresolvedConflicts.toString(),
                    color = if (summaryData.unresolvedConflicts > 0) Color(0xFFFF5722) else Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}