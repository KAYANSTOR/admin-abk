package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.ui.theme.GradientStart
import com.example.ui.theme.GradientEnd
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessBackground
import com.example.ui.theme.LightBackground

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardMetrics(
    val dateString: String = "",
    val totalPendingCommissions: String = "0 ر.ي",
    val accountsCount: String = "0",
    val cardsAvailable: String = "0",
    val todaySalesValue: String = "0 ر.ي",
    val todaySalesCount: String = "0 اشتراك",
    val monthSalesValue: String = "0 ر.ي",
    val monthSalesCount: String = "0 اشتراك",
    val trialAccountsCount: String = "0"
)

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _metrics = MutableStateFlow(DashboardMetrics())
    val metrics: StateFlow<DashboardMetrics> = _metrics.asStateFlow()

    init {
        updateDate()
        fetchData()
    }

    private fun updateDate() {
        val formatter = SimpleDateFormat("EEEE، d MMMM yyyy (hh:mm a)", Locale("ar"))
        _metrics.update { it.copy(dateString = formatter.format(Date())) }
    }

    private fun fetchData() {
        viewModelScope.launch {
            // Clients
            db.collection("clients").addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val activeCount = snapshot.documents.count { it.getBoolean("isActive") == true }
                val trialCount = snapshot.documents.count { it.getBoolean("isActive") == false }
                
                _metrics.update { it.copy(accountsCount = activeCount.toString(), trialAccountsCount = trialCount.toString()) }
            }

            // Serials
            db.collection("serials").addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val activeSerials = snapshot.documents.count { it.getString("statusName") == "ACTIVE" }
                _metrics.update { it.copy(cardsAvailable = activeSerials.toString()) }
            }

            // Subscriptions / Sales (using subscriptions collection for mock sales)
            db.collection("subscriptions").addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val count = snapshot.documents.size
                // Mock sales data based on count
                val todayVal = count * 1500
                val monthVal = count * 1500 * 30
                _metrics.update { 
                    it.copy(
                        todaySalesCount = "$count اشتراك",
                        todaySalesValue = "$todayVal ر.ي",
                        monthSalesCount = "${count * 30} اشتراك",
                        monthSalesValue = "$monthVal ر.ي"
                    )
                }
            }

            // Commissions
            db.collection("commissions").addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val pendingCount = snapshot.documents.count { 
                    val status = it.getString("statusTypeString") 
                    status == "WARNING" || status == "ERROR" 
                }
                // Mock value
                val pendingVal = pendingCount * 2500
                _metrics.update { it.copy(totalPendingCommissions = "$pendingVal ر.ي") }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel(), navController: NavController? = null) {
    val metrics by viewModel.metrics.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Header Section
        item {
            HeaderSection(dateString = metrics.dateString, navController = navController)
        }

        // Gradient Main Card (Total Balance / Pending Commissions)
        item {
            MainGradientCard(metrics = metrics, navController = navController)
        }

        // Sales Row
        item {
            SalesRow(metrics = metrics, navController = navController)
        }

        // Action Grid
        item {
            ActionGrid(navController)
        }

        // Recent Operations
        item {
            RecentOperationsSection()
        }
    }
}

@Composable
fun HeaderSection(dateString: String, navController: NavController? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { navController?.navigate("settings") },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(40.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "الإدارة العامة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateString,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SystemStatusAlert() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessBackground),
        border = BorderStroke(1.dp, SuccessGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, SuccessGreen, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("نشطة", color = SuccessGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "النظام يعمل بشكل سليم",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "متابعة حالة الشبكات المرتبطة بشكل عام",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MainGradientCard(metrics: DashboardMetrics, navController: NavController? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
            .padding(24.dp)
            .clickable { navController?.navigate("commissions") }
    ) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "إجمالي العمولات (المعلقة)",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = metrics.totalPendingCommissions,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController?.navigate("serials") }) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = metrics.cardsAvailable, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "سيريالات مفعلة", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController?.navigate("clients") }) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = metrics.accountsCount, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "الحسابات المرتبطة", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController?.navigate("clients") }) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = metrics.trialAccountsCount, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "فترة تجريبية", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun SalesRow(metrics: DashboardMetrics, navController: NavController? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SalesCard(
            modifier = Modifier.weight(1f).clickable { navController?.navigate("sales") },
            title = "مبيعات الشهر",
            amount = metrics.monthSalesValue,
            count = metrics.monthSalesCount,
            icon = Icons.Default.CalendarMonth
        )
        SalesCard(
            modifier = Modifier.weight(1f).clickable { navController?.navigate("subscriptions") },
            title = "مبيعات اليوم",
            amount = metrics.todaySalesValue,
            count = metrics.todaySalesCount,
            icon = Icons.Default.TrendingUp
        )
    }
}

@Composable
fun SalesCard(modifier: Modifier = Modifier, title: String, amount: String, count: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = amount, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(LightBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = count, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActionGrid(navController: NavController? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            ActionCard(modifier = Modifier.weight(1f), title = "العملاء", icon = Icons.Default.Router, onClick = { navController?.navigate("clients") })
            ActionCard(modifier = Modifier.weight(1f), title = "إصدار ترخيص", icon = Icons.Default.VpnKey, onClick = { navController?.navigate("licenses") })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            ActionCard(modifier = Modifier.weight(1f), title = "التسويات المالية", icon = Icons.Default.AccountBalanceWallet, onClick = { navController?.navigate("commissions") })
            ActionCard(modifier = Modifier.weight(1f), title = "إدارة السيريالات", icon = Icons.Default.CardMembership, onClick = { navController?.navigate("serials") })
        }
    }
}

@Composable
fun ActionCard(modifier: Modifier = Modifier, title: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.height(110.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LightBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun RecentOperationsSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { /* TODO */ }) {
                Text("جميع المعاملات")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "آخر العمليات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Text("لا توجد عمليات حديثة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("ستظهر العمليات هنا عند إصدار التراخيص", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
