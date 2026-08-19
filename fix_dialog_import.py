import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/CreateEmployeeDialog.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

if "import androidx.compose.foundation.layout.*" in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll")

content = content.replace(
    'Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {',
    'Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {'
)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

