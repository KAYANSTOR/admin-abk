import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove the misplaced dialog block
pattern = r'        if \(showPinDialog\).*?Text\("إلغاء"\)\s*\}\s*\}\s*\)\s*\}'
match = re.search(pattern, content, flags=re.DOTALL)
if match:
    dialog_str = match.group(0)
    content = content.replace(dialog_str, "")
    
    # Place it right before the last closing brace before @Composable fun SettingRow
    # Let's find "}\n\n@Composable\nfun SettingRow"
    content = content.replace("}\n\n@Composable\nfun SettingRow", dialog_str + "\n}\n\n@Composable\nfun SettingRow")

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
