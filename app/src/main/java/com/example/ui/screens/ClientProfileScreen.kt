package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

import java.util.UUID

// Data layer for Client Profile
data class ClientProfileModel(
    val id: String = UUID.randomUUID().toString(),
    val networkName: String,
    val deviceId: String,
    val phoneNumber: String,
    val status: String,
    val subscriptionExpiry: String?,
    val totalLicenses: Int,
    val totalSales: String,
    val pendingCommissions: String,
    val commissionPercentage: Double = 0.0
)


class ClientProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _client = MutableStateFlow<ClientProfileModel?>(null)
    val client: StateFlow<ClientProfileModel?> = _client.asStateFlow()

    fun loadClient(clientId: String) {
        viewModelScope.launch {
            db.collection("clients").document(clientId).addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val name = snapshot.getString("name") ?: ""
                val network = snapshot.getString("networkName") ?: snapshot.getString("storeName") ?: name
                val phone = snapshot.getString("phone") ?: ""
                val deviceId = snapshot.getString("deviceId") ?: ""
                val status = snapshot.getString("status") ?: "ACTIVE"
                val commission = snapshot.getDouble("commissionPercentage") ?: 0.0
                
                _client.value = ClientProfileModel(
                    id = snapshot.id,
                    networkName = network.ifEmpty { "عميل غير مسمى" },
                    deviceId = deviceId,
                    phoneNumber = phone,
                    status = status,
                    subscriptionExpiry = null,
                    totalLicenses = 0,
                    totalSales = "0",
                    pendingCommissions = "0",
                    commissionPercentage = commission
                )
            }
        }
    }

    fun updateStatus(newStatus: String) {
        val cid = _client.value?.id ?: return
        viewModelScope.launch {
            db.collection("clients").document(cid).update("status", newStatus)
        }
    }

    fun updateCommission(newPct: Double) {
        val cid = _client.value?.id ?: return
        viewModelScope.launch {
            db.collection("clients").document(cid).update("commissionPercentage", newPct)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ClientProfileScreen(
    clientId: String = "",
    viewModel: ClientProfileViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    LaunchedEffect(clientId) {
        if (clientId.isNotEmpty()) {
            viewModel.loadClient(clientId)
        }
    }
    val client by viewModel.client.collectAsState()
    
    if (client == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val scrollState = rememberScrollState()
    var showActionSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ملف العميل", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showActionSheet = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "خيارات")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        
        var showGenerateDialog by remember { mutableStateOf(false) }
        var showEditCommDialog by remember { mutableStateOf(false) }
        var commInput by remember { mutableStateOf("") }
        
        if (showEditCommDialog) {
            AlertDialog(
                onDismissRequest = { showEditCommDialog = false },
                title = { Text("تعديل نسبة العمولة", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("أدخل نسبة العمولة الخاصة بهذا العميل:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = commInput,
                            onValueChange = { commInput = it },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val pct = commInput.toDoubleOrNull()
                        if (pct != null) {
                            viewModel.updateCommission(pct)
                            showEditCommDialog = false
                        }
                    }) {
                        Text("حفظ")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditCommDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }


        if (showGenerateDialog) {
            AlertDialog(
                onDismissRequest = { showGenerateDialog = false },
                title = { Text("توليد ترخيص جديد") },
                text = { Text("جاري العمل على هذه الميزة. سيتم ربطها بواجهة برمجة التطبيقات لاحقاً.") },
                confirmButton = {
                    TextButton(onClick = { showGenerateDialog = false }) {
                        Text("حسناً")
                    }
                }
            )
        }

        var showSettleDialog by remember { mutableStateOf(false) }

        if (showSettleDialog) {
            AlertDialog(
                onDismissRequest = { showSettleDialog = false },
                title = { Text("تسوية العمولات") },
                text = { Text("هل أنت متأكد من رغبتك في تسوية العمولات المعلقة وقدرها ${client?.pendingCommissions}؟") },
                confirmButton = {
                    Button(
                        onClick = { showSettleDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("نعم، تسوية")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettleDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header: Identity
            client?.let { ClientIdentityCard(client = it) }

            // Section: Subscription & Licensing
            client?.let { ClientStatusCard(client = it, onGenerateLicense = { showGenerateDialog = true }) }

            // Section: Financials
            client?.let { ClientFinancialsCard(client = it, onSettleCommissions = { showSettleDialog = true }) }
        }

        if (showActionSheet) {
            ModalBottomSheet(
                onDismissRequest = { showActionSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        "إجراءات العميل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("توليد ترخيص جديد") },
                        leadingContent = { Icon(Icons.Default.VpnKey, null) },
                        modifier = Modifier.clickable { showActionSheet = false }
                    )
                    ListItem(
                        headlineContent = { Text("تمديد الاشتراك") },
                        leadingContent = { Icon(Icons.Default.Update, null) },
                        modifier = Modifier.clickable { showActionSheet = false }
                    )
                    ListItem(
                        headlineContent = { Text("إيقاف / تجميد الحساب") },
                        leadingContent = { Icon(Icons.Default.Block, null, tint = MaterialTheme.colorScheme.error) },
                        colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.clickable {
                            viewModel.updateStatus("مجمد")
                            showActionSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClientIdentityCard(client: ClientProfileModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = client.networkName.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = client.networkName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = client.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("معرف الجهاز (Device ID)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(client.deviceId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* Copy */ }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ClientStatusCard(client: ClientProfileModel, onGenerateLicense: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("حالة الاشتراك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("الحالة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                if (client.status == "نشط") com.example.ui.theme.SuccessBackground else MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = client.status,
                            color = if (client.status == "نشط") com.example.ui.theme.SuccessGreen else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("تاريخ الانتهاء", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(client.subscriptionExpiry ?: "غير محدد", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("التراخيص المصدرة", style = MaterialTheme.typography.bodyMedium)
                }
                Text("${client.totalLicenses} ترخيص", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            }
            
            Button(
                onClick = onGenerateLicense,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("توليد ترخيص جديد")
            }
        }
    }
}

@Composable
fun ClientFinancialsCard(client: ClientProfileModel, onSettleCommissions: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("المالية والعمولات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("إجمالي المبيعات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(client.totalSales, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(com.example.ui.theme.WarningBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Icon(Icons.Default.Money, contentDescription = null, tint = com.example.ui.theme.WarningOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("عمولات مستحقة", style = MaterialTheme.typography.bodySmall, color = com.example.ui.theme.WarningOrange)
                        Text(client.pendingCommissions, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.WarningOrange)
                    }
                }
            }
            
            OutlinedButton(
                onClick = onSettleCommissions,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسوية العمولات المعلقة")
            }
        }
    }
}
