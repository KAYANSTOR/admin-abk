package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.ui.theme.warning

data class DashboardMetrics(
    val usersCount: String = "1,284",
    val subscriptionsCount: String = "934",
    val totalSales: String = "8.4M",
    val dueCommissions: String = "642K"
)

class DashboardViewModel : ViewModel() {
    private val _metrics = MutableStateFlow(DashboardMetrics())
    val metrics: StateFlow<DashboardMetrics> = _metrics.asStateFlow()
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val metrics by viewModel.metrics.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
                // Header Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "المستخدمون",
                            value = metrics.usersCount,
                            icon = Icons.Default.Group,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "الاشتراكات",
                            value = metrics.subscriptionsCount,
                            icon = Icons.Default.CardMembership,
                            iconTint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "المبيعات",
                            value = metrics.totalSales,
                            icon = Icons.Default.ShoppingCart,
                            iconTint = MaterialTheme.colorScheme.tertiary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "العمولات المستحقة",
                            value = metrics.dueCommissions,
                            icon = Icons.Default.Money,
                            iconTint = MaterialTheme.colorScheme.warning
                        )
                    }
                }

                // Sales Analytics Placeholder
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("أداء المبيعات", "تحليل المبيعات اليومية والشهرية")
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "رسم بياني للمبيعات (قريباً)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Commission Overview
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("عمولات المنصة", "مستحقة هذا الشهر")
                }
                item {
                    CommissionOverviewCard()
                }

                // Expiring Licenses Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("تنبيهات النظام", "إشعارات هامة تتطلب انتباهك")
                }
                
                item {
                    LicenseItem(
                        name = "سارة الحربي",
                        expireDate = "2026/08/28",
                        initial = "س"
                    )
                }
                
                item {
                    LicenseItem(
                        name = "خالد المطيري",
                        expireDate = "2026/08/24",
                        initial = "خ"
                    )
                }

                // System Status Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("حالة المنظومة", "مؤشرات الاتصال المستقبلية")
                }
                
                item {
                    SystemStatusCard()
                }

                // Recent Operations Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader("آخر العمليات", "Audit log — Demo")
                        TextButton(onClick = { /* TODO */ }) {
                            Text("عرض السجل")
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                        }
                    }
                }
                
                item {
                    RecentOperationsCard()
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, iconTint: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LicenseItem(name: String, expireDate: String, initial: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ينتهي في $expireDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "تحذير",
                tint = MaterialTheme.colorScheme.warning
            )
        }
    }
}

@Composable
fun CommissionOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "642,500 ريال",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = 0.4f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "المدفوع: 257,000",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المتبقي: 385,500",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
fun SystemStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatusRow(title = "Admin API", status = "Demo", statusColor = MaterialTheme.colorScheme.secondary)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
            StatusRow(title = "License Engine", status = "جاهز", statusColor = MaterialTheme.colorScheme.secondary)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
            StatusRow(title = "Android Sync", status = "بانتظار العقد", statusColor = MaterialTheme.colorScheme.warning)
        }
    }
}

@Composable
fun StatusRow(title: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RecentOperationsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OperationItem(title = "تم تفعيل ترخيص أحمد العتيبي", time = "منذ 4 دقائق · Admin", isSuccess = true)
            OperationItem(title = "تم تمديد اشتراك شبكة جدة", time = "منذ ساعتين · Admin", isSuccess = false)
            OperationItem(title = "إنشاء سيريال جديد", time = "أمس · Admin", isSuccess = false)
        }
    }
}

@Composable
fun OperationItem(title: String, time: String, isSuccess: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isSuccess) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
