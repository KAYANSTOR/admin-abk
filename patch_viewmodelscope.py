import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientProfileScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import androidx.lifecycle.ViewModel',
    'import androidx.lifecycle.ViewModel\nimport androidx.lifecycle.viewModelScope'
)

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientProfileScreen.kt', 'w') as f:
    f.write(content)
