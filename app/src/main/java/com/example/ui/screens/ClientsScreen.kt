package com.example.ui.screens

import com.example.ui.components.TabButton

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.components.SearchAndFilterHeader
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class ClientModel(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val storeName: String = "",
    val networkName: String = "", // Legacy
    val status: String = "ACTIVE",
    val isActive: Boolean = true, // Legacy
    val deviceLimit: Int = 3,
    val deviceId: String = "",
    val commissionPercentage: Double = 0.0
) {
    val displayStatus: String
        get() = if (status.isNotEmpty()) status else (if (isActive) "ACTIVE" else "SUSPENDED")
        
    val isEffectivelyActive: Boolean
        get() = displayStatus in listOf("ACTIVE", "WARNING", "GRACE_PERIOD")
}

class ClientsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _clients = MutableStateFlow<List<ClientModel>>(emptyList())
    val clients: StateFlow<List<ClientModel>> = _clients.asStateFlow()

    init {
        fetchClients()
    }

    private fun fetchClients() {
        viewModelScope.launch {
            try {
                db.collection("clients")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("ClientsViewModel", "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        
                        val clientList = mutableListOf<ClientModel>()
                        for (doc in snapshot!!) {
                            val client = doc.toObject(ClientModel::class.java).copy(id = doc.id)
                            clientList.add(client)
                        }
                        _clients.value = clientList
                    }
            } catch (e: Exception) {
                Log.e("ClientsViewModel", "Error fetching clients", e)
            }
        }
    }
}

@Composable
fun ClientsScreen(viewModel: ClientsViewModel = viewModel(), navController: NavController? = null, initialTab: Int = 0) {
    val clients by viewModel.clients.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedTabIndex by remember { mutableStateOf(initialTab) }
    val tabs = listOf("النشطين", "غير النشطين")

    val filteredClients = clients.filter {
        (it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery, ignoreCase = true) || it.storeName.contains(searchQuery, ignoreCase = true) || it.networkName.contains(searchQuery, ignoreCase = true)) &&
        (if (selectedTabIndex == 0) it.isEffectivelyActive else !it.isEffectivelyActive)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إدارة العملاء",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = { /* TODO: Create Client Modal */ },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة عميل", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "النشطين",
                    isSelected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "غير النشطين",
                    isSelected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SearchAndFilterHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                placeholder = "بحث بالاسم أو الرقم..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (filteredClients.isEmpty()) {
                    Text(
                        text = "لا يوجد عملاء",
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
                        items(filteredClients, key = { it.id }) { client ->
                            ClientItemNew(
                                client = client,
                                onClick = { navController?.navigate("client_profile/${client.id}") }
                            )
                            if (client != filteredClients.last()) {
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
}

@Composable
fun ClientItemNew(
    client: ClientModel,
    onClick: () -> Unit
) {
    val displayStatus = client.displayStatus
    
    val badgeColor = when (displayStatus) {
        "ACTIVE" -> Color(0xFF34A853)
        "WARNING" -> Color(0xFFF59E0B)
        "GRACE_PERIOD" -> Color(0xFFFF9800)
        "OVERDUE" -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280) // SUSPENDED
    }
    val statusLabel = when (displayStatus) {
        "ACTIVE" -> "نشط"
        "WARNING" -> "إنذار"
        "GRACE_PERIOD" -> "فترة سماح"
        "OVERDUE" -> "متأخر"
        else -> "موقوف"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF2B7C8E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = client.name.firstOrNull()?.toString() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Column {
                Text(
                    text = client.name.ifEmpty { "عميل غير مسمى" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (client.storeName.isNotEmpty() || client.networkName.isNotEmpty()) {
                    val store = client.storeName.ifEmpty { client.networkName }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(store, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                if (client.phone.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(client.phone, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(6.dp).background(badgeColor, CircleShape))
                Text("الحالة", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}
