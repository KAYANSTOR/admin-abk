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

data class SubscriptionModel(
    val id: String = "",
    val network: String = "",
    val plan: String = "",
    val dateInfo: String = "",
    val statusText: String = "",
    val statusTypeString: String = StatusType.SUCCESS.name
) {
    val statusType: StatusType
        get() = try { StatusType.valueOf(statusTypeString) } catch (e: Exception) { StatusType.SUCCESS }
}

class SubscriptionsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _subscriptions = MutableStateFlow<List<SubscriptionModel>>(emptyList())
    val subscriptions: StateFlow<List<SubscriptionModel>> = _subscriptions.asStateFlow()

    init {
        fetchSubscriptions()
    }

    private fun fetchSubscriptions() {
        viewModelScope.launch {
            try {
                db.collection("subscriptions")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }
                        val list = mutableListOf<SubscriptionModel>()
                        for (doc in snapshot!!) {
                            val sub = doc.toObject(SubscriptionModel::class.java).copy(id = doc.id)
                            list.add(sub)
                        }
                        _subscriptions.value = list
                    }
            } catch (e: Exception) {
            }
        }
    }
}

@Composable
fun SubscriptionsScreen(viewModel: SubscriptionsViewModel = viewModel()) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredSubs = subscriptions.filter {
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
                text = "الاشتراكات",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SearchAndFilterHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                placeholder = "بحث باسم الشبكة..."
            )
        }

        // Real Data
        items(filteredSubs, key = { it.id }) { sub ->
            SubscriptionItem(
                network = sub.network,
                plan = sub.plan,
                dateInfo = sub.dateInfo,
                statusType = sub.statusType,
                statusText = sub.statusText
            )
        }
    }
}

@Composable
fun SubscriptionItem(network: String, plan: String, dateInfo: String, statusType: StatusType, statusText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = network,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$plan • $dateInfo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = statusText, type = statusType)
        }
    }
}
