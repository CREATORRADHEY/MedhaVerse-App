package com.medhaverse.demo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medhaverse.demo.R
import com.medhaverse.demo.data.MedhaVerseRepository
import com.medhaverse.demo.data.UserRole
import com.medhaverse.demo.databinding.ActivityRoleSelectionBinding
import com.medhaverse.demo.ui.main.MainActivity
import kotlinx.coroutines.launch

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding
    private lateinit var repository: MedhaVerseRepository
    private var selectedRole: UserRole? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MedhaVerseRepository(this)

        setupUI()
        startEntryAnimations()
    }

    private fun setupUI() {
        // Citizen Role
        binding.cardCitizen.setOnClickListener {
            animateCardSelection(binding.cardCitizen)
            selectRole(UserRole.CITIZEN)
        }

        // Collector Role
        binding.cardCollector.setOnClickListener {
            animateCardSelection(binding.cardCollector)
            selectRole(UserRole.COLLECTOR)
        }

        // Recycler Role
        binding.cardRecycler.setOnClickListener {
            animateCardSelection(binding.cardRecycler)
            selectRole(UserRole.RECYCLER)
        }

        // Institution Role
        binding.cardInstitution.setOnClickListener {
            animateCardSelection(binding.cardInstitution)
            selectRole(UserRole.INSTITUTION)
        }

        // Continue Button
        binding.btnContinue.setOnClickListener {
            selectedRole?.let { role ->
                animateButtonClick(binding.btnContinue)
                updateRole(role)
            } ?: run {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startEntryAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Animate title and subtitle
        binding.tvTitle.startAnimation(fadeIn)
        binding.tvSubtitle.startAnimation(fadeIn)

        // Animate cards with stagger
        binding.cardCitizen.postDelayed({
            binding.cardCitizen.startAnimation(slideUp)
            binding.cardCitizen.visibility = View.VISIBLE
        }, 100)

        binding.cardCollector.postDelayed({
            binding.cardCollector.startAnimation(slideUp)
            binding.cardCollector.visibility = View.VISIBLE
        }, 200)

        binding.cardRecycler.postDelayed({
            binding.cardRecycler.startAnimation(slideUp)
            binding.cardRecycler.visibility = View.VISIBLE
        }, 300)

        binding.cardInstitution.postDelayed({
            binding.cardInstitution.startAnimation(slideUp)
            binding.cardInstitution.visibility = View.VISIBLE
        }, 400)

        binding.btnContinue.postDelayed({
            binding.btnContinue.startAnimation(slideUp)
            binding.btnContinue.visibility = View.VISIBLE
        }, 500)
    }

    private fun animateCardSelection(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun animateButtonClick(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun selectRole(role: UserRole) {
        selectedRole = role

        // Reset all cards with animation
        resetCard(binding.cardCitizen)
        resetCard(binding.cardCollector)
        resetCard(binding.cardRecycler)
        resetCard(binding.cardInstitution)

        // Highlight selected card with animation
        val strokeWidth = (4 * resources.displayMetrics.density).toInt()
        val selectedCard = when (role) {
            UserRole.CITIZEN -> binding.cardCitizen
            UserRole.COLLECTOR -> binding.cardCollector
            UserRole.RECYCLER -> binding.cardRecycler
            UserRole.INSTITUTION -> binding.cardInstitution
            else -> return
        }

        selectedCard.strokeWidth = strokeWidth
        selectedCard.cardElevation = 12f

        // Animate continue button
        if (!binding.btnContinue.isEnabled) {
            binding.btnContinue.isEnabled = true
            binding.btnContinue.animate()
                .alpha(1f)
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(200)
                .withEndAction {
                    binding.btnContinue.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }
    }

    private fun resetCard(card: com.google.android.material.card.MaterialCardView) {
        card.strokeWidth = 0
        card.cardElevation = 4f
    }

    private fun updateRole(role: UserRole) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.alpha = 0f
        binding.progressBar.animate()
            .alpha(1f)
            .setDuration(200)
            .start()

        binding.btnContinue.isEnabled = false

        lifecycleScope.launch {
            repository.updateUserRole(role).collect { success ->
                binding.progressBar.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        binding.progressBar.visibility = View.GONE
                    }
                    .start()

                if (success) {
                    Toast.makeText(
                        this@RoleSelectionActivity,
                        "Role selected: ${role.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMain()
                } else {
                    Toast.makeText(
                        this@RoleSelectionActivity,
                        "Failed to update role",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnContinue.isEnabled = true
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, com.medhaverse.demo.ui.main.MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
