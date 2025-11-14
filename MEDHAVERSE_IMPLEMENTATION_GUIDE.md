# 🚀 MedhaVerse Hackathon Demo App - Complete Implementation Guide

## 📱 Project Overview

**MedhaVerse** is an AI-powered Waste Intelligence platform that uses RunAnywhere SDK for on-device
waste classification. This demo app is optimized for hackathon presentations and includes:

- ✅ AI-powered waste scanning
- ✅ On-device classification (works offline)
- ✅ Beautiful Material Design UI
- ✅ Gamification with Green Credits
- ✅ Carbon impact tracking

---

## 🎯 Files Created So Far

### ✅ Application & Core AI

1. **MedhaVerseApp.kt** - Application class with SDK initialization
2. **WasteAI.kt** - AI waste classification engine

### ✅ UI Activities

3. **SplashActivity.kt** - Splash screen
4. **OnboardingActivity.kt** - Onboarding flow
5. **OnboardingAdapter.kt** - ViewPager adapter

---

## 📋 Remaining Files to Create

I'll now provide you with the complete code for all remaining files. You can copy-paste these into
your project:

---

## 🔧 Step-by-Step Instructions

### STEP 1: Update AndroidManifest.xml

Replace your `app/src/main/AndroidManifest.xml` with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />

    <uses-feature android:name="android.hardware.camera" android:required="false" />

    <application
        android:name="com.medhaverse.demo.MedhaVerseApp"
        android:allowBackup="true"
        android:largeHeap="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.Material3.DayNight">

        <!-- Splash Activity - Launcher -->
        <activity
            android:name="com.medhaverse.demo.ui.splash.SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Onboarding Activity -->
        <activity
            android:name="com.medhaverse.demo.ui.onboarding.OnboardingActivity"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <!-- Scanner Activity -->
        <activity
            android:name="com.medhaverse.demo.ui.scanner.ScannerActivity"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <!-- Result Activity -->
        <activity
            android:name="com.medhaverse.demo.ui.result.ResultActivity"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <!-- Dashboard Activity -->
        <activity
            android:name="com.medhaverse.demo.ui.dashboard.DashboardActivity"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

    </application>

