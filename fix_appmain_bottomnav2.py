import re
filepath = "/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Make sure to replace any leftover bottomBar if regex didn't catch it correctly, but usually it does.
# Wait, my regex missed because of newlines perhaps? Let's check if 'bottomBar = {' is in the file multiple times.
if content.count('bottomBar = {') > 1:
    print("Regex failed to replace properly or duplicated.")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
