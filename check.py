with open("/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt", "r", encoding="utf-8") as f:
    lines = f.readlines()
for i, line in enumerate(lines[60:75]):
    print(f"{i+61}: {line}", end='')
