package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class ActiveAlarmState {
    object Idle : ActiveAlarmState()
    data class DonorAlarm(
        val requestId: Long,
        val bloodGroup: String,
        val location: String,
        val requesterName: String,
        val requesterPhone: String,
        val secondsRemaining: Int
    ) : ActiveAlarmState()

    data class RequesterConfirmationAlert(
        val requestId: Long,
        val donorName: String,
        val donorPhone: String,
        val bloodGroup: String,
        val location: String,
        val secondsRemaining: Int
    ) : ActiveAlarmState()
}

object EmergencyAlarmManager {
    private const val CHANNEL_EMERGENCY = "emergency_blood_alerts"
    private const val CHANNEL_RESPONSE = "blood_request_responses"
    private const val NOTIF_ID_DONOR = 1001
    private const val NOTIF_ID_REQUESTER = 1002

    private val _alarmState = MutableStateFlow<ActiveAlarmState>(ActiveAlarmState.Idle)
    val alarmState: StateFlow<ActiveAlarmState> = _alarmState.asStateFlow()

    private var toneJob: Job? = null
    private var vibrator: Vibrator? = null
    private var systemRingtone: Ringtone? = null
    private var isPlaying = false

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "Emergency Blood Request Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent high-priority 3-minute alarm alerts for nearby blood requests"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
            }

            val responseChannel = NotificationChannel(
                CHANNEL_RESPONSE,
                "Donor Acceptance Response Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a donor accepts your blood request"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }

            notificationManager.createNotificationChannel(emergencyChannel)
            notificationManager.createNotificationChannel(responseChannel)
        }
    }

    /**
     * Requirement 4: Continuous 3-minute alarm and vibration for donors
     */
    fun triggerDonorEmergencyAlarm(
        context: Context,
        requestId: Long,
        bloodGroup: String,
        location: String,
        requesterName: String,
        requesterPhone: String,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) {
        stopAlarm()

        val totalDurationSeconds = 30 // Max 30 seconds alert as requested
        _alarmState.value = ActiveAlarmState.DonorAlarm(
            requestId = requestId,
            bloodGroup = bloodGroup,
            location = location,
            requesterName = requesterName,
            requesterPhone = requesterPhone,
            secondsRemaining = totalDurationSeconds
        )

        showDonorNotification(context, requestId, bloodGroup, location)
        startAlarmAudioAndVibration(context, soundEnabled, vibrationEnabled, isUrgentSiren = true)

        // Count down for 30 seconds max
        toneJob = CoroutineScope(Dispatchers.Default).launch {
            for (sec in totalDurationSeconds downTo 1) {
                if (!isActive) break
                val current = _alarmState.value
                if (current is ActiveAlarmState.DonorAlarm) {
                    _alarmState.value = current.copy(secondsRemaining = sec)
                }
                delay(1000)
            }
            stopAlarm()
        }
    }

    /**
     * Requirement 6: 30-second call ringtone/alert when donor accepts
     */
    fun triggerRequesterAcceptanceAlert(
        context: Context,
        requestId: Long,
        donorName: String,
        donorPhone: String,
        bloodGroup: String,
        location: String,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) {
        stopAlarm()

        val totalDurationSeconds = 30 // 30 seconds as requested
        _alarmState.value = ActiveAlarmState.RequesterConfirmationAlert(
            requestId = requestId,
            donorName = donorName,
            donorPhone = donorPhone,
            bloodGroup = bloodGroup,
            location = location,
            secondsRemaining = totalDurationSeconds
        )

        showRequesterNotification(context, requestId, donorName, bloodGroup)
        startAlarmAudioAndVibration(context, soundEnabled, vibrationEnabled, isUrgentSiren = false)

        toneJob = CoroutineScope(Dispatchers.Default).launch {
            for (sec in totalDurationSeconds downTo 1) {
                if (!isActive) break
                val current = _alarmState.value
                if (current is ActiveAlarmState.RequesterConfirmationAlert) {
                    _alarmState.value = current.copy(secondsRemaining = sec)
                }
                delay(1000)
            }
            stopAlarm()
        }
    }

    private fun startAlarmAudioAndVibration(
        context: Context,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        isUrgentSiren: Boolean
    ) {
        isPlaying = true

        // Vibrator
        if (vibrationEnabled) {
            try {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                val pattern = if (isUrgentSiren) {
                    longArrayOf(0, 400, 200, 400, 200, 800)
                } else {
                    longArrayOf(0, 800, 400, 800, 400)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(pattern, 0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            } catch (e: Exception) {
                // Graceful fallback
            }
        }

        // Sound: Ringtone / Tone generator
        if (soundEnabled) {
            try {
                val soundUri = if (isUrgentSiren) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }

                systemRingtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    systemRingtone?.audioAttributes = AudioAttributes.Builder()
                        .setUsage(if (isUrgentSiren) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                systemRingtone?.play()
            } catch (e: Exception) {
                // Fallback to tone generator
                try {
                    val tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                    tone.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 3000)
                } catch (_: Exception) {}
            }
        }
    }

    fun stopAlarm() {
        toneJob?.cancel()
        toneJob = null

        try {
            systemRingtone?.stop()
            systemRingtone = null
        } catch (_: Exception) {}

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (_: Exception) {}

        isPlaying = false
        _alarmState.value = ActiveAlarmState.Idle
    }

    private fun showDonorNotification(
        context: Context,
        requestId: Long,
        bloodGroup: String,
        location: String
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "INBOX")
                putExtra("REQUEST_ID", requestId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_EMERGENCY)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🚨 EMERGENCY $bloodGroup BLOOD NEEDED!")
                .setContentText("Emergency request near $location. Tap to review immediately.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIF_ID_DONOR, builder.build())
        } catch (_: Exception) {}
    }

    private fun showRequesterNotification(
        context: Context,
        requestId: Long,
        donorName: String,
        bloodGroup: String
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "CONTACT_DONOR")
                putExtra("REQUEST_ID", requestId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_RESPONSE)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🩸 A Donor Accepted Your Request!")
                .setContentText("$donorName accepted to donate $bloodGroup blood! Tap to view phone number.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIF_ID_REQUESTER, builder.build())
        } catch (_: Exception) {}
    }
}
