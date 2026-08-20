import re

with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Settings clickable
old_settings = """                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryDark)
                }"""
new_settings = """                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White).clickable { navController?.navigate("settings") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryDark)
                }"""
content = content.replace(old_settings, new_settings)

# Notifications clickable
old_notifications = """                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryDark)
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset((-2).dp, 2.dp).size(16.dp).clip(CircleShape).background(Color.Red), contentAlignment = Alignment.Center) {
                        Text("3", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }"""
new_notifications = """                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White).clickable { navController?.navigate("notifications") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryDark)
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset((-2).dp, 2.dp).size(16.dp).clip(CircleShape).background(Color.Red), contentAlignment = Alignment.Center) {
                        Text("3", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }"""
content = content.replace(old_notifications, new_notifications)

with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
