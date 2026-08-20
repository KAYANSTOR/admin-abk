import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/EmployeesScreen.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

create_dialog_code = """
@Composable
fun CreateEmployeeDialogNew(onDismiss: () -> Unit, onSave: (String, String, String, String, List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STAFF") }
    var selectedPermissions by remember { mutableStateOf(setOf<String>()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                Text("إضافة موظف جديد", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("الاسم كامل") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("رقم الجوال") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it.filter { char -> char.isDigit() } },
                    placeholder = { Text("رمز الدخول (4 أرقام)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { role = "STAFF" }
                            .background(if (role == "STAFF") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("موظف", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (role == "STAFF") MaterialTheme.colorScheme.onSurface else Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { role = "ADMIN" }
                            .background(if (role == "ADMIN") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("مدير نظام", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (role == "ADMIN") MaterialTheme.colorScheme.onSurface else Color.Gray)
                    }
                }

                if (role == "STAFF") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("الصلاحيات (للموظف)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    
                    // Two columns grid
                    val chunked = ALL_PERMISSIONS.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in chunked) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                for ((id, label) in row) {
                                    val isSelected = selectedPermissions.contains(id)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { 
                                                selectedPermissions = if (isSelected) selectedPermissions - id else selectedPermissions + id 
                                            }
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha=0.1f) else Color.Transparent)
                                            .padding(4.dp)
                                    ) {
                                        Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.scale(0.8f))
                                        Text(text = label, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                                    }
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha=0.5f), contentColor = Color.Gray)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = { 
                            if (name.isNotBlank() && phone.isNotBlank() && pin.length == 4) {
                                val finalPerms = if (role == "ADMIN") ALL_PERMISSIONS.map { it.first } else selectedPermissions.toList()
                                onSave(name, phone, pin, role, finalPerms) 
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("إنشاء", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
"""

if "CreateEmployeeDialogNew" not in content:
    content += create_dialog_code

content = content.replace("// CreateEmployeeDialog(onDismiss = { showAddDialog = false })", """
        CreateEmployeeDialogNew(
            onDismiss = { showAddDialog = false },
            onSave = { newName, newPhone, newPin, newRole, newPermissions ->
                viewModel.createUser(newName, newPhone, newPin, newRole, newPermissions)
                showAddDialog = false
            }
        )
""")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

