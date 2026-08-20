package com.example.ui.screens

import com.example.ui.components.TabButton

import androidx.compose.ui.draw.clip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SearchAndFilterHeader
import com.example.ui.components.StatusType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.firestore.FieldValue

data class CommissionModel(
    val id: String = "",
    val network: String = "",
    val clientName: String = "",
    val description: String = "",
    val amount: String = "",
    val commissionAmount: String = "",
    val statusText: String = "",
    val statusTypeString: String = StatusType.WARNING.name,
    val settledAmount: String = "",
    val settlementRef: String = ""
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

    fun settleCommission(commissionId: String, amount: String, reference: String) {
        viewModelScope.launch {
            try {
                // Create settlement record
                val settlementData = hashMapOf(
                    "commissionId" to commissionId,
                    "amount" to amount,
                    "reference" to reference,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("settlements").add(settlementData)

                // Update commission
                db.collection("commissions").document(commissionId).update(
                    mapOf(
                        "statusTypeString" to "SUCCESS",
                        "statusText" to "مكتملة",
                        "settledAmount" to amount,
                        "settlementRef" to reference
                    )
                )
            } catch (e: Exception) {
                Log.e("CommissionsViewModel", "Error settling commission", e)
            }
        }
    }
}

@Composable
fun CommissionsScreen(viewModel: CommissionsViewModel = viewModel()) {
    val commissions by viewModel.commissions.collectAsState()
    var activeTab by remember { mutableStateOf(0) }
    var settlementModalOpen by remember { mutableStateOf<CommissionModel?>(null) }
    
    val filteredCommissions = commissions.filter {
        val isPending = it.statusTypeString == "WARNING"
        if (activeTab == 0) isPending else !isPending
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "العمولات",
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
                TabButton(
                    text = "المعلقة",
                    isSelected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "المكتملة",
                    isSelected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (filteredCommissions.isEmpty()) {
                    Text(
                        text = "لا توجد عمولات مطابقة.",
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
                        items(filteredCommissions, key = { it.id }) { commission ->
                            CommissionItemNew(
                                commission = commission,
                                onSettleClick = { settlementModalOpen = it }
                            )
                            if (commission != filteredCommissions.last()) {
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

        // Settlement Modal
        settlementModalOpen?.let { commission ->
            SettlementDialog(
                commission = commission,
                onDismiss = { settlementModalOpen = null },
                onSettle = { amount, ref ->
                    viewModel.settleCommission(commission.id, amount, ref)
                    settlementModalOpen = null
                }
            )
        }
    }
}



@Composable
fun CommissionItemNew(commission: CommissionModel, onSettleClick: (CommissionModel) -> Unit) {
    val displayAmount = commission.commissionAmount.ifEmpty { commission.amount.ifEmpty { "0" } }
    val isPending = commission.statusTypeString == "WARNING"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = displayAmount,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " ر.س",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                )
            }
            Text(
                text = commission.description.ifEmpty { "عملية غير مسماة" },
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (commission.clientName.isNotEmpty()) {
                Text(
                    text = commission.clientName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val badgeColor = if (isPending) Color(0xFF673AB7) else Color(0xFF34A853)
            val badgeBg = badgeColor.copy(alpha = 0.1f)
            
            Box(
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isPending) "معلقة" else "مكتملة",
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .clickable { onSettleClick(commission) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "تصفية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun SettlementDialog(commission: CommissionModel, onDismiss: () -> Unit, onSettle: (String, String) -> Unit) {
    var amount by remember { mutableStateOf(commission.commissionAmount.ifEmpty { commission.amount.ifEmpty { "" } }) }
    var reference by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تصفية العمولة", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("المبلغ المسدد", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("مرجع الدفع (اختياري)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    placeholder = { Text("رقم الحوالة أو الإيصال...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSettle(amount, reference) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("تأكيد السداد", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
