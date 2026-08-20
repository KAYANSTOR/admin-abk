import re
with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('fun DashboardHeader(userName: String, date: String)', 'fun DashboardHeader(userName: String, date: String, navController: NavController? = null)')
content = content.replace('DashboardHeader(userName = userName, date = "يوليو 25, 2026")', 'DashboardHeader(userName = userName, date = "يوليو 25, 2026", navController = navController)')

with open('/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
