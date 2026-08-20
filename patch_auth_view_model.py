import re

with open('/app/applet/app/src/main/java/com/example/ui/auth/AuthViewModel.kt', 'r') as f:
    content = f.read()

# Modify UserModel
if "val notificationsEnabled: Boolean" not in content:
    content = content.replace(
        "val isActive: Boolean = true",
        "val isActive: Boolean = true,\n    val notificationsEnabled: Boolean = true"
    )

# Add new methods to AuthViewModel
new_methods = """
    fun changePin(oldPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onError("المستخدم غير مسجل الدخول")
            return
        }
        if (user.pin != oldPin) {
            onError("رمز الدخول الحالي غير صحيح")
            return
        }
        if (newPin.length != 4) {
            onError("رمز الدخول الجديد يجب أن يكون 4 أرقام")
            return
        }
        
        viewModelScope.launch {
            try {
                db.collection("users").document(user.id).update("pin", newPin).await()
                _currentUser.value = user.copy(pin = newPin)
                onSuccess()
            } catch (e: Exception) {
                onError("حدث خطأ أثناء تحديث رمز الدخول")
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(user.id).update("notificationsEnabled", enabled).await()
                _currentUser.value = user.copy(notificationsEnabled = enabled)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error toggling notifications", e)
            }
        }
    }
}
"""

content = re.sub(r'}\s*$', new_methods, content)

with open('/app/applet/app/src/main/java/com/example/ui/auth/AuthViewModel.kt', 'w') as f:
    f.write(content)
