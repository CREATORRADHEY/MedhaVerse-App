package com.medhaverse.demo.data

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlin.random.Random

class MedhaVerseRepository(private val context: Context) {

    private val prefsManager = PreferencesManager(context)
    private val wasteAI = WasteAI

    // ==================== USER MANAGEMENT ====================

    fun getCurrentUser(): User? = prefsManager.getUser()

    fun saveUser(user: User) = prefsManager.saveUser(user)

    fun isLoggedIn(): Boolean = prefsManager.isLoggedIn()

    fun logout() = prefsManager.logout()

    // Mock OTP Login
    suspend fun sendOTP(phone: String): Flow<Boolean> = flow {
        delay(2000) // Simulate network
        emit(true)
    }

    suspend fun verifyOTP(phone: String, otp: String): Flow<User?> = flow {
        delay(1500)
        if (otp == "123456") { // Mock OTP
            val user = User(
                id = "user_${System.currentTimeMillis()}",
                phone = phone,
                name = "",
                role = UserRole.CITIZEN,
                greenCredits = 0,
                totalScans = 0,
                carbonSaved = 0.0
            )
            prefsManager.saveUser(user)
            emit(user)
        } else {
            emit(null)
        }
    }

    suspend fun completeProfile(
        name: String,
        email: String,
        address: String,
        city: String,
        pincode: String
    ): Flow<Boolean> = flow {
        delay(1000)
        getCurrentUser()?.let { user ->
            val updatedUser = user.copy(
                name = name,
                email = email,
                address = address,
                city = city,
                pincode = pincode
            )
            saveUser(updatedUser)
            emit(true)
        } ?: emit(false)
    }

    suspend fun updateUserRole(role: UserRole): Flow<Boolean> = flow {
        delay(500)
        getCurrentUser()?.let { user ->
            saveUser(user.copy(role = role))
            emit(true)
        } ?: emit(false)
    }

    // ==================== WASTE SCANNING & CLASSIFICATION ====================

    suspend fun classifyWaste(image: Bitmap): Flow<WasteResult?> = flow {
        delay(2000) // AI processing time

        // Use actual AI classification
        val result = wasteAI.classifyWaste(image)
        emit(result)
    }

