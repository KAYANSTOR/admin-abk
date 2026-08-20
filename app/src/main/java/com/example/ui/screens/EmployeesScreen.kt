package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.UserModel
import com.example.ui.components.SearchAndFilterHeader
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class EmployeesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _users = MutableStateFlow<List<UserModel>>(emptyList())
    val users: StateFlow<List<UserModel>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                db.collection("users").addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _isLoading.value = false
                        return@addSnapshotListener
                    }
                    val list = mutableListOf<UserModel>()
                    for (doc in snapshot!!) {
                        val user = doc.toObject(UserModel::class.java).copy(id = doc.id)
                        list.add(user)
                    }
                    _users.value = list
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleUserStatus(user: UserModel) {
        db.collection("users").document(user.id).update("isActive", !user.isActive)
    }

    fun updateUser(user: UserModel, updatedName: String, updatedPhone: String, updatedRole: String, updatedPermissions: List<String>) {
        db.collection("users").document(user.id).update(
            mapOf(
                "name" to updatedName,
                "phone" to updatedPhone,
                "role" to updatedRole,
                "permissions" to updatedPermissions
            )
        )
    }

    fun deleteUser(userId: String) {
        db.collection("users").document(userId).delete()
    }

    fun createUser(name: String, phone: String, pin: String, role: String, permissions: List<String>) {
        val newUser = mapOf(
            "name" to name,
            "phone" to phone,
            "pin" to pin,
            "role" to role,
            "permissions" to permissions,
            "isActive" to true
        )
        db.collection("users").add(newUser)
    }

}

val ALL_PERMISSIONS = listOf(
    "dashboard" to "لوحة التحكم",
    "clients" to "إدارة العملاء",
    "serials" to "السيريالات",
    "sales" to "المبيعات",
    "commissions" to "العمولات",
    "subscriptions" to "الاشتراكات",
    "employees" to "الموظفين",
    "settings" to "الإعدادات"
)

