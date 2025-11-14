package com.medhaverse.demo.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.medhaverse.demo.R
import com.medhaverse.demo.data.MedhaVerseRepository
import com.medhaverse.demo.data.PreferencesManager
import com.medhaverse.demo.data.UserRole
import com.medhaverse.demo.databinding.ActivityMainBinding
import com.medhaverse.demo.ui.auth.LoginActivity
import com.medhaverse.demo.ui.auth.RoleSelectionActivity
import com.medhaverse.demo.ui.scanner.ScannerActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: MedhaVerseRepository
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedhaVerseRepository(this)
        prefsManager = PreferencesManager(this)

        setupToolbar()
        setupNavigationDrawer()
        setupBottomNavigation()
        updateUIForRole()
        setupBackPressedCallback()

        // Load Home/Dashboard content by default
        loadDashboardContent()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Update header with user info
        val headerView = binding.navView.getHeaderView(0)
        val user = repository.getCurrentUser()
        
        headerView.findViewById<TextView>(R.id.tvUserName)?.text = 
            user?.name?.ifEmpty { "Welcome!" } ?: "Welcome!"
        headerView.findViewById<TextView>(R.id.tvUserRole)?.text = 
            when(user?.role) {
                UserRole.CITIZEN -> "👤 Citizen"
                UserRole.COLLECTOR -> "🚛 Collector"
                UserRole.RECYCLER -> "♻️ Recycler"
                UserRole.INSTITUTION -> "🏢 Institution"
                else -> "User"
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scanner -> {
                    openScanner()
                    true
                }
                R.id.nav_dashboard -> {
                    loadDashboardContent()
                    true
                }
                R.id.nav_rewards -> {
                    loadRewardsContent()
                    true
                }
                R.id.nav_community -> {
                    loadCommunityContent()
                    true
                }
                else -> false
            }
        }
        
        // Select dashboard by default
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
    }

    private fun updateUIForRole() {
        val user = repository.getCurrentUser()
        binding.toolbar.title = when (user?.role) {
            UserRole.CITIZEN -> "MedhaVerse - Citizen"
            UserRole.COLLECTOR -> "MedhaVerse - Collector"
            UserRole.RECYCLER -> "MedhaVerse - Recycler"
            UserRole.INSTITUTION -> "MedhaVerse - Institution"
            else -> "MedhaVerse"
        }
    }

    private fun loadDashboardContent() {
        binding.toolbar.title = "Dashboard"

        // Show stats
        lifecycleScope.launch {
            repository.getUserStats().collect { stats ->
                // Update content container with stats
                val content = binding.contentContainer
                content.removeAllViews()

                // Inflate and add dashboard content
                val dashboardView =
                    layoutInflater.inflate(R.layout.fragment_dashboard, content, false)

                // Update values
                dashboardView.findViewById<TextView>(R.id.tvTotalScans)?.text =
                    stats.totalScans.toString()
                dashboardView.findViewById<TextView>(R.id.tvGreenCredits)?.text =
                    stats.greenCredits.toString()
                dashboardView.findViewById<TextView>(R.id.tvCarbonSaved)?.text =
                    String.format("%.1f kg", stats.carbonSaved)
                dashboardView.findViewById<TextView>(R.id.tvAccuracy)?.text =
                    "${stats.accuracy.toInt()}%"
                dashboardView.findViewById<TextView>(R.id.tvRank)?.text = "#${stats.rank}"
                dashboardView.findViewById<TextView>(R.id.tvStreak)?.text = "${stats.streak} days"

                dashboardView.findViewById<View>(R.id.btnScanWaste)?.setOnClickListener {
                    openScanner()
                }

                content.addView(dashboardView)
            }
        }
    }

    private fun loadRewardsContent() {
        binding.toolbar.title = "Rewards"

        lifecycleScope.launch {
            repository.getUserStats().collect { stats ->
                val content = binding.contentContainer
                content.removeAllViews()

                val rewardsView = layoutInflater.inflate(R.layout.fragment_rewards, content, false)

                // Update green credits
                rewardsView.findViewById<TextView>(R.id.tvTotalCredits)?.text =
                    stats.greenCredits.toString()
                rewardsView.findViewById<TextView>(R.id.tvRewardLevel)?.text = when {
                    stats.greenCredits < 100 -> "🌱 Beginner"
                    stats.greenCredits < 500 -> "🌿 Intermediate"
                    stats.greenCredits < 1000 -> "🌳 Advanced"
                    else -> "🏆 Master"
                }

                content.addView(rewardsView)
            }
        }
    }

    private fun loadCommunityContent() {
        binding.toolbar.title = "Community"

        lifecycleScope.launch {
            repository.getLeaderboard().collect { leaderboard ->
                val content = binding.contentContainer
                content.removeAllViews()

                val communityView =
                    layoutInflater.inflate(R.layout.fragment_community, content, false)

                // Show leaderboard count
                communityView.findViewById<TextView>(R.id.tvLeaderboardCount)?.text =
                    "${leaderboard.size} active users"

                // Show top user
                if (leaderboard.isNotEmpty()) {
                    val topUser = leaderboard.first()
                    communityView.findViewById<TextView>(R.id.tvTopUser)?.text =
                        "🥇 ${topUser.name} - ${topUser.greenCredits} pts"
                }

                content.addView(communityView)
            }
        }
    }

    private fun openScanner() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                showComingSoon("Profile")
            }
            R.id.nav_settings -> {
                showComingSoon("Settings")
            }
            R.id.nav_switch_role -> {
                showRoleSwitchDialog()
            }
            R.id.nav_logout -> {
                showLogoutDialog()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showComingSoon(feature: String) {
        AlertDialog.Builder(this, R.style.ModernAlertDialog)
            .setTitle("Coming Soon!")
            .setMessage("$feature feature will be available soon. Stay tuned!")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showRoleSwitchDialog() {
        AlertDialog.Builder(this, R.style.ModernAlertDialog)
            .setTitle("Switch Role")
            .setMessage("Do you want to switch to a different role?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, RoleSelectionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.ModernAlertDialog)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear all user data
        prefsManager.logout()
        
        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
    
    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        finish()
                    }
                }
            })
    }
}
