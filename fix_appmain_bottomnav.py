import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# I will replace the bottomBar definition with the one that matches the image.
# The image has 4 tabs: "الرئيسية", "العملاء", "التراخيص", "الإعدادات"
# and a center FAB inside the navigation bar layout with an up arrow.

replacement_nav = """        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val items = listOf(
                NavigationItem("dashboard", "الرئيسية", Icons.Default.Home),
                NavigationItem("clients", "العملاء", Icons.Default.People),
                NavigationItem("placeholder", "", Icons.Default.Add), // Center gap
                NavigationItem("licenses", "التراخيص", Icons.Default.VpnKey),
                NavigationItem("settings", "الإعدادات", Icons.Default.Settings)
            )

            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.BottomCenter) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    items.forEachIndexed { index, item ->
                        if (index == 2) {
                            // Empty space for the FAB
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val selected = currentRoute?.startsWith(item.route) == true || 
                                           (item.route == "dashboard" && currentRoute == null)
                            
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.title,
                                        tint = if (selected) MaterialTheme.colorScheme.error else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        item.title,
                                        color = if (selected) MaterialTheme.colorScheme.error else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = MaterialTheme.colorScheme.error,
                                    unselectedIconColor = Color.Gray
                                )
                            )
                        }
                    }
                }
                
                // Floating Action Button matching the image
                if (currentUser?.role == "ADMIN" || currentUser?.permissions?.contains("clients") == true) {
                    FloatingActionButton(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showQuickActions = true 
                        },
                        containerColor = Color(0xFF141C2E), // Dark blue
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-10).dp)
                            .size(56.dp)
                    ) {
                        Icon(Icons.Default.KeyboardDoubleArrowUp, contentDescription = "إجراءات سريعة", modifier = Modifier.size(32.dp))
                    }
                }
            }
        }"""

# Using regex to replace the old bottomBar block.
content = re.sub(
    r'bottomBar = \{.*?containerColor = MaterialTheme\.colorScheme\.background\s*\} \s*\{ innerPadding ->',
    replacement_nav + ',\n        containerColor = MaterialTheme.colorScheme.background\n    ) { innerPadding ->',
    content,
    flags=re.DOTALL
)

# Ensure required icons are imported
icons_to_import = [
    "import androidx.compose.material.icons.filled.Home",
    "import androidx.compose.material.icons.filled.People",
    "import androidx.compose.material.icons.filled.Settings",
    "import androidx.compose.material.icons.filled.VpnKey",
    "import androidx.compose.material.icons.filled.Add",
    "import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp",
    "import androidx.compose.ui.graphics.Color",
    "import androidx.compose.ui.unit.sp"
]

for imp in icons_to_import:
    if imp not in content:
        content = content.replace("import androidx.compose.material.icons.Icons", imp + "\nimport androidx.compose.material.icons.Icons")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

