package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.FirebaseApp
import com.example.ui.components.EmergencyAlarmOverlay
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ContactRevealedScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.NameInputScreen
import com.example.ui.screens.OtpVerificationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.RequestSentSuccessScreen
import com.example.ui.screens.RequestStep1Screen
import com.example.ui.screens.RequestStep2Screen
import com.example.ui.screens.RequestStep3Screen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EBloodViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.EmergencyAlarmManager

class MainActivity : ComponentActivity() {

    private val viewModel: EBloodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* permissions granted/denied handled gracefully */ }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                }

                EBloodAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EBloodAppRoot(viewModel: EBloodViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val bottomTab by viewModel.activeBottomTab.collectAsStateWithLifecycle()
    val alarmState by viewModel.activeAlarmState.collectAsStateWithLifecycle()
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()

    val pendingCount = allRequests.count { it.status == "PENDING" }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (currentScreen == Screen.MAIN) {
                NavigationBar(
                    containerColor = DarkSurfaceCard,
                    contentColor = TextSecondary,
                    windowInsets = WindowInsets.navigationBars,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = bottomTab == 0,
                        onClick = { viewModel.setBottomTab(0) },
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Home",
                                fontSize = 11.sp,
                                fontWeight = if (bottomTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = bottomTab == 1,
                        onClick = { viewModel.setBottomTab(1) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingCount > 0) {
                                        Badge(containerColor = CrimsonPrimary) {
                                            Text(
                                                text = "$pendingCount",
                                                color = Color.White,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Mail,
                                    contentDescription = "Inbox",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Inbox",
                                fontSize = 11.sp,
                                fontWeight = if (bottomTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_inbox")
                    )

                    NavigationBarItem(
                        selected = bottomTab == 2,
                        onClick = { viewModel.setBottomTab(2) },
                        icon = {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = "History",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "History",
                                fontSize = 11.sp,
                                fontWeight = if (bottomTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_history")
                    )

                    NavigationBarItem(
                        selected = bottomTab == 3,
                        onClick = { viewModel.setBottomTab(3) },
                        icon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Profile",
                                fontSize = 11.sp,
                                fontWeight = if (bottomTab == 3) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            // Main Screen Routing
            when (currentScreen) {
                Screen.SPLASH -> SplashScreen()
                Screen.AUTH -> AuthScreen(viewModel = viewModel)
                Screen.OTP_VERIFY -> OtpVerificationScreen(viewModel = viewModel)
                Screen.NAME_INPUT -> NameInputScreen(viewModel = viewModel)
                Screen.PROFILE_SETUP -> ProfileSetupScreen(viewModel = viewModel)
                Screen.MAIN -> {
                    when (bottomTab) {
                        0 -> HomeScreen(viewModel = viewModel)
                        1 -> InboxScreen(viewModel = viewModel)
                        2 -> HistoryScreen(viewModel = viewModel)
                        3 -> ProfileScreen(viewModel = viewModel)
                    }
                }
                Screen.CREATE_REQUEST_STEP1 -> RequestStep1Screen(viewModel = viewModel)
                Screen.CREATE_REQUEST_STEP2 -> RequestStep2Screen(viewModel = viewModel)
                Screen.CREATE_REQUEST_STEP3 -> RequestStep3Screen(viewModel = viewModel)
                Screen.REQUEST_SENT_SUCCESS -> RequestSentSuccessScreen(viewModel = viewModel)
                Screen.CONTACT_REVEALED -> ContactRevealedScreen(viewModel = viewModel)
                Screen.ADMIN_LOGIN -> AdminLoginScreen(viewModel = viewModel)
                Screen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel = viewModel)
            }

            // Urgent Alarm & Ringtone Notification Overlay (Requirement 4 & 6)
            EmergencyAlarmOverlay(
                alarmState = alarmState,
                allRequests = allRequests,
                onAcceptRequest = { viewModel.acceptRequest(it) },
                onRejectRequest = { viewModel.rejectRequest(it) },
                onConfirmRequesterAlert = { viewModel.confirmAcceptanceAlertAndRevealContact(it) },
                onDismissAlarm = { viewModel.dismissAlarm() }
            )
        }
    }
}
