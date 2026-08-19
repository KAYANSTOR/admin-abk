with open("/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace(").kotlinx.coroutines.tasks.await()", ").await()")

with open("/app/applet/app/src/main/java/com/example/ui/DashboardScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)

print("Await fixed")
