package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonPrimaryDark
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.EBloodViewModel
import com.example.ui.viewmodel.InboxTab

@Composable
fun HomeScreen(viewModel: EBloodViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val requests by viewModel.allRequests.collectAsStateWithLifecycle()
    val showSupport by viewModel.showSupportDialog.collectAsStateWithLifecycle()
    val donationNum by viewModel.donationNumber.collectAsStateWithLifecycle()
    val emergencyNoticeText by viewModel.emergencyNotice.collectAsStateWithLifecycle()
    val appNoticeText by viewModel.appNotice.collectAsStateWithLifecycle()

    val pendingCount = requests.count { it.status == "PENDING" }
    val completedCount = requests.count { it.status == "COMPLETED" }
    val isAvailable = user?.isAvailable ?: true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Hello, ${user?.name ?: "Ashik"} 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ready to save a life today?",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Blood group badge (green circle like in video)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(SuccessGreen, CircleShape)
                            .testTag("blood_group_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.bloodGroup ?: "O+",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.setBottomTab(3) }, // Go to Profile/Settings
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Emergency Notice from Admin (if set)
            if (emergencyNoticeText.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = Color(0xFF450A0A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🚨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Emergency Announcement",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFCA5A5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = emergencyNoticeText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Dynamic App Notice (General Announcement)
            if (appNoticeText.isNotBlank() && emergencyNoticeText.isBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFF131D2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📢", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = appNoticeText,
                            fontSize = 12.sp,
                            color = Color(0xFFBAE6FD),
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // SOS Emergency Request Blood Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { viewModel.startCreateBloodRequest() }
                    .testTag("request_blood_banner"),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFE11D48), Color(0xFFBE123C))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "SOS",
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Request Blood",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Find donors near you instantly",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row: 3 cards (Donations, Pending, Available)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Donations
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$completedCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Donations",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Pending
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$pendingCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pending",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Available
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.toggleAvailability(!isAvailable) }
                        .testTag("toggle_available_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAvailable) SuccessGreen.copy(alpha = 0.5f) else DarkSurfaceBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isAvailable) SuccessGreen else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAvailable) "ON" else "OFF",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAvailable) SuccessGreen else TextMuted
                        )
                        Text(
                            text = "Available",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Quick Actions Header
            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 Grid of Quick Actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Inbox
                    QuickActionCard(
                        icon = Icons.Default.Mail,
                        iconTint = InfoBlue,
                        title = "Inbox",
                        subtitle = "$pendingCount pending",
                        modifier = Modifier.weight(1f).testTag("quick_action_inbox"),
                        onClick = {
                            viewModel.setBottomTab(1)
                            viewModel.activeInboxTab.value = InboxTab.PENDING
                        }
                    )

                    // My Requests
                    QuickActionCard(
                        icon = Icons.Default.Assignment,
                        iconTint = Color(0xFFF97316),
                        title = "My Requests",
                        subtitle = "Track requests",
                        modifier = Modifier.weight(1f).testTag("quick_action_requests"),
                        onClick = {
                            viewModel.setBottomTab(1)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Donations
                    QuickActionCard(
                        icon = Icons.Default.Folder,
                        iconTint = WarningAmber,
                        title = "Donations",
                        subtitle = "View history",
                        modifier = Modifier.weight(1f).testTag("quick_action_donations"),
                        onClick = {
                            viewModel.setBottomTab(2)
                        }
                    )

                    // Profile
                    QuickActionCard(
                        icon = Icons.Default.Person,
                        iconTint = Color(0xFFA855F7),
                        title = "Profile",
                        subtitle = "Edit details",
                        modifier = Modifier.weight(1f).testTag("quick_action_profile"),
                        onClick = {
                            viewModel.setBottomTab(3)
                        }
                    )
                }

                // Support / Donation button card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.showSupportDialog.value = true }
                        .testTag("quick_action_support_donation"),
                    color = DarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💖", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Donation / অনুদান",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Support Platform",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // How It Works section
            Text(
                text = "How It Works",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceCard, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HowItWorksStepItem(
                    stepNumber = "1",
                    stepColor = CrimsonPrimary,
                    title = "Create Request",
                    subtitle = "Select blood group & location"
                )
                HowItWorksStepItem(
                    stepNumber = "2",
                    stepColor = CrimsonPrimary,
                    title = "Donors Alerted",
                    subtitle = "Nearby donors get an urgent 3-minute alarm"
                )
                HowItWorksStepItem(
                    stepNumber = "3",
                    stepColor = CrimsonPrimary,
                    title = "Donor Accepts",
                    subtitle = "You receive a 30-second confirmation alert"
                )
                HowItWorksStepItem(
                    stepNumber = "4",
                    stepColor = CrimsonPrimary,
                    title = "Contact & Donate",
                    subtitle = "Donor's number revealed after you confirm"
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Support Platform Dialog (bKash send money)
    if (showSupport) {
        SupportDonationDialog(
            onDismiss = { viewModel.showSupportDialog.value = false },
            donationNumber = donationNum
        )
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun HowItWorksStepItem(
    stepNumber: String,
    stepColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(stepColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SupportDonationDialog(onDismiss: () -> Unit, donationNumber: String = "01969114300") {
    val context = LocalContext.current
    var isBengali by remember { mutableStateOf(true) }
    val bkashNumber = donationNumber

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💖", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Support EBloodDonation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "জীবন রক্ষাকারী অ্যাপে অনুদান করুন",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // bKash Send Money Box
                Text(
                    text = "📱 bKash Send Money / বিকাশ অনুদান নম্বর:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF97316)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF22111E), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "bKash (বিকাশ)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Personal (Send Money)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = bkashNumber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF43F5E)
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("bKash Number", bkashNumber)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Number Copied: $bkashNumber", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            modifier = Modifier.testTag("copy_bkash_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Language toggle tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBengali) CrimsonPrimary else Color.Transparent)
                            .clickable { isBengali = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "বাংলা বিবরণ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBengali) Color.White else TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isBengali) CrimsonPrimary else Color.Transparent)
                            .clickable { isBengali = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "English Info",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isBengali) Color.White else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isBengali) {
                    Text(
                        text = "🌟 কেন অনুদান করবেন এবং কীভাবে সাহায্য হবে?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "EBloodDonation একটি সম্পূর্ণ ফ্রি ও অলাভজনক জীবনরক্ষাকারী উদ্যোগ। প্রতিদিন শত শত মানুষ রক্তের জন্য হাহাকার করে। আপনার সামান্য অনুদান দিয়ে আমরা যা করি:\n\n" +
                                "• ২৪/৭ ক্লাউড সার্ভার: ডাটাবেজ ও সার্বিক সার্ভার খরচ যাতে রক্তদাতারা যেকোনো সময় দ্রুত সাড়া পায়।\n" +
                                "• জরুরি অ্যালার্ট গেটওয়ে: রক্তের প্রয়োজনে সাথে সাথে রক্তদাতাদের ৩-মিনিট টানা অ্যালার্ম বাজিয়ে সতর্ক করা।\n" +
                                "• ফ্রি ও বিজ্ঞাপনমুক্ত সেবা: কোনো ফি বা বিজ্ঞাপন ছাড়াই সবসময় চালু রাখা।",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "📌 অনুদান পাঠানোর নিয়ম (Instructions):\n" +
                                "১. উপরে বিকাশ নম্বরের পাশের Copy বাটনে চাপ দিন।\n" +
                                "২. আপনার বিকাশ বা নগদ অ্যাপে ঢুকে Send Money অপশনে যান।\n" +
                                "৩. রেফারেন্সে লিখুন: EBlood",
                        fontSize = 12.sp,
                        color = Color(0xFFFDE68A)
                    )
                } else {
                    Text(
                        text = "🌟 Why donate to EBloodDonation?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "EBloodDonation is a non-profit life-saving platform helping patients connect with voluntary blood donors instantly in emergencies without any fee or advertisement.\n\n" +
                                "• 24/7 Cloud Servers & Infrastructure\n" +
                                "• Emergency Urgent Alarm & SMS gateways\n" +
                                "• 100% Free for all donors and patients.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close / সম্পন্ন করুন", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = DarkSurfaceCard
    )
}
