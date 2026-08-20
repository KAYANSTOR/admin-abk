import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# 1. Update SettingsScreen signature
old_sig = """@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onManageEmployeesClick: () -> Unit = {},
    currentUser: UserModel? = null,
    isAdmin: Boolean = false
) {"""

new_sig = """@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onManageEmployeesClick: () -> Unit = {},
    currentUser: UserModel? = null,
    isAdmin: Boolean = false,
    onChangePin: (String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onToggleNotifications: (Boolean) -> Unit = {}
) {"""
content = content.replace(old_sig, new_sig)

# 2. Add state for PIN dialog
states = """    val commissionPercentage by viewModel.commissionPercentage.collectAsState()
    var showCommissionDialog by remember { mutableStateOf(false) }
    var percentageInput by remember { mutableStateOf("") }
    
    var showPinDialog by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
"""
content = re.sub(r'    val commissionPercentage by .*?    var percentageInput by remember \{ mutableStateOf\(""\) \}', states, content, flags=re.DOTALL)

# 3. Update the SettingRow usages for notifications and security
old_notif = """                    SettingRow(
                        icon = Icons.Default.Notifications,
                        title = "الإشعارات",
                        subtitle = "التحكم في تنبيهات النظام",
                        onClick = {}
                    )"""

new_notif = """                    SettingRow(
                        icon = Icons.Default.Notifications,
                        title = "الإشعارات",
                        subtitle = "التحكم في تنبيهات النظام",
                        trailingContent = {
                            Switch(
                                checked = currentUser.notificationsEnabled,
                                onCheckedChange = { onToggleNotifications(it) }
                            )
                        },
                        onClick = { onToggleNotifications(!currentUser.notificationsEnabled) }
                    )"""
content = content.replace(old_notif, new_notif)

old_sec = """                    SettingRow(
                        icon = Icons.Default.Lock,
                        title = "الأمان",
                        subtitle = "تغيير رمز الدخول (PIN)",
                        onClick = {}
                    )"""

new_sec = """                    SettingRow(
                        icon = Icons.Default.Lock,
                        title = "الأمان",
                        subtitle = "تغيير رمز الدخول (PIN)",
                        onClick = {
                            oldPin = ""
                            newPin = ""
                            pinError = null
                            showPinDialog = true
                        }
                    )"""
content = content.replace(old_sec, new_sec)

# 4. Add the PIN Dialog at the end
pin_dialog = """
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("تغيير رمز الدخول (PIN)", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        if (pinError != null) {
                            Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        OutlinedTextField(
                            value = oldPin,
                            onValueChange = { if (it.length <= 4) oldPin = it },
                            label = { Text("الرمز القديم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 4) newPin = it },
                            label = { Text("الرمز الجديد (4 أرقام)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        onChangePin(oldPin, newPin, {
                            showPinDialog = false
                        }, { err ->
                            pinError = err
                        })
                    }) {
                        Text("حفظ")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
}
"""
content = re.sub(r'}\s*$', pin_dialog, content)

# 5. Update SettingRow signature
old_row = """fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    destructive: Boolean = false
) {"""

new_row = """fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    destructive: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null
) {"""
content = content.replace(old_row, new_row)

old_row_icon = """                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = if (destructive) MaterialTheme.colorScheme.error else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )"""

new_row_icon = """                if (trailingContent != null) {
                    trailingContent()
                } else {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = if (destructive) MaterialTheme.colorScheme.error else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }"""
content = content.replace(old_row_icon, new_row_icon)

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
