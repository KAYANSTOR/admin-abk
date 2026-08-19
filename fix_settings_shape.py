import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

if "import androidx.compose.foundation.shape.RoundedCornerShape" not in content:
    content = content.replace("import androidx.compose.foundation.shape.CircleShape", "import androidx.compose.foundation.shape.CircleShape\nimport androidx.compose.foundation.shape.RoundedCornerShape")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

