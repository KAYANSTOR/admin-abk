package com.example.ui.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserModel(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val pin: String = "",
    val role: String = "EMPLOYEE", // "ADMIN" or "EMPLOYEE"
    val permissions: List<String> = emptyList(), // List of routes e.g. "clients", "licenses"
    val isActive: Boolean = true,
    val notificationsEnabled: Boolean = true
)

class AuthViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    fun login(name: String, phone: String, pin: String, sharedPref: SharedPreferences, onSuccess: () -> Unit) {
        if (name.isBlank() || phone.isBlank() || pin.length != 4) {
            _errorMsg.value = "الرجاء تعبئة جميع الحقول بشكل صحيح (كلمة المرور 4 أرقام)"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            try {
                // If the collection is completely empty, auto-create the very first admin for convenience
                val allUsers = db.collection("users").limit(1).get().await()
                if (allUsers.isEmpty) {
                    val admin = UserModel(
                        name = "jar",
                        phone = "773303455",
                        pin = "0808",
                        role = "ADMIN",
                        permissions = listOf("clients", "licenses", "serials", "commissions", "subscriptions", "employees")
                    )
                    db.collection("users").add(admin).await()
                    if (name == admin.name && phone == admin.phone && pin == admin.pin) {
                        sharedPref.edit().putString("userId", "admin_created").apply()
                        _currentUser.value = admin
                        onSuccess()
                        return@launch
                    }
                }

                val querySnapshot = db.collection("users")
                    .whereEqualTo("phone", phone)
                    .whereEqualTo("name", name)
                    .whereEqualTo("pin", pin)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    val user = doc.toObject(UserModel::class.java)?.copy(id = doc.id)
                    _currentUser.value = user
                    sharedPref.edit().putString("userId", doc.id).apply()
                    onSuccess()
                } else {
                    _errorMsg.value = "البيانات المدخلة غير صحيحة"
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _errorMsg.value = "حدث خطأ في الاتصال بقاعدة البيانات"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkAutoLogin(sharedPref: SharedPreferences, onFinished: (Boolean) -> Unit) {
        val userId = sharedPref.getString("userId", null)
        if (userId == null) {
            onFinished(false)
            return
        }
        
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    _currentUser.value = doc.toObject(UserModel::class.java)?.copy(id = doc.id)
                    onFinished(true)
                } else {
                    sharedPref.edit().remove("userId").apply()
                    onFinished(false)
                }
            } catch (e: Exception) {
                onFinished(false)
            }
        }
    }

    fun logout(sharedPref: SharedPreferences, onLogout: () -> Unit) {
        sharedPref.edit().remove("userId").apply()
        _currentUser.value = null
        onLogout()
    }

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
