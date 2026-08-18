package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SearchAndFilterHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Exclude
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import android.util.Log

enum class SerialStatus(val text: String, val type: StatusType) {
    ACTIVE("نشط", StatusType.SUCCESS),
    UNUSED("غير مستخدم", StatusType.INFO),
    FROZEN("مجمد", StatusType.WARNING),
    EXPIRED("منتهي", StatusType.ERROR)
}

data class SerialModel(
    val id: String = "",
    val code: String = "",
    val user: String = "",
    val plan: String = "",
    val statusName: String = SerialStatus.UNUSED.name
) {
    @get:Exclude
    val status: SerialStatus
        get() = try { SerialStatus.valueOf(statusName) } catch (e: Exception) { SerialStatus.UNUSED }
}

class SerialsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _serials = MutableStateFlow<List<SerialModel>>(emptyList())
    val serials: StateFlow<List<SerialModel>> = _serials.asStateFlow()

    init {
        fetchSerials()
    }

    private fun fetchSerials() {
        viewModelScope.launch {
            try {
                db.collection("serials")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("SerialsViewModel", "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        
                        val list = mutableListOf<SerialModel>()
                        for (doc in snapshot!!) {
                            val serial = doc.toObject(SerialModel::class.java).copy(id = doc.id)
                            list.add(serial)
                        }
                        _serials.value = list
                    }
            } catch (e: Exception) {
                Log.e("SerialsViewModel", "Error fetching serials", e)
            }
        }
    }

    fun addSerial(plan: String) {
        val newCode = "KTK-${generateRandomCode()}-${generateRandomCode()}-${generateRandomCode()}"
        val newSerial = SerialModel(
            code = newCode,
            user = "غير مرتبط",
            plan = plan,
            statusName = SerialStatus.UNUSED.name
        )
        db.collection("serials").add(newSerial)
    }

    fun freezeSerial(id: String) {
        db.collection("serials").document(id).update("statusName", SerialStatus.FROZEN.name)
    }
    
    fun unfreezeSerial(id: String) {
        db.collection("serials").document(id).update("statusName", SerialStatus.ACTIVE.name)
    }

    fun deleteSerial(id: String) {
        db.collection("serials").document(id).delete()
    }

    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..4).map { chars.random() }.joinToString("")
    }
}

@Composable
fun SerialsScreen(viewModel: SerialsViewModel = viewModel()) {
    val serials by viewModel.serials.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredSerials = serials.filter {
        it.code.contains(searchQuery, ignoreCase = true) ||
        it.user.contains(searchQuery, ignoreCase = true)
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
                    text = "إدارة السيريالات",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                SearchAndFilterHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    placeholder = "بحث برقم السيريال، الشبكة..."
                )
            }
            
            items(filteredSerials, key = { it.id }) { serial ->
                SerialItem(
                    serial = serial,
                    onFreeze = { viewModel.freezeSerial(serial.id) },
                    onUnfreeze = { viewModel.unfreezeSerial(serial.id) },
                    onDelete = { viewModel.deleteSerial(serial.id) }
                )
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart),
            containerColor = com.example.ui.theme.AccentPink,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "توليد سيريال جديد")
        }
    }

    if (showCreateDialog) {
        CreateSerialDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { plan ->
                viewModel.addSerial(plan)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateSerialDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var selectedPlan by remember { mutableStateOf("خطة شهرية") }
    val plans = listOf("خطة شهرية", "خطة ربع سنوية", "خطة سنوية")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء سيريال جديد") },
        text = {
            Column {
                Text("اختر خطة الاشتراك للسيريال الجديد:")
                Spacer(modifier = Modifier.height(12.dp))
                plans.forEach { plan ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlan = plan }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = plan == selectedPlan,
                            onClick = { selectedPlan = plan },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = plan, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(selectedPlan) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("إنشاء") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun SerialItem(
    serial: SerialModel,
    onFreeze: () -> Unit,
    onUnfreeze: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = serial.code,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = { /* TODO: Copy serial */ }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { expanded = true }, modifier = Modifier.size(32.dp)) {
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
                            if (serial.status == SerialStatus.FROZEN) {
                                DropdownMenuItem(
                                    text = { Text("إلغاء التجميد") },
                                    onClick = { onUnfreeze(); expanded = false }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("تجميد السيريال") },
                                    onClick = { onFreeze(); expanded = false }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("حذف السيريال", color = MaterialTheme.colorScheme.error) },
                                onClick = { onDelete(); expanded = false }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = serial.user,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = serial.plan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = serial.status.text, type = serial.status.type)
            }
        }
    }
}
