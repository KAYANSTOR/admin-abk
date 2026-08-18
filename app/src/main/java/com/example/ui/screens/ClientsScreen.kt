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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import androidx.navigation.NavController
import com.example.ui.components.SearchAndFilterHeader

data class ClientModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val networkName: String,
    val isActive: Boolean
)

class ClientsViewModel : ViewModel() {
    private val _clients = MutableStateFlow<List<ClientModel>>(
        listOf(
            ClientModel(name = "محمد العبدالله", networkName = "شبكة جدة السريعة", isActive = true),
            ClientModel(name = "سعد الدوسري", networkName = "شبكة الرياض بلس", isActive = true),
            ClientModel(name = "عبدالرحمن الفهد", networkName = "شبكة أبها نت", isActive = false)
        )
    )
    val clients: StateFlow<List<ClientModel>> = _clients.asStateFlow()
}

@Composable
fun ClientsScreen(viewModel: ClientsViewModel = viewModel(), navController: NavController? = null) {
    val clients by viewModel.clients.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("العملاء المفعلين", "الفترة التجريبية")

    val filteredClients = clients.filter {
        (it.name.contains(searchQuery, ignoreCase = true) || it.networkName.contains(searchQuery, ignoreCase = true)) &&
        (if (selectedTabIndex == 0) it.isActive else !it.isActive)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "إدارة العملاء",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
            
            SearchAndFilterHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                placeholder = "بحث باسم العميل أو الشبكة..."
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredClients, key = { it.id }) { client ->
                    ClientItem(
                        client = client,
                        onClick = { navController?.navigate("client_profile") }
                    )
                }
            }
        }
    }
}

@Composable
fun ClientItem(
    client: ClientModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = client.networkName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(
                        if (client.isActive) com.example.ui.theme.SuccessBackground else com.example.ui.theme.WarningBackground,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (client.isActive) "مفعل" else "تجريبي",
                    color = if (client.isActive) com.example.ui.theme.SuccessGreen else com.example.ui.theme.WarningOrange,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
