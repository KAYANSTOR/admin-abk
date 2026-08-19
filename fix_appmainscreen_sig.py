import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace(
    'fun AppMainScreen(\n    clientsViewModel: ClientsViewModel = viewModel(),\n    serialsViewModel: SerialsViewModel = viewModel(),\n    authViewModel: AuthViewModel = viewModel(),\n    onLogout: () -> Unit = {}\n) {',
    'fun AppMainScreen(\n    initialRoute: String? = null,\n    clientsViewModel: ClientsViewModel = viewModel(),\n    serialsViewModel: SerialsViewModel = viewModel(),\n    authViewModel: AuthViewModel = viewModel(),\n    onLogout: () -> Unit = {}\n) {'
)

# And use initialRoute for NavHost startDestination if I haven't correctly replaced it yet
content = content.replace(
    'NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.padding(innerPadding))',
    'NavHost(navController = navController, startDestination = initialRoute ?: "dashboard", modifier = Modifier.padding(innerPadding))'
)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

