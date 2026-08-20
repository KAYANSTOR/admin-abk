import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientProfileScreen.kt', 'r') as f:
    content = f.read()

# Add Commission Card
card_ui = """
            // Commission Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
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
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
"""

if "نسبة العمولة الخاصة" not in content:
    content = content.replace(
        "            // Quick Stats Row",
        card_ui + "\n            // Quick Stats Row"
    )

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientProfileScreen.kt', 'w') as f:
    f.write(content)
