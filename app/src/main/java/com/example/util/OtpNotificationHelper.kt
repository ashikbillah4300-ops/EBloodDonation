package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object OtpNotificationHelper {
    private const val CHANNEL_ID = "eblood_otp_channel"
    private const val NOTIFICATION_ID = 2001

    fun sendOtpNotification(context: Context, code: String, phone: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "OTP Verification Code",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Delivers SMS and Verification Codes"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("EBloodDonation SMS OTP")
                .setContentText("Your verification code is $code. Valid for 5 minutes.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Your EBloodDonation verification code is $code for mobile $phone. Do not share this code with anyone.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
