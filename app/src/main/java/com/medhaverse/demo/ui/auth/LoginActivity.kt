package com.medhaverse.demo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.medhaverse.demo.R
import com.medhaverse.demo.data.MedhaVerseRepository
import com.medhaverse.demo.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: MedhaVerseRepository
    private var otpSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedhaVerseRepository(this)

        // Check if already logged in
        if (repository.isLoggedIn()) {
            navigateToRoleSelection()
            return
        }

        setupUI()
        startEntryAnimations()

        // Auto-focus phone input after animations
        binding.etPhone.postDelayed({
            binding.etPhone.requestFocus()
        }, 600)
    }

    private fun setupUI() {
        // Phone input listeners
        binding.etPhone.doAfterTextChanged { text ->
            binding.tilPhone.error = null
            binding.btnSendOtp.isEnabled = text?.length == 10
        }

        // OTP input listeners
        binding.etOtp.doAfterTextChanged { text ->
            binding.tilOtp.error = null
            binding.btnVerifyOtp.isEnabled = text?.length == 6
        }

        binding.btnSendOtp.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (validatePhone(phone)) {
                sendOTP(phone)
            }
        }

        binding.btnVerifyOtp.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val otp = binding.etOtp.text.toString().trim()
            if (validateOTP(otp)) {
                verifyOTP(phone, otp)
            }
        }
    }

    private fun startEntryAnimations() {
        // Fade in logo
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.ivLogo.startAnimation(fadeIn)

        // Slide up content with delays
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        
        binding.tvTitle.postDelayed({
            binding.tvTitle.startAnimation(slideUp)
            binding.tvTitle.visibility = View.VISIBLE
        }, 200)

        binding.tvSubtitle.postDelayed({
            binding.tvSubtitle.startAnimation(slideUp)
            binding.tvSubtitle.visibility = View.VISIBLE
        }, 300)

        binding.phoneCard.postDelayed({
            binding.phoneCard.startAnimation(slideUp)
            binding.phoneCard.visibility = View.VISIBLE
        }, 400)

        binding.btnSendOtp.postDelayed({
            binding.btnSendOtp.startAnimation(slideUp)
            binding.btnSendOtp.visibility = View.VISIBLE
        }, 500)
    }

    private fun validatePhone(phone: String): Boolean {
        return when {
            phone.isEmpty() -> {
                binding.tilPhone.error = getString(R.string.error_phone_empty)
                shakeView(binding.tilPhone)
                false
            }
            phone.length != 10 -> {
                binding.tilPhone.error = getString(R.string.error_phone_invalid)
                shakeView(binding.tilPhone)
                false
            }
            else -> {
                binding.tilPhone.error = null
                true
            }
        }
    }

    private fun validateOTP(otp: String): Boolean {
        return when {
            otp.isEmpty() -> {
                binding.tilOtp.error = getString(R.string.error_otp_empty)
                shakeView(binding.tilOtp)
                false
            }
            otp.length != 6 -> {
                binding.tilOtp.error = getString(R.string.error_otp_invalid)
                shakeView(binding.tilOtp)
                false
            }
            else -> {
                binding.tilOtp.error = null
                true
            }
        }
    }

    private fun sendOTP(phone: String) {
        showLoading(true)
        lifecycleScope.launch {
            repository.sendOTP(phone).collect { success ->
                showLoading(false)
                if (success) {
                    otpSent = true
                    showOTPView()
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.otp_sent, phone),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Failed to send OTP",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun verifyOTP(phone: String, otp: String) {
        showLoading(true)
        lifecycleScope.launch {
            repository.verifyOTP(phone, otp).collect { user ->
                showLoading(false)
                if (user != null) {
                    // Success animation
                    val scaleIn = AnimationUtils.loadAnimation(this@LoginActivity, R.anim.scale_in)
                    binding.root.startAnimation(scaleIn)
                    
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    binding.root.postDelayed({
                        navigateToRoleSelection()
                    }, 300)
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.invalid_otp),
                        Toast.LENGTH_SHORT
                    ).show()
                    shakeView(binding.tilOtp)
                }
            }
        }
    }

    private fun showOTPView() {
        // Animate OTP view appearance
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Show the OTP card (parent container)
        binding.otpCard.visibility = View.VISIBLE
        binding.otpCard.startAnimation(slideUp)
        
        binding.btnSendOtp.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(300)
            .withEndAction {
                binding.btnSendOtp.visibility = View.GONE
            }
            .start()
        
        binding.btnVerifyOtp.visibility = View.VISIBLE
        binding.btnVerifyOtp.startAnimation(slideUp)
        
        binding.etPhone.isEnabled = false
        binding.etOtp.requestFocus()
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSendOtp.isEnabled = !loading
        binding.btnVerifyOtp.isEnabled = !loading
        
        if (loading) {
            binding.progressBar.alpha = 0f
            binding.progressBar.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }
    }

    private fun shakeView(view: View) {
        view.animate()
            .translationX(-10f)
            .setDuration(50)
            .withEndAction {
                view.animate()
                    .translationX(10f)
                    .setDuration(50)
                    .withEndAction {
                        view.animate()
                            .translationX(0f)
                            .setDuration(50)
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun navigateToRoleSelection() {
        val intent = Intent(this, RoleSelectionActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
