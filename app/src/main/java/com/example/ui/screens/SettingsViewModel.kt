package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _commissionPercentage = MutableStateFlow(0.0)
    val commissionPercentage: StateFlow<Double> = _commissionPercentage
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        db.collection("settings").document("general").addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val value = snapshot.getDouble("commissionPercentage") ?: 0.0
                _commissionPercentage.value = value
            } else if (snapshot != null && !snapshot.exists()) {
                // Initialize if not exists
                db.collection("settings").document("general").set(mapOf("commissionPercentage" to 0.0))
            }
        }
    }
    
    fun updateCommissionPercentage(newPercentage: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("settings").document("general")
                    .set(mapOf("commissionPercentage" to newPercentage), com.google.firebase.firestore.SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
