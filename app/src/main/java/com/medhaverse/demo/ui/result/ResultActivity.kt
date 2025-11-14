package com.medhaverse.demo.ui.result

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.medhaverse.demo.R
import com.medhaverse.demo.data.WasteResult
import com.medhaverse.demo.databinding.ActivityResultBinding
import com.medhaverse.demo.ui.dashboard.DashboardActivity
import com.medhaverse.demo.ui.scanner.ScannerActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var result: WasteResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        result = intent.getSerializableExtra("result") as? WasteResult
            ?: throw IllegalStateException("Result data missing")

        displayResult()
        setupButtons()
        playAnimations()
    }

    private fun displayResult() {
        // Category and type
        binding.tvCategory.text = result.category
        binding.tvType.text = result.type

        // Recyclability
        if (result.recyclable) {
            binding.tvRecyclable.text = "♻️ Recyclable"
            binding.tvRecyclable.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvRecyclable.text = "⚠️ Non-Recyclable"
            binding.tvRecyclable.setTextColor(getColor(android.R.color.holo_orange_dark))
        }

        // Carbon impact
        binding.tvCarbon.text = String.format("%.2f kg CO₂ saved", result.carbonSaved)

        // Credits earned
        binding.tvCredits.text = "+${result.credits}"

        // Disposal tip
        binding.tvTip.text = "💡 ${result.tip}"

        // Category icon
        binding.ivCategoryIcon.setImageResource(getCategoryIcon(result.category))
    }

    private fun getCategoryIcon(category: String): Int {
        return when (category.uppercase()) {
            "PLASTIC" -> android.R.drawable.ic_dialog_info
            "METAL" -> android.R.drawable.ic_dialog_info
            "PAPER" -> android.R.drawable.ic_dialog_info
            "ORGANIC" -> android.R.drawable.ic_dialog_info
            "EWASTE" -> android.R.drawable.ic_dialog_alert
            "GLASS" -> android.R.drawable.ic_dialog_info
            "TEXTILE" -> android.R.drawable.ic_dialog_info
            "HAZARDOUS" -> android.R.drawable.ic_dialog_alert
            else -> android.R.drawable.ic_dialog_info
        }
    }

    private fun setupButtons() {
        binding.btnScanAnother.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
            finish()
        }

        binding.btnDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    private fun playAnimations() {
        // Animate the credit counter
        animateCredits()
    }

    private fun animateCredits() {
        val credits = result.credits
        var current = 0

        val handler = android.os.Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                if (current <= credits) {
                    binding.tvCredits.text = "+$current"
                    current += 2
                    handler.postDelayed(this, 30)
                }
            }
        }
        handler.post(runnable)
    }
}
