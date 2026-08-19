import re
filepath = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Make absolutely sure the braces match.
# Let's count them.

content = content.replace("        }\n        \n        // Teal FAB overlapping the \"مبيعات اليوم\" card\n        FloatingActionButton(\n            onClick = { navController?.navigate(\"sales\") },\n            containerColor = TealGradientStart,\n            contentColor = Color.White,\n            shape = CircleShape,\n            modifier = Modifier\n                .align(Alignment.BottomEnd)\n                .offset(x = 12.dp, y = 20.dp)\n                .size(48.dp)\n        ) {\n            Icon(Icons.Default.Add, contentDescription = \"Add Sales\")\n        }\n    }\n}\n}",
                          "        }\n        \n        // Teal FAB overlapping the \"مبيعات اليوم\" card\n        FloatingActionButton(\n            onClick = { navController?.navigate(\"sales\") },\n            containerColor = TealGradientStart,\n            contentColor = Color.White,\n            shape = CircleShape,\n            modifier = Modifier\n                .align(Alignment.BottomEnd)\n                .offset(x = 12.dp, y = 20.dp)\n                .size(48.dp)\n        ) {\n            Icon(Icons.Default.Add, contentDescription = \"Add Sales\")\n        }\n    }\n}")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
