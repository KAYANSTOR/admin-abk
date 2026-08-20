package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Percent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import com.example.ui.auth.UserModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),

    onLogout: () -> Unit = {},
    onManageEmployeesClick: () -> Unit = {},
    currentUser: UserModel? = null,
    isAdmin: Boolean = false,
    onChangePin: (String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onToggleNotifications: (Boolean) -> Unit = {}
) {
    val gradientStart = Color(0xFF0F766E) // teal-start (approx)
    val gradientEnd = Color(0xFF7E22CE) // purple-end (approx)
    val brush = Brush.linearGradient(colors = listOf(gradientStart, gradientEnd))
    val brushLight = Brush.linearGradient(colors = listOf(gradientStart.copy(alpha=0.1f), gradientEnd.copy(alpha=0.1f)))

    val commissionPercentage by viewModel.commissionPercentage.collectAsState()
    var showCommissionDialog by remember { mutableStateOf(false) }
    var percentageInput by remember { mutableStateOf("") }
    
    var showPinDialog by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "الإعدادات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (currentUser != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.TopCenter) {
                        // Header Gradient Background
                        Box(modifier = Modifier.fillMaxWidth().height(96.dp).background(brushLight))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(brush)
                                    .padding(4.dp) // for border
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(brush),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = currentUser.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = currentUser.phone,
                                fontSize = 15.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp),
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (currentUser.role == "ADMIN") "مدير النظام" else "موظف",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    

                    if (isAdmin) {
                        SettingRow(
                            icon = Icons.Default.People,
                            title = "إدارة الموظفين",
                            subtitle = "إضافة أو حذف مستخدمين وصلاحياتهم",
                            onClick = onManageEmployeesClick
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                    SettingRow(
                        icon = Icons.Default.Notifications,
                        title = "الإشعارات",
                        subtitle = "التحكم في تنبيهات النظام",
                        trailingContent = {
                            Switch(
                                checked = (currentUser?.notificationsEnabled == true),
                                onCheckedChange = { onToggleNotifications(it) }
                            )
                        },
                        onClick = { onToggleNotifications(!(currentUser?.notificationsEnabled ?: false)) }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingRow(
                        icon = Icons.Default.Lock,
                        title = "الأمان",
                        subtitle = "تغيير رمز الدخول (PIN)",
                        onClick = {
                            oldPin = ""
                            newPin = ""
                            pinError = null
                            showPinDialog = true
                        }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    SettingRow(
                        icon = Icons.Default.Info,
                        title = "المساعدة والدعم",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingRow(
                        icon = Icons.Default.ExitToApp,
                        title = "تسجيل الخروج",
                        destructive = true,
                        onClick = onLogout
                    )
                }
            }
        }
    }

    if (showCommissionDialog) {
        AlertDialog(
            onDismissRequest = { showCommissionDialog = false },
            title = { Text("تعديل نسبة العمولة", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل النسبة المئوية (مثال: 20 لـ 20%):", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = percentageInput,
                        onValueChange = { percentageInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPct = percentageInput.toDoubleOrNull()
                        if (newPct != null) {
                            viewModel.updateCommissionPercentage(newPct)
                            showCommissionDialog = false
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommissionDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("تغيير رمز الدخول (PIN)", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        if (pinError != null) {
                            Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        OutlinedTextField(
                            value = oldPin,
                            onValueChange = { if (it.length <= 4) oldPin = it },
                            label = { Text("الرمز القديم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 4) newPin = it },
                            label = { Text("الرمز الجديد (4 أرقام)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        onChangePin(oldPin, newPin, {
                            showPinDialog = false
                        }, { err ->
                            pinError = err
                        })
                    }) {
                        Text("حفظ")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
}

@Composable


fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    destructive: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val iconBg = if (destructive) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.background
            val iconTint = if (destructive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (destructive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        if (trailingContent != null) {
            trailingContent()
        } else if (!destructive) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}


