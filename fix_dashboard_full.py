import re
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.UserModel
import com.example.ui.screens.ClientModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Query
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
    val todaySalesValue: String = "0",
    val monthSalesValue: String = "0",
    val totalRevenue: String = "0"
)

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _metrics = MutableStateFlow(DashboardMetrics())
    val metrics: StateFlow<DashboardMetrics> = _metrics
    
    private val _latestClients = MutableStateFlow<List<ClientModel>>(emptyList())
    val latestClients: StateFlow<List<ClientModel>> = _latestClients

    init {
        fetchMetrics()
        fetchLatestClients()
    }

    private fun fetchMetrics() {
        viewModelScope.launch {
            // Clients
            db.collection("clients").addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    val count = snapshot.documents.count { it.getBoolean("isActive") == true }
                    _metrics.value = _metrics.value.copy(activeClients = count)
                }
            }
            
            // Subscriptions
            db.collection("subscriptions").addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    val activeCount = snapshot.documents.count { it.getString("statusTypeString") == "SUCCESS" }
                    val trialCount = snapshot.documents.count { 
                        it.getString("plan")?.contains("تجريب", ignoreCase = true) == true
                    }
                    _metrics.value = _metrics.value.copy(
                        activeSubscriptions = activeCount,
                        trialCount = trialCount
                    )
                }
            }
            
            // Commissions
            db.collection("commissions").addSnapshotListener { snapshot: QuerySnapshot?, _ ->
                if (snapshot != null) {
                    val total = snapshot.documents.sumOf { (it.getString("amount")?.replace(",", "")?.toDoubleOrNull() ?: 0.0) }
                    _metrics.value = _metrics.value.copy(
                        totalCommissions = NumberFormat.getNumberInstance(Locale.US).format(total)
                    )
                }
            }
            
            // Sales
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
                    var allTotal = 0.0
                    
                    for (doc in snapshot.documents) {
                        val amount = doc.getString("amount")?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        
                        allTotal += amount
                        if (timestamp >= monthStart) monthTotal += amount
                        if (timestamp >= todayStart) todayTotal += amount
                    }
                    _metrics.value = _metrics.value.copy(
                        todaySalesValue = NumberFormat.getNumberInstance(Locale.US).format(todayTotal),
                        monthSalesValue = NumberFormat.getNumberInstance(Locale.US).format(monthTotal),
                        totalRevenue = NumberFormat.getNumberInstance(Locale.US).format(allTotal)
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
}

@Composable
fun DashboardScreen(navController: NavController? = null, authViewModel: AuthViewModel = viewModel(), viewModel: DashboardViewModel = viewModel()) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val latestClients by viewModel.latestClients.collectAsState()
    
    val userName = currentUser?.name ?: "مدير النظام"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F9FA))
    ) {
        DashboardHeader(userName = userName, date = "الأربعاء، 19 أغسطس 2026")
        
        HeroRevenueCard(amount = metrics.totalRevenue)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(Modifier.weight(1f), "فترة تجريبية", metrics.trialCount.toString(), PurpleIcon, Icons.Default.AccessTime)
            KpiCard(Modifier.weight(1f), "اشتراكات نشطة", metrics.activeSubscriptions.toString(), OrangeIcon, Icons.Default.Group)
            KpiCard(Modifier.weight(1f), "عملاء نشطون", metrics.activeClients.toString(), BlueIcon, Icons.Default.VpnKey)
            KpiCard(Modifier.weight(1f), "العمولات", metrics.totalCommissions, GreenIcon, Icons.Default.Payments)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text("الإجراءات السريعة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryDark)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(Modifier.weight(1f), "إصدار ترخيص", Icons.Default.VpnKey, TealGradientStart, onClick = { navController?.navigate("licenses") })
                QuickActionCard(Modifier.weight(1f), "إضافة عميل", Icons.Default.PersonAdd, TealGradientStart, onClick = { navController?.navigate("clients") })
                QuickActionCard(Modifier.weight(1f), "إضافة جهاز", Icons.Default.PhoneAndroid, TealGradientStart, onClick = { navController?.navigate("serials") })
                QuickActionCard(Modifier.weight(1f), "اشتراك جديد", Icons.Default.CreditCard, TealGradientStart, onClick = { navController?.navigate("subscriptions") })
            }
        }
        
        LatestClientsSection(navController, latestClients)
        SalesOverviewSection(metrics, navController)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DashboardHeader(userName: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryDark)
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset((-2).dp, 2.dp).size(18.dp).clip(CircleShape).background(Color.Red), contentAlignment = Alignment.Center) {
                        Text("3", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryDark)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TealGradientStart, modifier = Modifier.size(16.dp))
                Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text("مرحباً بك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TealGradientStart)
                Text(userName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(8.dp), tint = Color.DarkGray)
            }
        }
    }
}

@Composable
fun HeroRevenueCard(amount: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
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
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("كل الوقت", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("إجمالي الإيرادات", color = Color.White, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(amount, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("ري", color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}

@Composable
fun KpiCard(modifier: Modifier, title: String, value: String, color: Color, icon: ImageVector) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryDark)
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(color, RoundedCornerShape(2.dp)))
        }
    }
}

@Composable
fun QuickActionCard(modifier: Modifier, title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(90.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
        }
    }
}

@Composable
fun LatestClientsSection(navController: NavController?, clients: List<ClientModel>) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("عرض الكل", color = TealGradientStart, fontSize = 14.sp, modifier = Modifier.clickable { navController?.navigate("clients") })
            Text("آخر العملاء", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryDark)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (clients.isEmpty()) {
            Text("لا يوجد عملاء حالياً", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        } else {
            clients.forEach { client ->
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
            }
        }
    }
}

@Composable
fun LatestClientItem(name: String, phone: String, statusText: String, badgeText: String, avatarColor: Color, badgeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(badgeText, color = badgeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(6.dp).background(badgeColor, CircleShape))
            Text(statusText, color = Color.Gray, fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryDark)
                Text(phone, fontSize = 12.sp, color = Color.Gray)
            }
            Box(modifier = Modifier.size(40.dp).background(avatarColor, CircleShape), contentAlignment = Alignment.Center) {
                Text(name.firstOrNull()?.toString() ?: "?", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SalesOverviewSection(metrics: DashboardMetrics, navController: NavController?) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("عرض الكل", color = TealGradientStart, fontSize = 14.sp, modifier = Modifier.clickable { navController?.navigate("sales") })
                Text("نظرة سريعة على المبيعات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryDark)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SalesCardMin(Modifier.weight(1f), "مبيعات الشهر", metrics.monthSalesValue)
                SalesCardMin(Modifier.weight(1f), "مبيعات اليوم", metrics.todaySalesValue)
            }
        }
        FloatingActionButton(
            onClick = { navController?.navigate("sales") },
            containerColor = TealGradientStart,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp, y = 20.dp)
                .size(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Sales")
        }
    }
}

@Composable
fun SalesCardMin(modifier: Modifier, title: String, value: String) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Text(title, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.align(Alignment.End)) {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PrimaryDark)
                Text(" ري", color = PrimaryDark, fontSize = 14.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Canvas(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                val path = Path()
                path.moveTo(0f, size.height)
                path.lineTo(size.width * 0.2f, size.height * 0.8f)
                path.lineTo(size.width * 0.4f, size.height * 0.9f)
                path.lineTo(size.width * 0.6f, size.height * 0.2f)                
                path.lineTo(size.width * 0.8f, size.height * 0.4f)
                path.lineTo(size.width, 0f)
                drawPath(path, TealGradientStart, style = Stroke(width = 3f))
            }
        }
    }
}
"""

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