    fun getDisposalInfo(result: WasteResult): DisposalInfo {
        val categoryStr = result.category.uppercase()
        val category = when {
            categoryStr.contains("PLASTIC") -> WasteCategory.PLASTIC
            categoryStr.contains("METAL") -> WasteCategory.METAL
            categoryStr.contains("PAPER") -> WasteCategory.PAPER
            categoryStr.contains("ORGANIC") -> WasteCategory.ORGANIC
            categoryStr.contains("EWASTE") || categoryStr.contains("E-WASTE") -> WasteCategory.E_WASTE
            categoryStr.contains("GLASS") -> WasteCategory.GLASS
            categoryStr.contains("TEXTILE") -> WasteCategory.TEXTILE
            categoryStr.contains("HAZARD") -> WasteCategory.HAZARDOUS
            else -> WasteCategory.PLASTIC
        }

        return when (category) {
            WasteCategory.PLASTIC -> DisposalInfo(
                category = category,
                method = "Recycle",
                instructions = listOf(
                    "Clean and dry the plastic item",
                    "Remove labels if possible",
                    "Place in blue recycling bin",
                    "Do not mix with other waste types"
                ),
                nearestBinType = "Blue Recycling Bin",
                carbonSaved = 0.8,
                greenCreditsEarned = 25,
                tips = listOf(
                    "Avoid single-use plastics",
                    "Reuse plastic containers when possible",
                    "Choose biodegradable alternatives"
                )
            )

            WasteCategory.ORGANIC -> DisposalInfo(
                category = category,
                method = "Compost",
                instructions = listOf(
                    "Separate food waste from packaging",
                    "Place in green organic waste bin",
                    "Can be composted at home",
                    "Great for garden fertilizer"
                ),
                nearestBinType = "Green Organic Bin",
                carbonSaved = 1.2,
                greenCreditsEarned = 30,
                tips = listOf(
                    "Start home composting",
                    "Avoid wasting food",
                    "Use compost for plants"
                )
            )

            WasteCategory.PAPER -> DisposalInfo(
                category = category,
                method = "Recycle",
                instructions = listOf(
                    "Keep paper clean and dry",
                    "Remove plastic windows from envelopes",
                    "Place in yellow recycling bin",
                    "Flatten cardboard boxes"
                ),
                nearestBinType = "Yellow Paper Bin",
                carbonSaved = 0.6,
                greenCreditsEarned = 20,
                tips = listOf(
                    "Go digital when possible",
                    "Use both sides of paper",
                    "Recycle newspapers and magazines"
                )
            )

            WasteCategory.E_WASTE -> DisposalInfo(
                category = category,
                method = "Special E-Waste Facility",
                instructions = listOf(
                    "Do NOT throw in regular bins",
                    "Take to authorized e-waste collection center",
                    "Remove batteries if possible",
                    "Data wipe devices before disposal"
                ),
                nearestBinType = "E-Waste Collection Center",
                carbonSaved = 2.5,
                greenCreditsEarned = 50,
                tips = listOf(
                    "Donate working electronics",
                    "Repair instead of replace",
                    "Buy refurbished devices"
                )
            )

            WasteCategory.METAL -> DisposalInfo(
                category = category,
                method = "Recycle",
                instructions = listOf(
                    "Clean metal items",
                    "Separate aluminum and steel",
                    "Place in designated metal bin",
                    "Can be sold to scrap dealers"
                ),
                nearestBinType = "Metal Recycling Bin",
                carbonSaved = 1.5,
                greenCreditsEarned = 35,
                tips = listOf(
                    "Metal is 100% recyclable",
                    "Collect and sell bulk scrap",
                    "Reuse metal containers"
                )
            )

            WasteCategory.GLASS -> DisposalInfo(
                category = category,
                method = "Recycle",
                instructions = listOf(
                    "Rinse glass items",
                    "Remove caps and lids",
                    "Place in glass recycling bin",
                    "Wrap broken glass in newspaper"
                ),
                nearestBinType = "Glass Recycling Bin",
                carbonSaved = 0.5,
                greenCreditsEarned = 15,
                tips = listOf(
                    "Reuse glass jars",
                    "Glass can be recycled infinitely",
                    "Avoid breaking glass unnecessarily"
                )
            )

            WasteCategory.TEXTILE -> DisposalInfo(
                category = category,
                method = "Donate or Recycle",
                instructions = listOf(
                    "Wash and dry textiles",
                    "Donate wearable clothes to NGOs",
                    "Recycle torn textiles",
                    "Can be upcycled into new items"
                ),
                nearestBinType = "Textile Collection Box",
                carbonSaved = 1.0,
                greenCreditsEarned = 25,
                tips = listOf(
                    "Donate old clothes",
                    "Repair instead of discarding",
                    "Buy sustainable fashion"
                )
            )

            WasteCategory.HAZARDOUS -> DisposalInfo(
                category = category,
                method = "Hazardous Waste Facility",
                instructions = listOf(
                    "NEVER throw in regular bins",
                    "Contact municipal hazardous waste center",
                    "Keep in original container if possible",
                    "Label clearly if transferring containers"
                ),
                nearestBinType = "Hazardous Waste Center",
                carbonSaved = 3.0,
                greenCreditsEarned = 60,
                tips = listOf(
                    "Use eco-friendly alternatives",
                    "Properly store hazardous materials",
                    "Follow local disposal guidelines"
                )
            )
        }
    }

    private fun getSubType(category: WasteCategory): String = when (category) {
        WasteCategory.PLASTIC -> listOf(
            "PET Bottle",
            "HDPE Container",
            "Plastic Bag",
            "Polystyrene"
        ).random()

        WasteCategory.ORGANIC -> listOf(
            "Food Waste",
            "Garden Waste",
            "Vegetable Peels",
            "Fruit Scraps"
        ).random()

        WasteCategory.PAPER -> listOf("Newspaper", "Cardboard", "Office Paper", "Magazine").random()
        WasteCategory.E_WASTE -> listOf("Mobile Phone", "Computer", "Battery", "Charger").random()
        WasteCategory.METAL -> listOf(
            "Aluminum Can",
            "Steel Container",
            "Copper Wire",
            "Iron Scrap"
        ).random()

        WasteCategory.GLASS -> listOf(
            "Glass Bottle",
            "Window Glass",
            "Jar",
            "Broken Glass"
        ).random()

        WasteCategory.TEXTILE -> listOf(
            "Cotton Cloth",
            "Synthetic Fabric",
            "Old Clothes",
            "Bedding"
        ).random()

        WasteCategory.HAZARDOUS -> listOf("Paint", "Pesticide", "Battery Acid", "Chemical").random()
    }

