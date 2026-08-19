import re
filepath = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

icons_to_import = [
    "import androidx.compose.material.icons.filled.Home",
    "import androidx.compose.material.icons.filled.People",
    "import androidx.compose.material.icons.filled.Settings",
    "import androidx.compose.material.icons.filled.VpnKey",
    "import androidx.compose.material.icons.filled.Add",
    "import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp",
]

for imp in icons_to_import:
    if imp not in content:
        content = content.replace("import androidx.compose.material.icons.Icons", imp + "\nimport androidx.compose.material.icons.Icons")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
