package com.medhaverse.demo.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medhaverse.demo.R
import com.medhaverse.demo.data.MedhaVerseRepository
import com.medhaverse.demo.databinding.ActivitySplashBinding
import com.medhaverse.demo.ui.auth.LoginActivity
import com.medhaverse.demo.ui.main.MainActivity
import com.medhaverse.demo.ui.scanner.ScannerActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var repository: MedhaVerseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedhaVerseRepository(this)

        startAnimations()

        // Navigate after delay
        lifecycleScope.launch {
            delay(2500)
            navigateToNext()
        }
    }

    private fun startAnimations() {
        // Logo animation - scale and fade in
        binding.ivLogo.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .start()
        }

        // App name fade in with delay
        binding.tvAppName.postDelayed({
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            binding.tvAppName.startAnimation(fadeIn)
            binding.tvAppName.alpha = 1f
        }, 400)

        // Tagline fade in with delay
        binding.tvTagline.postDelayed({
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            binding.tvTagline.startAnimation(fadeIn)
            binding.tvTagline.alpha = 1f
        }, 600)

        // Loading indicator fade in
        binding.progressBar.postDelayed({
            binding.progressBar.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }, 800)
    }

    private fun navigateToNext() {
        // Fade out animation
        binding.root.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                val intent = if (repository.isLoggedIn()) {
                    // User is logged in, go directly to main app
                    Intent(this, com.medhaverse.demo.ui.main.MainActivity::class.java)
                } else {
                    // User not logged in, show login
                    Intent(this, LoginActivity::class.java)
                }
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            .start()
    }
}
