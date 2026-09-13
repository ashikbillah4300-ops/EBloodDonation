package com.example.data.network

import com.example.data.repository.EBloodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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

object BackendNetworkManager {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
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
                message = "⏳ Connection Timed Out. Server is sleeping or unreachable (Free tier may take 30-50s to spin up)."
            )
        } catch (e: java.net.UnknownHostException) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                success = false,
                latencyMs = latency,
                statusCode = 0,
                message = "🔴 Unknown Host / Domain name not found. Please verify the URL."
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
}
