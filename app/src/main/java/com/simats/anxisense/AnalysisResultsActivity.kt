package com.simats.anxisense

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.simats.anxisense.api.DoctorApi
import com.simats.anxisense.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AnalysisResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis_results)

        // Initialize Views
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val tvSubtitle: TextView = findViewById(R.id.tvSubtitle)
        
        val anxietyScoreCard: CardView = findViewById(R.id.anxietyScoreCard)
        val tvAnxietyScoreBig: TextView = findViewById(R.id.tvAnxietyScoreBig)
        val tvAnxietyLevelText: TextView = findViewById(R.id.tvAnxietyLevelText)
        val pbAnxietyScore: ProgressBar = findViewById(R.id.pbAnxietyScore)
        
        val tvGuideFooter: TextView = findViewById(R.id.tvGuideFooter)
        val tvClinicalInterpretation: TextView = findViewById(R.id.tvClinicalInterpretation)
        val llKeyMetricsContainer: LinearLayout = findViewById(R.id.llKeyMetricsContainer)
        
        val btnNewScan: AppCompatButton = findViewById(R.id.btnNewScan)
        val btnViewRecommendations: AppCompatButton = findViewById(R.id.btnViewRecommendations)

        // Get data from intent
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "N/A"
        
        // Priority: Raw API values -> Percentage String -> Default
        val rawAnxietyScore = intent.getFloatExtra("ANXIETY_SCORE_RAW", -1f)
        val rawAnxietyLevelStr = intent.getStringExtra("ANXIETY_LEVEL_RAW")
        val anxietyPercentageStr = intent.getStringExtra("ANXIETY_PERCENTAGE") ?: "0%"

        // Display Patient Info
        tvSubtitle.text = "$patientName • $patientId"
        
        // Calculate Score (0-100)
        val finalScore = if (rawAnxietyScore != -1f) {
            rawAnxietyScore.toInt()
        } else {
            // Parse "85%" -> 85
            try {
                anxietyPercentageStr.replace("%", "").toInt()
            } catch (e: Exception) {
                0
            }
        }

        // --- Apply Logic for New UI Guide ---
        // 0-39: Low (Green)
        // 40-69: Moderate (Orange)
        // 70-100: High (Red)
        val (levelText, colorHex, interpretation) = when {
            finalScore < 40 -> Triple(
                "Low Anxiety", 
                "#10B981", 
                "Patient exhibits minimal anxiety indicators. Proceed with standard care protocol. Continue to monitor for any changes in demeanor."
            )
            finalScore < 70 -> Triple(
                "Moderate Anxiety", 
                "#F59E0B", 
                "Patient shows elevated anxiety indicators. Consider additional reassurance and monitor closely during procedure. Verify comfort levels frequently."
            )
            else -> Triple(
                "High Anxiety", 
                "#EF4444", 
                "Patient exhibits significant anxiety markers. Consider pausing, using calming techniques, or discussing sedation options if applicable. Prioritize patient comfort."
            )
        }

        // Update Score Card
        tvAnxietyScoreBig.text = finalScore.toString()
        tvAnxietyLevelText.text = levelText
        anxietyScoreCard.setCardBackgroundColor(Color.parseColor(colorHex))
        pbAnxietyScore.progress = finalScore
        
        // Update Guide Footer
        tvGuideFooter.text = "Current Score: $finalScore - The patient falls under $levelText category"
        
        // Update Clinical Interpretation
        tvClinicalInterpretation.text = interpretation

        // Dynamic Emotion Metrics
        val emotionData = intent.getStringExtra("EMOTION_DATA")
        if (emotionData != null) {
            try {
                llKeyMetricsContainer.removeAllViews()
                val emotionsJson = org.json.JSONObject(emotionData)
                val emotionList = mutableListOf<Pair<String, Double>>()
                
                val keys = emotionsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = emotionsJson.getDouble(key)
                    emotionList.add(key to value)
                }
                
                // Sort by value descending
                emotionList.sortByDescending { it.second }
                
                // Inflate and add views
                for ((emotion, value) in emotionList) {
                    if (value < 1.0) continue // Skip clutter
                    
                    val view = layoutInflater.inflate(R.layout.item_emotion_metric, llKeyMetricsContainer, false)
                    val tvLabel = view.findViewById<TextView>(R.id.tvEmotionLabel)
                    val tvPercentage = view.findViewById<TextView>(R.id.tvEmotionPercentage)
                    val progressBar = view.findViewById<ProgressBar>(R.id.pbEmotion)
                    
                    val capitalizedEmotion = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    tvLabel.text = capitalizedEmotion
                    tvPercentage.text = String.format("%.1f%%", value)
                    progressBar.progress = value.toInt()
                    
                    // Optional: Color code metrics if desired, or keep uniform
                     val metricColor = when(emotion.lowercase()) {
                        "angry" -> "#EF4444"
                        "happy" -> "#10B981"
                        "sad" -> "#3B82F6"
                        "fear" -> "#8B5CF6"
                        "surprise" -> "#F59E0B"
                        "disgust" -> "#14B8A6"
                        else -> "#6B7280"
                    }
                    progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(metricColor))
                    
                    llKeyMetricsContainer.addView(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Back Button
        btnBack.setOnClickListener { finish() }

        // New Scan Button -> QuickScanActivity
        btnNewScan.setOnClickListener {
             // Save first? Probably safer to just go to new scan without saving current if user hits this, 
             // BUT usually "New Scan" implies discarding or finishing. 
             // I'll assume it discards this result and starts fresh.
             val intent = Intent(this, QuickScanActivity::class.java)
             startActivity(intent)
             finish()
        }

        // View Recommendations (Acts as Save & Continue)
        btnViewRecommendations.setOnClickListener {
            val progressDialog = android.app.ProgressDialog(this)
            progressDialog.setMessage("Saving Assessment...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            saveAssessmentData(
                patientName, 
                patientId, 
                finalScore.toFloat(),
                levelText, 
                progressDialog
            )
        }
    }

    private fun saveAssessmentData(
        patientName: String,
        patientId: String,
        anxietyScore: Float,
        anxietyLevel: String,
        progressDialog: android.app.ProgressDialog
    ) {
        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
        val doctorId = sharedPreferences.getInt("DOCTOR_ID", -1)

        if (doctorId == -1) {
            progressDialog.dismiss()
            Toast.makeText(this, "Doctor ID not found. Please relogin.", Toast.LENGTH_LONG).show()
            return
        }

        val internalPatientId = intent.getIntExtra("INTERNAL_PATIENT_ID", -1)
        if (internalPatientId == -1) {
             progressDialog.dismiss()
             Toast.makeText(this, "Error: Patient ID missing. Cannot save.", Toast.LENGTH_SHORT).show()
             return
        }

        val dominantEmotion = intent.getStringExtra("DOMINANT_EMOTION")

        val request = DoctorApi.SaveAssessmentRequest(
            patient_id = internalPatientId,
            doctor_id = doctorId,
            anxiety_score = anxietyScore,
            anxiety_level = anxietyLevel,
            dominant_emotion = dominantEmotion
        )

        RetrofitClient.instance.saveAssessment(request)
            .enqueue(object : Callback<DoctorApi.SaveAssessmentResponse> {
                override fun onResponse(
                    call: Call<DoctorApi.SaveAssessmentResponse>,
                    response: Response<DoctorApi.SaveAssessmentResponse>
                ) {
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@AnalysisResultsActivity, "Assessment saved!", Toast.LENGTH_SHORT).show()
                        // Proceed to Records or Dashboard
                        val intent = Intent(this@AnalysisResultsActivity, PatientRecordsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: response.message()
                        Toast.makeText(this@AnalysisResultsActivity, "Failed: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<DoctorApi.SaveAssessmentResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@AnalysisResultsActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
