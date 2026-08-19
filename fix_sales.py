import re

app_path = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"
with open(app_path, "r", encoding="utf-8") as f:
    app_content = f.read()

app_content = app_content.replace(
    'composable("sales") { SalesScreen() }',
    'composable("sales?filter={filter}", arguments = listOf(androidx.navigation.navArgument("filter") { defaultValue = "all"; type = androidx.navigation.NavType.StringType })) { backStackEntry ->\n                SalesScreen(filter = backStackEntry.arguments?.getString("filter") ?: "all")\n            }'
)

with open(app_path, "w", encoding="utf-8") as f:
    f.write(app_content)
print("AppMainScreen updated for sales filter")
