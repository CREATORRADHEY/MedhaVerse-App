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
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera error: ${exc.message}", Toast.LENGTH_SHORT).show()
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
