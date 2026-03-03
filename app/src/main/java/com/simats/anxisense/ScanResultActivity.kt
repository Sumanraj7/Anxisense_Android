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

        // 1. Retrieve intent data first
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "N/A"
        val imageUriString = intent.getStringExtra("IMAGE_URI")
        val internalPatientId = intent.getIntExtra("INTERNAL_PATIENT_ID", -1)
        val anxietyScore = intent.getFloatExtra("ANXIETY_SCORE", 0f)
        val anxietyLevel = intent.getStringExtra("ANXIETY_LEVEL") ?: "Moderate"
        val emotionData = intent.getStringExtra("EMOTION_DATA")

        // 2. Initialize Views
        val btnBack: AppCompatButton = findViewById(R.id.btnBack)
        val btnAnalyze: AppCompatButton = findViewById(R.id.btnAnalyze)
        val tvScanId: TextView = findViewById(R.id.tvScanId)
        val ivCapturedImage: ImageView = findViewById(R.id.ivCapturedImage)

        // 3. Set dynamic data
        tvScanId.text = "#AS-${System.currentTimeMillis().toString().takeLast(5)}"
        
        // Display the captured image thumbnail
        imageUriString?.let { uriString ->
             try {
                val uri = Uri.parse(uriString)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                ivCapturedImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("ScanResultActivity", "Error loading image", e)
            }
        }

        // 4. Set up Click Listeners
        btnAnalyze.setOnClickListener {
            val intent = Intent(this, AnalysisResultsActivity::class.java)
            intent.putExtra("PATIENT_NAME", patientName)
            intent.putExtra("PATIENT_ID", patientId)
            if (internalPatientId != -1) {
                intent.putExtra("INTERNAL_PATIENT_ID", internalPatientId)
            }
            
            val percentageStr = "${anxietyScore.toInt()}%"
            intent.putExtra("ANXIETY_PERCENTAGE", percentageStr)
            intent.putExtra("ANXIETY_LEVEL_RAW", anxietyLevel)
            intent.putExtra("ANXIETY_SCORE_RAW", anxietyScore)
            intent.putExtra("DOMINANT_EMOTION", this@ScanResultActivity.intent.getStringExtra("DOMINANT_EMOTION"))
            intent.putExtra("EMOTION_DATA", emotionData)
            intent.putExtra("IS_QUICK_SCAN", this@ScanResultActivity.intent.getBooleanExtra("IS_QUICK_SCAN", false))
            intent.putExtra("IMAGE_URI", imageUriString)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
