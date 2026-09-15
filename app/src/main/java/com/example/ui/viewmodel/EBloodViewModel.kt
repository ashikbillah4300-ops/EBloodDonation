package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AppSetting
import com.example.data.model.BloodRequest
import com.example.data.model.DonationRecord
import com.example.data.model.DonorUser
import com.example.data.network.BackendNetworkManager
import com.example.data.network.ConnectionTestResult
import com.example.data.network.SyncResult
import com.example.data.repository.EBloodRepository
import com.example.util.ActiveAlarmState
import com.example.util.DeviceUtils
import com.example.util.EmergencyAlarmManager
import com.example.util.OtpNotificationHelper
import com.example.util.SessionManager
import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    AUTH,
    OTP_VERIFY,
    NAME_INPUT,
    PROFILE_SETUP,
    MAIN,
    CREATE_REQUEST_STEP1,
    CREATE_REQUEST_STEP2,
    CREATE_REQUEST_STEP3,
    REQUEST_SENT_SUCCESS,
    CONTACT_REVEALED,
    ADMIN_LOGIN,
    ADMIN_DASHBOARD
}

enum class AuthTab {
    NEW_DONOR,
    EXISTING
}

enum class InboxTab {
    PENDING,
    ACCEPTED,
    REJECTED
}

enum class HistoryTab {
    AS_DONOR,
    AS_REQUESTER
}

class EBloodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EBloodRepository
    val sessionManager = SessionManager(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = EBloodRepository(db.appDao())
        EmergencyAlarmManager.initNotificationChannels(application)
        viewModelScope.launch {
            repository.initializeDefaultSettingsIfEmpty()
            repository.seedInitialDataIfNeeded()

            // Automatically purge notifications/requests older than 2 days (48 hours)
            val twoDaysCutoff = System.currentTimeMillis() - (48 * 60 * 60 * 1000L)
            repository.clearOldRequests(twoDaysCutoff)

            // Pre-populate admin settings editing state from loaded settings
            launch {
                appSettingsList.collect { list ->
                    list.find { it.settingKey == "app_logo_url" }?.let {
                        if (editAppLogoUrl.value.isBlank()) editAppLogoUrl.value = it.settingValue
                    }
                    list.find { it.settingKey == "donation_number" }?.let {
                        if (editDonationNumber.value == "01969114300") editDonationNumber.value = it.settingValue
                    }
                    list.find { it.settingKey == "contact_number" }?.let {
                        if (editContactNumber.value == "01969114300") editContactNumber.value = it.settingValue
                    }
                    list.find { it.settingKey == "support_number" }?.let {
                        if (editSupportNumber.value == "01969114300") editSupportNumber.value = it.settingValue
                    }
                    list.find { it.settingKey == "app_notice" }?.let {
                        if (editAppNotice.value.isBlank()) editAppNotice.value = it.settingValue
                    }
                    list.find { it.settingKey == "emergency_notice" }?.let {
                        if (editEmergencyNotice.value.isBlank()) editEmergencyNotice.value = it.settingValue
                    }
                }
            }

            // Auto-fetch fresh settings from online backend in background
            val onlineUrl = sessionManager.getBackendUrl()
            if (onlineUrl.isNotBlank()) {
                try {
                    BackendNetworkManager.fetchAndSyncSettings(onlineUrl, repository)
                } catch (e: Exception) {
                    // Fallback to local database silently
                }
            }

            // Check if user was previously logged in
            val hasActiveSession = sessionManager.isLoggedIn()
            var user = repository.getCurrentUser()

            if (user == null && hasActiveSession) {
                val phone = sessionManager.getPhone()
                val existing = repository.getDonorByPhone(phone)
                user = if (existing != null) {
                    repository.signInUser(phone)
                } else {
                    repository.registerOrUpdateUser(
                        name = sessionManager.getName(),
                        phone = phone,
                        bloodGroup = sessionManager.getBloodGroup(),
                        location = sessionManager.getLocation(),
                        address = sessionManager.getAddress()
                    )
                }
            } else if (user != null) {
                // Keep session preferences in sync
                sessionManager.saveSession(
                    phone = user.phone,
                    name = user.name,
                    bloodGroup = user.bloodGroup,
                    location = user.location,
                    address = user.address
                )
            }

            delay(1200)
            if (user != null || hasActiveSession) {
                _currentScreen.value = Screen.MAIN
            } else {
                _currentScreen.value = Screen.AUTH
            }
        }
    }

    // Dynamic App Settings from Database
    val appSettingsList: StateFlow<List<AppSetting>> = repository.getAllSettingsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val donationNumber: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "donation_number" }?.settingValue ?: "01969114300"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "01969114300")

    val depositMethod: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "deposit_method" }?.settingValue ?: "Wallet"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Wallet")

    val appName: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "app_name" }?.settingValue ?: "EBlood Donation"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EBlood Donation")

    val depositNumber: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "deposit_number" }?.settingValue
            ?: list.find { it.settingKey == "donation_number" }?.settingValue
            ?: "01969114300"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "01969114300")

    val contactNumber: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "contact_number" }?.settingValue ?: "01969114300"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "01969114300")

    val supportNumber: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "support_number" }?.settingValue ?: "01969114300"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "01969114300")

    val appNotice: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "app_notice" }?.settingValue ?: "জরুরী রক্তের প্রয়োজনে EBloodDonation সবসময় আপনার পাশে আছে।"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "জরুরী রক্তের প্রয়োজনে EBloodDonation সবসময় আপনার পাশে আছে।")

    val emergencyNotice: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "emergency_notice" }?.settingValue ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val maintenanceMode: StateFlow<Boolean> = appSettingsList.map { list ->
        (list.find { it.settingKey == "maintenance_mode" }?.settingValue ?: "false").equals("true", ignoreCase = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val appLogoUrl: StateFlow<String> = appSettingsList.map { list ->
        list.find { it.settingKey == "app_logo_url" }?.settingValue ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Admin Panel Security & Management State
    val allDonorsList: StateFlow<List<DonorUser>> = repository.getAllDonorsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDonationRecords: StateFlow<List<DonationRecord>> = repository.getAllDonationRecordsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var isAdminLoggedIn = MutableStateFlow(false)
    var adminUsername = MutableStateFlow("ashikbillah4300@gmail.com")
    var adminPasswordInput = MutableStateFlow("")
    var adminLoginError = MutableStateFlow<String?>(null)
    var adminLoginAttempts = MutableStateFlow(0)
    var adminLockedUntil = MutableStateFlow(0L)

    var adminActiveTab = MutableStateFlow(0) // 0: Overview, 1: Users, 2: Requests, 3: Settings, 4: Sync
    var adminUserSearchQuery = MutableStateFlow("")
    var adminBloodGroupFilter = MutableStateFlow("ALL")
    var adminRequestStatusFilter = MutableStateFlow("ALL")

    // Admin Settings Form State
    var editDonationNumber = MutableStateFlow("01969114300")
    var editContactNumber = MutableStateFlow("01969114300")
    var editSupportNumber = MutableStateFlow("01969114300")
    var editAppName = MutableStateFlow("EBlood Donation")
    var editDepositMethod = MutableStateFlow("Wallet")
    var editAppNotice = MutableStateFlow("")
    var editEmergencyNotice = MutableStateFlow("")
    var editMaintenanceMode = MutableStateFlow(false)
    var editAppSosAlarmEnabled = MutableStateFlow(true)
    var editAppLogoUrl = MutableStateFlow("")

    // Website Control Form State (2nd section)
    var editWebsiteTitle = MutableStateFlow("EBlood Donation — রক্তদান ও সেবা প্ল্যাটফর্ম")
    var editWebsiteAnnouncement = MutableStateFlow("জরুরি রক্তদান ও তাৎক্ষণিক ডোনার খোঁজার নির্ভরযোগ্য প্ল্যাটফর্ম")
    var editWebsiteHeroTitle = MutableStateFlow("এক ক্লিকেই রক্তদাতা খুঁজুন, বাঁচান একটি মূল্যবান জীবন")
    var editWebsiteHeroSubtitle = MutableStateFlow("আপনার এরিয়ার ভেরিফায়েড রক্তদাতা, রিয়েল-টাইম ব্লাড রিকোয়েস্ট, লাইভ হসপিটাল ডিরেক্টরি এবং ২৪/৭ জরুরি হটলাইন সার্ভিস — সবই এখন একটি প্ল্যাটফর্মে।")
    var editWebsiteApkVersion = MutableStateFlow("v1.0 Live APK")
    var editWebsiteHelpline = MutableStateFlow("+8801969114300")
    var editWebsiteSupportEmail = MutableStateFlow("support@eblood.org")
    var editWebsiteShowPublicDonors = MutableStateFlow(true)
    var editWebsiteMaintenance = MutableStateFlow(false)
    var editWebsiteFooterText = MutableStateFlow("© 2026 EBlood Donation. সকল অধিকার সংরক্ষিত। জীবন রক্ষায় একটি মানবিক প্ল্যাটফর্ম।")

    var adminSettingsSavedFeedback = MutableStateFlow<String?>(null)

    // Online Backend & Cloud Sync State
    var backendServerUrl = MutableStateFlow(sessionManager.getBackendUrl())
    var isTestingConnection = MutableStateFlow(false)
    var connectionTestResult = MutableStateFlow<ConnectionTestResult?>(null)
    var isOnlineSyncing = MutableStateFlow(false)
    var onlineSyncFeedback = MutableStateFlow<String?>(null)

    val currentUser: StateFlow<DonorUser?> = repository.currentUserFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allRequests: StateFlow<List<BloodRequest>> = repository.allRequestsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeAlarmState: StateFlow<ActiveAlarmState> = EmergencyAlarmManager.alarmState

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeBottomTab = MutableStateFlow(0) // 0: Home, 1: Inbox, 2: History, 3: Profile
    val activeBottomTab: StateFlow<Int> = _activeBottomTab.asStateFlow()

    // Auth & OTP State
    var authTab = MutableStateFlow(AuthTab.NEW_DONOR)
    var inputName = MutableStateFlow("")
    var inputPhone = MutableStateFlow("")
    var showAlreadyRegisteredDialog = MutableStateFlow(false)
    var showOtpSentDialog = MutableStateFlow(false)
    var otpCode = MutableStateFlow("")
    var isVerifyingOtp = MutableStateFlow(false)
    var isSendingSms = MutableStateFlow(false)
    var otpCountdown = MutableStateFlow(60)
    var verificationIdState = MutableStateFlow<String?>(null)
    var generatedFallbackOtp = MutableStateFlow<String?>(null)
    var authErrorMessage = MutableStateFlow<String?>("")
    private var otpTimerJob: Job? = null

    // Profile Setup (for new registration)
    var setupBloodGroup = MutableStateFlow("O+")
    var setupLocation = MutableStateFlow("Uttara, Dhaka, Dhaka District")
    var setupAddress = MutableStateFlow("Sector 11, Uttara, Dhaka")

    // Emergency Request Creation
    var reqLocation = MutableStateFlow("Uttara, Dhaka, Dhaka District")
    var reqLatitude = MutableStateFlow(23.8786)
    var reqLongitude = MutableStateFlow(90.3766)
    var reqBloodGroup = MutableStateFlow("O+")
    var isNearMeSelected = MutableStateFlow(false)
    var reqAddressInput = MutableStateFlow("")
    var availableDonors = MutableStateFlow<List<DonorUser>>(emptyList())
    var selectedDonorIds = MutableStateFlow<Set<Long>>(emptySet())
    var donorSearchQuery = MutableStateFlow("")
    var sortNearestFirst = MutableStateFlow(true)
    var sentRequestDonorCount = MutableStateFlow(1)

    // Modals & Dialogs
    var showSupportDialog = MutableStateFlow(false)
    var showEditBloodGroupDialog = MutableStateFlow(false)
    var showUpdateLocationDialog = MutableStateFlow(false)

    // Inbox & History tabs
    var activeInboxTab = MutableStateFlow(InboxTab.PENDING)
    var activeHistoryTab = MutableStateFlow(HistoryTab.AS_DONOR)

    // Theme Settings (Light / Dark mode)
    var isDarkMode = MutableStateFlow(sessionManager.isDarkMode())
    var themeMode = MutableStateFlow(sessionManager.getThemeMode())

    fun toggleDarkMode() {
        val newDark = !isDarkMode.value
        isDarkMode.value = newDark
        val newMode = if (newDark) "DARK" else "LIGHT"
        themeMode.value = newMode
        sessionManager.setDarkMode(newDark)
        sessionManager.setThemeMode(newMode)
    }

    fun setThemeMode(mode: String) {
        themeMode.value = mode
        val isDark = when (mode) {
            "DARK" -> true
            "LIGHT" -> false
            else -> false
        }
        isDarkMode.value = isDark
        sessionManager.setThemeMode(mode)
        sessionManager.setDarkMode(isDark)
    }

    // Active revealed contact
    var revealedRequest = MutableStateFlow<BloodRequest?>(null)

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setBottomTab(tabIndex: Int) {
        _activeBottomTab.value = tabIndex
        _currentScreen.value = Screen.MAIN
    }

    // Auth Flows
    fun onAuthTabChanged(tab: AuthTab) {
        authTab.value = tab
    }

    fun onSendOtpClicked(activity: Activity? = null) {
        val phone = inputPhone.value.trim()
        val cleanDigits = phone.filter { it.isDigit() }
        if (cleanDigits.length < 10) {
            authErrorMessage.value = "অনুগ্রহ করে সঠিক মোবাইল নম্বর দিন"
            return
        }

        val formattedPhone = if (cleanDigits.startsWith("0")) cleanDigits else "0$cleanDigits"
        val e164Phone = if (cleanDigits.startsWith("880")) "+$cleanDigits"
        else if (cleanDigits.startsWith("0")) "+88$cleanDigits"
        else "+880$cleanDigits"

        authErrorMessage.value = null
        isSendingSms.value = true

        // Just like WhatsApp: immediately navigate to OTP verification screen
        proceedToEnterOtp()

        viewModelScope.launch {
            // Generate a dynamic, unique 6-digit OTP code
            val dynamicOtp = (100000..999999).random().toString()
            generatedFallbackOtp.value = dynamicOtp

            // Deliver notification to device notification tray
            val ctx: android.content.Context = activity ?: getApplication<Application>()
            OtpNotificationHelper.sendOtpNotification(ctx, dynamicOtp, formattedPhone)

            val isEmulator = DeviceUtils.isEmulator()

            // On real physical devices with cellular radios, attempt Firebase carrier SMS.
            // On emulators/virtual devices, skip PhoneAuthProvider to prevent Play Integrity (-14)
            // and unconfigured reCAPTCHA Enterprise errors.
            if (!isEmulator && activity != null) {
                try {
                    val auth = FirebaseAuth.getInstance()
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(e164Phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                isSendingSms.value = false
                                val code = credential.smsCode
                                if (!code.isNullOrEmpty()) {
                                    otpCode.value = code
                                    verifyOtp()
                                }
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                isSendingSms.value = false
                                verificationIdState.value = null
                            }

                            override fun onCodeSent(
                                verificationId: String,
                                token: PhoneAuthProvider.ForceResendingToken
                            ) {
                                isSendingSms.value = false
                                verificationIdState.value = verificationId
                            }
                        })
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                } catch (e: Exception) {
                    isSendingSms.value = false
                    verificationIdState.value = null
                }
            } else {
                isSendingSms.value = false
                verificationIdState.value = null
            }
        }
    }

    fun proceedToEnterOtp() {
        showOtpSentDialog.value = false
        otpCode.value = ""
        authErrorMessage.value = null
        _currentScreen.value = Screen.OTP_VERIFY
        startOtpCountdown()
    }

    private fun startOtpCountdown() {
        otpTimerJob?.cancel()
        otpCountdown.value = 60
        otpTimerJob = viewModelScope.launch {
            while (otpCountdown.value > 0) {
                delay(1000)
                otpCountdown.value -= 1
            }
        }
    }

    fun verifyOtp() {
        if (otpCode.value.length < 6) return
        isVerifyingOtp.value = true
        authErrorMessage.value = null

        val phone = inputPhone.value.trim()
        val cleanDigits = phone.filter { it.isDigit() }
        val formattedPhone = if (cleanDigits.startsWith("0")) cleanDigits else "0$cleanDigits"

        val verId = verificationIdState.value
        val code = otpCode.value.trim()

        if (!verId.isNullOrBlank()) {
            try {
                val credential = PhoneAuthProvider.getCredential(verId, code)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        isVerifyingOtp.value = false
                        if (task.isSuccessful) {
                            finishAuthFlow(formattedPhone)
                        } else {
                            if (code == generatedFallbackOtp.value) {
                                finishAuthFlow(formattedPhone)
                            } else {
                                authErrorMessage.value = "The OTP code you entered is invalid. Please check your SMS or notification."
                            }
                        }
                    }
                return
            } catch (e: Exception) {
                if (code == generatedFallbackOtp.value) {
                    isVerifyingOtp.value = false
                    finishAuthFlow(formattedPhone)
                    return
                }
                isVerifyingOtp.value = false
                authErrorMessage.value = e.localizedMessage ?: "Verification error"
                return
            }
        }

        // Dynamic 6-digit OTP verification (from Notification or configured carrier test code)
        if (code == generatedFallbackOtp.value) {
            viewModelScope.launch {
                delay(400)
                isVerifyingOtp.value = false
                finishAuthFlow(formattedPhone)
            }
        } else {
            isVerifyingOtp.value = false
            authErrorMessage.value = "Invalid OTP code. Please check your notification or SMS."
        }
    }

    private fun finishAuthFlow(formattedPhone: String) {
        viewModelScope.launch {
            val existingUser = repository.getDonorByPhone(formattedPhone)
            if (existingUser != null && existingUser.name.isNotBlank()) {
                val user = repository.signInUser(formattedPhone)
                if (user != null) {
                    sessionManager.saveSession(
                        phone = user.phone,
                        name = user.name,
                        bloodGroup = user.bloodGroup,
                        location = user.location,
                        address = user.address
                    )
                    _currentScreen.value = Screen.MAIN
                    return@launch
                }
            }

            // New number: prompt for user's name just like WhatsApp profile setup
            authErrorMessage.value = null
            inputName.value = ""
            setupLocation.value = "Uttara, Dhaka, Dhaka District"
            setupAddress.value = "Sector 11, Uttara, Dhaka"
            _currentScreen.value = Screen.NAME_INPUT
        }
    }

    fun submitName(name: String, goToProfileSetup: Boolean = true) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            authErrorMessage.value = "অনুগ্রহ করে আপনার নাম লিখুন"
            return
        }
        inputName.value = trimmed
        authErrorMessage.value = null

        val phone = inputPhone.value.trim()
        val cleanDigits = phone.filter { it.isDigit() }
        val formattedPhone = if (cleanDigits.startsWith("0")) cleanDigits else "0$cleanDigits"

        viewModelScope.launch {
            if (goToProfileSetup) {
                _currentScreen.value = Screen.PROFILE_SETUP
            } else {
                val user = repository.registerOrUpdateUser(
                    name = trimmed,
                    phone = formattedPhone,
                    bloodGroup = setupBloodGroup.value.ifBlank { "O+" },
                    location = setupLocation.value.ifBlank { "Uttara, Dhaka, Dhaka District" },
                    address = setupAddress.value.ifBlank { "Sector 11, Uttara, Dhaka" }
                )
                sessionManager.saveSession(
                    phone = user.phone,
                    name = user.name,
                    bloodGroup = user.bloodGroup,
                    location = user.location,
                    address = user.address
                )
                _currentScreen.value = Screen.MAIN
            }
        }
    }

    fun completeProfileSetup() {
        val phone = inputPhone.value.trim()
        val cleanDigits = phone.filter { it.isDigit() }
        val formattedPhone = if (cleanDigits.startsWith("0")) cleanDigits else "0$cleanDigits"
        val name = if (inputName.value.isNotBlank()) inputName.value.trim() else "User"

        viewModelScope.launch {
            val user = repository.registerOrUpdateUser(
                name = name,
                phone = formattedPhone,
                bloodGroup = setupBloodGroup.value,
                location = setupLocation.value,
                address = setupAddress.value
            )
            sessionManager.saveSession(
                phone = user.phone,
                name = user.name,
                bloodGroup = user.bloodGroup,
                location = user.location,
                address = user.address
            )
            _currentScreen.value = Screen.MAIN
        }
    }

    // Toggle Donor Availability
    fun toggleAvailability(isAvailable: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateCurrentUser(user.copy(isAvailable = isAvailable))
        }
    }

    fun toggleAlarmSound(enabled: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateCurrentUser(user.copy(alarmSoundEnabled = enabled))
        }
    }

    fun toggleAlarmVibration(enabled: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateCurrentUser(user.copy(alarmVibrationEnabled = enabled))
        }
    }

    fun updateBloodGroup(newGroup: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateCurrentUser(user.copy(bloodGroup = newGroup))
            sessionManager.updateProfile(bloodGroup = newGroup)
            showEditBloodGroupDialog.value = false
        }
    }

    fun updateLocation(newLocation: String, newAddress: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateCurrentUser(user.copy(location = newLocation, address = newAddress))
            sessionManager.updateProfile(location = newLocation, address = newAddress)
            showUpdateLocationDialog.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            sessionManager.clearSession()
            EmergencyAlarmManager.stopAlarm()
            inputName.value = ""
            inputPhone.value = ""
            _currentScreen.value = Screen.AUTH
        }
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return Math.round(r * c * 10.0) / 10.0
    }

    // Blood Request Creation Flow
    fun startCreateBloodRequest() {
        val user = currentUser.value
        val hasSession = sessionManager.isLoggedIn()
        if (user == null && !hasSession) {
            _currentScreen.value = Screen.AUTH
            return
        }
        isNearMeSelected.value = false
        reqAddressInput.value = ""
        reqLocation.value = user?.location ?: sessionManager.getLocation().ifEmpty { "Uttara, Dhaka, Dhaka District" }
        reqLatitude.value = user?.latitude ?: 23.8786
        reqLongitude.value = user?.longitude ?: 90.3766
        reqBloodGroup.value = "O+"
        _currentScreen.value = Screen.CREATE_REQUEST_STEP1
    }

    fun selectNearMeOption() {
        isNearMeSelected.value = true
        val user = currentUser.value
        reqLocation.value = "আমার কাছের জায়গা (Near Me)"
        reqLatitude.value = user?.latitude ?: 23.8786
        reqLongitude.value = user?.longitude ?: 90.3766
    }

    fun setCustomAddress(address: String) {
        isNearMeSelected.value = false
        reqAddressInput.value = address
        reqLocation.value = address.ifBlank { "Uttara, Dhaka" }
    }

    fun goToBloodGroupStep() {
        _currentScreen.value = Screen.CREATE_REQUEST_STEP2
    }

    fun searchDonorsForGroup(group: String) {
        reqBloodGroup.value = group
        val user = currentUser.value
        val currentPhone = (user?.phone?.ifBlank { null } ?: sessionManager.getPhone()).trim()
        val currentUserId = user?.id

        viewModelScope.launch {
            repository.getAvailableDonorsByBloodGroup(group).collect { localDonors ->
                // STRICT REQUIREMENT: Requester's own name/phone must NEVER appear in donor list
                val filtered = localDonors.filter { donor ->
                    !donor.isCurrentUser &&
                    (currentPhone.isEmpty() || donor.phone.trim() != currentPhone) &&
                    (currentUserId == null || donor.id != currentUserId)
                }

                // Also fetch online donors if backend is configured
                val onlineUrl = sessionManager.getBackendUrl()
                val candidateDonors = if (onlineUrl.isNotBlank()) {
                    try {
                        val remoteDonors = BackendNetworkManager.fetchDonors(
                            rawUrl = onlineUrl,
                            bloodGroup = group,
                            excludePhone = currentPhone,
                            latitude = reqLatitude.value,
                            longitude = reqLongitude.value,
                            nearMe = isNearMeSelected.value
                        )
                        val remoteFiltered = remoteDonors.filter { d ->
                            currentPhone.isEmpty() || d.phone.trim() != currentPhone
                        }
                        if (remoteFiltered.isNotEmpty()) remoteFiltered else filtered
                    } catch (e: Exception) {
                        filtered
                    }
                } else {
                    filtered
                }

                // Calculate distance and sort ascending: closest donor is #1, 2nd closest is #2, 3rd is #3...
                val reqLat = reqLatitude.value
                val reqLng = reqLongitude.value
                val sortedWithDistances = candidateDonors.map { donor ->
                    val dist = donor.distanceKm ?: calculateDistanceKm(reqLat, reqLng, donor.latitude, donor.longitude)
                    donor.copy(distanceKm = dist)
                }.sortedBy { it.distanceKm ?: 999.0 }

                availableDonors.value = sortedWithDistances
                selectedDonorIds.value = sortedWithDistances.map { it.id }.toSet()
                _currentScreen.value = Screen.CREATE_REQUEST_STEP3
            }
        }
    }

    fun toggleDonorSelection(donorId: Long) {
        val current = selectedDonorIds.value.toMutableSet()
        if (current.contains(donorId)) {
            current.remove(donorId)
        } else {
            current.add(donorId)
        }
        selectedDonorIds.value = current
    }

    fun selectAllDonors() {
        selectedDonorIds.value = availableDonors.value.map { it.id }.toSet()
    }

    fun clearDonorSelection() {
        selectedDonorIds.value = emptySet()
    }

    fun sendEmergencyAlert() {
        val user = currentUser.value
        val hasSession = sessionManager.isLoggedIn()
        if (user == null && !hasSession) {
            _currentScreen.value = Screen.AUTH
            return
        }
        val donorCount = if (selectedDonorIds.value.isNotEmpty()) selectedDonorIds.value.size else 1
        sentRequestDonorCount.value = donorCount

        viewModelScope.launch {
            val reqName = user?.name?.ifBlank { null } ?: sessionManager.getName().ifBlank { "Emergency Requester" }
            val reqPhone = user?.phone?.ifBlank { null } ?: sessionManager.getPhone().ifBlank { "" }

            val localRequest = BloodRequest(
                requesterName = reqName,
                requesterPhone = reqPhone,
                bloodGroup = reqBloodGroup.value,
                location = reqLocation.value,
                latitude = reqLatitude.value,
                longitude = reqLongitude.value,
                selectedDonorCount = donorCount,
                status = "PENDING",
                isUrgentAlertActive = true
            )

            val reqId = repository.createBloodRequest(localRequest)

            // Sync with backend PostgreSQL and dispatch targeted FCM notifications to compatible donors
            val backendUrl = sessionManager.getBackendUrl()
            val backendToken = sessionManager.getBackendToken()
            if (backendUrl.isNotBlank()) {
                try {
                    BackendNetworkManager.createBloodRequest(
                        rawUrl = backendUrl,
                        authToken = backendToken,
                        authPhone = reqPhone,
                        requestItem = localRequest.copy(id = reqId)
                    )
                } catch (e: Exception) {
                    // Non-blocking network sync
                }
            }

            _currentScreen.value = Screen.REQUEST_SENT_SUCCESS
        }
    }

    // Donor Actions: Accept or Reject
    fun acceptRequest(request: BloodRequest) {
        EmergencyAlarmManager.stopAlarm()
        val user = currentUser.value
        val donorName = user?.name?.ifBlank { null } ?: sessionManager.getName().ifBlank { "Verified Donor" }
        val donorPhone = user?.phone?.ifBlank { null } ?: sessionManager.getPhone()

        viewModelScope.launch {
            val updated = request.copy(
                status = "ACCEPTED",
                acceptedDonorName = donorName,
                acceptedDonorPhone = donorPhone,
                isUrgentAlertActive = false
            )
            repository.updateBloodRequest(updated)

            // Update status on backend PostgreSQL
            val backendUrl = sessionManager.getBackendUrl()
            val backendToken = sessionManager.getBackendToken()
            if (backendUrl.isNotBlank()) {
                try {
                    BackendNetworkManager.updateBloodRequestStatus(
                        rawUrl = backendUrl,
                        authToken = backendToken,
                        requestId = request.id,
                        status = "ACCEPTED",
                        acceptedDonorName = donorName,
                        acceptedDonorPhone = donorPhone
                    )
                } catch (_: Exception) {}
            }

            // Requirement 6: Requester gets alert
            EmergencyAlarmManager.triggerRequesterAcceptanceAlert(
                context = getApplication(),
                requestId = request.id,
                donorName = donorName,
                donorPhone = donorPhone,
                bloodGroup = request.bloodGroup,
                location = request.location,
                soundEnabled = user?.alarmSoundEnabled ?: true,
                vibrationEnabled = user?.alarmVibrationEnabled ?: true
            )

            revealedRequest.value = updated
            activeInboxTab.value = InboxTab.ACCEPTED
        }
    }

    fun rejectRequest(request: BloodRequest) {
        EmergencyAlarmManager.stopAlarm()
        viewModelScope.launch {
            val updated = request.copy(
                status = "REJECTED",
                isUrgentAlertActive = false
            )
            repository.updateBloodRequest(updated)

            val backendUrl = sessionManager.getBackendUrl()
            val backendToken = sessionManager.getBackendToken()
            if (backendUrl.isNotBlank()) {
                try {
                    BackendNetworkManager.updateBloodRequestStatus(
                        rawUrl = backendUrl,
                        authToken = backendToken,
                        requestId = request.id,
                        status = "REJECTED"
                    )
                } catch (_: Exception) {}
            }

            activeInboxTab.value = InboxTab.REJECTED
        }
    }

    // Requester confirms/accepts donor from Inbox and reveals donor phone number
    fun confirmDonorAndRevealPhone(request: BloodRequest) {
        EmergencyAlarmManager.stopAlarm()
        viewModelScope.launch {
            val updated = request.copy(requesterConfirmed = true)
            repository.updateBloodRequest(updated)

            val backendUrl = sessionManager.getBackendUrl()
            val backendToken = sessionManager.getBackendToken()
            if (backendUrl.isNotBlank()) {
                try {
                    BackendNetworkManager.updateBloodRequestStatus(
                        rawUrl = backendUrl,
                        authToken = backendToken,
                        requestId = request.id,
                        status = request.status,
                        acceptedDonorName = request.acceptedDonorName,
                        acceptedDonorPhone = request.acceptedDonorPhone,
                        requesterConfirmed = true
                    )
                } catch (_: Exception) {}
            }

            revealedRequest.value = updated
        }
    }

    // Manual delete notification/request
    fun deleteBloodRequest(requestId: Long) {
        viewModelScope.launch {
            repository.deleteBloodRequest(requestId)
            val backendUrl = sessionManager.getBackendUrl()
            val backendToken = sessionManager.getBackendToken()
            if (backendUrl.isNotBlank()) {
                try {
                    BackendNetworkManager.deleteBloodRequest(backendUrl, backendToken, requestId)
                } catch (_: Exception) {}
            }
        }
    }

    // Clear all notifications manually
    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllRequests()
        }
    }

    // Auto purge older than 2 days
    fun purgeOldRequests() {
        viewModelScope.launch {
            val twoDaysCutoff = System.currentTimeMillis() - (48 * 60 * 60 * 1000L)
            repository.clearOldRequests(twoDaysCutoff)
        }
    }

    // Requester confirms/opens acceptance alert
    fun confirmAcceptanceAlertAndRevealContact(requestId: Long) {
        EmergencyAlarmManager.stopAlarm()
        viewModelScope.launch {
            val req = repository.getRequestById(requestId)
            if (req != null) {
                revealedRequest.value = req
                _currentScreen.value = Screen.CONTACT_REVEALED
            }
        }
    }

    fun dismissAlarm() {
        EmergencyAlarmManager.stopAlarm()
    }

    fun completeDonation(request: BloodRequest) {
        viewModelScope.launch {
            repository.updateBloodRequest(request.copy(status = "COMPLETED"))
            repository.addDonationRecord(
                DonationRecord(
                    role = "DONOR",
                    bloodGroup = request.bloodGroup,
                    counterpartyName = request.requesterName,
                    counterpartyPhone = request.requesterPhone,
                    location = request.location,
                    status = "Completed"
                )
            )
            repository.addDonationRecord(
                DonationRecord(
                    role = "REQUESTER",
                    bloodGroup = request.bloodGroup,
                    counterpartyName = request.acceptedDonorName ?: "ashik",
                    counterpartyPhone = request.acceptedDonorPhone ?: "01969114300",
                    location = request.location,
                    status = "Completed"
                )
            )
            _currentScreen.value = Screen.MAIN
            _activeBottomTab.value = 2 // History tab
        }
    }

    // Admin Operations & Security
    fun openAdminPanel() {
        // Enforce password prompt every single time entering admin panel
        isAdminLoggedIn.value = false
        adminPasswordInput.value = ""
        adminLoginError.value = null
        _currentScreen.value = Screen.ADMIN_LOGIN
    }

    fun adminLogin(usernameInput: String, passwordInput: String) {
        val now = System.currentTimeMillis()
        if (adminLockedUntil.value > now) {
            val remainingSec = (adminLockedUntil.value - now) / 1000
            adminLoginError.value = "Too many failed attempts. Locked for ${remainingSec}s"
            return
        }

        val cleanUser = usernameInput.trim()
        val cleanPass = passwordInput.trim()

        if (cleanUser.isBlank() || cleanPass.isBlank()) {
            adminLoginError.value = "অনুগ্রহ করে অ্যাডমিন ইউজারনেম/ইমেইল এবং পাসওয়ার্ড দিন"
            return
        }

        viewModelScope.launch {
            val result = BackendNetworkManager.adminLogin(
                rawUrl = sessionManager.getBackendUrl(),
                username = cleanUser,
                pass = cleanPass
            )

            if (result.success) {
                if (!result.token.isNullOrBlank()) {
                    sessionManager.setBackendToken(result.token)
                }
                isAdminLoggedIn.value = true
                adminLoginError.value = null
                adminLoginAttempts.value = 0
                adminPasswordInput.value = ""

                // Populate form with current live database values
                editDonationNumber.value = donationNumber.value
                editDepositMethod.value = depositMethod.value
                editAppName.value = appName.value
                editContactNumber.value = contactNumber.value
                editSupportNumber.value = supportNumber.value
                editAppNotice.value = appNotice.value
                editEmergencyNotice.value = emergencyNotice.value
                editMaintenanceMode.value = maintenanceMode.value
                editAppLogoUrl.value = appLogoUrl.value
                _currentScreen.value = Screen.ADMIN_DASHBOARD
            } else {
                val attempts = adminLoginAttempts.value + 1
                adminLoginAttempts.value = attempts
                if (attempts >= 5) {
                    adminLockedUntil.value = now + 60_000 // lock for 60 seconds
                    adminLoginError.value = "সুরক্ষা সতর্কতা: একাধিকবার ভুল পাসওয়ার্ড দেওয়া হয়েছে। ১ মিনিট পর চেষ্টা করুন।"
                } else {
                    adminLoginError.value = result.message ?: "ভুল ইউজারনেম বা পাসওয়ার্ড! (${5 - attempts} বার চেষ্টা বাকি)"
                }
            }
        }
    }

    fun adminLogout() {
        // Clear auth state to force password on next entry
        isAdminLoggedIn.value = false
        adminPasswordInput.value = ""
        adminLoginError.value = null
        _currentScreen.value = Screen.MAIN
    }

    fun saveAppSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
            adminSettingsSavedFeedback.value = "Settings updated successfully! Changes are live in the app."
            delay(3000)
            adminSettingsSavedFeedback.value = null
        }
    }

    fun saveAppSettings() {
        viewModelScope.launch {
            val settingsMap = mapOf(
                "donation_number" to editDonationNumber.value.trim(),
                "deposit_number" to editDonationNumber.value.trim(),
                "deposit_method" to editDepositMethod.value.trim(),
                "app_name" to editAppName.value.trim(),
                "contact_number" to editContactNumber.value.trim(),
                "support_number" to editSupportNumber.value.trim(),
                "app_notice" to editAppNotice.value.trim(),
                "emergency_notice" to editEmergencyNotice.value.trim(),
                "maintenance_mode" to editMaintenanceMode.value.toString(),
                "app_sos_alarm_enabled" to editAppSosAlarmEnabled.value.toString(),
                "app_logo_url" to editAppLogoUrl.value.trim()
            )
            settingsMap.forEach { (k, v) ->
                repository.saveSetting(k, v)
            }

            // Synchronize with online backend PostgreSQL
            val backendUrl = sessionManager.getBackendUrl()
            val backendToken = sessionManager.getBackendToken()
            if (backendUrl.isNotBlank() && !backendToken.isNullOrBlank()) {
                try {
                    BackendNetworkManager.updateAdminSettings(backendUrl, backendToken, settingsMap)
                } catch (_: Exception) {}
            }

            adminSettingsSavedFeedback.value = "📱 Mobile App controls saved to database and synchronized live!"
            delay(3500)
            adminSettingsSavedFeedback.value = null
        }
    }

    fun saveWebsiteSettings() {
        viewModelScope.launch {
            repository.saveSetting("website_title", editWebsiteTitle.value.trim())
            repository.saveSetting("website_announcement", editWebsiteAnnouncement.value.trim())
            repository.saveSetting("website_hero_title", editWebsiteHeroTitle.value.trim())
            repository.saveSetting("website_hero_subtitle", editWebsiteHeroSubtitle.value.trim())
            repository.saveSetting("website_apk_version", editWebsiteApkVersion.value.trim())
            repository.saveSetting("website_helpline", editWebsiteHelpline.value.trim())
            repository.saveSetting("website_support_email", editWebsiteSupportEmail.value.trim())
            repository.saveSetting("website_show_public_donors", editWebsiteShowPublicDonors.value.toString())
            repository.saveSetting("website_maintenance", editWebsiteMaintenance.value.toString())
            repository.saveSetting("website_footer_text", editWebsiteFooterText.value.trim())
            adminSettingsSavedFeedback.value = "🌐 Website controls saved and published live!"
            delay(3500)
            adminSettingsSavedFeedback.value = null
        }
    }

    fun saveAllAdminSettings() {
        saveAppSettings()
        saveWebsiteSettings()
    }

    fun toggleDonorAccountStatus(donor: DonorUser) {
        viewModelScope.launch {
            val updated = donor.copy(isEnabled = !donor.isEnabled)
            repository.updateDonorStatus(updated)
        }
    }

    fun deleteDonorAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteDonor(id)
        }
    }

    fun updateBloodRequestStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateRequestStatus(id, newStatus)
        }
    }

    // Online Backend Testing & Sync Operations
    fun testBackendConnection(customUrl: String? = null) {
        val targetUrl = customUrl ?: backendServerUrl.value
        viewModelScope.launch {
            isTestingConnection.value = true
            connectionTestResult.value = null
            val result = BackendNetworkManager.testServerConnection(targetUrl)
            connectionTestResult.value = result
            isTestingConnection.value = false
            if (result.success) {
                sessionManager.setBackendUrl(targetUrl)
                backendServerUrl.value = BackendNetworkManager.sanitizeUrl(targetUrl)
            }
        }
    }

    fun syncFromOnlineBackend(customUrl: String? = null) {
        val targetUrl = customUrl ?: backendServerUrl.value
        viewModelScope.launch {
            isOnlineSyncing.value = true
            onlineSyncFeedback.value = "Connecting to online server..."
            val result = BackendNetworkManager.fetchAndSyncSettings(targetUrl, repository)
            isOnlineSyncing.value = false
            onlineSyncFeedback.value = result.message
            if (result.success) {
                sessionManager.setBackendUrl(targetUrl)
                backendServerUrl.value = BackendNetworkManager.sanitizeUrl(targetUrl)
            }
            delay(4000)
            onlineSyncFeedback.value = null
        }
    }

    fun setQuickBackendUrl(url: String) {
        backendServerUrl.value = url
        sessionManager.setBackendUrl(url)
    }
}
