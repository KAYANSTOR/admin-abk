package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EmployeesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _employees = MutableStateFlow<List<UserModel>>(emptyList())
    val employees: StateFlow<List<UserModel>> = _employees.asStateFlow()

    init {
        fetchEmployees()
    }

    private fun fetchEmployees() {
        viewModelScope.launch {
            db.collection("users")
                .whereEqualTo("role", "EMPLOYEE")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    val list = mutableListOf<UserModel>()
                    for (doc in snapshot!!) {
                        list.add(doc.toObject(UserModel::class.java).copy(id = doc.id))
                    }
                    _employees.value = list
                }
        }
    }

    fun updatePermissions(userId: String, newPermissions: List<String>) {
        db.collection("users").document(userId).update("permissions", newPermissions)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(onBackClick: () -> Unit, viewModel: EmployeesViewModel = viewModel()) {
    val employees by viewModel.employees.collectAsState()
    var selectedEmployee by remember { mutableStateOf<UserModel?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الموظفين", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(employees, key = { it.id }) { employee ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(employee.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(employee.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { selectedEmployee = employee }) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل الصلاحيات", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
    
    if (selectedEmployee != null) {
        EditPermissionsDialog(
            employee = selectedEmployee!!,
            onDismiss = { selectedEmployee = null },
            onSave = { permissions ->
                viewModel.updatePermissions(selectedEmployee!!.id, permissions)
                selectedEmployee = null
            }
        )
    }
}

@Composable
fun EditPermissionsDialog(employee: UserModel, onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    val permissionOptions = listOf(
        "clients" to "إدارة العملاء",
        "licenses" to "إدارة التراخيص",
        "serials" to "إدارة السيريالات",
        "commissions" to "إدارة التقارير والعمولات",
        "subscriptions" to "إدارة الاشتراكات"
    )

    var selectedPermissions by remember { mutableStateOf(employee.permissions.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل صلاحيات: ${employee.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                permissionOptions.forEach { (route, title) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedPermissions.contains(route),
                            onCheckedChange = { checked ->
                                selectedPermissions = if (checked) selectedPermissions + route else selectedPermissions - route
                            }
                        )
                        Text(text = title)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedPermissions.toList()) }) {
                Text("حفظ الصلاحيات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