    suspend fun saveScan(scan: WasteScan): Flow<Boolean> = flow {
        delay(500)
        // Save locally (mock - in real app save to local DB)
        prefsManager.incrementScanCount()

        val disposalInfo = scan.disposalInfo
        if (disposalInfo != null) {
            prefsManager.addGreenCredits(disposalInfo.greenCreditsEarned)
            prefsManager.addCarbonSaved(disposalInfo.carbonSaved)
        }

        // Update user stats
        getCurrentUser()?.let { user ->
            val updated = user.copy(
                totalScans = user.totalScans + 1,
                greenCredits = user.greenCredits + (disposalInfo?.greenCreditsEarned ?: 0),
                carbonSaved = user.carbonSaved + (disposalInfo?.carbonSaved ?: 0.0)
            )
            saveUser(updated)
        }

        emit(true)
    }

    // ==================== PICKUP MANAGEMENT ====================

    suspend fun requestPickup(request: PickupRequest): Flow<String> = flow {
        delay(1500)
        val pickupId = "pickup_${System.currentTimeMillis()}"
        // Save to local DB / Firebase
        emit(pickupId)
    }

    suspend fun getMyPickups(): Flow<List<PickupRequest>> = flow {
        delay(1000)
        val userId = getCurrentUser()?.id ?: ""

        // Mock data
        val pickups = listOf(
            PickupRequest(
                id = "p1",
                userId = userId,
                wasteTypes = listOf(WasteCategory.PLASTIC, WasteCategory.PAPER),
                estimatedWeight = 5.0,
                location = LocationData(28.6139, 77.2090, "New Delhi"),
                scheduledTime = System.currentTimeMillis() + 86400000,
                status = PickupStatus.SCHEDULED,
                collectorId = "collector_1",
                notes = "Please call before arrival"
            ),
            PickupRequest(
                id = "p2",
                userId = userId,
                wasteTypes = listOf(WasteCategory.E_WASTE),
                estimatedWeight = 2.0,
                location = LocationData(28.6139, 77.2090, "New Delhi"),
                scheduledTime = System.currentTimeMillis() + 172800000,
                status = PickupStatus.REQUESTED,
                notes = "Old laptop and chargers"
            )
        )

        emit(pickups)
    }

    suspend fun cancelPickup(pickupId: String): Flow<Boolean> = flow {
        delay(800)
        emit(true)
    }

    // ==================== COLLECTION CENTERS ====================

    suspend fun getNearbyCollectionCenters(
        latitude: Double,
        longitude: Double
    ): Flow<List<CollectionCenter>> = flow {
        delay(1000)

        val centers = listOf(
            CollectionCenter(
                id = "center_1",
                name = "Green Recycling Hub",
                location = LocationData(latitude + 0.01, longitude + 0.01, "Near Market"),
                wasteTypes = listOf(
                    WasteCategory.PLASTIC,
                    WasteCategory.PAPER,
                    WasteCategory.METAL
                ),
                openHours = "Mon-Sat 9AM-6PM",
                phone = "+91-9876543210",
                rating = 4.5f,
                distanceKm = 1.2
            ),
            CollectionCenter(
                id = "center_2",
                name = "E-Waste Collection Point",
                location = LocationData(latitude + 0.02, longitude - 0.01, "Industrial Area"),
                wasteTypes = listOf(WasteCategory.E_WASTE),
                openHours = "Mon-Fri 10AM-5PM",
                phone = "+91-9876543211",
                rating = 4.8f,
                distanceKm = 2.5
            ),
            CollectionCenter(
                id = "center_3",
                name = "Municipal Waste Center",
                location = LocationData(latitude - 0.01, longitude + 0.02, "City Center"),
                wasteTypes = WasteCategory.values().toList(),
                openHours = "Daily 8AM-8PM",
                phone = "+91-9876543212",
                rating = 4.2f,
                distanceKm = 3.8
            )
        )

        emit(centers)
    }

    // ==================== GREEN CREDITS & REWARDS ====================

    fun getGreenCredits(): Int = prefsManager.getGreenCredits()

    suspend fun getCreditTransactions(): Flow<List<CreditTransaction>> = flow {
        delay(800)
        val userId = getCurrentUser()?.id ?: ""

        // Mock transactions
        val transactions = listOf(
            CreditTransaction(
                id = "t1",
                userId = userId,
                type = CreditTransactionType.SCAN,
                amount = 25,
                description = "Scanned plastic waste",
                timestamp = System.currentTimeMillis() - 86400000
            ),
            CreditTransaction(
                id = "t2",
                userId = userId,
                type = CreditTransactionType.DISPOSAL,
                amount = 30,
                description = "Proper organic waste disposal",
                timestamp = System.currentTimeMillis() - 172800000
            ),
            CreditTransaction(
                id = "t3",
                userId = userId,
                type = CreditTransactionType.CHALLENGE,
                amount = 50,
                description = "Completed weekly challenge",
                timestamp = System.currentTimeMillis() - 259200000
            )
        )

        emit(transactions)
    }

