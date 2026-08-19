import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/EmployeesScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

new_logic = """
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUserForEdit by remember { mutableStateOf<UserModel?>(null) }
"""

content = content.replace("var showAddDialog by remember { mutableStateOf(false) }", new_logic)

new_dialog_logic = """
    if (showAddDialog) {
        CreateEmployeeDialog(onDismiss = { showAddDialog = false })
    }
    
    if (selectedUserForEdit != null) {
        EditEmployeeDialog(
            user = selectedUserForEdit!!, 
            onDismiss = { selectedUserForEdit = null },
            onSave = { updatedUser -> 
                viewModel.updateUserPermissions(updatedUser)
                selectedUserForEdit = null
            }
        )
    }
"""
content = content.replace(
    'if (showAddDialog) {\n        CreateEmployeeDialog(onDismiss = { showAddDialog = false })\n    }',
    new_dialog_logic
)

card_logic = """
fun EmployeeCard(user: UserModel, onToggleStatus: () -> Unit, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditClick() },
"""
content = content.replace(
    'fun EmployeeCard(user: UserModel, onToggleStatus: () -> Unit) {\n    Card(\n        modifier = Modifier.fillMaxWidth(),',
    card_logic
)

call_logic = 'EmployeeCard(user, onToggleStatus = { viewModel.toggleUserStatus(user) }, onEditClick = { selectedUserForEdit = user })'
content = content.replace('EmployeeCard(user, onToggleStatus = { viewModel.toggleUserStatus(user) })', call_logic)

viewmodel_logic = """
    fun toggleUserStatus(user: UserModel) {
        db.collection("users").document(user.id).update("isActive", !user.isActive)
    }

    fun updateUserPermissions(user: UserModel) {
        db.collection("users").document(user.id).update(
            "permissions", user.permissions
        )
    }
"""
content = content.replace(
    'fun toggleUserStatus(user: UserModel) {\n        db.collection("users").document(user.id).update("isActive", !user.isActive)\n    }',
    viewmodel_logic
)

edit_dialog = """
@Composable
fun EditEmployeeDialog(user: UserModel, onDismiss: () -> Unit, onSave: (UserModel) -> Unit) {
    val permissionOptions = listOf(
        "clients" to "إدارة العملاء",
        "licenses" to "إدارة التراخيص",
        "serials" to "إدارة السيريالات",
        "commissions" to "إدارة التقارير والعمولات",
        "subscriptions" to "إدارة الاشتراكات"
    )
    var selectedPermissions by remember { mutableStateOf(user.permissions.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل صلاحيات ${user.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("الصلاحيات الممنوحة:", style = MaterialTheme.typography.titleSmall)
                permissionOptions.forEach { (route, title) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPermissions = if (selectedPermissions.contains(route)) {
                                    selectedPermissions - route
                                } else {
                                    selectedPermissions + route
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedPermissions.contains(route),
                            onCheckedChange = { checked ->
                                selectedPermissions = if (checked) selectedPermissions + route else selectedPermissions - route
                            }
                        )
                        Text(text = title)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(user.copy(permissions = selectedPermissions.toList()))
            }) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
"""

content = content + "\n" + edit_dialog

if "import androidx.compose.foundation.clickable" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.clickable")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

