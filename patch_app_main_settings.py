import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt', 'r') as f:
    content = f.read()

old_settings_call = """            composable("settings") { SettingsScreen(
                onLogout = { 
                    authViewModel.logout(sharedPref) {
                        onLogout()
                    }
                }, 
                onManageEmployeesClick = { navController.navigate("employees") }, 
                currentUser = currentUser,
                isAdmin = currentUser?.role == "ADMIN"
            ) }"""

new_settings_call = """            composable("settings") { SettingsScreen(
                onLogout = { 
                    authViewModel.logout(sharedPref) {
                        onLogout()
                    }
                }, 
                onManageEmployeesClick = { navController.navigate("employees") }, 
                currentUser = currentUser,
                isAdmin = currentUser?.role == "ADMIN",
                onChangePin = { oldPin, newPin, onSuccess, onError ->
                    authViewModel.changePin(oldPin, newPin, onSuccess, onError)
                },
                onToggleNotifications = { enabled ->
                    authViewModel.toggleNotifications(enabled)
                }
            ) }"""

content = content.replace(old_settings_call, new_settings_call)

with open('/app/applet/app/src/main/java/com/example/ui/screens/AppMainScreen.kt', 'w') as f:
    f.write(content)
