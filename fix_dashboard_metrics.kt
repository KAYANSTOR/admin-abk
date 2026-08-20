

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
                    val count = snapshot.documents.count {
                        val status = it.getString("status") ?: ""
                        val isActiveLegacy = it.getBoolean("isActive") ?: true
                        val displayStatus = if (status.isNotEmpty()) status else if (isActiveLegacy) "ACTIVE" else "SUSPENDED"
                        displayStatus in listOf("ACTIVE", "WARNING", "GRACE_PERIOD")
                    }
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
                        
                        val status = doc.getString("statusTypeString") ?: "WARNING"
                        if (status == "SUCCESS") {
                            total += amount
                        } else if (status == "WARNING") {
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
                    _latestClients.value = list.sortedByDescending { it.id }.take(4) // Fallback sorting
                }
            }
    }
}
