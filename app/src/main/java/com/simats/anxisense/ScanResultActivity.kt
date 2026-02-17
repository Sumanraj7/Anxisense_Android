package com.simats.anxisense

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        // Initialize Views
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnRetry: AppCompatButton = findViewById(R.id.btnRetry)
        val btnAnalyze: AppCompatButton = findViewById(R.id.btnAnalyze)
        val tvSubtitle: TextView = findViewById(R.id.tvSubtitle)
        val ivCapturedImage: ImageView = findViewById(R.id.ivCapturedImage)

        // Get data from intent
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "N/A"
        val imageUriString = intent.getStringExtra("IMAGE_URI")

        tvSubtitle.text = "$patientName • $patientId"
        
        // Display the captured image
        imageUriString?.let { uriString ->
             try {
                val uri = Uri.parse(uriString)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                ivCapturedImage.setImageBitmap(bitmap)
                ivCapturedImage.scaleType = ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                Log.e("ScanResultActivity", "Error loading image", e)
                // ivCapturedImage keeps default placeholder
            }
        }

        // Back Button - Goes to Dashboard (since previous activities finished)
        btnBack.setOnClickListener {
            finish()
        }

        // Retrieve analysis results
        val anxietyScore = intent.getFloatExtra("ANXIETY_SCORE", 0f)
        val anxietyLevel = intent.getStringExtra("ANXIETY_LEVEL") ?: "Moderate"
        val emotionData = intent.getStringExtra("EMOTION_DATA") // Optional: JSON string of emotions
        val internalPatientId = intent.getIntExtra("INTERNAL_PATIENT_ID", -1)

        // Analyze Results Button - Proceed to Final Report
        btnAnalyze.setOnClickListener {
            val intent = Intent(this, AnalysisResultsActivity::class.java)
            intent.putExtra("PATIENT_NAME", patientName)
            intent.putExtra("PATIENT_ID", patientId)
            if (internalPatientId != -1) {
                intent.putExtra("INTERNAL_PATIENT_ID", internalPatientId)
            }
            
            // Format score as percentage string for AnalysisResultsActivity
            val percentageStr = "${anxietyScore.toInt()}%"
            intent.putExtra("ANXIETY_PERCENTAGE", percentageStr)
            intent.putExtra("ANXIETY_LEVEL_RAW", anxietyLevel)
            intent.putExtra("ANXIETY_SCORE_RAW", anxietyScore)
            intent.putExtra("DOMINANT_EMOTION", intent.getStringExtra("DOMINANT_EMOTION"))
            intent.putExtra("EMOTION_DATA", emotionData)
            
            intent.putExtra("IMAGE_URI", imageUriString)
            startActivity(intent)
            // Do not finish() here if we want users to be able to come back to this confirmation
            // But typically confirmation screens are one-off. Let's finish() to keep stack clean?
            // Actually, keep it so they can press 'Back' from results to re-verify if they want.
        }

        // Retry Scan Button - Restart FacialScanActivity
        btnRetry.setOnClickListener {
            val intent = Intent(this, FacialScanActivity::class.java)
            intent.putExtra("PATIENT_NAME", patientName)
            intent.putExtra("PATIENT_ID", patientId)
            if (internalPatientId != -1) {
                intent.putExtra("INTERNAL_PATIENT_ID", internalPatientId)
            }
            startActivity(intent)
            finish() // Close this result screen
        }
    }
}
