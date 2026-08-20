with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "@Composablefun DashboardScreen",
    "@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n@Composable\nfun DashboardScreen"
)
content = content.replace(
    "@Composable\nfun DashboardScreen",
    "@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n@Composable\nfun DashboardScreen"
)

with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
