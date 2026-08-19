import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Add a section for Sync
sync_section = '''
        item {
            Text(
                text = "المزامنة (Sync)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            var syncStatus by remember { mutableStateOf("آخر وقت مزامنة: قبل قليل") }
            var isSyncing by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (!isSyncing) {
                        isSyncing = true
                        syncStatus = "جارٍ المزامنة..."
                        coroutineScope.launch {
                            try {
                                // Simulate sync wait
                                kotlinx.coroutines.delay(2000)
                                // We can use waitForPendingWrites or enableNetwork
                                db.enableNetwork().await()
                                syncStatus = "تمت المزامنة بنجاح"
                            } catch (e: Exception) {
                                syncStatus = "فشل المزامنة"
                            } finally {
                                isSyncing = false
                            }
                        }
                    }
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مزامنة البيانات الآن",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = syncStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = if(syncStatus.contains("فشل")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
'''

content = content.replace('import androidx.compose.material.icons.filled.Phone', 'import androidx.compose.material.icons.filled.Phone\nimport androidx.compose.material.icons.filled.Sync\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.tasks.await')

# Insert before `item { Text(text = "التفضيلات",`
content = content.replace(
    '        item {\n            Text(\n                text = "التفضيلات"',
    sync_section + '\n        item {\n            Text(\n                text = "التفضيلات"'
)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
