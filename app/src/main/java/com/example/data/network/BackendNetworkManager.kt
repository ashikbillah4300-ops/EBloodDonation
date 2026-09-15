package com.example.data.network

import com.example.data.model.BloodRequest
import com.example.data.model.DonationRecord
import com.example.data.model.DonorUser
import com.example.data.repository.EBloodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ConnectionTestResult(
    val success: Boolean,
    val latencyMs: Long,
    val statusCode: Int,
    val message: String,
    val rawData: String? = null
)

data class SyncResult(
    val success: Boolean,
    val message: String,
    val syncedCount: Int = 0
)

data class AuthResult(
    val success: Boolean,
    val token: String? = null,
    val userId: Long? = null,
    val phone: String? = null,
    val name: String? = null,
    val bloodGroup: String? = null,
    val location: String? = null,
    val address: String? = null,
    val message: String? = null,
    val notRegistered: Boolean = false
)

object BackendNetworkManager {

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun sanitizeUrl(rawUrl: String): String {
        var clean = rawUrl.trim()
        if (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length - 1)
        }
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        return clean
    }

    /**
     * Test connection to the backend server with latency measurement
     */
    suspend fun testServerConnection(rawUrl: String): ConnectionTestResult = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        val startTime = System.currentTimeMillis()

        try {
            val request = Request.Builder()
                .url("$baseUrl/api/health")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val code = response.code
                val bodyString = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val serviceName = try {
                        val json = JSONObject(bodyString)
                        json.optString("service", "EBlood Cloud API")
                    } catch (e: Exception) {
                        "EBlood Cloud API"
                    }
                    ConnectionTestResult(
                        success = true,
                        latencyMs = latency,
                        statusCode = code,
                        message = "🟢 Online Connected! ($serviceName)",
                        rawData = bodyString
                    )
                } else {
                    ConnectionTestResult(
                        success = false,
                        latencyMs = latency,
                        statusCode = code,
                        message = "🔴 Server responded with HTTP $code: ${response.message}",
                        rawData = bodyString
                    )
                }
            }
        } catch (e: java.net.ConnectException) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                success = false,
                latencyMs = latency,
                statusCode = 0,
                message = "🔴 Connection Refused. Server is not running at this address, or port is closed."
            )
        } catch (e: java.net.SocketTimeoutException) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                success = false,
                latencyMs = latency,
                statusCode = 408,
                message = "⏳ Connection Timed Out. Server may be spinning up (free tier)."
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                success = false,
                latencyMs = latency,
                statusCode = -1,
                message = "🔴 Error: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    /**
     * Register or update user on PostgreSQL via REST API
     */
    suspend fun registerUser(
        rawUrl: String,
        name: String,
        phone: String,
        bloodGroup: String,
        location: String,
        address: String,
        fcmToken: String? = null,
        firebaseToken: String? = null
    ): AuthResult = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        try {
            val json = JSONObject().apply {
                put("name", name)
                put("phone", phone)
                put("bloodGroup", bloodGroup)
                put("location", location)
                put("address", address)
                if (!fcmToken.isNullOrBlank()) put("fcmToken", fcmToken)
                if (!firebaseToken.isNullOrBlank()) put("firebaseToken", firebaseToken)
            }

            val request = Request.Builder()
                .url("$baseUrl/api/auth/register")
                .header("Accept", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val root = JSONObject(body)
                if (response.isSuccessful && root.optBoolean("success", false)) {
                    val data = root.optJSONObject("data")
                    val userObj = data?.optJSONObject("user")
                    AuthResult(
                        success = true,
                        token = data?.optString("token"),
                        userId = userObj?.optLong("id"),
                        phone = userObj?.optString("phone", phone),
                        name = userObj?.optString("name", name),
                        bloodGroup = userObj?.optString("bloodGroup", bloodGroup),
                        location = userObj?.optString("location", location),
                        address = userObj?.optString("address", address),
                        message = root.optString("message", "Registered successfully")
                    )
                } else {
                    AuthResult(
                        success = false,
                        message = root.optString("message", "Registration failed: HTTP ${response.code}")
                    )
                }
            }
        } catch (e: Exception) {
            AuthResult(success = false, message = e.localizedMessage ?: "Network error during registration")
        }
    }

    /**
     * Authenticate user with backend using verified phone
     */
    suspend fun loginUser(
        rawUrl: String,
        phone: String,
        fcmToken: String? = null,
        firebaseToken: String? = null
    ): AuthResult = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        try {
            val json = JSONObject().apply {
                put("phone", phone)
                if (!fcmToken.isNullOrBlank()) put("fcmToken", fcmToken)
                if (!firebaseToken.isNullOrBlank()) put("firebaseToken", firebaseToken)
            }

            val request = Request.Builder()
                .url("$baseUrl/api/auth/login")
                .header("Accept", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val root = JSONObject(body)
                val notRegistered = root.optBoolean("notRegistered", false)

                if (response.isSuccessful && root.optBoolean("success", false)) {
                    val data = root.optJSONObject("data")
                    val userObj = data?.optJSONObject("user")
                    AuthResult(
                        success = true,
                        token = data?.optString("token"),
                        userId = userObj?.optLong("id"),
                        phone = userObj?.optString("phone", phone),
                        name = userObj?.optString("name", ""),
                        bloodGroup = userObj?.optString("bloodGroup", "O+"),
                        location = userObj?.optString("location", ""),
                        address = userObj?.optString("address", ""),
                        message = root.optString("message", "Logged in successfully")
                    )
                } else {
                    AuthResult(
                        success = false,
                        notRegistered = notRegistered,
                        message = root.optString("message", "Login failed")
                    )
                }
            }
        } catch (e: Exception) {
            AuthResult(success = false, message = e.localizedMessage ?: "Network error")
        }
    }

    /**
     * Create blood request on PostgreSQL and trigger targeted FCM notifications
     */
    suspend fun createBloodRequest(
        rawUrl: String,
        authToken: String?,
        authPhone: String?,
        requestItem: BloodRequest
    ): BloodRequest? = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        try {
            val json = JSONObject().apply {
                put("requesterName", requestItem.requesterName)
                put("requesterPhone", requestItem.requesterPhone)
                put("bloodGroup", requestItem.bloodGroup)
                put("unitsNeeded", requestItem.selectedDonorCount)
                put("hospitalName", requestItem.location)
                put("location", requestItem.location)
                put("latitude", requestItem.latitude)
                put("longitude", requestItem.longitude)
                put("neededBefore", "জরুরি প্রয়োজন")
            }

            val builder = Request.Builder()
                .url("$baseUrl/api/blood-requests")
                .header("Accept", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))

            if (!authToken.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $authToken")
            }
            if (!authPhone.isNullOrBlank()) {
                builder.header("x-user-phone", authPhone)
            }

            client.newCall(builder.build()).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val root = JSONObject(body)
                val data = root.optJSONObject("data") ?: return@withContext null

                requestItem.copy(
                    id = data.optLong("id", requestItem.id),
                    status = data.optString("status", "ACTIVE")
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fetch blood requests from PostgreSQL backend
     */
    suspend fun fetchBloodRequests(
        rawUrl: String,
        status: String? = null
    ): List<BloodRequest> = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        val list = mutableListOf<BloodRequest>()

        try {
            val url = if (!status.isNullOrBlank() && status != "ALL") {
                "$baseUrl/api/blood-requests?status=$status"
            } else {
                "$baseUrl/api/blood-requests"
            }

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(body)
                val data = root.optJSONObject("data") ?: return@withContext emptyList()
                val requestsArray = data.optJSONArray("requests") ?: JSONArray()

                for (i in 0 until requestsArray.length()) {
                    val obj = requestsArray.getJSONObject(i)
                    list.add(
                        BloodRequest(
                            id = obj.optLong("id"),
                            requesterName = obj.optString("requesterName", "Requester"),
                            requesterPhone = obj.optString("requesterPhone", ""),
                            bloodGroup = obj.optString("bloodGroup", "O+"),
                            location = obj.optString("hospitalName", obj.optString("location", "")),
                            latitude = obj.optDouble("latitude", 23.8786),
                            longitude = obj.optDouble("longitude", 90.3766),
                            selectedDonorCount = obj.optInt("unitsNeeded", 1),
                            status = obj.optString("status", "ACTIVE"),
                            acceptedDonorName = if (obj.isNull("acceptedDonorName")) null else obj.optString("acceptedDonorName"),
                            acceptedDonorPhone = if (obj.isNull("acceptedDonorPhone")) null else obj.optString("acceptedDonorPhone"),
                            isUrgentAlertActive = true,
                            requesterConfirmed = obj.optBoolean("requesterConfirmed", false)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // fallback to empty
        }
        list
    }

    /**
     * Update blood request status on PostgreSQL
     */
    suspend fun updateBloodRequestStatus(
        rawUrl: String,
        authToken: String?,
        requestId: Long,
        status: String,
        acceptedDonorName: String? = null,
        acceptedDonorPhone: String? = null,
        requesterConfirmed: Boolean? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        try {
            val json = JSONObject().apply {
                put("status", status)
                if (acceptedDonorName != null) put("acceptedDonorName", acceptedDonorName)
                if (acceptedDonorPhone != null) put("acceptedDonorPhone", acceptedDonorPhone)
                if (requesterConfirmed != null) put("requesterConfirmed", requesterConfirmed)
            }

            val builder = Request.Builder()
                .url("$baseUrl/api/blood-requests/$requestId")
                .header("Accept", "application/json")
                .patch(json.toString().toRequestBody(JSON_MEDIA_TYPE))

            if (!authToken.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $authToken")
            }

            client.newCall(builder.build()).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete blood request on PostgreSQL backend
     */
    suspend fun deleteBloodRequest(
        rawUrl: String,
        authToken: String?,
        requestId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        try {
            val builder = Request.Builder()
                .url("$baseUrl/api/blood-requests/$requestId")
                .header("Accept", "application/json")
                .delete()

            if (!authToken.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $authToken")
            }

            client.newCall(builder.build()).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fetch registered donors from PostgreSQL backend
     */
    suspend fun fetchDonors(
        rawUrl: String,
        bloodGroup: String? = null,
        location: String? = null,
        excludePhone: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        nearMe: Boolean = false
    ): List<DonorUser> = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        val list = mutableListOf<DonorUser>()

        try {
            val urlBuilder = StringBuilder("$baseUrl/api/users?isAvailable=true")
            if (!bloodGroup.isNullOrBlank() && bloodGroup != "ALL") {
                urlBuilder.append("&bloodGroup=").append(bloodGroup)
            }
            if (!location.isNullOrBlank() && location != "ALL") {
                urlBuilder.append("&location=").append(location)
            }
            if (!excludePhone.isNullOrBlank()) {
                urlBuilder.append("&excludePhone=").append(excludePhone.trim())
            }
            if (latitude != null && longitude != null) {
                urlBuilder.append("&latitude=").append(latitude).append("&longitude=").append(longitude)
            }
            if (nearMe) {
                urlBuilder.append("&nearMe=true")
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(body)
                val data = root.optJSONObject("data") ?: return@withContext emptyList()
                val usersArray = data.optJSONArray("users") ?: JSONArray()

                for (i in 0 until usersArray.length()) {
                    val obj = usersArray.getJSONObject(i)
                    list.add(
                        DonorUser(
                            id = obj.optLong("id"),
                            name = obj.optString("name", "Donor"),
                            phone = obj.optString("phone", ""),
                            bloodGroup = obj.optString("bloodGroup", "O+"),
                            location = obj.optString("location", "Dhaka"),
                            address = obj.optString("address", ""),
                            latitude = obj.optDouble("latitude", 23.8786),
                            longitude = obj.optDouble("longitude", 90.3766),
                            isAvailable = obj.optBoolean("isAvailable", true),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            alarmSoundEnabled = obj.optBoolean("alarmSoundEnabled", true),
                            alarmVibrationEnabled = obj.optBoolean("alarmVibrationEnabled", true),
                            distanceKm = if (obj.has("distanceKm") && !obj.isNull("distanceKm")) obj.optDouble("distanceKm") else null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        list
    }

    /**
     * Register device FCM token with backend
     */
    suspend fun registerFcmToken(
        rawUrl: String,
        token: String,
        phone: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        try {
            val json = JSONObject().apply {
                put("token", token)
                if (!phone.isNullOrBlank()) put("phone", phone)
                put("deviceType", "android")
            }

            val request = Request.Builder()
                .url("$baseUrl/api/users/notification-token")
                .header("Accept", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Pull dynamic settings from the online backend and update local Room DB
     */
    suspend fun fetchAndSyncSettings(
        rawUrl: String,
        repository: EBloodRepository
    ): SyncResult = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)

        try {
            val request = Request.Builder()
                .url("$baseUrl/api/settings")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext SyncResult(
                        success = false,
                        message = "Failed to sync: HTTP ${response.code}"
                    )
                }

                val body = response.body?.string() ?: return@withContext SyncResult(false, "Empty server response")
                val rootJson = JSONObject(body)
                val dataObj = rootJson.optJSONObject("data") ?: rootJson

                var count = 0
                val keys = dataObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = dataObj.optString(key, "")
                    if (value.isNotEmpty()) {
                        repository.saveSetting(key, value)
                        count++
                    }
                }

                SyncResult(
                    success = true,
                    message = "Successfully synced $count settings with online server!",
                    syncedCount = count
                )
            }
        } catch (e: Exception) {
            SyncResult(
                success = false,
                message = "Sync failed: ${e.localizedMessage ?: "Network error"}"
            )
        }
    }

    /**
     * Authenticate admin via backend-only verification
     */
    suspend fun adminLogin(
        rawUrl: String,
        username: String,
        pass: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        try {
            val json = JSONObject().apply {
                put("username", username)
                put("password", pass)
            }

            val request = Request.Builder()
                .url("$baseUrl/api/admin/login")
                .header("Accept", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val root = JSONObject(body)
                if (response.isSuccessful && root.optBoolean("success", false)) {
                    val data = root.optJSONObject("data")
                    AuthResult(
                        success = true,
                        token = data?.optString("token"),
                        message = root.optString("message", "Admin authenticated")
                    )
                } else {
                    AuthResult(
                        success = false,
                        message = root.optString("message", "Invalid administrative credentials")
                    )
                }
            }
        } catch (e: Exception) {
            AuthResult(success = false, message = "Could not connect to backend admin auth: ${e.localizedMessage}")
        }
    }

    /**
     * Update settings from admin panel
     */
    suspend fun updateAdminSettings(
        rawUrl: String,
        adminToken: String?,
        settingsMap: Map<String, String>
    ): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = sanitizeUrl(rawUrl)
        try {
            val settingsJson = JSONObject()
            settingsMap.forEach { (k, v) -> settingsJson.put(k, v) }

            val json = JSONObject().apply {
                put("settings", settingsJson)
            }

            val builder = Request.Builder()
                .url("$baseUrl/api/settings")
                .header("Accept", "application/json")
                .patch(json.toString().toRequestBody(JSON_MEDIA_TYPE))

            if (!adminToken.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $adminToken")
            }

            client.newCall(builder.build()).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}
