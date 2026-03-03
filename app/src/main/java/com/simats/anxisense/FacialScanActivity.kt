package com.simats.anxisense

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

/**
 * FacialScanActivity - Handles facial scanning with camera or uploaded images
 * 
 * Features:
 * - Camera preview with front/back camera switching
 * - Image upload from gallery
 * - Pinch-to-zoom and drag-to-pan for uploaded images
 * - Face detection using ML Kit
 * - Scanning progress with landmarks detection
 * - Navigation to analysis screen
 */
class FacialScanActivity : AppCompatActivity() {

    // UI Components
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var tvGuideText: TextView
    private lateinit var tvLandmarksCount: TextView
    private lateinit var tvCameraLabel: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardScanningStatus: CardView
    private lateinit var layoutScanProgress: LinearLayout
    private lateinit var layoutActionButtons: LinearLayout
    private lateinit var btnStartScan: AppCompatButton
    private lateinit var btnCapturePhoto: AppCompatButton
    private lateinit var btnStopScan: AppCompatButton
    private lateinit var ivUploadedImage: ImageView
    
    // State variables
    private var isScanning = false
    private var uploadedImageUri: Uri? = null
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var currentProgress = 0
    private var isUsingUploadedImage = false
    private val handler = Handler(Looper.getMainLooper())
    
