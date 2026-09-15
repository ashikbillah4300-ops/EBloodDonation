package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppLogo
import com.example.data.model.BloodRequest
import com.example.data.model.DonorUser
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EBloodViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminLoginScreen(viewModel: EBloodViewModel) {
    var username by remember { mutableStateOf("ashikbillah4300@gmail.com") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val loginError by viewModel.adminLoginError.collectAsStateWithLifecycle()
    val attempts by viewModel.adminLoginAttempts.collectAsStateWithLifecycle()
    val appLogoUrl by viewModel.appLogoUrl.collectAsStateWithLifecycle()
    val backendUrl by viewModel.backendServerUrl.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("admin_login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { viewModel.adminLogout() },
                    modifier = Modifier.testTag("admin_back_to_app_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to App",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Admin Official Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color(0xFF6366F1), CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                AppLogo(
                    logoUrl = appLogoUrl,
                    backendBaseUrl = backendUrl,
                    contentDescription = "EBlood Logo",
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "EBlood Secure Admin Panel",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "Authorized Personnel Only",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA5B4FC)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF312E81))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Sign in to Dashboard",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Admin Email or Username",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_username_input"),
                        placeholder = { Text("ashikbillah4300@gmail.com", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF818CF8))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Secure Admin Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input"),
                        placeholder = { Text("••••••••••••", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF818CF8))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.adminLogin(username, password) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (!loginError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = loginError ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrimsonPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.adminLogin(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Authenticate & Enter Dashboard",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security Notes Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF181829),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF272740))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔒", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Security & Protection Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Rate-limiting: Brute force lockout enabled after 5 failed attempts.\n• Password is authenticated securely against backend database.\n• Admin email configured via backend environment.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(viewModel: EBloodViewModel) {
    val activeTab by viewModel.adminActiveTab.collectAsStateWithLifecycle()
    val allDonors by viewModel.allDonorsList.collectAsStateWithLifecycle()
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()
    val allDonations by viewModel.allDonationRecords.collectAsStateWithLifecycle()
    val feedbackMsg by viewModel.adminSettingsSavedFeedback.collectAsStateWithLifecycle()
    val appLogoUrl by viewModel.appLogoUrl.collectAsStateWithLifecycle()
    val backendUrl by viewModel.backendServerUrl.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val totalUsers = allDonors.size
    val totalDonors = allDonors.count { it.isAvailable }
    val totalRequests = allRequests.size
    val pendingRequests = allRequests.count { it.status == "PENDING" }
    val completedRequests = allRequests.count { it.status == "COMPLETED" }
    val totalDonationCount = allDonations.size.coerceAtLeast(12)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("admin_dashboard_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin Navigation Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFF6366F1), CircleShape)
                                .padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AppLogo(
                                logoUrl = appLogoUrl,
                                backendBaseUrl = backendUrl,
                                contentDescription = "EBlood Logo",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EBlood Admin Panel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Live System Synchronized",
                                fontSize = 11.sp,
                                color = SuccessGreen
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Theme Toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("admin_theme_toggle_button")
                        ) {
                            Text(
                                text = if (isDarkMode) "🌙" else "☀️",
                                fontSize = 16.sp
                            )
                        }

                        // Return to App (Logs out so next entry demands password)
                        IconButton(
                            onClick = { viewModel.adminLogout() },
                            modifier = Modifier.testTag("admin_exit_to_app_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit to App",
                                tint = TextSecondary
                            )
                        }
                        // Logout
                        IconButton(
                            onClick = { viewModel.adminLogout() },
                            modifier = Modifier.testTag("admin_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = CrimsonPrimary
                            )
                        }
                    }
                }
            }

            // Tab Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    0 to "📱 App Control",
                    1 to "🌐 Website Control",
                    2 to "📊 Overview",
                    3 to "👥 Users (${totalUsers})",
                    4 to "🩸 Requests (${totalRequests})",
                    5 to "🔄 Server & API"
                )

                tabs.forEach { (index, title) ->
                    val isSelected = activeTab == index
                    val tabColor = when (index) {
                        0 -> if (isSelected) CrimsonPrimary else Color(0xFF334155)
                        1 -> if (isSelected) Color(0xFF0284C7) else Color(0xFF334155)
                        else -> if (isSelected) Color(0xFF4F46E5) else Color(0xFF334155)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tabColor)
                            .clickable { viewModel.adminActiveTab.value = index }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("admin_tab_$index")
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            // Feedback toast message if any
            if (!feedbackMsg.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SuccessGreen.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feedbackMsg ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            // Content according to active tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    0 -> AdminAppControlTab(viewModel = viewModel)
                    1 -> AdminWebsiteControlTab(viewModel = viewModel)
                    2 -> AdminOverviewTab(
                        totalUsers = totalUsers,
                        totalDonors = totalDonors,
                        totalRequests = totalRequests,
                        pendingRequests = pendingRequests,
                        completedRequests = completedRequests,
                        totalDonations = totalDonationCount,
                        recentUsers = allDonors.take(4),
                        recentRequests = allRequests.take(4),
                        onTabSelect = { viewModel.adminActiveTab.value = it }
                    )
                    3 -> AdminUsersTab(viewModel = viewModel, allDonors = allDonors)
                    4 -> AdminRequestsTab(viewModel = viewModel, allRequests = allRequests)
                    5 -> AdminOnlineBackendSyncTab(viewModel = viewModel)
                    else -> AdminAppControlTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminOverviewTab(
    totalUsers: Int,
    totalDonors: Int,
    totalRequests: Int,
    pendingRequests: Int,
    completedRequests: Int,
    totalDonations: Int,
    recentUsers: List<DonorUser>,
    recentRequests: List<BloodRequest>,
    onTabSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "System Overview & Metrics",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Real-time summary of donors, recipients and requests",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Grid (Row 1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminMetricCard(
                title = "Total Users",
                value = "$totalUsers",
                icon = "👥",
                bgColor = Color(0xFF1E1B4B),
                borderColor = Color(0xFF4338CA),
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Active Donors",
                value = "$totalDonors",
                icon = "🩸",
                bgColor = Color(0xFF22111E),
                borderColor = Color(0xFFBE123C),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Metrics Grid (Row 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminMetricCard(
                title = "Total Requests",
                value = "$totalRequests",
                icon = "📋",
                bgColor = Color(0xFF142D3D),
                borderColor = Color(0xFF0284C7),
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Pending SOS",
                value = "$pendingRequests",
                icon = "⏳",
                bgColor = Color(0xFF2B1D0E),
                borderColor = Color(0xFFD97706),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Metrics Grid (Row 3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminMetricCard(
                title = "Completed",
                value = "$completedRequests",
                icon = "✅",
                bgColor = Color(0xFF0F2E1E),
                borderColor = Color(0xFF059669),
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Donations Made",
                value = "$totalDonations",
                icon = "💖",
                bgColor = Color(0xFF281125),
                borderColor = Color(0xFF9333EA),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Users Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Registered Users",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "View All →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF818CF8),
                modifier = Modifier.clickable { onTabSelect(3) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        recentUsers.forEach { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = user.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "${user.phone} • ${user.location}", fontSize = 11.sp, color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(CrimsonPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = user.bloodGroup, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Blood Requests
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Blood Requests",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "View All →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF818CF8),
                modifier = Modifier.clickable { onTabSelect(4) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        recentRequests.forEach { req ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Requester: ${req.requesterName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "${req.requesterPhone} • ${req.location}", fontSize = 11.sp, color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                when (req.status) {
                                    "COMPLETED" -> SuccessGreen.copy(alpha = 0.2f)
                                    "PENDING" -> Color(0xFFF97316).copy(alpha = 0.2f)
                                    else -> Color.Gray.copy(alpha = 0.2f)
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${req.bloodGroup} • ${req.status}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (req.status) {
                                "COMPLETED" -> SuccessGreen
                                "PENDING" -> Color(0xFFF97316)
                                else -> Color.White
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: String,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                Text(text = icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun AdminUsersTab(viewModel: EBloodViewModel, allDonors: List<DonorUser>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("ALL") }
    var inspectingUser by remember { mutableStateOf<DonorUser?>(null) }
    val context = LocalContext.current

    val bloodGroups = listOf("ALL", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    val filteredUsers = allDonors.filter { user ->
        val matchesGroup = selectedGroup == "ALL" || user.bloodGroup.equals(selectedGroup, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                user.name.contains(searchQuery, ignoreCase = true) ||
                user.phone.contains(searchQuery, ignoreCase = true) ||
                user.location.contains(searchQuery, ignoreCase = true)
        matchesGroup && matchesQuery
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "User Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = "Search, filter, view and manage user account access", fontSize = 12.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_user_search_input"),
            placeholder = { Text("Search by name, phone (+880...), or location", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Blood Group Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            bloodGroups.forEach { group ->
                val isSelected = selectedGroup == group
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CrimsonPrimary else Color(0xFF1E293B))
                        .clickable { selectedGroup = group }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = group,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Showing ${filteredUsers.size} users",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        filteredUsers.forEach { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CrimsonPrimary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = user.bloodGroup, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = user.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = user.phone, fontSize = 12.sp, color = Color(0xFF818CF8))
                            }
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(if (user.isEnabled) SuccessGreen.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (user.isEnabled) "ACTIVE" else "DISABLED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (user.isEnabled) SuccessGreen else Color.Red
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "📍 ${user.location} (${user.address})", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "Availability: ${if (user.isAvailable) "Ready to Donate" else "Busy / Unavailable"}",
                        fontSize = 11.sp,
                        color = if (user.isAvailable) SuccessGreen else TextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Inspect Details
                        Button(
                            onClick = { inspectingUser = user },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Details", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Toggle Enable/Disable
                        Button(
                            onClick = {
                                viewModel.toggleDonorAccountStatus(user)
                                Toast.makeText(
                                    context,
                                    "User ${user.name} is now ${if (!user.isEnabled) "Enabled" else "Disabled"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isEnabled) Color(0xFF991B1B) else Color(0xFF065F46)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = if (user.isEnabled) "Disable" else "Enable", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Delete
                        IconButton(
                            onClick = {
                                viewModel.deleteDonorAccount(user.id)
                                Toast.makeText(context, "User deleted from database", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }

    // User details inspection dialog
    if (inspectingUser != null) {
        val u = inspectingUser!!
        val dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(u.createdAt))

        AlertDialog(
            onDismissRequest = { inspectingUser = null },
            title = { Text(text = "User Profile: ${u.name}", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    Text(text = "• Mobile Number: ${u.phone}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "• Blood Group: ${u.bloodGroup}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "• Location: ${u.location}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "• Full Address: ${u.address}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "• Registered At: $dateFormatted", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "• Coordinates: ${u.latitude}, ${u.longitude}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "• Alarm Sound: ${u.alarmSoundEnabled}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "• Account Status: ${if (u.isEnabled) "Active" else "Disabled"}", fontSize = 13.sp, color = if (u.isEnabled) SuccessGreen else Color.Red)
                }
            },
            confirmButton = {
                Button(onClick = { inspectingUser = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AdminRequestsTab(viewModel: EBloodViewModel, allRequests: List<BloodRequest>) {
    var statusFilter by remember { mutableStateOf("ALL") }
    val statuses = listOf("ALL", "PENDING", "ACTIVE", "ACCEPTED", "COMPLETED", "CANCELLED")

    val filtered = allRequests.filter { req ->
        statusFilter == "ALL" || req.status.equals(statusFilter, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Blood Request Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = "Review live patient emergency blood calls and update status", fontSize = 12.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(14.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statuses.forEach { st ->
                val isSelected = statusFilter == st
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFF4F46E5) else Color(0xFF1E293B))
                        .clickable { statusFilter = st }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = st,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        filtered.forEach { req ->
            val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(req.timestamp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(CrimsonPrimary, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = req.bloodGroup, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Req #${req.id}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        // Current Status
                        Box(
                            modifier = Modifier
                                .background(
                                    when (req.status) {
                                        "COMPLETED" -> SuccessGreen.copy(alpha = 0.2f)
                                        "PENDING" -> Color(0xFFF97316).copy(alpha = 0.2f)
                                        "CANCELLED" -> Color.Red.copy(alpha = 0.2f)
                                        else -> Color(0xFF6366F1).copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = req.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = when (req.status) {
                                    "COMPLETED" -> SuccessGreen
                                    "PENDING" -> Color(0xFFF97316)
                                    "CANCELLED" -> Color.Red
                                    else -> Color(0xFF818CF8)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Requester: ${req.requesterName} (${req.requesterPhone})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = "Location: ${req.location}", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "Target Donors: ${req.selectedDonorCount} • Time: $dateFormatted", fontSize = 11.sp, color = TextMuted)

                    if (!req.acceptedDonorName.isNullOrBlank()) {
                        Text(text = "Accepted Donor: ${req.acceptedDonorName} (${req.acceptedDonorPhone})", fontSize = 11.sp, color = SuccessGreen)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Change Status to:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("PENDING", "ACTIVE", "COMPLETED", "CANCELLED").forEach { newStatus ->
                            val isCurrent = req.status == newStatus
                            Button(
                                onClick = { viewModel.updateBloodRequestStatus(req.id, newStatus) },
                                enabled = !isCurrent,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (newStatus) {
                                        "COMPLETED" -> Color(0xFF059669)
                                        "PENDING" -> Color(0xFFD97706)
                                        "CANCELLED" -> Color(0xFFDC2626)
                                        else -> Color(0xFF4F46E5)
                                    },
                                    disabledContainerColor = Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            ) {
                                Text(newStatus.take(4), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAppControlTab(viewModel: EBloodViewModel) {
    val editDonationNum by viewModel.editDonationNumber.collectAsStateWithLifecycle()
    val editDepositMethod by viewModel.editDepositMethod.collectAsStateWithLifecycle()
    val editAppName by viewModel.editAppName.collectAsStateWithLifecycle()
    val editContactNum by viewModel.editContactNumber.collectAsStateWithLifecycle()
    val editSupportNum by viewModel.editSupportNumber.collectAsStateWithLifecycle()
    val editNotice by viewModel.editAppNotice.collectAsStateWithLifecycle()
    val editEmergency by viewModel.editEmergencyNotice.collectAsStateWithLifecycle()
    val editMaintenance by viewModel.editMaintenanceMode.collectAsStateWithLifecycle()
    val editSosAlarm by viewModel.editAppSosAlarmEnabled.collectAsStateWithLifecycle()
    val editLogoUrl by viewModel.editAppLogoUrl.collectAsStateWithLifecycle()
    val backendUrl by viewModel.backendServerUrl.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = CrimsonPrimary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📱", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "১ম সেকশন: অ্যাপ কন্ট্রোল (Mobile App Control)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Mobile App Control Center", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
        Text(
            text = "মোবাইল অ্যাপের লোগো, ডিপোজিট গেটওয়ে ও নম্বর, জরুরি রেড এলার্ট, নোটিশ ও মেইন্টেন্যান্স কন্ট্রোল",
            fontSize = 12.sp,
            color = Color(0xFFA5B4FC)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App & Website Brand Logo Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎨", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App & Website Logo / ব্র্যান্ড লোগো",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF312E81), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Live Sync", fontSize = 10.sp, color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Web Admin Panel থেকে ফাইল আপলোড করলে অথবা নিচে ছবির লিঙ্ক দিলে অ্যাপের সকল জায়গায় স্বয়ংক্রিয়ভাবে নতুন লোগো কার্যকর হবে।",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(12.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AppLogo(
                            logoUrl = editLogoUrl,
                            backendBaseUrl = backendUrl,
                            contentDescription = "Active Logo Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Logo URL / পাথ:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editLogoUrl,
                            onValueChange = { viewModel.editAppLogoUrl.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_edit_logo_url_input"),
                            placeholder = { Text("/logo.svg or https://...", color = TextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "App Display Name / অ্যাপের নাম:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editAppName,
                    onValueChange = { viewModel.editAppName.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { viewModel.editAppLogoUrl.value = "/logo.svg" }
                    ) {
                        Text("Reset Default Logo", fontSize = 11.sp, color = Color(0xFFA5B4FC))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Deposit Gateway & Number Management Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💳", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deposit Gateway & Number (বিকাশ / নগদ / রকেট)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "অ্যাপে ইউজার ডিপোজিট বা ডোনেশনে ক্লিক করলে এখানে সেট করা গেটওয়ে ও নম্বরটি সাথে সাথে প্রদর্শিত হবে।",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "পেমেন্ট / ডিপোজিট মেথড (Payment Method):", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Wallet", "bKash", "Nagad", "Rocket").forEach { method ->
                        val isSelected = editDepositMethod.equals(method, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CrimsonPrimary else Color(0xFF1E293B))
                                .clickable { viewModel.editDepositMethod.value = method }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "ডিপোজিট পার্সোনাল নম্বর (Account Number):", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editDonationNum,
                    onValueChange = { viewModel.editDonationNumber.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_edit_donation_number_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Emergency Crisis Notice Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚨", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emergency Crisis Banner (Red Alert)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7F1D1D), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Critical Notice", fontSize = 10.sp, color = Color(0xFFFECACA), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Leave blank to hide. If set, shown prominently on user home screen in red.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = editEmergency,
                    onValueChange = { viewModel.editEmergencyNotice.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_edit_emergency_notice_input"),
                    placeholder = { Text("e.g. Urgent crisis: Blood needed in Dhaka Medical...", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // General Notice & Contacts Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📢 General App Notice", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = editNotice,
                    onValueChange = { viewModel.editAppNotice.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_edit_app_notice_input"),
                    placeholder = { Text("General app news or updates banner", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "📞 App Support Helpline", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editSupportNum,
                            onValueChange = { viewModel.editSupportNumber.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "📱 Contact Phone Number", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editContactNum,
                            onValueChange = { viewModel.editContactNumber.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Switches (Maintenance Mode & SOS Siren Alarm)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "App Maintenance Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Temporarily notify users of server maintenance", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = editMaintenance,
                        onCheckedChange = { viewModel.editMaintenanceMode.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CrimsonPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Emergency SOS Siren Alarm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "ক্রিটিক্যাল রিকোয়েস্টে অডিও সাইরেন সক্রিয় রাখা", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = editSosAlarm,
                        onCheckedChange = { viewModel.editAppSosAlarmEnabled.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF4444)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button for App Controls
        Button(
            onClick = { viewModel.saveAppSettings() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("admin_save_app_settings_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save Mobile App Controls & Sync Live",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AdminWebsiteControlTab(viewModel: EBloodViewModel) {
    val editWebTitle by viewModel.editWebsiteTitle.collectAsStateWithLifecycle()
    val editWebAnnouncement by viewModel.editWebsiteAnnouncement.collectAsStateWithLifecycle()
    val editWebHeroTitle by viewModel.editWebsiteHeroTitle.collectAsStateWithLifecycle()
    val editWebHeroSubtitle by viewModel.editWebsiteHeroSubtitle.collectAsStateWithLifecycle()
    val editWebApkVersion by viewModel.editWebsiteApkVersion.collectAsStateWithLifecycle()
    val editWebHelpline by viewModel.editWebsiteHelpline.collectAsStateWithLifecycle()
    val editWebEmail by viewModel.editWebsiteSupportEmail.collectAsStateWithLifecycle()
    val editWebShowDonors by viewModel.editWebsiteShowPublicDonors.collectAsStateWithLifecycle()
    val editWebMaintenance by viewModel.editWebsiteMaintenance.collectAsStateWithLifecycle()
    val editWebFooterText by viewModel.editWebsiteFooterText.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = Color(0xFF0284C7).copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🌐", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "২য় সেকশন: ওয়েবসাইট কন্ট্রোল (Website Control)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Website & Portal Control Center", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
        Text(
            text = "পাবলিক ল্যান্ডিং ওয়েবসাইট, শিরোনাম, ডাউনলোড ব্যাজ, রক্তদাতা ডিরেক্টরি ও কনটেন্ট পরিচালনা",
            fontSize = 12.sp,
            color = Color(0xFF38BDF8).copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Website Title & Announcement Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌐", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Website Title & Top Announcement",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ব্রাউজার ট্যাব টাইটেল এবং ওয়েবসাইটের মূল হেডার টাইটেল",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "ওয়েবসাইট ব্রাউজার টাইটেল (Browser Tab Title):", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editWebTitle,
                    onValueChange = { viewModel.editWebsiteTitle.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "ওয়েবসাইটের শীর্ষ এনাউন্সমেন্ট ব্যাজ (Top Announcement Badge):", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editWebAnnouncement,
                    onValueChange = { viewModel.editWebsiteAnnouncement.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Hero Headlines Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✨", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hero Banner Headlines / মূল ব্যানার ও বার্তা",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "মূল শিরোনাম (Hero Main Headline):", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editWebHeroTitle,
                    onValueChange = { viewModel.editWebsiteHeroTitle.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "সাব-টাইটেল / বিস্তারিত বার্তা (Hero Subtitle):", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editWebHeroSubtitle,
                    onValueChange = { viewModel.editWebsiteHeroSubtitle.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. APK Version & Public Donors Directory
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "ওয়েবসাইটের প্রদর্শিত APK সংস্করণ (APK Version Badge):", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editWebApkVersion,
                    onValueChange = { viewModel.editWebsiteApkVersion.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "ওয়েবসাইটে রক্তদাতা তালিকা প্রদর্শন", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "পাবলিক ওয়েবসাইটে লাইভ ডোনার ডিরেক্টরি প্রদর্শন করার সুইচ", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = editWebShowDonors,
                        onCheckedChange = { viewModel.editWebsiteShowPublicDonors.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Helpline, Support Email & Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "📞 ওয়েবসাইটের পাবলিক হটলাইন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editWebHelpline,
                            onValueChange = { viewModel.editWebsiteHelpline.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "✉️ সাপোর্ট ইমেইল", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editWebEmail,
                            onValueChange = { viewModel.editWebsiteSupportEmail.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "ওয়েবসাইট মেইন্টেন্যান্স মোড", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "সক্রিয় করলে ওয়েবসাইটে মেইন্টেন্যান্স স্ক্রিন ভেসে উঠবে", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = editWebMaintenance,
                        onCheckedChange = { viewModel.editWebsiteMaintenance.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF59E0B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "ওয়েবসাইট ফুটার কপিরাইট টেক্সট:", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editWebFooterText,
                    onValueChange = { viewModel.editWebsiteFooterText.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button for Website Control
        Button(
            onClick = { viewModel.saveWebsiteSettings() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("admin_save_website_settings_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save Website Controls & Publish Live",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AdminSettingsTab(viewModel: EBloodViewModel) {
    AdminAppControlTab(viewModel = viewModel)
}

@Composable
fun AdminOnlineBackendSyncTab(viewModel: EBloodViewModel) {
    val currentUrl by viewModel.backendServerUrl.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val testResult by viewModel.connectionTestResult.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isOnlineSyncing.collectAsStateWithLifecycle()
    val syncFeedback by viewModel.onlineSyncFeedback.collectAsStateWithLifecycle()

    var showGuide by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🌐 Online Backend & Cloud Sync",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Connect your mobile app to an online cloud server, test latency, and sync live without rebuilding the APK.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Connection Status Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = when {
                isTesting -> Color(0xFF2B200E)
                testResult?.success == true -> Color(0xFF0F2E1E)
                testResult?.success == false -> Color(0xFF2D1419)
                else -> Color(0xFF1E293B)
            },
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                when {
                    isTesting -> Color(0xFFF59E0B)
                    testResult?.success == true -> Color(0xFF10B981)
                    testResult?.success == false -> Color(0xFFF43F5E)
                    else -> Color(0xFF334155)
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFFF59E0B),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = when {
                                    testResult?.success == true -> "🟢"
                                    testResult?.success == false -> "🔴"
                                    else -> "⚪"
                                },
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                isTesting -> "Pinging Server..."
                                testResult?.success == true -> "ONLINE CONNECTED"
                                testResult?.success == false -> "OFFLINE / DISCONNECTED"
                                else -> "Server Not Tested"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isTesting -> Color(0xFFF59E0B)
                                testResult?.success == true -> Color(0xFF34D399)
                                testResult?.success == false -> Color(0xFFFB7185)
                                else -> TextPrimary
                            }
                        )
                    }

                    if (testResult != null && !isTesting) {
                        Surface(
                            color = if (testResult?.success == true) Color(0xFF064E3B) else Color(0xFF881337),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${testResult?.latencyMs ?: 0} ms",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        isTesting -> "Sending ping to /api/health and measuring HTTP response time..."
                        testResult != null -> testResult?.message ?: ""
                        else -> "Enter your cloud server URL below and tap 'Test Connection' to check status."
                    },
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Backend URL Input Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Online Backend Server URL",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Supported: https:// (Render, Railway, VPS) or http:// (LAN/Emulator)",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentUrl,
                    onValueChange = { viewModel.backendServerUrl.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_backend_url_input"),
                    singleLine = true,
                    placeholder = { Text("https://your-backend.onrender.com", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Presets
                Text(text = "Quick URL Presets:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "🚀 Render Cloud" to "https://eblooddonation.onrender.com",
                        "💻 Android Emulator" to "http://10.0.2.2:5000",
                        "🏠 Localhost" to "http://localhost:5000"
                    )

                    presets.forEach { (label, url) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setQuickBackendUrl(url)
                                },
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF93C5FD)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.testBackendConnection() },
                        enabled = !isTesting,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_test_connection_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("⚡ Test Connection", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.syncFromOnlineBackend() },
                        enabled = !isSyncing,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_sync_backend_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Syncing...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("🔄 Sync Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sync feedback toast if any
                if (!syncFeedback.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF064E3B),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = syncFeedback ?: "",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deployment & Setup Guide Accordion
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGuide = !showGuide },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How to Host & Connect Online (ফ্রি গাইড)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Text(
                        text = if (showGuide) "▲ Hide" else "▼ Show",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                if (showGuide) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "অনলাইনে ব্যাকএন্ড ফ্রি চালু ও কানেক্ট করার ৩টি ধাপ:\n\n" +
                                "১. ফ্রি PostgreSQL ডাটাবেজ:\n" +
                                "• Neon.tech অথবা Supabase-এ ফ্রি একাউন্ট খুলে Connection String (DATABASE_URL) কপি করুন।\n\n" +
                                "২. Render.com এ ২ মিনিটে হোস্ট করুন:\n" +
                                "• Render.com এ গিয়ে 'New Web Service' চাপুন এবং রিপোজিটোরি কানেক্ট করুন।\n" +
                                "• Root Directory দিন: backend\n" +
                                "• Build Command: npm install\n" +
                                "• Start Command: node src/server.js\n" +
                                "• Environment Variables এ DATABASE_URL পেস্ট করে Deploy চাপুন।\n\n" +
                                "৩. অ্যাপে লিংক পেস্ট করুন:\n" +
                                "• Render থেকে পাওয়া HTTPS লিংকটি কপি করে উপরের বক্সে পেস্ট করুন।\n" +
                                "• 'Test Connection' চাপুন এবং এরপর 'Sync Now' চাপলেই অনলাইন সার্ভারের সাথে মোবাইল অ্যাপ সম্পূর্ণ কানেক্টেড হয়ে যাবে!\n\n" +
                                "📌 নোট: Render-এর ফ্রি সার্ভার ১৫ মিনিট ইনঅ্যাক্টিভ থাকলে স্লিপ মোডে যায়। প্রথমবার টেস্ট করতে ৩০-৪০ সেকেন্ড লাগতে পারে।",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Architecture Diagram
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "System Architecture Diagram", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF020617), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "       ┌────────────────────────────────┐\n" +
                                "       │  Cloud PostgreSQL (Neon/Supabase)│\n" +
                                "       │   Users • BloodRequests • Settings │\n" +
                                "       └──────────────┬─────────────────┘\n" +
                                "                      │\n" +
                                "              Express API (Render)\n" +
                                "                      │\n" +
                                "       ┌──────────────┴───────────────┐\n" +
                                "       │                              │\n" +
                                " ┌─────▼────────┐              ┌──────▼───────┐\n" +
                                " │  Web Admin   │              │  Mobile App  │\n" +
                                " │  Dashboard   │◄────────────►│EBloodDonation│\n" +
                                " └──────────────┘              └──────────────┘",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFF67E8F9),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
