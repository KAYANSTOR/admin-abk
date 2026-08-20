with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    lines = f.readlines()

dialog_lines = """        if (showPinDialog) {
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
"""

# Let's insert it at line 267 (0-indexed 266)
lines.insert(267, dialog_lines)

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.writelines(lines)
