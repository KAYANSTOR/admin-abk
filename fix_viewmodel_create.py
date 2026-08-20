import re

filepath = "/app/applet/app/src/main/java/com/example/ui/screens/EmployeesScreen.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

create_func = """
    fun createUser(name: String, phone: String, pin: String, role: String, permissions: List<String>) {
        val newUser = mapOf(
            "name" to name,
            "phone" to phone,
            "pin" to pin,
            "role" to role,
            "permissions" to permissions,
            "isActive" to true
        )
        db.collection("users").add(newUser)
    }
"""

if "fun createUser" not in content:
    content = content.replace("    fun deleteUser(userId: String) {\n        db.collection(\"users\").document(userId).delete()\n    }", f"    fun deleteUser(userId: String) {{\n        db.collection(\"users\").document(userId).delete()\n    }}\n{create_func}")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

