import re
filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

viewmodel_replacement = """class DashboardViewModel : ViewModel() {
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val _metrics = MutableStateFlow(DashboardMetrics())
    val metrics: StateFlow<DashboardMetrics> = _metrics
    
    private val _latestClients = MutableStateFlow<List<com.example.ui.screens.ClientModel>>(emptyList())
    val latestClients: StateFlow<List<com.example.ui.screens.ClientModel>> = _latestClients

    init {
        fetchMetrics()
        fetchLatestClients()
    }

    private fun fetchMetrics() {
        viewModelScope.launch {
            // Clients
            db.collection("clients").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val count = snapshot.documents.count { it.getBoolean("isActive") == true }
                    _metrics.value = _metrics.value.copy(activeClients = count)
                }
            }
            
            // Subscriptions
            db.collection("subscriptions").addSnapshotListener { snapshot, _ ->
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
            db.collection("commissions").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val total = snapshot.documents.sumOf { (it.getString("amount")?.replace(",", "")?.toDoubleOrNull() ?: 0.0) }
                    _metrics.value = _metrics.value.copy(
                        totalCommissions = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(total)
                    )
                }
            }
            
            // Sales
            db.collection("sales").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val cal = java.util.Calendar.getInstance()
                    val todayStart = cal.apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
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
                        todaySalesValue = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(todayTotal),
                        monthSalesValue = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(monthTotal)
                    )
                }
            }
        }
    }
    
    private fun fetchLatestClients() {
        db.collection("clients")
            .limit(4)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(com.example.ui.screens.ClientModel::class.java)?.copy(id = it.id) }
                    _latestClients.value = list
                }
            }
    }
}"""
content = re.sub(r'class DashboardViewModel : ViewModel\(\) \{.*?\}', viewmodel_replacement, content, flags=re.DOTALL)

latest_clients_section = """@Composable
fun LatestClientsSection(navController: NavController?, clients: List<com.example.ui.screens.ClientModel>) {
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
}"""
content = re.sub(r'@Composable\s*fun LatestClientsSection.*?\}\s*\}', latest_clients_section, content, flags=re.DOTALL)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
