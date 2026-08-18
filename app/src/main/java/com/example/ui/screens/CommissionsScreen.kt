package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.SearchAndFilterHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusType

import androidx.compose.foundation.lazy.items
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import android.util.Log

data class CommissionModel(
    val id: String = "",
    val network: String = "",
    val totalSales: String = "",
    val commissionAmount: String = "",
    val rate: String = "",
    val statusText: String = "",
    val statusTypeString: String = StatusType.WARNING.name
) {
    val statusType: StatusType
        get() = try { StatusType.valueOf(statusTypeString) } catch (e: Exception) { StatusType.WARNING }
}

class CommissionsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _commissions = MutableStateFlow<List<CommissionModel>>(emptyList())
    val commissions: StateFlow<List<CommissionModel>> = _commissions.asStateFlow()

    init {
        fetchCommissions()
    }

    private fun fetchCommissions() {
        viewModelScope.launch {
            try {
                db.collection("commissions")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("CommissionsViewModel", "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        
                        val list = mutableListOf<CommissionModel>()
                        for (doc in snapshot!!) {
                            val commission = doc.toObject(CommissionModel::class.java).copy(id = doc.id)
                            list.add(commission)
                        }
                        _commissions.value = list
                    }
            } catch (e: Exception) {
                Log.e("CommissionsViewModel", "Error fetching commissions", e)
            }
        }
    }
}

@Composable
fun CommissionsScreen(viewModel: CommissionsViewModel = viewModel()) {
    val commissions by viewModel.commissions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCommissions = commissions.filter {
        it.network.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "العمولات المستحقة",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Removed Overview Card

        item {
            SearchAndFilterHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                placeholder = "بحث باسم المستخدم، الشبكة..."
            )
        }

        item {
            Text(
                text = "تفصيل العمولات (أغسطس 2026)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Real Commission Data
        items(filteredCommissions, key = { it.id }) { commission ->
            CommissionItem(
                network = commission.network,
                totalSales = commission.totalSales,
                commissionAmount = commission.commissionAmount,
                rate = commission.rate,
                statusType = commission.statusType,
                statusText = commission.statusText
            )
        }
    }
}

@Composable
fun CommissionItem(network: String, totalSales: String, commissionAmount: String, rate: String, statusType: StatusType, statusText: String) {
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
                    text = network,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(status = statusText, type = statusType)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "المبيعات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalSales,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = "النسبة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = rate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "قيمة العمولة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = commissionAmount,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
