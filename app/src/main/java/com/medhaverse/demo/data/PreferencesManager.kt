package com.medhaverse.demo.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "medhaverse_prefs",
        Context.MODE_PRIVATE
    )

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // ==================== AUTHENTICATION ====================

    fun saveUser(user: User) {
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("user_phone", user.phone)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putString("user_role", user.role.name)
            putString("user_profile_image", user.profileImageUrl)
            putInt("user_green_credits", user.greenCredits)
            putInt("user_total_scans", user.totalScans)
            putFloat("user_carbon_saved", user.carbonSaved.toFloat())
            putString("user_address", user.address)
            putString("user_city", user.city)
            putString("user_pincode", user.pincode)
            putBoolean("user_verified", user.verified)
            putLong("user_created_at", user.createdAt)
            putString("user_language", user.language)
            apply()
        }
        _currentUser.value = user
    }

    fun getUser(): User? {
        val userId = prefs.getString("user_id", null) ?: return null
        return User(
            id = userId,
            phone = prefs.getString("user_phone", "") ?: "",
            name = prefs.getString("user_name", "") ?: "",
            email = prefs.getString("user_email", "") ?: "",
            role = UserRole.valueOf(
                prefs.getString("user_role", UserRole.CITIZEN.name) ?: UserRole.CITIZEN.name
            ),
            profileImageUrl = prefs.getString("user_profile_image", "") ?: "",
            greenCredits = prefs.getInt("user_green_credits", 0),
            totalScans = prefs.getInt("user_total_scans", 0),
            carbonSaved = prefs.getFloat("user_carbon_saved", 0f).toDouble(),
            address = prefs.getString("user_address", "") ?: "",
            city = prefs.getString("user_city", "") ?: "",
            pincode = prefs.getString("user_pincode", "") ?: "",
            verified = prefs.getBoolean("user_verified", false),
            createdAt = prefs.getLong("user_created_at", System.currentTimeMillis()),
            language = prefs.getString("user_language", "en") ?: "en"
        ).also {
            _currentUser.value = it
        }
    }

    fun isLoggedIn(): Boolean = prefs.getString("user_id", null) != null

    fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    // ==================== ONBOARDING ====================

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean("onboarding_completed", false)

    // ==================== APP SETTINGS ====================

    fun setLanguage(languageCode: String) {
        prefs.edit().putString("app_language", languageCode).apply()
        _currentUser.value?.let { user ->
            saveUser(user.copy(language = languageCode))
        }
    }

    fun getLanguage(): String = prefs.getString("app_language", "en") ?: "en"

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)

    fun setLocationPermissionGranted(granted: Boolean) {
        prefs.edit().putBoolean("location_permission", granted).apply()
    }

    fun isLocationPermissionGranted(): Boolean = prefs.getBoolean("location_permission", false)

    fun setCameraPermissionGranted(granted: Boolean) {
        prefs.edit().putBoolean("camera_permission", granted).apply()
    }

    fun isCameraPermissionGranted(): Boolean = prefs.getBoolean("camera_permission", false)

    // ==================== STATS & TRACKING ====================

    fun incrementScanCount() {
        val currentCount = prefs.getInt("scan_count", 0)
        prefs.edit().putInt("scan_count", currentCount + 1).apply()
    }

    fun getScanCount(): Int = prefs.getInt("scan_count", 0)

    fun addGreenCredits(credits: Int) {
        val currentCredits = prefs.getInt("green_credits", 0)
        prefs.edit().putInt("green_credits", currentCredits + credits).apply()
    }

    fun getGreenCredits(): Int = prefs.getInt("green_credits", 0)

    fun addCarbonSaved(carbon: Double) {
        val currentCarbon = prefs.getFloat("carbon_saved", 0f)
        prefs.edit().putFloat("carbon_saved", currentCarbon + carbon.toFloat()).apply()
    }

    fun getCarbonSaved(): Double = prefs.getFloat("carbon_saved", 0f).toDouble()

    fun updateStreak(days: Int) {
        prefs.edit().putInt("streak_days", days).apply()
    }

    fun getStreak(): Int = prefs.getInt("streak_days", 0)

    fun setLastScanDate(timestamp: Long) {
        prefs.edit().putLong("last_scan_date", timestamp).apply()
    }

    fun getLastScanDate(): Long = prefs.getLong("last_scan_date", 0)

    // ==================== OFFLINE MODE ====================

    fun getPendingSyncCount(): Int = prefs.getInt("pending_sync_count", 0)

    fun incrementPendingSyncCount() {
        val count = getPendingSyncCount()
        prefs.edit().putInt("pending_sync_count", count + 1).apply()
    }

    fun decrementPendingSyncCount() {
        val count = getPendingSyncCount()
        if (count > 0) {
            prefs.edit().putInt("pending_sync_count", count - 1).apply()
        }
    }

    fun clearPendingSyncCount() {
        prefs.edit().putInt("pending_sync_count", 0).apply()
    }

    // ==================== FIRST TIME FLAGS ====================

    fun isFirstLaunch(): Boolean = prefs.getBoolean("first_launch", true)

    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean("first_launch", false).apply()
    }

    fun shouldShowTutorial(feature: String): Boolean =
        prefs.getBoolean("tutorial_$feature", true)

    fun setTutorialShown(feature: String) {
        prefs.edit().putBoolean("tutorial_$feature", false).apply()
    }
}
