package com.SnakeDetection


import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.SnakeDetection.Constants.LABELS_PATH
import com.SnakeDetection.Constants.MODEL_PATH
import com.SnakeDetection.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Detector.DetectorListener {
    private lateinit var binding: ActivityMainBinding
    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var detector: Detector? = null
    private var isCameraActive = false

    private lateinit var cameraExecutor: ExecutorService
    private val snakeInfoProvider = SnakeInfoProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateMainLayout()
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        bindListeners()
        setupSnakeTapListener()
    }

    private fun updateMainLayout() {
        // Make sure you have snake_library_button in your layout
        val libraryButton = findViewById<Button>(R.id.snakeLibraryButton)
        libraryButton.setOnClickListener {
            showSnakeLibrary()
        }
    }

    private fun bindListeners() {
        binding.apply {
            isGpu.setOnCheckedChangeListener { buttonView, isChecked ->
                cameraExecutor.submit {
                    detector?.restart(isGpu = isChecked)
                }
                if (isChecked) {
                    buttonView.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.orange))
                } else {
                    buttonView.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.gray))
                }
            }

            // Add a button to show educational content without detection
            snakeLibraryButton.setOnClickListener {
                showSnakeLibrary()
            }
        }
    }

    private fun showSnakeLibrary() {
        // Release camera resources first
        releaseCamera()

        // Create a dialog to show a list of all snakes
        val snakeNames = snakeInfoProvider.getAllSnakeNames()

        AlertDialog.Builder(this)
            .setTitle("Snake Library")
            .setItems(snakeNames.toTypedArray()) { dialog, which ->
                val selectedSnake = snakeNames[which]
                val intent = Intent(this, SnakeDetailsActivity::class.java).apply {
                    putExtra(SnakeDetailsActivity.EXTRA_SNAKE_NAME, selectedSnake)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Restart camera when canceling dialog
                if (!isCameraActive) {
                    startCamera()
                }
            }
            .setOnDismissListener {
                // Restart camera when dialog is dismissed
                if (!isCameraActive) {
                    startCamera()
                }
            }
            .show()
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
                isCameraActive = true
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
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview = Preview.Builder()
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
        if (it[Manifest.permission.CAMERA] == true) { startCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector?.close()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if (!isCameraActive) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            }
        }
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
            // First release camera resources
            releaseCamera()

            // Then navigate to snake details activity
            val intent = Intent(this, SnakeDetailsActivity::class.java).apply {
                putExtra(SnakeDetailsActivity.EXTRA_SNAKE_NAME, snakeInfo.name)
            }
            startActivity(intent)
        }
    }

    // Add a method to release camera resources
    private fun releaseCamera() {
        try {
            isCameraActive = false
            cameraProvider?.unbindAll()
            imageAnalyzer?.clearAnalyzer()
            camera = null
            preview = null
            imageAnalyzer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing camera: ${e.message}")
        }
    }
    // Modify showSnakeLibrary to release camera resources

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            binding.inferenceTime.text = "${inferenceTime}ms"
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }
}

