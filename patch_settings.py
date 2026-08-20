with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Add imports if missing
imports = [
    "import androidx.compose.runtime.*",
    "import androidx.lifecycle.viewmodel.compose.viewModel",
    "import androidx.compose.ui.window.Dialog",
    "import androidx.compose.foundation.text.KeyboardOptions",
    "import androidx.compose.ui.text.input.KeyboardType",
    "import androidx.compose.material.icons.filled.Percent"
]
for imp in imports:
    if imp not in content:
        content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\n" + imp)

# Add parameters to SettingsScreen
if "viewModel: SettingsViewModel = viewModel()" not in content:
    content = content.replace(
        "fun SettingsScreen(",
        "fun SettingsScreen(\n    viewModel: SettingsViewModel = viewModel(),\n"
    )

# Insert the dialog state and UI
dialog_state = """
    val commissionPercentage by viewModel.commissionPercentage.collectAsState()
    var showCommissionDialog by remember { mutableStateOf(false) }
    var percentageInput by remember { mutableStateOf("") }
"""
if "val commissionPercentage by viewModel" not in content:
    content = content.replace(
        "val brushLight = Brush.linearGradient(colors = listOf(gradientStart.copy(alpha=0.1f), gradientEnd.copy(alpha=0.1f)))",
        "val brushLight = Brush.linearGradient(colors = listOf(gradientStart.copy(alpha=0.1f), gradientEnd.copy(alpha=0.1f)))\n" + dialog_state
    )

# Add the SettingRow for Commission
row = """
                    if (isAdmin) {
                        SettingRow(
                            icon = Icons.Default.Percent,
                            title = "نسبة العمولة",
                            subtitle = "النسبة الحالية: ${commissionPercentage}%",
                            onClick = { 
                                percentageInput = commissionPercentage.toString()
                                showCommissionDialog = true 
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
"""
if "نسبة العمولة" not in content:
    content = content.replace(
        "if (isAdmin) {",
        row + "                    if (isAdmin) {"
    )

# Add the Dialog at the end of SettingsScreen
dialog_ui = """
    if (showCommissionDialog) {
        AlertDialog(
            onDismissRequest = { showCommissionDialog = false },
            title = { Text("تعديل نسبة العمولة", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل النسبة المئوية (مثال: 20 لـ 20%):", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = percentageInput,
                        onValueChange = { percentageInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPct = percentageInput.toDoubleOrNull()
                        if (newPct != null) {
                            viewModel.updateCommissionPercentage(newPct)
                            showCommissionDialog = false
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommissionDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
"""
if "showCommissionDialog)" not in content:
    content = content.replace(
        "    }\n}\n\n@Composable\nfun SettingRow(",
        "    }\n" + dialog_ui + "\n}\n\n@Composable\nfun SettingRow("
    )

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
