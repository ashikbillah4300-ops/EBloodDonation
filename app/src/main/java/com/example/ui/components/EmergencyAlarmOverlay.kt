package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodRequest
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.util.ActiveAlarmState

@Composable
fun EmergencyAlarmOverlay(
    alarmState: ActiveAlarmState,
    allRequests: List<BloodRequest>,
    onAcceptRequest: (BloodRequest) -> Unit,
    onRejectRequest: (BloodRequest) -> Unit,
    onConfirmRequesterAlert: (Long) -> Unit,
    onDismissAlarm: () -> Unit
) {
    AnimatedVisibility(
        visible = alarmState !is ActiveAlarmState.Idle,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        when (alarmState) {
            is ActiveAlarmState.DonorAlarm -> {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                val matchedRequest = allRequests.find { it.id == alarmState.requestId }
                    ?: BloodRequest(
                        id = alarmState.requestId,
                        requesterName = alarmState.requesterName,
                        requesterPhone = alarmState.requesterPhone,
                        bloodGroup = alarmState.bloodGroup,
                        location = alarmState.location
                    )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .testTag("donor_alarm_banner"),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E0A0D),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CrimsonPrimary),
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .scale(pulseScale)
                                        .background(CrimsonPrimary.copy(alpha = 0.25f), CircleShape)
                                        .border(1.5.dp, CrimsonPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Urgent Alarm",
                                        tint = CrimsonPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "🚨 URGENT BLOOD ALARM",
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPrimary,
                                        fontSize = 15.sp
                                    )
                                    val minutes = alarmState.secondsRemaining / 60
                                    val seconds = alarmState.secondsRemaining % 60
                                    Text(
                                        text = "Ringing & Vibrating: %02d:%02d".format(minutes, seconds),
                                        color = Color(0xFFFDA4AF),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismissAlarm,
                                modifier = Modifier.testTag("dismiss_alarm_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss sound",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2A1218), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Blood Group Needed:",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(SuccessGreen, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = alarmState.bloodGroup,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 Location: ${alarmState.location}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "👤 Patient Contact: ${alarmState.requesterName}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRejectRequest(matchedRequest) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("reject_request_alarm_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onAcceptRequest(matchedRequest) },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(44.dp)
                                    .testTag("accept_request_alarm_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SuccessGreen
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Accept", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accept & Donate", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            is ActiveAlarmState.RequesterConfirmationAlert -> {
                val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
                val ringScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "ringScale"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .testTag("requester_alert_banner"),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF06281D),
                    border = androidx.compose.foundation.BorderStroke(2.dp, SuccessGreen),
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .scale(ringScale)
                                        .background(SuccessGreen.copy(alpha = 0.25f), CircleShape)
                                        .border(1.5.dp, SuccessGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Calling Alert",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "🎉 DONOR ACCEPTED!",
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "30s Call Ringtone: %02ds".format(alarmState.secondsRemaining),
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(onClick = onDismissAlarm) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${alarmState.donorName} agreed to donate ${alarmState.bloodGroup} blood!",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onConfirmRequesterAlert(alarmState.requestId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("open_donor_contact_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen
                            )
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call Donor", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Accept Alert & Reveal Phone Number",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            ActiveAlarmState.Idle -> {}
        }
    }
}
