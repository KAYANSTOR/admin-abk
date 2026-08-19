import re
import os

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# We need to change allBottomItems
content = re.sub(
    r'val allBottomItems = listOf\(.*?\)',
    r'''val allBottomItems = listOf(
        NavigationItem("dashboard", "الرئيسية", Icons.Default.Home),
        NavigationItem("clients", "العملاء", Icons.Default.Group),
        NavigationItem("licenses", "التراخيص", Icons.Default.VpnKey),
        NavigationItem("settings", "الإعدادات", Icons.Default.Settings)
    )''',
    content,
    flags=re.DOTALL
)

# Replace the Scaffold bottomBar and floatingActionButton
# We will use a custom bottom bar inside the Scaffold's bottomBar slot, or use the Scaffold's floatingActionButton with FabPosition.Center
# But since we need a custom shape and spacing, we will set scaffold bottomBar to our custom composable and floatingActionButton to null, or keep floatingActionButton and use FabPosition.Center. 

# Let's use a custom bottom bar instead.
new_scaffold = '''
    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomItems.forEachIndexed { index, item ->
                            if (index == 2) {
                                // Add a spacer for the center FAB
                                Spacer(modifier = Modifier.width(56.dp))
                            }
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            IconButton(
                                onClick = {
                                    navController.navigate(item.route) {
                                        if (item.route == "dashboard") {
                                            popUpTo("dashboard") { inclusive = false }
                                        } else {
                                            popUpTo("dashboard") { saveState = true }
                                            restoreState = true
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // FAB
                if (currentUser?.role == "ADMIN" || currentUser?.permissions?.contains("clients") == true) {
                    FloatingActionButton(
                        onClick = { showQuickActions = true },
                        containerColor = com.example.ui.theme.AccentPink,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-16).dp)
                            .size(64.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إجراءات سريعة", modifier = Modifier.size(32.dp))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->'''

# The current Scaffold starts at `Scaffold(` and ends before `NavHost`
content = re.sub(
    r'Scaffold\(\s*bottomBar = \{.*?\n\s*\)\s*\{\s*innerPadding\s*->',
    new_scaffold,
    content,
    flags=re.DOTALL
)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
print("File updated successfully.")
