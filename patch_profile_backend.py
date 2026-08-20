import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientProfileScreen.kt', 'r') as f:
    content = f.read()

imports = """
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
"""

if "import com.google.firebase.firestore.FirebaseFirestore" not in content:
    content = content.replace("import kotlinx.coroutines.flow.asStateFlow", "import kotlinx.coroutines.flow.asStateFlow\n" + imports)

viewmodel = """
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
"""

content = re.sub(r'class ClientProfileViewModel.*?\n}\n', viewmodel, content, flags=re.DOTALL)

screen_sig = """
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
"""

content = re.sub(r'@Composable\nfun ClientProfileScreen\(\n.*?val client by viewModel\.client\.collectAsState\(\)', screen_sig, content, flags=re.DOTALL)

# Let's add the Commission Edit Dialog right after the Scaffold topBar setup, inside the innerPadding part
dialogs = """
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
"""

content = content.replace("var showGenerateDialog by remember { mutableStateOf(false) }", dialogs)

# Also update the card to have an Edit button
old_card = """                    Column {
                        Text("نسبة العمولة الخاصة", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${client.commissionPercentage}%", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }"""

new_card = """                    Column {
                        Text("نسبة العمولة الخاصة", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${client?.commissionPercentage}%", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), RoundedCornerShape(12.dp))
                            .clickable { commInput = client?.commissionPercentage.toString(); showEditCommDialog = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("تعديل", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }"""

content = content.replace(old_card, new_card)

# Since `client` is now nullable, replace `client.` with `client?.` where applicable in the main UI, but wait, I early returned `if (client == null) return` above, so Compose compiler might complain since it's a nullable type without explicit cast, or we can use `client!!` in the child Composables.
# Actually, `val client = client` would smart cast, but in Compose `client` is a delegated property, so it can't smart cast.
# Let's fix that.
content = content.replace("ClientIdentityCard(client = client)", "client?.let { ClientIdentityCard(client = it) }")
content = content.replace("ClientStatusCard(client = client, onGenerateLicense = { showGenerateDialog = true })", "client?.let { ClientStatusCard(client = it, onGenerateLicense = { showGenerateDialog = true }) }")
content = content.replace("ClientFinancialsCard(client = client, onSettleCommissions = { showSettleDialog = true })", "client?.let { ClientFinancialsCard(client = it, onSettleCommissions = { showSettleDialog = true }) }")
content = content.replace("${client.pendingCommissions}", "${client?.pendingCommissions}")
content = content.replace("client.commissionPercentage", "client?.commissionPercentage")

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientProfileScreen.kt', 'w') as f:
    f.write(content)