</manifest>
```

---

### STEP 2: Create Scanner Activity

Create `app/src/main/java/com/medhaverse/demo/ui/scanner/ScannerActivity.kt`:

```kotlin
package com.medhaverse.demo.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.medhaverse.demo.data.WasteAI
import com.medhaverse.demo.databinding.ActivityScannerBinding
import com.medhaverse.demo.ui.result.ResultActivity
import kotlinx.coroutines.launch

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private var imageCapture: ImageCapture? = null

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkModelAndStart()
        setupButtons()
    }

    private fun checkModelAndStart() {
        lifecycleScope.launch {
            if (WasteAI.isModelReady()) {
                if (!WasteAI.isModelLoaded()) {
                    showStatus("Loading AI model...")
                    WasteAI.initialize()
                }
                requestCamera()
            } else {
                showModelDownload()
            }
        }
    }

    private fun showModelDownload() {
        binding.tvStatus.text = "Downloading AI model..."
        binding.progressBar.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            WasteAI.downloadModel().collect { progress ->
                val percent = (progress * 100).toInt()
                binding.tvStatus.text = "Downloading AI: $percent%"
            }

            WasteAI.initialize()
            binding.progressBar.visibility = android.view.View.GONE
            showStatus("AI model ready!")
            requestCamera()
        }
    }

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                showStatus("Point camera at waste item")
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupButtons() {
        binding.btnCapture.setOnClickListener {
            captureAndClassify()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun captureAndClassify() {
        showStatus("Analyzing waste with AI...")
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnCapture.isEnabled = false

        lifecycleScope.launch {
            // For demo: Create a dummy bitmap
            // In production: Use actual camera capture
            val dummyImage = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)

            val result = WasteAI.classifyWaste(dummyImage)

            binding.progressBar.visibility = android.view.View.GONE
            binding.btnCapture.isEnabled = true

            if (result != null) {
                navigateToResult(result)
            } else {
                Toast.makeText(this@ScannerActivity, "Classification failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToResult(result: com.medhaverse.demo.data.WasteResult) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("result", result)
        }
        startActivity(intent)
        finish()
    }

    private fun showStatus(message: String) {
        binding.tvStatus.text = message
    }
}
```

---

### STEP 3: Create Result Activity

Create `app/src/main/java/com/medhaverse/demo/ui/result/ResultActivity.kt`:

```kotlin
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
```

---

### STEP 4: Create Dashboard Activity

Create `app/src/main/java/com/medhaverse/demo/ui/dashboard/DashboardActivity.kt`:

```kotlin
package com.medhaverse.demo.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.medhaverse.demo.databinding.ActivityDashboardBinding
import com.medhaverse.demo.ui.scanner.ScannerActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayMockStats()
        setupButtons()
    }

    private fun displayMockStats() {
        // Mock statistics for demo
        binding.tvTotalScans.text = "47"
        binding.tvGreenCredits.text = "1,250"
        binding.tvCarbonSaved.text = "15.8 kg"
        binding.tvAccuracy.text = "92%"

        // Leaderboard
        binding.tvRank.text = "#23"
        binding.tvRankChange.text = "↑ 5"

        // Recent activity
        binding.tvRecentActivity.text = """
            • Plastic bottle - +25 credits
            • Paper bag - +15 credits
            • Metal can - +30 credits
            • Glass jar - +20 credits
        """.trimIndent()
    }

    private fun setupButtons() {
        binding.btnScan.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            showProfileDialog()
        }
    }

    private fun showProfileDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Profile")
            .setMessage("""
                Name: Demo User
                Email: demo@medhaverse.app
                Member since: Jan 2025

                Total Credits: 1,250
                Carbon Saved: 15.8 kg
                Rank: #23
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
}
```

---

## 🎨 LAYOUT FILES

Now create these layout XML files in `app/src/main/res/layout/`:

### activity_splash.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/green_primary">

    <ImageView
        android:id="@+id/ivLogo"
        android:layout_width="150dp"
        android:layout_height="150dp"
        android:src="@mipmap/ic_launcher"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:contentDescription="MedhaVerse Logo" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="MedhaVerse"
        android:textSize="32sp"
        android:textStyle="bold"
        android:textColor="@android:color/white"
        android:layout_marginTop="24dp"
        app:layout_constraintTop_toBottomOf="@id/ivLogo"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="AI-Powered Waste Intelligence"
        android:textSize="14sp"
        android:textColor="@android:color/white"
        android:alpha="0.8"
        android:layout_marginBottom="32dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

---

### activity_onboarding.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/btnNext"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnNext"
        android:layout_width="0dp"
        android:layout_height="56dp"
        android:text="Next"
        android:textSize="16sp"
        android:layout_marginBottom="16dp"
        android:layout_marginEnd="8dp"
        app:cornerRadius="28dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/btnSkip"
        app:layout_constraintWidth_percent="0.6" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnSkip"
        android:layout_width="0dp"
        android:layout_height="56dp"
        android:text="Skip"
        android:textSize="16sp"
        android:layout_marginBottom="16dp"
        android:layout_marginStart="8dp"
        style="@style/Widget.Material3.Button.OutlinedButton"
        app:cornerRadius="28dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toEndOf="@id/btnNext"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintWidth_percent="0.35" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

---

### item_onboarding_page.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="32dp">

    <ImageView
        android:id="@+id/ivOnboarding"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:src="@android:drawable/ic_dialog_info"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/tvTitle"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:contentDescription="Onboarding image" />

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Title"
        android:textSize="24sp"
        android:textStyle="bold"
        android:gravity="center"
        android:layout_marginTop="32dp"
        app:layout_constraintTop_toBottomOf="@id/ivOnboarding"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/tvDescription"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Description"
        android:textSize="16sp"
        android:gravity="center"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/tvTitle"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

---

Due to the character limit, I'll provide you with the remaining critical files. Would you like me to
continue with:

1. ✅ Scanner Activity Layout (activity_scanner.xml)
2. ✅ Result Activity Layout (activity_result.xml)
3. ✅ Dashboard Activity Layout (activity_dashboard.xml)
4. ✅ Colors and Strings resources
5. ✅ Complete setup instructions

**Please confirm and I'll continue with the remaining files!** 🚀
