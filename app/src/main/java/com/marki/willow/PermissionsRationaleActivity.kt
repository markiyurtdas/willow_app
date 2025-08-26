package com.marki.willow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marki.willow.ui.theme.WillowTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WillowTheme {
                PermissionsRationaleScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsRationaleScreen(
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Health Connect Permissions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            PrivacyPolicySection(
                title = "Why We Need These Permissions",
                content = "Willow needs access to your health data to help you track and manage your sleep and exercise patterns. This helps you maintain a healthy lifestyle by providing insights into your daily activities."
            )
            
            PrivacyPolicySection(
                title = "Sleep Data",
                content = "• Read Sleep: View your sleep sessions from Health Connect to display them in the app\n" +
                         "• Write Sleep: Save your manually entered sleep data to Health Connect for backup and sync across devices"
            )
            
            PrivacyPolicySection(
                title = "Exercise Data", 
                content = "• Read Exercise: View your exercise sessions from Health Connect to display them in the app\n" +
                         "• Write Exercise: Save your manually entered exercise data to Health Connect for backup and sync across devices"
            )
            
            PrivacyPolicySection(
                title = "Data Usage",
                content = "• Your health data is stored locally on your device and in Health Connect\n" +
                         "• No health data is sent to external servers or third parties\n" +
                         "• Data is only used within the Willow app for tracking and analytics purposes\n" +
                         "• You can revoke these permissions at any time in your device settings"
            )
            
            PrivacyPolicySection(
                title = "Data Security",
                content = "• All data access follows Android's security guidelines\n" +
                         "• Health Connect provides additional security layers for sensitive health data\n" +
                         "• Data is encrypted and protected according to Android standards"
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your Privacy Matters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You have full control over your health data permissions. You can grant or deny specific permissions and change them at any time in your device settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicySection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
            )
        }
    }
}