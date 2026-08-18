package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.LicenseItem

@Composable
fun LicensesScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp), // space for FAB
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "جميع التراخيص",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                LicenseItem(
                    name = "سارة الحربي",
                    expireDate = "2026/08/28",
                    initial = "س"
                )
            }
            item {
                LicenseItem(
                    name = "خالد المطيري",
                    expireDate = "2026/08/24",
                    initial = "خ"
                )
            }
            item {
                LicenseItem(
                    name = "أحمد العتيبي",
                    expireDate = "2026/09/10",
                    initial = "أ"
                )
            }
            item {
                LicenseItem(
                    name = "شبكة جدة",
                    expireDate = "2027/01/15",
                    initial = "ش"
                )
            }
        }

        FloatingActionButton(
            onClick = { /* TODO: Add new license */ },
            modifier = Modifier
                .padding(16.dp)
                .align(androidx.compose.ui.Alignment.BottomStart), // BottomStart because RTL puts it on the right visually
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة ترخيص")
        }
    }
}
