package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSerialScreen(onBackClick: () -> Unit, onActivate: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<ClientModel?>(null) }
    var searchPerformed by remember { mutableStateOf(false) }
    
    var selectedPeriod by remember { mutableStateOf(1) }
    val periods = listOf(1 to "شهر واحد", 3 to "3 أشهر", 6 to "6 أشهر", 12 to "سنة واحدة")
    
    var expanded by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إنشاء سيريال وتفعيل", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("البحث عن عميل (الفترة التجريبية)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("رقم الهاتف أو Device ID") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                )
            )
            
            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        scope.launch {
                            isSearching = true
                            searchPerformed = false
                            
                            try {
                                val querySnapshot = db.collection("clients")
                                    .whereEqualTo("deviceId", searchQuery)
                                    .get()
                                    .await()
                                    
                                if (!querySnapshot.isEmpty) {
                                    val doc = querySnapshot.documents[0]
                                    searchResult = doc.toObject(ClientModel::class.java)?.copy(id = doc.id)
                                } else {
                                    searchResult = null
                                }
                            } catch (e: Exception) {
                                searchResult = null
                            }
                            
                            isSearching = false
                            searchPerformed = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("بحث")
                }
            }
            
            if (searchPerformed && searchResult == null) {
                Text(
                    "لم يتم العثور على عميل بهذا المعرف.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (searchResult != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text("بيانات العميل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(searchResult!!.name, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Router, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(searchResult!!.networkName)
                        }
                    }
                }
                
                Text("مدة الاشتراك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = periods.find { it.first == selectedPeriod }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        periods.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.second) },
                                onClick = {
                                    selectedPeriod = period.first
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                searchResult?.let { client ->
                                    db.collection("clients").document(client.id)
                                        .update("isActive", true)
                                        .await()
                                    onActivate()
                                }
                            } catch (e: Exception) {
                                // Handle error
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.SuccessGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("توليد السيريال وتفعيل العميل", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
