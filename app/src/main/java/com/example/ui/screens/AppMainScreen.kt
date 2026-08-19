package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.ui.DashboardScreen
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

import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.UserModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScreen(
    initialRoute: String? = null,
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
    val haptic = LocalHapticFeedback.current
    var showQuickActions by remember { mutableStateOf(false) }
    var showCreateSerialDialog by remember { mutableStateOf(false) }
    var showCreateUserDialog by remember { mutableStateOf(false) }

    val allBottomItems = listOf(
        NavigationItem("dashboard", "الرئيسية", Icons.Default.Home),
        NavigationItem("clients", "العملاء", Icons.Default.Group),
        NavigationItem("licenses", "التراخيص", Icons.Default.VpnKey),
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
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomItems.forEachIndexed { index, item ->
                            if (index == 2) {
                                // Add a spacer for the center FAB
                                Spacer(modifier = Modifier.width(56.dp))
                            }
                            val selected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?") == item.route } == true
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // FAB
                if (currentUser?.role == "ADMIN" || currentUser?.permissions?.contains("clients") == true) {
                    FloatingActionButton(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showQuickActions = true 
                        },
                        containerColor = androidx.compose.ui.graphics.Color(0xFF141C2E), // Dark blue to match reference
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-16).dp)
                            .size(64.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إجراءات سريعة", modifier = Modifier.size(32.dp))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute ?: "dashboard",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) },
            exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) },
            popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) },
            popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) }
        ) {
            composable("dashboard") { DashboardScreen(navController = navController) }
            composable("clients?tab={tab}", arguments = listOf(androidx.navigation.navArgument("tab") { defaultValue = 0; type = androidx.navigation.NavType.IntType })) { backStackEntry ->
                ClientsScreen(navController = navController, initialTab = backStackEntry.arguments?.getInt("tab") ?: 0)
            }
            composable("client_profile") { ClientProfileScreen(onBackClick = { navController.popBackStack() }) }
            composable("licenses") { LicensesScreen() }
            composable("serials") { SerialsScreen(serialsViewModel) }
            composable("create_serial") { CreateSerialScreen(onBackClick = { navController.popBackStack() }, onActivate = { navController.navigate("clients") { popUpTo("dashboard") } }) }
            composable("subscriptions") { SubscriptionsScreen() }
            composable("sales?filter={filter}", arguments = listOf(androidx.navigation.navArgument("filter") { defaultValue = "all"; type = androidx.navigation.NavType.StringType })) { backStackEntry ->
                SalesScreen(filter = backStackEntry.arguments?.getString("filter") ?: "all")
            }
            composable("commissions") { CommissionsScreen() }
            composable("notifications") { NotificationsScreen(onBackClick = { navController.popBackStack() }) }
            composable("employees") { EmployeesScreen() }
            composable("settings") { SettingsScreen(
                onLogout = { 
                    authViewModel.logout(sharedPref) {
                        onLogout()
                    }
                }, 
                onManageEmployeesClick = { navController.navigate("employees") }, 
                currentUser = currentUser,
                isAdmin = currentUser?.role == "ADMIN"
            ) }
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

