package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.BloodRequest
import com.example.data.model.DonationRecord
import com.example.data.model.DonorUser
import kotlinx.coroutines.flow.Flow

class EBloodRepository(private val dao: AppDao) {

    val currentUserFlow: Flow<DonorUser?> = dao.getCurrentUserFlow()
    val allRequestsFlow: Flow<List<BloodRequest>> = dao.getAllRequestsFlow()

    fun getRequestsByStatusFlow(status: String): Flow<List<BloodRequest>> =
        dao.getRequestsByStatusFlow(status)

    fun getRecordsByRoleFlow(role: String): Flow<List<DonationRecord>> =
        dao.getRecordsByRoleFlow(role)

    fun getAvailableDonorsByBloodGroup(bloodGroup: String): Flow<List<DonorUser>> =
        dao.getAvailableDonorsByBloodGroup(bloodGroup)

    suspend fun getCurrentUser(): DonorUser? = dao.getCurrentUser()

    suspend fun getDonorByPhone(phone: String): DonorUser? = dao.getDonorByPhone(phone)

    suspend fun seedInitialDataIfNeeded() {
        // Remove all unauthenticated sample/mock donors from the database
        val mockPhones = listOf(
            "01711223344", "01812345678", "01688776655", "01955443322",
            "01522334455", "01799887766", "01855667788"
        )
        dao.deleteDonorsByPhones(mockPhones)
    }

    suspend fun registerOrUpdateUser(
        name: String,
        phone: String,
        bloodGroup: String,
        location: String,
        address: String
    ): DonorUser {
        dao.clearCurrentUsers()
        val existing = dao.getDonorByPhone(phone)
        val user = if (existing != null) {
            existing.copy(
                name = name,
                bloodGroup = bloodGroup,
                location = location,
                address = address,
                isCurrentUser = true
            )
        } else {
            DonorUser(
                name = name,
                phone = phone,
                bloodGroup = bloodGroup,
                location = location,
                address = address,
                isCurrentUser = true
            )
        }
        val id = dao.insertDonor(user)
        return user.copy(id = if (user.id == 0L) id else user.id)
    }

    suspend fun signInUser(phone: String): DonorUser? {
        val user = dao.getDonorByPhone(phone)
        if (user != null) {
            dao.clearCurrentUsers()
            val updated = user.copy(isCurrentUser = true)
            dao.updateDonor(updated)
            return updated
        }
        return null
    }

    suspend fun signOut() {
        dao.clearCurrentUsers()
    }

    suspend fun updateCurrentUser(user: DonorUser) {
        dao.updateDonor(user)
    }

    suspend fun createBloodRequest(request: BloodRequest): Long {
        return dao.insertRequest(request)
    }

    suspend fun updateBloodRequest(request: BloodRequest) {
        dao.updateRequest(request)
    }

    suspend fun getRequestById(id: Long): BloodRequest? {
        return dao.getRequestById(id)
    }

    suspend fun addDonationRecord(record: DonationRecord) {
        dao.insertRecord(record)
    }

    // Admin & Settings
    fun getAllDonorsFlow(): Flow<List<DonorUser>> = dao.getAllDonorsFlow()

    fun getAllDonationRecordsFlow(): Flow<List<DonationRecord>> = dao.getAllDonationRecordsFlow()

    fun getAllSettingsFlow(): Flow<List<com.example.data.model.AppSetting>> = dao.getAllSettingsFlow()

    suspend fun getSettingValue(key: String): String? = dao.getSettingValue(key)

    suspend fun saveSetting(key: String, value: String) {
        dao.insertSetting(
            com.example.data.model.AppSetting(
                settingKey = key,
                settingValue = value,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateDonorStatus(donor: DonorUser) {
        dao.updateDonor(donor)
    }

    suspend fun deleteDonor(id: Long) {
        dao.deleteDonorById(id)
    }

    suspend fun updateRequestStatus(id: Long, status: String) {
        dao.updateRequestStatus(id, status)
    }

    suspend fun initializeDefaultSettingsIfEmpty() {
        val existingDonationNumber = dao.getSettingValue("donation_number")
        if (existingDonationNumber == null) {
            val defaults = listOf(
                com.example.data.model.AppSetting("donation_number", "01969114300"),
                com.example.data.model.AppSetting("contact_number", "01969114300"),
                com.example.data.model.AppSetting("support_number", "01969114300"),
                com.example.data.model.AppSetting("app_notice", "জরুরী রক্তের প্রয়োজনে EBloodDonation সবসময় আপনার পাশে আছে।"),
                com.example.data.model.AppSetting("emergency_notice", ""),
                com.example.data.model.AppSetting("maintenance_mode", "false"),
                com.example.data.model.AppSetting("app_version", "1.0.0"),
                com.example.data.model.AppSetting("donation_instructions", "বিকাশ অ্যাপে 'Send Money' অপশন বেছে নিয়ে উপরের নম্বরে আপনার সামর্থ্য অনুযায়ী অনুদান পাঠাতে পারেন।")
            )
            dao.insertSettings(defaults)
        }
    }
}
