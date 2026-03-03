package com.simats.anxisense

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class AnxietyAnalysisActivity : AppCompatActivity() {

    private lateinit var tvPatientInfo: TextView
    private lateinit var tvAnalysisDescription: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var ivAnalyzedImage: ImageView
    private lateinit var btnRetry: androidx.appcompat.widget.AppCompatButton

    private var currentProgress = 0
    private val handler = Handler(Looper.getMainLooper())
    private var imageUriString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anxiety_analysis)

        tvPatientInfo = findViewById(R.id.tvPatientInfo)
        tvAnalysisDescription = findViewById(R.id.tvAnalysisDescription)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        ivAnalyzedImage = findViewById(R.id.ivAnalyzedImage)
        btnRetry = findViewById(R.id.btnRetry)

        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "N/A"
        imageUriString = intent.getStringExtra("IMAGE_URI")

        tvPatientInfo.text = "$patientName • $patientId"

        imageUriString?.let { uriString ->
            try {
                val uri = Uri.parse(uriString)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                ivAnalyzedImage.setImageBitmap(bitmap)
                ivAnalyzedImage.scaleType = ImageView.ScaleType.CENTER_CROP
                performAnalysis(bitmap)
            } catch (e: IOException) {
                Log.e("AnxietyAnalysis", "Error loading image", e)
            }
        } ?: run {
            // Fallback if no image URI
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnRetry.setOnClickListener {
            // Restart Facial Scan Activity
            val retryIntent = Intent(this, FacialScanActivity::class.java)
            retryIntent.putExtra("PATIENT_NAME", intent.getStringExtra("PATIENT_NAME"))
            retryIntent.putExtra("PATIENT_ID", intent.getStringExtra("PATIENT_ID"))
            if (intent.hasExtra("INTERNAL_PATIENT_ID")) {
                retryIntent.putExtra("INTERNAL_PATIENT_ID", intent.getIntExtra("INTERNAL_PATIENT_ID", -1))
            }
            startActivity(retryIntent)
            finish()
        }
    }

    private fun performAnalysis(bitmap: Bitmap) {
        tvStatus.text = "Preparing image for analysis..."
        progressBar.visibility = android.view.View.VISIBLE
        btnRetry.visibility = android.view.View.GONE
        
        // Convert Bitmap to File
        val file = createImageFile(bitmap)
        if (file == null) {
            handleError("Failed to prepare image for upload.")
            return
        }

        uploadImageForAnalysis(file)
    }

    private fun createImageFile(bitmap: Bitmap): java.io.File? {
        return try {
            val validDir = java.io.File(cacheDir, "analysis_images")
            if (!validDir.exists()) validDir.mkdirs()
            
            val file = java.io.File(validDir, "temp_face_${System.currentTimeMillis()}.jpg")
            val bos = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
            val bitmapData = bos.toByteArray()
            
            val fos = java.io.FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageForAnalysis(file: java.io.File) {
        tvStatus.text = "Uploading to AnxiSense AI..."

        val requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), file)
        val body = okhttp3.MultipartBody.Part.createFormData("image", file.name, requestFile)

        com.simats.anxisense.api.RetrofitClient.instance.analyzeFace(body)
            .enqueue(object : retrofit2.Callback<com.simats.anxisense.api.DoctorApi.AnalyzeResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.AnalyzeResponse>,
                    response: retrofit2.Response<com.simats.anxisense.api.DoctorApi.AnalyzeResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        completeAnalysis(
                            result.anxiety_score,
                            result.anxiety_level,
                            result.dominant_emotion,
                            result.emotion_probabilities
                        )
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            val json = org.json.JSONObject(errorBody ?: "")
                            json.optString("error", "Analysis failed: ${response.message()}")
                        } catch (e: Exception) {
                            "Server Error: ${response.message()}"
                        }
                        handleError(errorMessage)
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.AnalyzeResponse>, t: Throwable) {
                    handleError("Network Error: ${t.message}")
                }
            })
    }

    private fun handleError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        tvStatus.text = "Analysis Failed"
        progressBar.visibility = android.view.View.GONE
        btnRetry.visibility = android.view.View.VISIBLE
        Log.e("AnxietyAnalysis", msg)
    }

    private fun completeAnalysis(
        anxietyScore: Float, 
        anxietyLevel: String, 
        dominantEmotion: String,
        emotionProbabilities: Map<String, Float>
    ) {
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "N/A"
        val internalPatientId = intent.getIntExtra("INTERNAL_PATIENT_ID", -1)

        val intent = Intent(this, ScanResultActivity::class.java)
        intent.putExtra("PATIENT_NAME", patientName)
        intent.putExtra("PATIENT_ID", patientId)
        if (internalPatientId != -1) {
            intent.putExtra("INTERNAL_PATIENT_ID", internalPatientId)
        }
        intent.putExtra("IS_QUICK_SCAN", this@AnxietyAnalysisActivity.intent.getBooleanExtra("IS_QUICK_SCAN", false))
        intent.putExtra("IMAGE_URI", imageUriString)

        intent.putExtra("ANXIETY_SCORE", anxietyScore)
        intent.putExtra("ANXIETY_LEVEL", anxietyLevel)
        intent.putExtra("DOMINANT_EMOTION", dominantEmotion)
        
        // Convert Map to JSON String manually or use Gson if available. 
        // For simplicity/robustness without adding deps checking:
        val emotionsJson = com.google.gson.Gson().toJson(emotionProbabilities)
        intent.putExtra("EMOTION_DATA", emotionsJson)

        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
