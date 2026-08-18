package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import com.example.ui.components.SearchAndFilterHeader

data class LicenseModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val expireDate: String,
    val initial: String,
    val isActive: Boolean
)

class LicensesViewModel : ViewModel() {
    private val _licenses = MutableStateFlow<List<LicenseModel>>(
        listOf(
            LicenseModel(name = "سارة الحربي", expireDate = "2026/08/28", initial = "س", isActive = true),
            LicenseModel(name = "خالد المطيري", expireDate = "2026/08/24", initial = "خ", isActive = false),
            LicenseModel(name = "أحمد العتيبي", expireDate = "2026/09/10", initial = "أ", isActive = true),
            LicenseModel(name = "شبكة جدة", expireDate = "2027/01/15", initial = "ش", isActive = true)
        )
    )
    val licenses: StateFlow<List<LicenseModel>> = _licenses.asStateFlow()

    fun toggleLicense(id: String) {
        _licenses.update { current ->
            current.map { if (it.id == id) it.copy(isActive = !it.isActive) else it }
        }
    }

    fun addLicense(name: String, expireDate: String, initial: String) {
        _licenses.update { current ->
            listOf(LicenseModel(name = name, expireDate = expireDate, initial = initial, isActive = true)) + current
        }
    }
}

@Composable
fun LicensesScreen(viewModel: LicensesViewModel = viewModel()) {
    val licenses by viewModel.licenses.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredLicenses = licenses.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "إدارة التراخيص",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                SearchAndFilterHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    placeholder = "بحث باسم المرخص له..."
                )
            }
            
            items(filteredLicenses, key = { it.id }) { license ->
                DetailedLicenseItem(
                    license = license,
                    onToggle = { viewModel.toggleLicense(license.id) }
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart),
            containerColor = com.example.ui.theme.AccentPink,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة ترخيص")
        }
    }

    if (showAddDialog) {
        // Simple Add Dialog stub for Licenses
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إصدار ترخيص جديد") },
            text = { Text("جاري العمل على هذه الميزة...") },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("حسناً")
                }
            }
        )
    }
}

@Composable
fun DetailedLicenseItem(
    license: LicenseModel,
    onToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (license.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = license.initial,
                    color = if (license.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = license.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (!license.isActive) TextDecoration.LineThrough else null
                )
                Text(
                    text = if (license.isActive) "ينتهي في ${license.expireDate}" else "تم إيقاف الترخيص",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (license.isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "خيارات",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text(if (license.isActive) "إيقاف الترخيص" else "تفعيل الترخيص", color = if (license.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) },
                        onClick = { onToggle(); expanded = false }
                    )
                }
            }
        }
    }
}
