import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'composable("client_profile") { ClientProfileScreen(onBackClick = { navController.popBackStack() }) }',
    'composable("client_profile/{clientId}", arguments = listOf(androidx.navigation.navArgument("clientId") { type = androidx.navigation.NavType.StringType })) { backStackEntry ->\n                val clientId = backStackEntry.arguments?.getString("clientId") ?: ""\n                ClientProfileScreen(clientId = clientId, onBackClick = { navController.popBackStack() })\n            }'
)

with open('/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt', 'w') as f:
    f.write(content)