@Composable
fun EmployeesScreen(viewModel: EmployeesViewModel = viewModel(), currentUserId: String? = null) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUserForEdit by remember { mutableStateOf<UserModel?>(null) }

    val filteredUsers = users.filter { it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مستخدم")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "إدارة الموظفين",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            SearchAndFilterHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                placeholder = "بحث بالاسم، رقم الجوال..."
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredUsers.isEmpty()) {
                    Text(
                        text = "لا يوجد موظفين",
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            EmployeeCardNew(
                                user = user,
                                isCurrentUser = user.id == currentUserId,
                                onToggleStatus = { viewModel.toggleUserStatus(user) },
                                onEditClick = { selectedUserForEdit = user },
                                onDeleteClick = { viewModel.deleteUser(user.id) }
                            )
                            if (user != filteredUsers.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 16.dp),
                                    color = Color.LightGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    if (showAddDialog) {
        
        CreateEmployeeDialogNew(
            onDismiss = { showAddDialog = false },
            onSave = { newName, newPhone, newPin, newRole, newPermissions ->
                viewModel.createUser(newName, newPhone, newPin, newRole, newPermissions)
                showAddDialog = false
            }
        )

    }
    
    if (selectedUserForEdit != null) {
        EditEmployeeDialogNew(
            user = selectedUserForEdit!!, 
            onDismiss = { selectedUserForEdit = null },
            onSave = { updatedName, updatedPhone, updatedRole, updatedPermissions -> 
                viewModel.updateUser(selectedUserForEdit!!, updatedName, updatedPhone, updatedRole, updatedPermissions)
                selectedUserForEdit = null
            }
        )
    }
}

@Composable
fun EmployeeCardNew(
    user: UserModel, 
    isCurrentUser: Boolean,
    onToggleStatus: () -> Unit, 
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isAdmin = user.role == "ADMIN"
    val avatarBg = if (isAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color(0xFFF3F4F6)
    val avatarColor = if (isAdmin) MaterialTheme.colorScheme.primary else Color.Gray

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().let {
            if (!user.isActive) it.background(Color.White.copy(alpha = 0.6f)) else it
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).background(avatarBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.toString() ?: "?",
                        color = avatarColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrentUser) {
                            Box(modifier = Modifier.padding(start = 4.dp).background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("أنت", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        if (!user.isActive) {
                            Box(modifier = Modifier.padding(start = 4.dp).background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("موقوف", fontSize = 10.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                    Text(
                        text = user.phone,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val roleBg = if (isAdmin) MaterialTheme.colorScheme.primary else Color(0xFFF3F4F6)
                val roleColor = if (isAdmin) Color.White else Color.Gray
                Box(
                    modifier = Modifier.background(roleBg, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(if (isAdmin) Icons.Default.Shield else Icons.Default.Person, contentDescription = null, tint = roleColor, modifier = Modifier.size(12.dp))
                        Text(if (isAdmin) "مدير نظام" else "موظف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = roleColor)
                    }
                }
                
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp).background(Color(0xFFF9FAFB), CircleShape)) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (!isCurrentUser) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleStatus() }
                        .background(if (user.isActive) Color(0xFFFFF7ED) else Color(0xFFF0FDF4))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (user.isActive) "إيقاف الحساب" else "تنشيط الحساب",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isActive) Color(0xFFEA580C) else Color(0xFF16A34A)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (showDeleteConfirm) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDeleteClick(); showDeleteConfirm = false }
                                .background(Color(0xFFEF4444))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("تأكيد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDeleteConfirm = false }
                                .background(Color(0xFFF3F4F6))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("إلغاء", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDeleteConfirm = true }
                            .background(Color(0xFFFEF2F2))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("حذف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun EditEmployeeDialogNew(user: UserModel, onDismiss: () -> Unit, onSave: (String, String, String, List<String>) -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var role by remember { mutableStateOf(user.role) }
    var selectedPermissions by remember { mutableStateOf(user.permissions.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                Text("تعديل بيانات الموظف", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("الاسم كامل") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("رقم الجوال") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false // phone is disabled for edit in web app
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { role = "STAFF" }
                            .background(if (role == "STAFF") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("موظف", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (role == "STAFF") MaterialTheme.colorScheme.onSurface else Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { role = "ADMIN" }
                            .background(if (role == "ADMIN") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("مدير نظام", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (role == "ADMIN") MaterialTheme.colorScheme.onSurface else Color.Gray)
                    }
                }

                if (role == "STAFF") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("الصلاحيات (للموظف)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    
                    // Two columns grid
                    val chunked = ALL_PERMISSIONS.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in chunked) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                for ((id, label) in row) {
                                    val isSelected = selectedPermissions.contains(id)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { 
                                                selectedPermissions = if (isSelected) selectedPermissions - id else selectedPermissions + id 
                                            }
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha=0.1f) else Color.Transparent)
                                            .padding(4.dp)
                                    ) {
                                        Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.scale(0.8f))
                                        Text(text = label, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                                    }
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha=0.5f), contentColor = Color.Gray)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = { 
                            val finalPerms = if (role == "ADMIN") ALL_PERMISSIONS.map { it.first } else selectedPermissions.toList()
                            onSave(name, phone, role, finalPerms) 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("حفظ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateEmployeeDialogNew(onDismiss: () -> Unit, onSave: (String, String, String, String, List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STAFF") }
    var selectedPermissions by remember { mutableStateOf(setOf<String>()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                Text("إضافة موظف جديد", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("الاسم كامل") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("رقم الجوال") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it.filter { char -> char.isDigit() } },
                    placeholder = { Text("رمز الدخول (4 أرقام)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { role = "STAFF" }
                            .background(if (role == "STAFF") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("موظف", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (role == "STAFF") MaterialTheme.colorScheme.onSurface else Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { role = "ADMIN" }
                            .background(if (role == "ADMIN") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("مدير نظام", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (role == "ADMIN") MaterialTheme.colorScheme.onSurface else Color.Gray)
                    }
                }

                if (role == "STAFF") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("الصلاحيات (للموظف)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    
                    // Two columns grid
                    val chunked = ALL_PERMISSIONS.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in chunked) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                for ((id, label) in row) {
                                    val isSelected = selectedPermissions.contains(id)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { 
                                                selectedPermissions = if (isSelected) selectedPermissions - id else selectedPermissions + id 
                                            }
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha=0.1f) else Color.Transparent)
                                            .padding(4.dp)
                                    ) {
                                        Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.scale(0.8f))
                                        Text(text = label, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                                    }
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha=0.5f), contentColor = Color.Gray)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = { 
                            if (name.isNotBlank() && phone.isNotBlank() && pin.length == 4) {
                                val finalPerms = if (role == "ADMIN") ALL_PERMISSIONS.map { it.first } else selectedPermissions.toList()
                                onSave(name, phone, pin, role, finalPerms) 
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("إنشاء", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
