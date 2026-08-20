import os
filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

content = """package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.auth.AuthViewModel
import com.example.ui.screens.ClientModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

val TealGradientStart = Color(0xFF2B7C8E)
val PurpleGradientEnd = Color(0xFF8B5E9C)
val PrimaryDark = Color(0xFF141C2E)
val GreenIcon = Color(0xFF34A853)
val OrangeIcon = Color(0xFFFF9800)
val PurpleIcon = Color(0xFF673AB7)
val BlueIcon = Color(0xFF2196F3)

data class DashboardMetrics(
    val trialCount: Int = 0,
    val activeSubscriptions: Int = 0,
    val activeClients: Int = 0,
    val totalCommissions: String = "0",
    val pendingCommissions: String = "0",
    val todaySalesValue: String = "0",
    val monthSalesValue: String = "0"
)

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _metrics = MutableStateFlow(DashboardMetrics())
    val metrics: StateFlow<DashboardMetrics> = _metrics
    
    private val _latestClients = MutableStateFlow<List<ClientModel>>(emptyList())
    val latestClients: StateFlow<List<ClientModel>> = _latestClients

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        fetchMetrics()
        fetchLatestClients()
    }

    private fun fetchMetrics() {
        viewModelScope.launch {
            db.collection("clients").addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    val count = snapshot.documents.count { it.getBoolean("isActive") == true }
                    _metrics.value = _metrics.value.copy(activeClients = count)
                }
            }
            
            db.collection("subscriptions").addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    val activeCount = snapshot.documents.count { it.getString("statusTypeString") == "SUCCESS" }
                    val trialCount = snapshot.documents.count { 
                        val plan = it.getString("plan") ?: ""
                        val statusText = it.getString("statusText") ?: ""
                        plan.contains("تجريب", ignoreCase = true) || statusText.contains("تجريب", ignoreCase = true)
                    }
                    _metrics.value = _metrics.value.copy(
                        activeSubscriptions = activeCount,
                        trialCount = trialCount
                    )
                }
            }
            
            db.collection("commissions").addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    var total = 0.0
                    var pending = 0.0
                    for (doc in snapshot.documents) {
                        val amountStr = doc.getString("commissionAmount") ?: doc.getString("amount") ?: "0"
                        val amount = amountStr.replace(",", "").toDoubleOrNull() ?: 0.0
                        total += amount
                        
                        val status = doc.getString("statusTypeString") ?: "WARNING"
                        if (status == "WARNING") {
                            pending += amount
                        }
                    }
                    _metrics.value = _metrics.value.copy(
                        totalCommissions = NumberFormat.getNumberInstance(Locale.US).format(total),
                        pendingCommissions = NumberFormat.getNumberInstance(Locale.US).format(pending)
                    )
                }
            }
            
            db.collection("sales").addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    val cal = Calendar.getInstance()
                    val todayStart = cal.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val monthStart = cal.timeInMillis
                    
                    var todayTotal = 0.0
                    var monthTotal = 0.0
                    
                    for (doc in snapshot.documents) {
                        val amount = doc.getString("amount")?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        
                        if (timestamp >= monthStart) monthTotal += amount
                        if (timestamp >= todayStart) todayTotal += amount
                    }
                    _metrics.value = _metrics.value.copy(
                        todaySalesValue = NumberFormat.getNumberInstance(Locale.US).format(todayTotal),
                        monthSalesValue = NumberFormat.getNumberInstance(Locale.US).format(monthTotal)
                    )
                }
            }
        }
    }
    
    private fun fetchLatestClients() {
        db.collection("clients")
            .limit(4)
            .addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(ClientModel::class.java)?.copy(id = it.id) }
                    _latestClients.value = list
                }
            }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1200)
            _isRefreshing.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController? = null, authViewModel: AuthViewModel = viewModel(), viewModel: DashboardViewModel = viewModel()) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val latestClients by viewModel.latestClients.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val userName = currentUser?.name ?: "مدير النظام"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                DashboardHeader(userName = userName, date = "الأربعاء، 19 أغسطس 2026")
                
                HeroRevenueCard(
                    amount = metrics.pendingCommissions,
                    onClick = { navController?.navigate("commissions") }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 2x2 Grid for KPIs
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiCard(Modifier.weight(1f), "إجمالي العمولات", metrics.totalCommissions, GreenIcon, Icons.Default.Payments)
                        KpiCard(Modifier.weight(1f), "عملاء نشطون", metrics.activeClients.toString(), BlueIcon, Icons.Default.VpnKey)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiCard(Modifier.weight(1f), "اشتراكات نشطة", metrics.activeSubscriptions.toString(), OrangeIcon, Icons.Default.Group)
                        KpiCard(Modifier.weight(1f), "فترة تجريبية", metrics.trialCount.toString(), PurpleIcon, Icons.Default.AccessTime)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text("الإجراءات السريعة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 2x2 Grid for Quick Actions for better proportions
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionCard(Modifier.weight(1f), "اشتراك جديد", Icons.Default.CreditCard, TealGradientStart, onClick = { navController?.navigate("subscriptions") })
                            QuickActionCard(Modifier.weight(1f), "إضافة جهاز", Icons.Default.PhoneAndroid, TealGradientStart, onClick = { navController?.navigate("serials") })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionCard(Modifier.weight(1f), "إضافة عميل", Icons.Default.PersonAdd, TealGradientStart, onClick = { navController?.navigate("clients") })
                            QuickActionCard(Modifier.weight(1f), "إصدار ترخيص", Icons.Default.VpnKey, TealGradientStart, onClick = { navController?.navigate("licenses") })
                        }
                    }
                }
                
                LatestClientsSection(navController, latestClients)
                SalesOverviewSection(metrics, navController)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DashboardHeader(userName: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(8.dp), tint = Color.DarkGray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text("مرحباً بك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TealGradientStart)
                Text(userName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryDark)
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryDark)
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset((-2).dp, 2.dp).size(16.dp).clip(CircleShape).background(Color.Red), contentAlignment = Alignment.Center) {
                        Text("3", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TealGradientStart, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun HeroRevenueCard(amount: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .background(Brush.horizontalGradient(listOf(PurpleGradientEnd, TealGradientStart)))
    ) {
        Canvas(modifier = Modifier.fillMaxWidth(0.55f).fillMaxHeight().padding(top = 40.dp).align(Alignment.CenterStart)) {
            val path = Path()
            val points = listOf(
                Offset(0f, size.height * 0.8f),
                Offset(size.width * 0.15f, size.height * 0.6f),
                Offset(size.width * 0.3f, size.height * 0.5f),
                Offset(size.width * 0.45f, size.height * 0.7f),
                Offset(size.width * 0.6f, size.height * 0.3f),
                Offset(size.width * 0.75f, size.height * 0.4f),
                Offset(size.width * 0.9f, size.height * 0.1f),
                Offset(size.width, size.height * 0.0f)
            )
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            drawPath(path, Color.White.copy(alpha = 0.5f), style = Stroke(width = 4f))
            points.forEach { point -> drawCircle(Color.White, radius = 6f, center = point) }
        }

        Column(modifier = Modifier.padding(24.dp).align(Alignment.TopEnd), horizontalAlignment = Alignment.End) {
            Box(modifier = Modifier.background(Color.White.copy(alpha=0.2f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("كل الوقت", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("العمولات المعلقة", color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ري", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(bottom = 6.dp), fontWeight = FontWeight.Bold)
                Text(amount, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun KpiCard(modifier: Modifier, title: String, value: String, color: Color, icon: ImageVector) {
    Card(
        modifier = modifier.height(105.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(38.dp).background(color.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = PrimaryDark)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(color, RoundedCornerShape(2.dp)))
        }
    }
}

@Composable
fun QuickActionCard(modifier: Modifier, title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(95.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
        }
    }
}

@Composable
fun LatestClientsSection(navController: NavController?, clients: List<ClientModel>) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("آخر العملاء", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = PrimaryDark)
            Text("عرض الكل", color = TealGradientStart, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { navController?.navigate("clients") })
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (clients.isEmpty()) {
            Text("لا يوجد عملاء حالياً", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        } else {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    clients.forEachIndexed { index, client ->
                        val badgeColor = if (client.isActive) GreenIcon else Color.Red
                        val statusText = if (client.isActive) "نشط" else "موقوف"
                        val remainingText = if (client.isActive) "اشتراك فعال" else "متوقف"
                        
                        LatestClientItem(
                            name = client.name.ifEmpty { "عميل غير مسمى" },
                            phone = client.networkName.ifEmpty { "لا توجد شبكة" },
                            statusText = remainingText,
                            badgeText = statusText,
                            avatarColor = TealGradientStart,
                            badgeColor = badgeColor
                        )
                        if (index < clients.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LatestClientItem(name: String, phone: String, statusText: String, badgeText: String, avatarColor: Color, badgeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).background(avatarColor, CircleShape), contentAlignment = Alignment.Center) {
                Text(name.firstOrNull()?.toString() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(horizontalAlignment = Alignment.Start) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryDark)
                Text(phone, fontSize = 13.sp, color = Color.Gray)
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Box(modifier = Modifier.background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text(badgeText, color = badgeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(6.dp).background(badgeColor, CircleShape))
                Text(statusText, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SalesOverviewSection(metrics: DashboardMetrics, navController: NavController?) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("نظرة سريعة على المبيعات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = PrimaryDark)
                Text("عرض الكل", color = TealGradientStart, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { navController?.navigate("sales") })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SalesCardMin(Modifier.weight(1f), "مبيعات اليوم", metrics.todaySalesValue)
                SalesCardMin(Modifier.weight(1f), "مبيعات الشهر", metrics.monthSalesValue)
            }
        }
        FloatingActionButton(
            onClick = { navController?.navigate("sales") },
            containerColor = TealGradientStart,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-12).dp, y = 20.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Sales", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun SalesCardMin(modifier: Modifier, title: String, value: String) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.align(Alignment.Start)) {
                Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = PrimaryDark)
                Text(" ري", color = PrimaryDark, fontSize = 14.sp, modifier = Modifier.padding(bottom = 3.dp), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.weight(1f))
            Canvas(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                val path = Path()
                path.moveTo(size.width, size.height)
                path.lineTo(size.width * 0.8f, size.height * 0.8f)
                path.lineTo(size.width * 0.6f, size.height * 0.9f)
                path.lineTo(size.width * 0.4f, size.height * 0.2f)                
                path.lineTo(size.width * 0.2f, size.height * 0.4f)
                path.lineTo(0f, 0f)
                drawPath(path, TealGradientStart, style = Stroke(width = 4f))
            }
        }
    }
}
"""
with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

