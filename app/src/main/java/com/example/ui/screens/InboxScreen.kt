package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BloodRequest
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EBloodViewModel
import com.example.ui.viewmodel.InboxTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InboxScreen(viewModel: EBloodViewModel) {
    val currentTab by viewModel.activeInboxTab.collectAsStateWithLifecycle()
    val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pendingRequests = allRequests.filter { it.status == "PENDING" }
    val acceptedRequests = allRequests.filter { it.status == "ACCEPTED" || it.status == "COMPLETED" }
    val rejectedRequests = allRequests.filter { it.status == "REJECTED" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("inbox_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Inbox",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                IconButton(onClick = { /* Refresh */ }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextSecondary
                    )
                }
            }

            // Tabs: Pending | Accepted | Rejected
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                        color = Color(0xFFF59E0B),
                        height = 3.dp
                    )
                },
                divider = {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkSurfaceBorder))
                }
            ) {
                Tab(
                    selected = currentTab == InboxTab.PENDING,
                    onClick = { viewModel.activeInboxTab.value = InboxTab.PENDING },
                    text = {
                        Text(
                            text = "Pending",
                            fontWeight = if (currentTab == InboxTab.PENDING) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentTab == InboxTab.PENDING) Color(0xFFF59E0B) else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = currentTab == InboxTab.ACCEPTED,
                    onClick = { viewModel.activeInboxTab.value = InboxTab.ACCEPTED },
                    text = {
                        Text(
                            text = "Accepted",
                            fontWeight = if (currentTab == InboxTab.ACCEPTED) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentTab == InboxTab.ACCEPTED) Color(0xFFF59E0B) else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = currentTab == InboxTab.REJECTED,
                    onClick = { viewModel.activeInboxTab.value = InboxTab.REJECTED },
                    text = {
                        Text(
                            text = "Rejected",
                            fontWeight = if (currentTab == InboxTab.REJECTED) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentTab == InboxTab.REJECTED) Color(0xFFF59E0B) else TextSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Contents
            when (currentTab) {
                InboxTab.PENDING -> {
                    if (pendingRequests.isEmpty()) {
                        EmptyInboxState(
                            icon = Icons.Default.Mail,
                            title = "No pending requests",
                            subtitle = "You'll be notified when someone needs your blood type nearby."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingRequests) { req ->
                                PendingRequestCard(
                                    request = req,
                                    onAccept = { viewModel.acceptRequest(req) },
                                    onReject = { viewModel.rejectRequest(req) }
                                )
                            }
                        }
                    }
                }

                InboxTab.ACCEPTED -> {
                    if (acceptedRequests.isEmpty()) {
                        EmptyInboxState(
                            icon = Icons.Default.Check,
                            title = "No accepted requests",
                            subtitle = "Accepted blood requests will be listed here."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(acceptedRequests) { req ->
                                AcceptedRequestCard(
                                    request = req,
                                    onCall = {
                                        val phone = req.acceptedDonorPhone ?: req.requesterPhone
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(intent)
                                    },
                                    onSms = {
                                        val phone = req.acceptedDonorPhone ?: req.requesterPhone
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
                                        context.startActivity(intent)
                                    },
                                    onComplete = { viewModel.completeDonation(req) }
                                )
                            }
                        }
                    }
                }

                InboxTab.REJECTED -> {
                    if (rejectedRequests.isEmpty()) {
                        EmptyInboxState(
                            icon = Icons.Default.Block,
                            title = "No rejected requests",
                            subtitle = "No rejected requests to show."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(rejectedRequests) { req ->
                                RejectedRequestCard(request = req)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyInboxState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF141D2B), CircleShape)
                    .border(1.dp, Color(0xFF26354D), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (icon == Icons.Default.Block) CrimsonPrimary else InfoBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PendingRequestCard(
    request: BloodRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(SuccessGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = request.bloodGroup,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🚨 Urgent Blood Needed",
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        fontSize = 14.sp
                    )
                }

                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(request.timestamp))
                Text(text = timeStr, fontSize = 11.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "📍 Location: ${request.location}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Text(
                text = "👤 Requester: ${request.requesterName}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Accept / Reject Buttons (Requirement 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonPrimary)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Accept", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accept Request", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AcceptedRequestCard(
    request: BloodRequest,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onComplete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(SuccessGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = request.bloodGroup,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (request.status == "COMPLETED") "Donation Completed" else "Request Accepted",
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        fontSize = 14.sp
                    )
                }

                val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(request.timestamp))
                Text(text = timeStr, fontSize = 11.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "📍 Location: ${request.location}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            val donorName = request.acceptedDonorName ?: "ashik"
            val donorPhone = request.acceptedDonorPhone ?: "01969114300"
            Text(
                text = "🩸 Donor: $donorName ($donorPhone)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onSms,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                ) {
                    Icon(Icons.Default.Message, contentDescription = "SMS", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMS", fontWeight = FontWeight.Bold)
                }

                if (request.status != "COMPLETED") {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1.2f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Text("Complete", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RejectedRequestCard(request: BloodRequest) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2A161E), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = request.bloodGroup,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Request Declined", color = TextSecondary, fontSize = 13.sp)
                }
                Text(text = "Declined", color = CrimsonPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Location: ${request.location}", fontSize = 12.sp, color = TextMuted)
        }
    }
}