    suspend fun getAvailableRewards(): Flow<List<Reward>> = flow {
        delay(1000)

        val rewards = listOf(
            Reward(
                id = "r1",
                title = "₹50 Amazon Voucher",
                description = "Get ₹50 off on Amazon",
                creditsCost = 500,
                imageUrl = "",
                partner = "Amazon",
                category = "voucher",
                available = true
            ),
            Reward(
                id = "r2",
                title = "10% Off on Flipkart",
                description = "Save 10% on your next purchase",
                creditsCost = 300,
                imageUrl = "",
                partner = "Flipkart",
                category = "discount",
                available = true
            ),
            Reward(
                id = "r3",
                title = "Eco Warrior Badge",
                description = "Exclusive badge for top recyclers",
                creditsCost = 1000,
                imageUrl = "",
                partner = "MedhaVerse",
                category = "badge",
                available = true
            ),
            Reward(
                id = "r4",
                title = "Free Movie Ticket",
                description = "Get 1 free movie ticket at PVR",
                creditsCost = 750,
                imageUrl = "",
                partner = "PVR",
                category = "voucher",
                available = true
            )
        )

        emit(rewards)
    }

    suspend fun redeemReward(rewardId: String): Flow<Boolean> = flow {
        delay(1500)
        // Check if user has enough credits
        val reward = getAvailableRewards().toString() // Simplified
        emit(true)
    }

    // ==================== ANALYTICS & DASHBOARD ====================

    suspend fun getUserStats(): Flow<UserStats> = flow {
        delay(1000)
        val user = getCurrentUser()

        val stats = UserStats(
            userId = user?.id ?: "",
            totalScans = user?.totalScans ?: 47,
            totalWeight = 35.5,
            carbonSaved = user?.carbonSaved ?: 15.8,
            greenCredits = user?.greenCredits ?: 1250,
            accuracy = 92f,
            rank = 23,
            streak = prefsManager.getStreak(),
            badges = getBadges().toList().first(),
            monthlyData = mapOf(
                "Jan" to 12,
                "Feb" to 18,
                "Mar" to 25,
                "Apr" to 30,
                "May" to 35,
                "Jun" to 42
            ),
            categoryBreakdown = mapOf(
                WasteCategory.PLASTIC to 20,
                WasteCategory.PAPER to 15,
                WasteCategory.ORGANIC to 8,
                WasteCategory.METAL to 3,
                WasteCategory.E_WASTE to 1
            )
        )

        emit(stats)
    }

    suspend fun getLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        delay(1200)
        val currentUserId = getCurrentUser()?.id ?: ""

        val leaderboard = listOf(
            LeaderboardEntry(1, "user1", "Priya Sharma", "", 5200, 45.2, false),
            LeaderboardEntry(2, "user2", "Rahul Kumar", "", 4850, 42.1, false),
            LeaderboardEntry(3, "user3", "Sneha Patel", "", 4200, 38.5, false),
            LeaderboardEntry(
                23,
                currentUserId,
                getCurrentUser()?.name ?: "You",
                "",
                1250,
                15.8,
                true
            ),
            LeaderboardEntry(24, "user4", "Amit Singh", "", 1180, 14.2, false),
            LeaderboardEntry(25, "user5", "Neha Gupta", "", 1050, 12.9, false)
        )

