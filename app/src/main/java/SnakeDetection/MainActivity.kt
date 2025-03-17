package com.SnakeDetection

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.SnakeDetection.Constants.LABELS_PATH
import com.SnakeDetection.Constants.MODEL_PATH
import com.SnakeDetection.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Detector.DetectorListener {
    private lateinit var binding: ActivityMainBinding
    private var isFrontCamera = false
    private var isFlashOn = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var detector: Detector? = null
    private var isCameraActive = false

    // Track if warning has been shown
    private var warningShown = false

    // Track if we've detected any snakes
    private var snakesDetected = false
    private var lastDetectedSnake: String? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var outputDirectory: File
    private val snakeInfoProvider = SnakeInfoProvider()

    // Gallery image picker
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                analyzeGalleryImage(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading gallery image: ${e.message}")
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create the output directory for photos
        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
        }

        setupButtons()
        setupCameraControls()
        setupShutterAndGallery()
        setupSnakeTapListener()

        // Hide the SHOW RESULT button (only use START SCANNING button)
        binding.showResultButton.visibility = View.GONE
    }

    private fun setupShutterAndGallery() {
        // Photo library button
        binding.photoLibraryButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // Camera shutter button
        binding.cameraShutterButton.setOnClickListener {
            if (isCameraActive) {
                takePhoto()
            } else {
                Toast.makeText(this, "Start scanning first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun analyzeGalleryImage(bitmap: Bitmap) {
        // First release camera if it's active
        if (isCameraActive) {
            releaseCamera()
        }

        // Show loading indicator or message
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show()

        // Submit the bitmap for snake detection
        cameraExecutor.execute {
            detector?.detect(bitmap)
        }

        // Set the flag to show we're in gallery image mode
        snakesDetected = false

        // Change button to indicate we can start camera scanning
        binding.startScanButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.sandColor))
        binding.startScanButtonText.text = "START SCANNING"
        binding.startScanButtonText.setTextColor(Color.BLACK)
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(
                        baseContext,
                        "Failed to capture photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)
                    Toast.makeText(baseContext, "Photo saved", Toast.LENGTH_SHORT).show()

                    // Optionally scan the image for snakes
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, savedUri)
                        analyzeGalleryImage(bitmap)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error analyzing captured photo: ${e.message}")
                    }
                }
            }
        )
    }

    private fun setupCameraControls() {
        // Setup flip camera button
        binding.flipCameraButton.setOnClickListener {
            if (isCameraActive) {
                isFrontCamera = !isFrontCamera
                releaseCamera()
                startCamera()
            } else {
                Toast.makeText(this, "Start scanning first", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup flash button
        binding.flashButton.setOnClickListener {
            if (isCameraActive && camera?.cameraInfo?.hasFlashUnit() == true) {
                isFlashOn = !isFlashOn
                camera?.cameraControl?.enableTorch(isFlashOn)
                updateFlashButtonIcon()
            } else {
                Toast.makeText(this, "Camera not active or flash not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup zoom slider
        binding.zoomSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isCameraActive) {
                    // Convert progress (0-100) to zoom ratio (1.0-5.0)
                    val zoomRatio = 1.0f + (progress / 25.0f)
                    camera?.cameraControl?.setZoomRatio(zoomRatio)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateFlashButtonIcon() {
        if (isFlashOn) {
            binding.flashButton.setImageResource(R.drawable.ic_flash_on)
        } else {
            binding.flashButton.setImageResource(R.drawable.ic_flash_off)
        }
    }

    private fun setupButtons() {
        // Setup GPU toggle (hidden for normal users)
        binding.isGpu.setOnCheckedChangeListener { buttonView, isChecked ->
            cameraExecutor.submit {
                detector?.restart(isGpu = isChecked)
            }
            if (isChecked) {
                buttonView.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.orange))
            } else {
                buttonView.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.gray))
            }
        }

        // Start Scanning button
        binding.startScanButton.setOnClickListener {
            if (isCameraActive) {
                // If already scanning, stop the camera
                releaseCamera()

                // If we've already detected a snake, change text to SCAN AGAIN
                if (snakesDetected) {
                    binding.startScanButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.scanAgainBlue))
                    binding.startScanButtonText.text = "SCAN AGAIN"
                    binding.startScanButtonText.setTextColor(Color.WHITE)
                }
            } else {
                // If not scanning, start the camera
                // Only show warning dialog the first time
                if (!warningShown) {
                    showScanningWarningDialog()
                    warningShown = true
                } else {
                    // Start camera directly for subsequent scans
                    if (allPermissionsGranted()) {
                        startCamera()
                        // Don't reset detection state so we know to keep "SCAN AGAIN" text
                    } else {
                        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
                    }
                }
            }
        }
    }

    private fun showScanningWarningDialog() {
        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_scanning_warning)

        val proceedButton = dialog.findViewById<Button>(R.id.proceedButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)

        proceedButton.setOnClickListener {
            dialog.dismiss()

            // Start camera if permissions granted
            if (allPermissionsGranted()) {
                startCamera()
                // Reset detection state
                snakesDetected = false
                lastDetectedSnake = null
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
                isCameraActive = true

                // Change to RED background to indicate STOP
                binding.startScanButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.scanningRed))
                binding.startScanButtonText.text = "STOP SCANNING"
                binding.startScanButtonText.setTextColor(Color.WHITE)

                // Check if torch state changed while camera was inactive
                camera?.cameraControl?.enableTorch(isFlashOn)

                // Enable camera controls
                binding.flipCameraButton.isEnabled = true
                binding.flashButton.isEnabled = camera?.cameraInfo?.hasFlashUnit() == true
                binding.zoomSlider.isEnabled = true
                binding.cameraShutterButton.isEnabled = true

                // Listen for torch state changes
                camera?.cameraInfo?.torchState?.observe(this) { state ->
                    isFlashOn = state == TorchState.ON
                    updateFlashButtonIcon()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error starting camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(if (isFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
            .build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        // Set up the image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            detector?.detect(rotatedBitmap)
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    // Function to handle permission results and other lifecycle methods
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.CAMERA] == true) {
            if (!warningShown) {
                showScanningWarningDialog()
                warningShown = true
            } else {
                startCamera()
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        detector?.close()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        // Don't automatically restart camera on resume
    }

    // Override onPause to release camera resources
    override fun onPause() {
        releaseCamera()
        super.onPause()
    }

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf (
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            binding.overlay.clear()
        }
    }

    private fun setupSnakeTapListener() {
        binding.overlay.setOnSnakeClickListener { snakeInfo ->
            // Track the last detected snake
            lastDetectedSnake = snakeInfo.name
            snakesDetected = true

            // Stop the camera (disable scanning) when user taps for information
            releaseCamera()

            // Change button to "SCAN AGAIN" with blue color
            binding.startScanButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.scanAgainBlue))
            binding.startScanButtonText.text = "SCAN AGAIN"
            binding.startScanButtonText.setTextColor(Color.WHITE)

            // Navigate to snake details activity
            val intent = Intent(this, SnakeDetailsActivity::class.java).apply {
                putExtra(SnakeDetailsActivity.EXTRA_SNAKE_NAME, snakeInfo.name)
            }
            startActivity(intent)
        }
    }

    // Method to release camera resources
    private fun releaseCamera() {
        try {
            isCameraActive = false
            cameraProvider?.unbindAll()
            imageAnalyzer?.clearAnalyzer()
            camera = null
            preview = null
            imageCapture = null
            imageAnalyzer = null

            // Disable camera controls when camera is not active
            binding.flipCameraButton.isEnabled = false
            binding.flashButton.isEnabled = false
            binding.zoomSlider.isEnabled = false
            binding.cameraShutterButton.isEnabled = false

            // Set button appearance based on detection state
            if (snakesDetected) {
                binding.startScanButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.scanAgainBlue))
                binding.startScanButtonText.text = "SCAN AGAIN"
                binding.startScanButtonText.setTextColor(Color.WHITE)
            } else {
                binding.startScanButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.sandColor))
                binding.startScanButtonText.text = "START SCANNING"
                binding.startScanButtonText.setTextColor(Color.BLACK)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing camera: ${e.message}")
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            binding.inferenceTime.text = "${inferenceTime}ms"
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }

            // Check if there are any detection results
            if (boundingBoxes.isNotEmpty()) {
                snakesDetected = true
                // Store the first detected snake's name
                lastDetectedSnake = boundingBoxes[0].clsName
            }
        }
    }
}