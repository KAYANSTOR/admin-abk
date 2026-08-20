package com.example.ui.screens
import com.example.ui.components.TabButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SearchAndFilterHeader
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class SubscriptionModel(
    val id: String = "",
    val network: String = "", // Legacy
    val plan: String = "",
    val amount: String = "",
    val nextBilling: String = "",
    val statusText: String = "",
    val statusTypeString: String = "SUCCESS"
)

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
                            Log.w("SubsViewModel", "Listen failed.", e)
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
                Log.e("SubsViewModel", "Error fetching subs", e)
            }
        }
    }
}

@Composable
fun SubscriptionsScreen(viewModel: SubscriptionsViewModel = viewModel(), filter: String = "active") {
    val subscriptions by viewModel.subscriptions.collectAsState()
    
    val initialTab = if (filter == "trial") 1 else 0
    var activeTab by remember { mutableStateOf(initialTab) }

    val filteredSubs = subscriptions.filter { sub ->
        val isSuccess = sub.statusTypeString == "SUCCESS"
        if (activeTab == 0) isSuccess else !isSuccess
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "الاشتراكات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TabButton(text = "فعالة", isSelected = activeTab == 0, onClick = { activeTab = 0 }, modifier = Modifier.weight(1f))
                TabButton(text = "تجريبية / معلقة", isSelected = activeTab == 1, onClick = { activeTab = 1 }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (filteredSubs.isEmpty()) {
                    Text(
                        text = "لا توجد اشتراكات مطابقة.",
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
                        items(filteredSubs, key = { it.id }) { sub ->
                            SubscriptionItemNew(sub = sub)
                            if (sub != filteredSubs.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 16.dp),
                                    color = Color.LightGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionItemNew(sub: SubscriptionModel) {
    val isSuccess = sub.statusTypeString == "SUCCESS"
    val planName = sub.plan.ifEmpty { "خطة غير معروفة" }
    val statusText = sub.statusText.ifEmpty { "لا توجد تفاصيل للحالة" }
    
    val badgeColor = if (isSuccess) Color(0xFF34A853) else Color(0xFFFF9800)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = planName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statusText,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isSuccess) "فعال" else "تجريبي / معلق",
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
