import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("import com.example.ui.DashboardScreen", "")
content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport com.example.ui.DashboardScreen")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
