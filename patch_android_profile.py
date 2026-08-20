import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientProfileScreen.kt', 'r') as f:
    content = f.read()

# Add field to model
if "val commissionPercentage: Double" not in content:
    content = content.replace(
        "val pendingCommissions: String",
        "val pendingCommissions: String,\n    val commissionPercentage: Double = 0.0"
    )

# Update mock
if "commissionPercentage =" not in content:
    content = content.replace(
        'pendingCommissions = "2,310 ر.ي"',
        'pendingCommissions = "2,310 ر.ي",\n            commissionPercentage = 15.0'
    )

with open('/app/applet/app/src/main/java/com/example/ui/screens/ClientProfileScreen.kt', 'w') as f:
    f.write(content)