        emit(leaderboard)
    }

    // ==================== COMMUNITY & LEARNING ====================

    suspend fun getLearningModules(): Flow<List<LearningModule>> = flow {
        delay(800)

        val modules = listOf(
            LearningModule(
                id = "m1",
                title = "Plastic Recycling 101",
                description = "Learn how to properly recycle different types of plastic",
                content = "Detailed content about plastic recycling...",
                imageUrl = "",
                duration = 5,
                category = "Basics",
                completed = false,
                creditsReward = 20
            ),
            LearningModule(
                id = "m2",
                title = "Composting at Home",
                description = "Turn your organic waste into nutrient-rich compost",
                content = "Step-by-step guide to composting...",
                imageUrl = "",
                duration = 8,
                category = "Advanced",
                completed = false,
                creditsReward = 30
            ),
            LearningModule(
                id = "m3",
                title = "E-Waste Hazards",
                description = "Understanding the dangers of improper e-waste disposal",
                content = "Important information about e-waste...",
                imageUrl = "",
                duration = 6,
                category = "Safety",
                completed = true,
                creditsReward = 25
            )
        )

        emit(modules)
    }

    suspend fun getChallenges(): Flow<List<Challenge>> = flow {
        delay(800)

        val now = System.currentTimeMillis()
        val challenges = listOf(
            Challenge(
                id = "c1",
                title = "Daily Scanner",
                description = "Scan 5 waste items today",
                targetCount = 5,
                currentCount = 3,
                reward = 50,
                startDate = now,
                endDate = now + 86400000,
                type = "daily",
                participants = 1250
            ),
            Challenge(
                id = "c2",
                title = "Weekly Recycler",
                description = "Recycle 20 items this week",
                targetCount = 20,
                currentCount = 12,
                reward = 150,
                startDate = now - 259200000,
                endDate = now + 345600000,
                type = "weekly",
                participants = 850
            ),
            Challenge(
                id = "c3",
                title = "Refer a Friend",
                description = "Invite 3 friends to MedhaVerse",
                targetCount = 3,
                currentCount = 1,
                reward = 200,
                startDate = now,
                endDate = now + 2592000000,
                type = "monthly",
                participants = 2100
            )
        )

        emit(challenges)
    }

    suspend fun getCommunityNews(): Flow<List<CommunityNews>> = flow {
        delay(700)

        val news = listOf(
            CommunityNews(
                id = "n1",
                title = "New E-Waste Collection Drive",
                content = "Join us this Saturday for a city-wide e-waste collection campaign...",
                imageUrl = "",
                author = "MedhaVerse Team",
                timestamp = System.currentTimeMillis() - 86400000,
                category = "event"
            ),
            CommunityNews(
                id = "n2",
                title = "Plastic Ban Update",
                content = "Important updates on the single-use plastic ban in your area...",
                imageUrl = "",
                author = "Municipal Corp",
                timestamp = System.currentTimeMillis() - 172800000,
                category = "news"
            ),
            CommunityNews(
                id = "n3",
                title = "Tip: Reduce Food Waste",
                content = "Here are 10 simple ways to reduce food waste at home...",
                imageUrl = "",
                author = "Green Expert",
                timestamp = System.currentTimeMillis() - 259200000,
                category = "tip"
            )
        )

        emit(news)
    }

    // ==================== BADGES ====================

    suspend fun getBadges(): Flow<List<Badge>> = flow {
        delay(600)

        val scans = getCurrentUser()?.totalScans ?: 0

        val badges = listOf(
            Badge(
                id = "b1",
                name = "First Scan",
                description = "Complete your first waste scan",
                iconUrl = "",
                requirement = "1 scan",
                earned = scans >= 1
            ),
            Badge(
                id = "b2",
                name = "Eco Novice",
                description = "Scan 10 waste items",
                iconUrl = "",
                requirement = "10 scans",
                earned = scans >= 10
            ),
            Badge(
                id = "b3",
                name = "Recycling Pro",
                description = "Scan 50 waste items",
                iconUrl = "",
                requirement = "50 scans",
                earned = scans >= 50
            ),
            Badge(
                id = "b4",
                name = "Eco Warrior",
                description = "Scan 100 waste items",
                iconUrl = "",
                requirement = "100 scans",
                earned = scans >= 100
            )
        )

        emit(badges)
    }

    // ==================== QR CODE SCANNING ====================

    suspend fun scanQRCode(qrData: String): Flow<QRCodeData?> = flow {
        delay(500)

        // Parse QR data
        val qrCode = QRCodeData(
            id = qrData,
            type = "household",
            ownerId = "user_123",
            location = LocationData(28.6139, 77.2090, "Delhi"),
            metadata = mapOf("bin_type" to "recyclable"),
            createdAt = System.currentTimeMillis()
        )

        emit(qrCode)
    }

    // ==================== COLLECTOR FEATURES ====================

    suspend fun getCollectionRoute(): Flow<CollectionRoute?> = flow {
        delay(1000)

        if (getCurrentUser()?.role == UserRole.COLLECTOR) {
            val route = CollectionRoute(
                id = "route_1",
                collectorId = getCurrentUser()?.id ?: "",
                pickups = emptyList(),
                totalDistance = 12.5,
                estimatedTime = 120,
                status = "active",
                date = System.currentTimeMillis()
            )
            emit(route)
        } else {
            emit(null)
        }
    }

    suspend fun markPickupComplete(pickupId: String, log: CollectionLog): Flow<Boolean> = flow {
        delay(800)
        emit(true)
    }
}
