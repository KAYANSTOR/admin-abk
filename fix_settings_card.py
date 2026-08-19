import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

new_card = """
        item {
            if (currentUser != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)), // Slate 200 light gray
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF64748B), // Slate 500
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = currentUser.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B) // Slate 800 (PrimaryDark equivalent)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if(currentUser.role == "ADMIN") "الإدارة العامة" else "موظف",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B) // Slate 500
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8) // Slate 400
                        )
                    }
                }
            }
        }
"""

# Replace the existing card in SettingsScreen
# It looks like:
# item {
#     if (currentUser != null) {
#         Card(
#             modifier = Modifier.fillMaxWidth(),
#             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
#             elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
#         ) {
# ... to the end of that item block.

# Using a regex to replace the whole item block for currentUser
content = re.sub(
    r'item \{\s*if \(currentUser != null\) \{.*?(?=\s*item \{|\Z)',
    new_card.strip() + '\n\n',
    content,
    flags=re.DOTALL
)

# Need to import Color and BorderStroke if they are not already.
if "import androidx.compose.ui.graphics.Color" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.foundation.BorderStroke")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

print("Settings card updated")
