package com.marki.willow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.marki.willow.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Willow Health") }
            )
        }
    ) { paddingValues ->
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