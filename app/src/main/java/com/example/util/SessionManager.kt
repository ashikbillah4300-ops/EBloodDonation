package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("eblood_user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_PHONE = "key_user_phone"
        private const val KEY_NAME = "key_user_name"
        private const val KEY_BLOOD_GROUP = "key_blood_group"
        private const val KEY_LOCATION = "key_location"
        private const val KEY_ADDRESS = "key_address"
        private const val KEY_BACKEND_URL = "key_backend_url"
        private const val KEY_BACKEND_TOKEN = "key_backend_token"
        const val DEFAULT_BACKEND_URL = "https://eblooddonation.onrender.com"
    }

    fun saveSession(
        phone: String,
        name: String = "Ashik",
        bloodGroup: String = "O+",
        location: String = "Uttara, Dhaka, Dhaka District",
        address: String = "Sector 11, Uttara, Dhaka"
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_PHONE, phone)
            .putString(KEY_NAME, name)
            .putString(KEY_BLOOD_GROUP, bloodGroup)
            .putString(KEY_LOCATION, location)
            .putString(KEY_ADDRESS, address)
            .apply()
    }

    fun updateProfile(
        name: String? = null,
        bloodGroup: String? = null,
        location: String? = null,
        address: String? = null
    ) {
        val editor = prefs.edit()
        name?.let { editor.putString(KEY_NAME, it) }
        bloodGroup?.let { editor.putString(KEY_BLOOD_GROUP, it) }
        location?.let { editor.putString(KEY_LOCATION, it) }
        address?.let { editor.putString(KEY_ADDRESS, it) }
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) return true
        try {
            val fbUser = FirebaseAuth.getInstance().currentUser
            if (fbUser != null) return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getPhone(): String {
        val saved = prefs.getString(KEY_PHONE, null)
        if (!saved.isNullOrBlank()) return saved
        try {
            val fbPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber
            if (!fbPhone.isNullOrBlank()) {
                val clean = fbPhone.replace("+88", "").filter { it.isDigit() }
                return if (clean.startsWith("0")) clean else "0$clean"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "01969114300"
    }

    fun getName(): String = prefs.getString(KEY_NAME, "Ashik") ?: "Ashik"
    fun getBloodGroup(): String = prefs.getString(KEY_BLOOD_GROUP, "O+") ?: "O+"
    fun getLocation(): String = prefs.getString(KEY_LOCATION, "Uttara, Dhaka, Dhaka District") ?: "Uttara, Dhaka, Dhaka District"
    fun getAddress(): String = prefs.getString(KEY_ADDRESS, "Sector 11, Uttara, Dhaka") ?: "Sector 11, Uttara, Dhaka"

    fun getBackendUrl(): String = prefs.getString(KEY_BACKEND_URL, DEFAULT_BACKEND_URL) ?: DEFAULT_BACKEND_URL

    fun setBackendUrl(url: String) {
        prefs.edit().putString(KEY_BACKEND_URL, url.trim()).apply()
    }

    fun getBackendToken(): String? = prefs.getString(KEY_BACKEND_TOKEN, null)

    fun setBackendToken(token: String?) {
        prefs.edit().putString(KEY_BACKEND_TOKEN, token).apply()
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("key_is_dark_mode", false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("key_is_dark_mode", enabled).apply()
    }

    fun getThemeMode(): String = prefs.getString("key_theme_mode", "LIGHT") ?: "LIGHT"

    fun setThemeMode(mode: String) {
        prefs.edit().putString("key_theme_mode", mode).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
