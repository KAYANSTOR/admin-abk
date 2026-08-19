import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/ClientsScreen.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Modify ClientsScreen signature
content = re.sub(
    r'@Composable\s*fun\s*ClientsScreen\s*\(\s*viewModel:\s*ClientsViewModel\s*=\s*viewModel\(\)\s*,\s*navController:\s*NavController\?\s*=\s*null\s*\)\s*\{',
    r'@Composable\nfun ClientsScreen(viewModel: ClientsViewModel = viewModel(), navController: NavController? = null, initialTab: Int = 0) {',
    content
)

# Modify var selectedTabIndex by remember { mutableStateOf(0) }
content = re.sub(
    r'var\s*selectedTabIndex\s*by\s*remember\s*\{\s*mutableStateOf\(0\)\s*\}',
    r'var selectedTabIndex by remember { mutableStateOf(initialTab) }',
    content
)

# Also let's handle the trial accounts filtering based on what's active.
# The code already has: (if (selectedTabIndex == 0) it.isActive else !it.isActive)
# We just need to make sure `isActive` correctly maps.
with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

# Modify AppMainScreen.kt navigation routing
app_path = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"
with open(app_path, "r", encoding="utf-8") as f:
    app_content = f.read()

# Find `composable("clients") { ClientsScreen(navController = navController) }`
app_content = app_content.replace(
    'composable("clients") { ClientsScreen(navController = navController) }',
    'composable("clients?tab={tab}", arguments = listOf(androidx.navigation.navArgument("tab") { defaultValue = 0; type = androidx.navigation.NavType.IntType })) { backStackEntry ->\n                ClientsScreen(navController = navController, initialTab = backStackEntry.arguments?.getInt("tab") ?: 0)\n            }'
)

# Also update bottom bar navigation to just "clients" if it doesn't already drop arguments
# Navigation graph handles "clients?tab={tab}", so navigating to "clients" will default to tab=0.
# The item.route for clients is "clients".
# Wait, for bottom items route matching, `currentDestination?.hierarchy?.any { it.route?.startsWith(item.route) == true }` is better if routes have arguments.
app_content = app_content.replace(
    'it.route == item.route',
    'it.route?.substringBefore("?") == item.route'
)

# Update DashboardScreen.kt actions to navigate to clients with tab=1
dash_path = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"
with open(dash_path, "r", encoding="utf-8") as f:
    dash_content = f.read()

dash_content = dash_content.replace(
    'Modifier.clickable { navController?.navigate("clients") }',
    'Modifier.clickable { navController?.navigate("clients?tab=1") }'
)

# While here, fix the Sales actions too.
# "مبيعات الشهر" -> navigate to "sales?filter=month"
# "مبيعات اليوم" -> navigate to "sales?filter=today"
# In SalesRow:
# SalesCard( modifier = Modifier.weight(1f).clickable { navController?.navigate("sales") }, title = "مبيعات الشهر", ... )
# SalesCard( modifier = Modifier.weight(1f).clickable { navController?.navigate("subscriptions") }, title = "مبيعات اليوم", ... )

dash_content = dash_content.replace(
    'Modifier.weight(1f).clickable { navController?.navigate("sales") }',
    'Modifier.weight(1f).clickable { navController?.navigate("sales?filter=month") }'
)
dash_content = dash_content.replace(
    'Modifier.weight(1f).clickable { navController?.navigate("subscriptions") }',
    'Modifier.weight(1f).clickable { navController?.navigate("sales?filter=today") }'
)

with open(dash_path, "w", encoding="utf-8") as f:
    f.write(dash_content)

with open(app_path, "w", encoding="utf-8") as f:
    f.write(app_content)
print("Clients and Navigation updated")
