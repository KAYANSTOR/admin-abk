with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Try to remove the SettingRow for commission
start_str = "if (isAdmin) {"
end_str = "                        SettingRow("
# Actually it's easier to use regex or string replace.
row = """                    if (isAdmin) {
                        SettingRow(
                            icon = Icons.Default.Percent,
                            title = "نسبة العمولة",
                            subtitle = "النسبة الحالية: ${commissionPercentage}%",
                            onClick = { 
                                percentageInput = commissionPercentage.toString()
                                showCommissionDialog = true 
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }"""

if row in content:
    content = content.replace(row, "")

with open('/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
