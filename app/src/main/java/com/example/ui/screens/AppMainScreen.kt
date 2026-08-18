package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.DashboardScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.UserModel
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScreen(
    clientsViewModel: ClientsViewModel = viewModel(),
    serialsViewModel: SerialsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("KayanPrefs", Context.MODE_PRIVATE) }
    
    val currentUser by authViewModel.currentUser.collectAsState()
    
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            authViewModel.checkAutoLogin(sharedPref) {}
        }
    }

    val navController = rememberNavController()
    var showQuickActions by remember { mutableStateOf(false) }
    var showCreateSerialDialog by remember { mutableStateOf(false) }
    var showCreateUserDialog by remember { mutableStateOf(false) }

    val allBottomItems = listOf(
        NavigationItem("dashboard", "الرئيسية", Icons.Default.Home),
        NavigationItem("clients", "العملاء", Icons.Default.Group),
        NavigationItem("licenses", "التراخيص", Icons.Default.VpnKey),
        NavigationItem("commissions", "التقارير", Icons.Default.BarChart),
        NavigationItem("settings", "الإعدادات", Icons.Default.Settings)
    )
    
    val bottomItems = allBottomItems.filter { item ->
        currentUser?.role == "ADMIN" || item.route == "dashboard" || item.route == "settings" || currentUser?.permissions?.contains(item.route) == true
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var backPressedOnce by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    androidx.activity.compose.BackHandler(enabled = true) {
        if (currentDestination?.route == "dashboard") {
            if (backPressedOnce) {
                (context as? android.app.Activity)?.finish()
            } else {
                backPressedOnce = true
                android.widget.Toast.makeText(context, "اضغط مرة أخرى للخروج من التطبيق", android.widget.Toast.LENGTH_SHORT).show()
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    backPressedOnce = false
                }
            }
        } else {
            navController.popBackStack("dashboard", false)
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                actions = {
                    bottomItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        IconButton(
                            onClick = {
                                navController.navigate(item.route) {
                                    if (item.route == "dashboard") {
                                        popUpTo("dashboard") { inclusive = false }
                                    } else {
                                        popUpTo("dashboard") { saveState = true }
                                        restoreState = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    item.icon, 
                                    contentDescription = item.title,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = item.title, 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser?.role == "ADMIN" || currentUser?.permissions?.contains("clients") == true) {
                FloatingActionButton(
                    onClick = { showQuickActions = true },
                    containerColor = com.example.ui.theme.AccentPink,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إجراءات سريعة")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(navController = navController) }
            composable("clients") { ClientsScreen(navController = navController) }
            composable("client_profile") { ClientProfileScreen(onBackClick = { navController.popBackStack() }) }
            composable("licenses") { LicensesScreen() }
            composable("serials") { SerialsScreen(serialsViewModel) }
            composable("create_serial") { CreateSerialScreen(onBackClick = { navController.popBackStack() }, onActivate = { navController.navigate("clients") { popUpTo("dashboard") } }) }
            composable("subscriptions") { SubscriptionsScreen() }
            composable("sales") { SalesScreen() }
            composable("commissions") { CommissionsScreen() }
            composable("employees") { EmployeesScreen(onBackClick = { navController.popBackStack() }) }
            composable("settings") { SettingsScreen(onLogout = { 
                authViewModel.logout(sharedPref) {
                    onLogout()
                }
            }, onManageEmployeesClick = { navController.navigate("employees") }, isAdmin = currentUser?.role == "ADMIN") }
        }

        if (showQuickActions) {
            ModalBottomSheet(
                onDismissRequest = { showQuickActions = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    Text(
                        text = "إجراءات سريعة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    QuickActionItem(
                        title = "إنشاء سيريال للعميل",
                        icon = Icons.Default.VpnKey,
                        onClick = { 
                            showQuickActions = false
                            navController.navigate("create_serial") 
                        }
                    )
                    if (currentUser?.role == "ADMIN") {
                        QuickActionItem(
                            title = "إنشاء مستخدم مع الصلاحيات",
                            icon = Icons.Default.PersonAdd,
                            onClick = { 
                                showQuickActions = false
                                showCreateUserDialog = true 
                            }
                        )
                    }
                    QuickActionItem(
                        title = "تجميد / حذف اشتراك",
                        icon = Icons.Default.Block,
                        onClick = { 
                            showQuickActions = false
                            navController.navigate("subscriptions") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        isDestructive = true
                    )
                    QuickActionItem(
                        title = "تسوية وتصفية عمولة",
                        icon = Icons.Default.DoneAll,
                        onClick = { 
                            showQuickActions = false
                            navController.navigate("commissions") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
        
        if (showCreateUserDialog) {
            CreateEmployeeDialog(onDismiss = { showCreateUserDialog = false })
        }
        
        if (showCreateSerialDialog) {
            CreateSerialDialog(
                onDismiss = { showCreateSerialDialog = false },
                onCreate = { plan ->
                    serialsViewModel.addSerial(plan)
                    showCreateSerialDialog = false
                    navController.navigate("serials") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun QuickActionItem(title: String, icon: ImageVector, onClick: () -> Unit, isDestructive: Boolean = false) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class NavigationItem(val route: String, val title: String, val icon: ImageVector)

