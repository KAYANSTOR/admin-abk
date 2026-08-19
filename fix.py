import re

# Fix AppMainScreen.kt
f_app = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"
with open(f_app, "r", encoding="utf-8") as f:
    c_app = f.read()

# Replace the messy list with the correct one
correct_list = '''val allBottomItems = listOf(
        NavigationItem("dashboard", "الرئيسية", Icons.Default.Home),
        NavigationItem("clients", "العملاء", Icons.Default.Group),
        NavigationItem("licenses", "التراخيص", Icons.Default.VpnKey),
        NavigationItem("settings", "الإعدادات", Icons.Default.Settings)
    )'''

# We know it starts at val allBottomItems and ends at the second )
c_app = re.sub(r'val allBottomItems = listOf\(.*?\)\s*\)', correct_list, c_app, flags=re.DOTALL)

with open(f_app, "w", encoding="utf-8") as f:
    f.write(c_app)

# Fix DashboardScreen.kt
f_dash = "/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt"
with open(f_dash, "r", encoding="utf-8") as f:
    c_dash = f.read()

c_dash = "import kotlinx.coroutines.tasks.await\n" + c_dash

with open(f_dash, "w", encoding="utf-8") as f:
    f.write(c_dash)

print("Fixed")
