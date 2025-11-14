package com.medhaverse.demo.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.viewpager2.widget.ViewPager2
import com.medhaverse.demo.databinding.ActivityOnboardingBinding
import com.medhaverse.demo.ui.scanner.ScannerActivity
import kotlin.jvm.java

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        val pages = listOf(
            OnboardingPage(
                title = "Welcome to MedhaVerse",
                description = "AI-powered waste intelligence platform transforming India's circular economy",
                imageRes = android.R.drawable.ic_dialog_info
            ),
            OnboardingPage(
                title = "Scan & Classify Waste",
                description = "Use on-device AI to instantly identify waste types and get proper disposal guidance",
                imageRes = android.R.drawable.ic_menu_camera
            ),
            OnboardingPage(
                title = "Earn Green Credits",
                description = "Get rewarded for proper waste disposal and contribute to a sustainable future",
                imageRes = android.R.drawable.btn_star_big_on
            )
        )

        val adapter = OnboardingAdapter(pages)
        binding.viewPager.adapter = adapter

        // Page change listener to update button text
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == pages.size - 1) {
                    binding.btnNext.text = "Get Started"
                } else {
                    binding.btnNext.text = "Next"
                }
            }
        })
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            val adapter = binding.viewPager.adapter as OnboardingAdapter

            if (current < adapter.itemCount - 1) {
                binding.viewPager.currentItem = current + 1
            } else {
                navigateToScanner()
            }
        }

        binding.btnSkip.setOnClickListener {
            navigateToScanner()
        }
    }

    private fun navigateToScanner() {
        startActivity(Intent(this, ScannerActivity::class.java))
        finish()
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)
