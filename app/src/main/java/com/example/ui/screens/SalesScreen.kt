package com.example.ui.screens
import com.example.ui.components.TabButton

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SaleTransaction(
    val id: String = "",
    val amount: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val invoice: String = "", // Legacy
    val network: String = "" // Legacy
)

class SalesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _sales = MutableStateFlow<List<SaleTransaction>>(emptyList())
    val sales: StateFlow<List<SaleTransaction>> = _sales.asStateFlow()

    init {
        fetchSales()
    }

    private fun fetchSales() {
        viewModelScope.launch {
            try {
                db.collection("sales")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }
                        val list = mutableListOf<SaleTransaction>()
                        for (doc in snapshot!!) {
                            val sale = doc.toObject(SaleTransaction::class.java).copy(id = doc.id)
                            list.add(sale)
                        }
                        _sales.value = list
                    }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}

@Composable
fun SalesScreen(viewModel: SalesViewModel = viewModel(), filter: String = "all") {
    val sales by viewModel.sales.collectAsState()
    
    val initialTab = when(filter) {
        "today" -> 0
        "month" -> 1
        else -> 2
    }
    var activeTab by remember { mutableStateOf(initialTab) }

    val filteredSales = sales.filter { sale ->
        if (activeTab == 2) return@filter true
        
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis
        val saleTime = sale.timestamp
        
        if (activeTab == 0) {
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis
            return@filter saleTime >= todayStart
        }
        if (activeTab == 1) {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis
            return@filter saleTime >= monthStart
        }
        true
    }

    val totalAmount = filteredSales.sumOf { it.amount.replace(",", "").toDoubleOrNull() ?: 0.0 }
    val formattedTotal = NumberFormat.getNumberInstance(Locale.US).format(totalAmount)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "تقرير المبيعات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Sales Header Card matching Web (Primary Dark background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF141C2E))
                    .padding(24.dp)
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomStart).size(96.dp).offset(x = (-16).dp, y = 16.dp),
                    tint = Color.White.copy(alpha = 0.05f)
                )
                Column {
                    Text(
                        text = "إجمالي المبيعات (${if (activeTab == 0) "اليوم" else if (activeTab == 1) "الشهر" else "الكل"})",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = formattedTotal,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 32.sp
                        )
                        Text(
                            text = "ر.س",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "إجمالي عدد العمليات: ${filteredSales.size} عملية",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TabButton(text = "اليوم", isSelected = activeTab == 0, onClick = { activeTab = 0 }, modifier = Modifier.weight(1f))
                TabButton(text = "الشهر", isSelected = activeTab == 1, onClick = { activeTab = 1 }, modifier = Modifier.weight(1f))
                TabButton(text = "الكل", isSelected = activeTab == 2, onClick = { activeTab = 2 }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (filteredSales.isEmpty()) {
                    Text(
                        text = "لا توجد مبيعات في هذه الفترة.",
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
                        items(filteredSales, key = { it.id }) { sale ->
                            SaleItemNew(sale = sale)
                            if (sale != filteredSales.last()) {
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
fun SaleItemNew(sale: SaleTransaction) {
    val displayAmount = sale.amount.ifEmpty { "0" }
    val displayDesc = sale.description.ifEmpty { "عملية مبيعات جديدة" }
    
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale("ar", "SA"))
    val dateString = if (sale.timestamp > 0) sdf.format(Date(sale.timestamp)) else "غير معروف"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
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
                text = displayDesc,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = dateString,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
