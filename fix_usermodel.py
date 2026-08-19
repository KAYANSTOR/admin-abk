import re

filepath = "/app/applet/app/src/main/java/com/example/ui/auth/AuthViewModel.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace(
    'val permissions: List<String> = emptyList() // List of routes e.g. "clients", "licenses"',
    'val permissions: List<String> = emptyList(), // List of routes e.g. "clients", "licenses"\n    val isActive: Boolean = true'
)

# And check for login validation
content = content.replace(
    'if (user.pin == pin) {',
    'if (user.pin == pin) {\n                        if (!user.isActive) {\n                            _errorMsg.value = "هذا الحساب موقوف، الرجاء مراجعة الإدارة"\n                            _isLoading.value = false\n                            return@launch\n                        }'
)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
