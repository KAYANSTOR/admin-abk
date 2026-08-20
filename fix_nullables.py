import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('currentUser.notificationsEnabled', '(currentUser?.notificationsEnabled == true)')
content = content.replace('!(currentUser?.notificationsEnabled == true)', '!(currentUser?.notificationsEnabled ?: false)')

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
