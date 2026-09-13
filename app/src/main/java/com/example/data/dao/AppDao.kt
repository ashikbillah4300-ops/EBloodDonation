package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BloodRequest
import com.example.data.model.DonationRecord
import com.example.data.model.DonorUser
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Current User & Donors
    @Query("SELECT * FROM donors WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<DonorUser?>

    @Query("SELECT * FROM donors WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): DonorUser?

    @Query("SELECT * FROM donors WHERE phone = :phone LIMIT 1")
    suspend fun getDonorByPhone(phone: String): DonorUser?

    @Query("SELECT * FROM donors WHERE isCurrentUser = 0 AND bloodGroup = :bloodGroup AND isAvailable = 1")
    fun getAvailableDonorsByBloodGroup(bloodGroup: String): Flow<List<DonorUser>>

    @Query("SELECT * FROM donors WHERE isCurrentUser = 0")
    fun getAllOtherDonors(): Flow<List<DonorUser>>

    @Query("SELECT COUNT(*) FROM donors")
    suspend fun getDonorCount(): Int

    @Query("DELETE FROM donors WHERE phone IN (:phones)")
    suspend fun deleteDonorsByPhones(phones: List<String>)

    @Query("DELETE FROM donors WHERE isCurrentUser = 0")
    suspend fun deleteAllUnauthenticatedDonors()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: DonorUser): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonors(donors: List<DonorUser>)

    @Update
    suspend fun updateDonor(donor: DonorUser)

    @Query("UPDATE donors SET isCurrentUser = 0")
    suspend fun clearCurrentUsers()

    // Blood Requests
    @Query("SELECT * FROM blood_requests ORDER BY timestamp DESC")
    fun getAllRequestsFlow(): Flow<List<BloodRequest>>

    @Query("SELECT * FROM blood_requests WHERE status = :status ORDER BY timestamp DESC")
    fun getRequestsByStatusFlow(status: String): Flow<List<BloodRequest>>

    @Query("SELECT * FROM blood_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: Long): BloodRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BloodRequest): Long

    @Update
    suspend fun updateRequest(request: BloodRequest)

    @Delete
    suspend fun deleteRequest(request: BloodRequest)

    // Donation Records
    @Query("SELECT * FROM donation_records WHERE role = :role ORDER BY timestamp DESC")
    fun getRecordsByRoleFlow(role: String): Flow<List<DonationRecord>>

    @Query("SELECT * FROM donation_records ORDER BY timestamp DESC")
    fun getAllDonationRecordsFlow(): Flow<List<DonationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DonationRecord): Long

    // Admin & Full Donor Queries
    @Query("SELECT * FROM donors ORDER BY id DESC")
    fun getAllDonorsFlow(): Flow<List<DonorUser>>

    @Query("DELETE FROM donors WHERE id = :id")
    suspend fun deleteDonorById(id: Long)

    @Query("UPDATE blood_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Long, status: String)

    // App Settings
    @Query("SELECT * FROM app_settings")
    fun getAllSettingsFlow(): Flow<List<com.example.data.model.AppSetting>>

    @Query("SELECT settingValue FROM app_settings WHERE settingKey = :key LIMIT 1")
    fun getSettingValueFlow(key: String): Flow<String?>

    @Query("SELECT settingValue FROM app_settings WHERE settingKey = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: com.example.data.model.AppSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<com.example.data.model.AppSetting>)
}
