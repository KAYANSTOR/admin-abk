with open("/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Let's find the start of allBottomItems and the end of it.
start_idx = content.find("val allBottomItems = listOf")
if start_idx != -1:
    end_idx = content.find("val bottomItems =", start_idx)
    
    if end_idx != -1:
        correct_list = '''val allBottomItems = listOf(
        NavigationItem("dashboard", "الرئيسية", Icons.Default.Home),
        NavigationItem("clients", "العملاء", Icons.Default.Group),
        NavigationItem("licenses", "التراخيص", Icons.Default.VpnKey),
        NavigationItem("settings", "الإعدادات", Icons.Default.Settings)
    )
    
    '''
        content = content[:start_idx] + correct_list + content[end_idx:]

with open("/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)

# For DashboardScreen.kt
with open("/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Fix the import issue by removing it from the top
content = content.replace("import kotlinx.coroutines.tasks.await\npackage com.example.ui", "package com.example.ui\nimport kotlinx.coroutines.tasks.await")
content = content.replace("package com.example.ui\nimport kotlinx.coroutines.tasks.await", "package com.example.ui\nimport kotlinx.coroutines.tasks.await")

if "import kotlinx.coroutines.tasks.await" not in content:
    content = content.replace("package com.example.ui", "package com.example.ui\nimport kotlinx.coroutines.tasks.await")
else:
    # If it was incorrectly at the beginning (before package)
    if content.startswith("import kotlinx.coroutines.tasks.await"):
        content = content.replace("import kotlinx.coroutines.tasks.await\n", "", 1)
        content = content.replace("package com.example.ui", "package com.example.ui\nimport kotlinx.coroutines.tasks.await")

with open("/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed2")
