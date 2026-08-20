import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'navController?.navigate("client_profile")',
    'navController?.navigate("client_profile/${client.id}")'
)

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientsScreen.kt', 'w') as f:
    f.write(content)