    // Image manipulation properties
    private val imageMatrix = Matrix()
    private val savedMatrix = Matrix()
    private var scale = 1f
    private val lastPoint = PointF()
    private val startPoint = PointF()
    private var mode = NONE
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    
    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadedImageUri = it
            isUsingUploadedImage = true
            displayUploadedImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facial_scan)

        try {
            // Initialize Views
            viewFinder = findViewById(R.id.cameraPreview)
            tvGuideText = findViewById(R.id.tvGuideText)
            tvLandmarksCount = findViewById(R.id.tvLandmarksCount)
            tvCameraLabel = findViewById(R.id.tvCameraLabel)
            tvProgressPercent = findViewById(R.id.tvProgressPercent)
            progressBar = findViewById(R.id.progressBar)
            cardScanningStatus = findViewById(R.id.cardScanningStatus)
            layoutScanProgress = findViewById(R.id.layoutScanProgress)
            layoutActionButtons = findViewById(R.id.layoutActionButtons)
            btnStartScan = findViewById(R.id.btnStartScan)
            btnCapturePhoto = findViewById(R.id.btnCapturePhoto)
            btnStopScan = findViewById(R.id.btnStopScan)
            ivUploadedImage = findViewById(R.id.ivUploadedImage)
            
            val btnBack: ImageView = findViewById(R.id.btnBack)
            val btnUploadImage: FloatingActionButton = findViewById(R.id.btnUploadImage)
            val btnSwitchCamera: FloatingActionButton = findViewById(R.id.btnSwitchCamera)
            val tvSubtitle: TextView = findViewById(R.id.tvSubtitle)

            // Get patient data from intent
            val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
            val patientId = intent.getStringExtra("PATIENT_ID") ?: "N/A"
            tvSubtitle.text = "$patientName • $patientId"

            // Setup click listeners
            btnBack.setOnClickListener { finish() }

            cameraExecutor = Executors.newSingleThreadExecutor()

            // Request camera permissions and start camera
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }

            btnStartScan.setOnClickListener {
                if (!isScanning) {
                    startScanningProcess(patientName, patientId)
                }
            }
            
            btnUploadImage.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }
            
            btnSwitchCamera.setOnClickListener {
                switchCamera()
            }
            
            btnCapturePhoto.setOnClickListener {
                capturePhoto()
            }
            
            btnStopScan.setOnClickListener {
                stopScanning()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FacialScanActivity", e)
            Toast.makeText(this, "Error loading camera: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private var imageCapture: ImageCapture? = null

    /**
     * Initialize and start the camera with face detection
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Setup preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Setup image capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Setup face detection analyzer
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FaceAnalyzer { faceDetected ->
                        runOnUiThread {
                            if (faceDetected && !isScanning) {
                                tvGuideText.text = "Face Detected • Ready to Scan"
                                tvGuideText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                            } else if (!isScanning) {
                                tvGuideText.text = "Position face within guide"
                                tvGuideText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                            }
                        }
                    })
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    
    /**
     * Switch between front and back camera
     */
    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        startCamera()
    }

    /**
     * Display uploaded image and setup touch gestures
     */
    private fun displayUploadedImage(uri: Uri) {
        try {
            // Hide camera preview and show uploaded image
            viewFinder.visibility = View.GONE
            ivUploadedImage.visibility = View.VISIBLE
            
            // Load and display the image
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            
            // Set the bitmap directly - centerCrop will handle the scaling
            ivUploadedImage.setImageBitmap(bitmap)
            ivUploadedImage.scaleType = ImageView.ScaleType.CENTER_CROP
            
            // Hide face guide overlay when showing uploaded image
            findViewById<View>(R.id.faceGuideOverlay).visibility = View.GONE
            
            // Update guide text
            tvGuideText.visibility = View.VISIBLE
            tvGuideText.text = "Image uploaded successfully"
            tvGuideText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
            
            // Show and enable analyze button
            btnStartScan.visibility = View.VISIBLE
            btnStartScan.isEnabled = true
            btnStartScan.text = "Analyze Uploaded Image"
            
            Toast.makeText(this, "Image uploaded successfully. Ready to analyze.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error loading image", e)
            
            // Reset to camera view on error
            isUsingUploadedImage = false
            uploadedImageUri = null
            ivUploadedImage.visibility = View.GONE
            viewFinder.visibility = View.VISIBLE
            findViewById<View>(R.id.faceGuideOverlay).visibility = View.VISIBLE
        }
    }
    
    /**
     * Setup pinch-to-zoom and drag-to-pan gestures for uploaded image
     */
    private fun setupImageGestures() {
        // Initialize scale gesture detector
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        
        // Reset matrix and scale
        imageMatrix.reset()
        scale = 1f
        ivUploadedImage.imageMatrix = imageMatrix
        
        // Setup touch listener
        ivUploadedImage.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(imageMatrix)
                    startPoint.set(event.x, event.y)
                    mode = DRAG
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    savedMatrix.set(imageMatrix)
                    mode = ZOOM
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG) {
                        imageMatrix.set(savedMatrix)
                        val dx = event.x - startPoint.x
                        val dy = event.y - startPoint.y
                        imageMatrix.postTranslate(dx, dy)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }
            }
            
            ivUploadedImage.imageMatrix = imageMatrix
            true
        }
    }
    
    /**
     * Scale gesture listener for pinch-to-zoom
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor
            scale = max(0.5f, min(scale, 5.0f)) // Limit zoom between 0.5x and 5x
            
            imageMatrix.set(savedMatrix)
            imageMatrix.postScale(scale, scale, detector.focusX, detector.focusY)
            
            return true
        }
    }
    
    /**
     * Start the scanning process
     */
    private fun startScanningProcess(name: String, id: String) {
        isScanning = true
        currentProgress = 0
        
        // Show scanning UI
        btnStartScan.visibility = View.GONE
        cardScanningStatus.visibility = View.VISIBLE
        tvCameraLabel.visibility = View.VISIBLE
        tvLandmarksCount.visibility = View.VISIBLE
        layoutScanProgress.visibility = View.VISIBLE
        layoutActionButtons.visibility = View.VISIBLE
        tvGuideText.visibility = View.GONE
        
        // Update camera label
        tvCameraLabel.text = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            "FRONT CAMERA"
        } else {
            "BACK CAMERA"
        }
        
        // Simulate scanning progress
        simulateScanningProgress()
    }
    
    /**
     * Simulate scanning progress (7 seconds)
     */
    private fun simulateScanningProgress() {
        val progressRunnable = object : Runnable {
            override fun run() {
                if (isScanning && currentProgress < 100) {
                    currentProgress += 1  // 100 updates * 70ms = 7000ms (7 seconds)
                    progressBar.progress = currentProgress
                    tvProgressPercent.text = "$currentProgress%"
                    
                    // Update landmarks count progressively
                    val landmarks = (currentProgress / 11).coerceIn(0, 9)
                    tvLandmarksCount.text = "Landmarks Detected\n$landmarks/9"
                    
                    handler.postDelayed(this, 70)  // 70ms delay for 7 second total
                } else if (currentProgress >= 100) {
                    if (isUsingUploadedImage) {
                        completeScan()
                    } else {
                        capturePhoto()
                    }
                }
            }
        }
        handler.post(progressRunnable)
    }
    
    /**
     * Capture photo during scanning or when scan completes
     */
    private fun capturePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = java.io.File(
            externalCacheDir,
            "AnxiSense_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Photo capture failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)
                    
                    // If scanning, this auto-completes the process
                    if (isScanning) {
                        try {
                            // Run cropping in background to avoid blocking UI thread if it's slow
                            cameraExecutor.execute {
                                cropAndSaveImage(photoFile)
                                runOnUiThread {
                                    completeScan(savedUri.toString())
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error initiating crop", e)
                            completeScan(savedUri.toString())
                        }
                    } else {
                         Toast.makeText(baseContext, "Photo captured!", Toast.LENGTH_SHORT).show()
                         // Optionally open analysis for this single photo immediately
                         uploadedImageUri = savedUri
                         isUsingUploadedImage = true
                         displayUploadedImage(savedUri)
                    }
                }
            }
        )
    }

    private fun cropAndSaveImage(photoFile: java.io.File) {
        try {
            // 1. Decode bitmap
            var bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath) ?: return
            
            // 2. Handle Rotation from EXIF
            val exif = android.media.ExifInterface(photoFile.absolutePath)
            val orientation = exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION, 
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
            
            val matrix = android.graphics.Matrix()
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            
            // Apply rotation if needed
            if (orientation != android.media.ExifInterface.ORIENTATION_NORMAL && 
                orientation != android.media.ExifInterface.ORIENTATION_UNDEFINED) {
                bitmap = android.graphics.Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
            }
            
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
            val detector = FaceDetection.getClient(options)
            
            // 3. Detect Face Sync (since we are in background thread)
            val task = detector.process(inputImage)
            val faces = com.google.android.gms.tasks.Tasks.await(task)
            
            var finalBitmap: android.graphics.Bitmap? = null
            
            if (faces.isNotEmpty()) {
                // Use the largest face
                val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                face?.let {
                    val box = it.boundingBox
                    
                    // Add padding (20%)
                    val paddingX = (box.width() * 0.2).toInt()
                    val paddingY = (box.height() * 0.2).toInt()
                    
                    val startX = (box.left - paddingX).coerceAtLeast(0)
                    val startY = (box.top - paddingY).coerceAtLeast(0)
                    val endX = (box.right + paddingX).coerceAtMost(bitmap.width)
                    val endY = (box.bottom + paddingY).coerceAtMost(bitmap.height)
                    
                    val width = endX - startX
                    val height = endY - startY
                    
                    if (width > 0 && height > 0) {
                        finalBitmap = android.graphics.Bitmap.createBitmap(
                            bitmap, startX, startY, width, height
                        )
                        Log.d(TAG, "Cropped to detected face: $width x $height")
                    }
                }
            }
            
            // Fallback if no face detected or crop failed
            if (finalBitmap == null) {
                // Crop Center-Top (Face Guide) - Tighter crop (45% width)
                val width = bitmap.width
                val height = bitmap.height
                
                val cropWidth = (width * 0.45).toInt() 
                val cropHeight = (cropWidth * 1.5).toInt() 
                
                val startX = (width - cropWidth) / 2
                val startY = (height * 0.15).toInt() 
                
                val finalWidth = if (startX + cropWidth > width) width - startX else cropWidth
                val finalHeight = if (startY + cropHeight > height) height - startY else cropHeight
                
                if (finalWidth > 0 && finalHeight > 0) {
                    finalBitmap = android.graphics.Bitmap.createBitmap(
                        bitmap, startX, startY, finalWidth, finalHeight
                    )
                    Log.d(TAG, "Fallback center crop: $finalWidth x $finalHeight")
                }
            }
            
            // 4. Save back
            if (finalBitmap != null) {
                val out = java.io.FileOutputStream(photoFile)
                finalBitmap!!.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                
                if (bitmap != finalBitmap) {
                     bitmap.recycle()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cropping image with ML Kit", e)
        }
    }

    /**
     * Stop the scanning process
     */
    private fun stopScanning() {
        isScanning = false
        handler.removeCallbacksAndMessages(null)
        resetUI()
    }

    /**
     * Complete scan and navigate to analysis screen
     */
    private fun completeScan(imagePath: String? = null) {
        isScanning = false
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "N/A"
        val internalPatientId = intent.getIntExtra("INTERNAL_PATIENT_ID", -1)
        val isQuickScan = intent.getBooleanExtra("IS_QUICK_SCAN", false)

        val finalImageUriString = imagePath ?: uploadedImageUri?.toString()

        if (finalImageUriString == null) {
            Toast.makeText(this, "No image to analyze.", Toast.LENGTH_SHORT).show()
            stopScanning()
            return
        }

        val finalImageUri = Uri.parse(finalImageUriString)

        val intent = Intent(this, AnxietyAnalysisActivity::class.java).apply {
            putExtra("PATIENT_NAME", patientName)
            putExtra("PATIENT_ID", patientId)
            if (internalPatientId != -1) {
                putExtra("INTERNAL_PATIENT_ID", internalPatientId)
            }
            putExtra("IS_QUICK_SCAN", isQuickScan)
            putExtra("IMAGE_URI", finalImageUriString)

            // Grant URI permission if it is a content URI
            if (finalImageUri.scheme == "content") {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        startActivity(intent)
        finish()
    }
    
    /**
     * Reset UI to initial state
     */
    private fun resetUI() {
        btnStartScan.visibility = View.VISIBLE
        btnStartScan.text = "Start Scan"
        cardScanningStatus.visibility = View.GONE
        tvCameraLabel.visibility = View.GONE
        tvLandmarksCount.visibility = View.GONE
        layoutScanProgress.visibility = View.GONE
        layoutActionButtons.visibility = View.GONE
        tvGuideText.visibility = View.VISIBLE
        tvGuideText.text = "Position face within guide"
        tvGuideText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        currentProgress = 0
        progressBar.progress = 0
        
        // Reset image upload state
        if (isUsingUploadedImage) {
            ivUploadedImage.visibility = View.GONE
            viewFinder.visibility = View.VISIBLE
            findViewById<View>(R.id.faceGuideOverlay).visibility = View.VISIBLE
            isUsingUploadedImage = false
            uploadedImageUri = null
        }
    }

    /**
     * Check if all required permissions are granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::cameraExecutor.isInitialized) {
                cameraExecutor.shutdown()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down camera executor", e)
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Face detection analyzer using ML Kit
     */
    private class FaceAnalyzer(private val onFaceDetected: (Boolean) -> Unit) : ImageAnalysis.Analyzer {
        private val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        private val detector = FaceDetection.getClient(options)

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        onFaceDetected(faces.isNotEmpty())
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        
        // Touch gesture modes
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }
}
