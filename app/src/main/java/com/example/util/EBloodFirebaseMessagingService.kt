package com.example.util

import android.util.Log
import com.example.data.network.BackendNetworkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EBloodFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Firebase Cloud Messaging Token generated: $token")

        val sessionManager = SessionManager(applicationContext)
        val userPhone = sessionManager.getPhone()
        val backendUrl = sessionManager.getBackendUrl()

        CoroutineScope(Dispatchers.IO).launch {
            BackendNetworkManager.registerFcmToken(backendUrl, token, userPhone)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val type = data["type"] ?: "UNKNOWN"

        when (type) {
            "EMERGENCY_BLOOD_REQUEST" -> {
                val requestId = data["requestId"]?.toLongOrNull() ?: System.currentTimeMillis()
                val bloodGroup = data["bloodGroup"] ?: "জরুরি রক্ত"
                val hospital = data["hospitalName"] ?: data["location"] ?: "নিকটবর্তী হাসপাতাল"
                val requesterName = data["requesterName"] ?: "জরুরি গ্রহীতা"
                val requesterPhone = data["requesterPhone"] ?: ""

                // Trigger 3-minute persistent siren & heads-up notification as specified in specs
                EmergencyAlarmManager.triggerDonorEmergencyAlarm(
                    context = applicationContext,
                    requestId = requestId,
                    bloodGroup = bloodGroup,
                    location = hospital,
                    requesterName = requesterName,
                    requesterPhone = requesterPhone,
                    soundEnabled = true,
                    vibrationEnabled = true
                )
            }

            "DONOR_ACCEPTED" -> {
                val requestId = data["requestId"]?.toLongOrNull() ?: 0L
                val donorName = data["donorName"] ?: "একজন ডোনার"
                val donorPhone = data["donorPhone"] ?: ""
                val bloodGroup = data["bloodGroup"] ?: ""
                val location = data["location"] ?: ""

                // Trigger 30-second ringtone confirmation alert
                EmergencyAlarmManager.triggerRequesterAcceptanceAlert(
                    context = applicationContext,
                    requestId = requestId,
                    donorName = donorName,
                    donorPhone = donorPhone,
                    bloodGroup = bloodGroup,
                    location = location,
                    soundEnabled = true,
                    vibrationEnabled = true
                )
            }

            else -> {
                // Generic notification payload
                val title = remoteMessage.notification?.title ?: data["title"] ?: "EBlood Donation Alert"
                val body = remoteMessage.notification?.body ?: data["message"] ?: "নতুন একটি জরুরি নোটিফিকেশন এসেছে।"
                Log.i(TAG, "Received general notification: $title - $body")
            }
        }
    }

    companion object {
        private const val TAG = "EBloodFCMService"
    }
}
