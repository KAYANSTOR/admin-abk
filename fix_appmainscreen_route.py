import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Add initialRoute parameter
content = content.replace(
    '@Composable\nfun AppMainScreen(onLogout: () -> Unit = {}) {',
    '@Composable\nfun AppMainScreen(initialRoute: String? = null, onLogout: () -> Unit = {}) {'
)

# Use it as startDestination for NavHost inside AppMainScreen
content = content.replace(
    'NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.padding(innerPadding))',
    'NavHost(navController = navController, startDestination = initialRoute ?: "dashboard", modifier = Modifier.padding(innerPadding))'
)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

