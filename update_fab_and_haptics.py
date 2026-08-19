import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Add haptic feedback to bottom navigation items
# First, import LocalHapticFeedback and HapticFeedbackType
if "import androidx.compose.ui.platform.LocalHapticFeedback" not in content:
    content = content.replace("import androidx.compose.ui.platform.LocalContext", "import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalHapticFeedback\nimport androidx.compose.ui.hapticfeedback.HapticFeedbackType")

# Then add val haptic = LocalHapticFeedback.current in AppMainScreen
if "val haptic =" not in content:
    content = content.replace("val navController = rememberNavController()", "val navController = rememberNavController()\n    val haptic = LocalHapticFeedback.current")

# Update onClick to trigger haptic feedback
content = content.replace(
    '''onClick = {
                                    navController.navigate(item.route) {''',
    '''onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate(item.route) {'''
)

# Update FAB color to dark blue and add haptic feedback to it
content = content.replace(
    '''FloatingActionButton(
                        onClick = { showQuickActions = true },
                        containerColor = com.example.ui.theme.AccentPink,''',
    '''FloatingActionButton(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showQuickActions = true 
                        },
                        containerColor = Color(0xFF141C2E), // Dark blue to match reference'''
)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

print("Updated FAB and added haptics")
