package com.medhaverse.demo.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.io.Serializable

// ==================== USER & AUTH MODELS ====================

enum class UserRole {
    CITIZEN,
    COLLECTOR,
    RECYCLER,
    ADMIN,
    INSTITUTION
}

@Parcelize
data class User(
    val id: String = "",
    val phone: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CITIZEN,
    val profileImageUrl: String = "",
    val greenCredits: Int = 0,
    val totalScans: Int = 0,
    val carbonSaved: Double = 0.0,
    val address: String = "",
    val city: String = "",
    val pincode: String = "",
    val verified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val language: String = "en" // en, hi
) : Parcelable

// ==================== WASTE CLASSIFICATION MODELS ====================

enum class WasteCategory {
    PLASTIC,
    METAL,
    PAPER,
    ORGANIC,
    E_WASTE,
    GLASS,
    TEXTILE,
    HAZARDOUS
}

enum class WasteQuality {
    CLEAN,
    DIRTY,
    MIXED
}

// Note: WasteResult is defined in WasteAI.kt

data class DisposalInfo(
    val category: WasteCategory,
    val method: String,
    val instructions: List<String>,
    val nearestBinType: String,
    val carbonSaved: Double,
    val greenCreditsEarned: Int,
    val tips: List<String>
)

// ==================== WASTE SCAN & TRACKING ====================

@Parcelize
data class WasteScan(
    val id: String = "",
    val userId: String = "",
    val result: @RawValue WasteResult? = null,
    val disposalInfo: @RawValue DisposalInfo? = null,
    val location: LocationData? = null,
    val qrCode: String = "",
    val collectorId: String = "",
    val verified: Boolean = false,
    val synced: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
) : Parcelable

// ==================== GREEN CREDITS & REWARDS ====================

enum class CreditTransactionType {
    SCAN,
    DISPOSAL,
    PICKUP,
    REFERRAL,
    CHALLENGE,
    REWARD,
    REDEMPTION
}

data class CreditTransaction(
    val id: String = "",
    val userId: String = "",
    val type: CreditTransactionType,
    val amount: Int,
    val description: String,
    val scanId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Reward(
    val id: String = "",
    val title: String = "",
    val description: String,
    val creditsCost: Int,
    val imageUrl: String = "",
    val partner: String = "",
    val category: String = "", // discount, voucher, badge
    val available: Boolean = true
)

data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String,
    val iconUrl: String = "",
    val requirement: String = "",
    val earned: Boolean = false
)

// ==================== PICKUP & SCHEDULING ====================

enum class PickupStatus {
    REQUESTED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

data class PickupRequest(
    val id: String = "",
    val userId: String = "",
    val wasteTypes: List<WasteCategory>,
    val estimatedWeight: Double = 0.0,
    val location: LocationData? = null,
    val scheduledTime: Long = 0,
    val status: PickupStatus = PickupStatus.REQUESTED,
    val collectorId: String = "",
    val notes: String = "",
    val imageUrls: List<String> = emptyList(),
    val qrCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==================== COLLECTION CENTER & BINS ====================

data class CollectionCenter(
    val id: String = "",
    val name: String = "",
    val location: LocationData? = null,
    val wasteTypes: List<WasteCategory>,
    val openHours: String = "",
    val phone: String = "",
    val rating: Float = 0f,
    val distanceKm: Double = 0.0
)

data class SmartBin(
    val id: String = "",
    val qrCode: String = "",
    val location: LocationData? = null,
    val wasteType: WasteCategory,
    val fillLevel: Int = 0, // 0-100
    val lastCollection: Long = 0,
    val status: String = "active" // active, full, maintenance
)

// ==================== COLLECTOR & ROUTES ====================

data class CollectionRoute(
    val id: String = "",
    val collectorId: String = "",
    val pickups: List<PickupRequest>,
    val totalDistance: Double = 0.0,
    val estimatedTime: Int = 0, // minutes
    val status: String = "pending",
    val date: Long = System.currentTimeMillis()
)

data class CollectionLog(
    val id: String = "",
    val collectorId: String = "",
    val pickupId: String = "",
    val binId: String = "",
    val wasteCategory: WasteCategory,
    val weight: Double = 0.0,
    val quality: WasteQuality,
    val imageUrl: String = "",
    val qrScanned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// ==================== ANALYTICS & DASHBOARD ====================

data class UserStats(
    val userId: String = "",
    val totalScans: Int = 0,
    val totalWeight: Double = 0.0,
    val carbonSaved: Double = 0.0,
    val greenCredits: Int = 0,
    val accuracy: Float = 0f,
    val rank: Int = 0,
    val streak: Int = 0,
    val badges: List<Badge> = emptyList(),
    val monthlyData: Map<String, Int> = emptyMap(),
    val categoryBreakdown: Map<WasteCategory, Int> = emptyMap()
)

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val name: String,
    val profileImageUrl: String = "",
    val greenCredits: Int,
    val carbonSaved: Double,
    val isCurrentUser: Boolean = false
)

// ==================== COMMUNITY & LEARNING ====================

data class LearningModule(
    val id: String = "",
    val title: String = "",
    val description: String,
    val content: String,
    val imageUrl: String = "",
    val duration: Int = 0, // minutes
    val category: String = "",
    val completed: Boolean = false,
    val creditsReward: Int = 0
)

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val reward: Int,
    val startDate: Long,
    val endDate: Long,
    val type: String = "", // daily, weekly, monthly
    val participants: Int = 0
)

data class CommunityNews(
    val id: String = "",
    val title: String = "",
    val content: String,
    val imageUrl: String = "",
    val author: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "" // news, event, tip
)

// ==================== INSTITUTION & MUNICIPALITY ====================

data class Institution(
    val id: String = "",
    val name: String = "",
    val type: String = "", // college, mall, hostel, hospital
    val location: LocationData? = null,
    val adminUsers: List<String> = emptyList(),
    val totalWasteCollected: Double = 0.0,
    val carbonSaved: Double = 0.0,
    val complianceScore: Float = 0f
)

data class MunicipalityDashboard(
    val municipalityId: String = "",
    val totalWaste: Double = 0.0,
    val wasteByCategory: Map<WasteCategory, Double> = emptyMap(),
    val activeCollectors: Int = 0,
    val completedPickups: Int = 0,
    val citizens: Int = 0,
    val carbonSaved: Double = 0.0,
    val recyclingRate: Float = 0f,
    val alerts: List<String> = emptyList()
)

// ==================== QR CODE & VERIFICATION ====================

data class QRCodeData(
    val id: String = "",
    val type: String = "", // household, bin, pickup
    val ownerId: String = "",
    val location: LocationData? = null,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

data class VerificationRecord(
    val id: String = "",
    val scanId: String = "",
    val collectorId: String = "",
    val citizenId: String = "",
    val qrCode: String = "",
    val verified: Boolean = false,
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val signature: String = ""
)

// ==================== NOTIFICATIONS ====================

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // pickup, reward, challenge, news
    val read: Boolean = false,
    val actionUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)