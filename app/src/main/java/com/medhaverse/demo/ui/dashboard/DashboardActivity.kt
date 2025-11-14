package com.medhaverse.demo.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medhaverse.demo.data.MedhaVerseRepository
import com.medhaverse.demo.databinding.ActivityDashboardBinding
import com.medhaverse.demo.ui.main.MainActivity
import com.medhaverse.demo.ui.scanner.ScannerActivity
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var repository: MedhaVerseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedhaVerseRepository(this)

        setupToolbar()
        loadUserStats()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Dashboard"
        }
    }

    private fun loadUserStats() {
        showLoading(true)
        
        lifecycleScope.launch {
            repository.getUserStats().collect { stats ->
                showLoading(false)
                
                // Update stats
                binding.tvTotalScans?.text = stats.totalScans.toString()
                binding.tvGreenCredits?.text = stats.greenCredits.toString()
                binding.tvCarbonSaved?.text = String.format("%.1f kg", stats.carbonSaved)
                binding.tvAccuracy?.text = "${stats.accuracy.toInt()}%"
                binding.tvRank?.text = "#${stats.rank}"
                binding.tvStreak?.text = "${stats.streak} days"
                
                // Show welcome message
                val user = repository.getCurrentUser()
                binding.tvWelcome?.text = "Welcome back, ${user?.name ?: "User"}!"
            }
        }
        
        loadLeaderboard()
    }
    
    private fun loadLeaderboard() {
        lifecycleScope.launch {
            repository.getLeaderboard().collect { leaderboard ->
                // For now, just show count
                binding.tvLeaderboardCount?.text = "${leaderboard.size} users"
            }
        }
    }

    private fun setupButtons() {
        binding.btnScanWaste?.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }
        
        binding.btnViewRewards?.setOnClickListener {
            // TODO: Open rewards
        }
        
        binding.btnBack?.setOnClickListener {
            finish()
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        binding.scrollView?.visibility = if (loading) View.GONE else View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
