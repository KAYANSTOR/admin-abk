import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# 1. Update signature
content = re.sub(
    r'isAdmin: Boolean = false\s*\) \{',
    'isAdmin: Boolean = false,\n    onChangePin: (String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },\n    onToggleNotifications: (Boolean) -> Unit = {}\n) {',
    content
)

# 2. Fix the dangling dialog at the end
# The dialog is inside the file but outside the SettingsScreen function.
# Let's find the Dialog string and move it inside the SettingsScreen.
# Wait, looking at the previous patch: I used `re.sub(r'}\s*$', pin_dialog, content)`
# Since `content` already has the dialog at the end, I need to strip it, and put it inside `SettingsScreen`.
# Let's just grab the whole dialog, remove it, and insert it before the closing brace of SettingsScreen.

dialog_pattern = r'        if \(showPinDialog\).*?Text\("إلغاء"\)\s*\}\s*\}\s*\)\s*\}\s*\}'
dialog_match = re.search(dialog_pattern, content, flags=re.DOTALL)
if dialog_match:
    dialog_text = dialog_match.group(0)
    content = content.replace(dialog_text, "")
    # Now find the end of SettingsScreen, which is before the `fun SettingRow` function
    content = content.replace("fun SettingRow(", dialog_text[:-1] + "\nfun SettingRow(")

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)

