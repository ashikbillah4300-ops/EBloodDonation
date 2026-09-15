package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "donors",
    indices = [Index(value = ["phone"], unique = true)]
)
data class DonorUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val bloodGroup: String, // "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    val location: String, // e.g. "Uttara, Dhaka, Dhaka District"
    val address: String,
    val latitude: Double = 23.8786,
    val longitude: Double = 90.3766,
    val isAvailable: Boolean = true,
    val isCurrentUser: Boolean = false,
    val isEnabled: Boolean = true,
    val alarmSoundEnabled: Boolean = true,
    val alarmVibrationEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val distanceKm: Double? = null
)

@Entity(tableName = "blood_requests")
data class BloodRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requesterName: String,
    val requesterPhone: String,
    val bloodGroup: String,
    val location: String,
    val latitude: Double = 23.8786,
    val longitude: Double = 90.3766,
    val selectedDonorCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    val acceptedDonorName: String? = null,
    val acceptedDonorPhone: String? = null,
    val isUrgentAlertActive: Boolean = false,
    val requesterConfirmed: Boolean = false
)

@Entity(tableName = "donation_records")
data class DonationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "DONOR" or "REQUESTER"
    val bloodGroup: String,
    val counterpartyName: String,
    val counterpartyPhone: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Completed"
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val settingKey: String,
    val settingValue: String,
    val updatedAt: Long = System.currentTimeMillis()
)
