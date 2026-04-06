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
import androidx.core.content.ContextCompat
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
        val tvPatientNameLabel: TextView = findViewById(R.id.tvPatientNameLabel)
        val tvPatientIdLabel: TextView = findViewById(R.id.tvPatientIdLabel)
        
        val tvAnxietyScoreBig: TextView = findViewById(R.id.tvAnxietyScoreBig)
        val tvAnxietyLevelText: TextView = findViewById(R.id.tvAnxietyLevelText)
        val pbAnxietyCircle: ProgressBar = findViewById(R.id.pbAnxietyCircle)
        
        val llKeyMetricsContainer: LinearLayout = findViewById(R.id.llKeyMetricsContainer)
        
        val btnBack: View = findViewById(R.id.btnBack)
        val btnNewScan: AppCompatButton = findViewById(R.id.btnNewScan)
        val btnViewRecommendations: AppCompatButton = findViewById(R.id.btnViewRecommendations)

        // Get data from intent
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "N/A"
        
        // Priority: Raw API values -> Percentage String -> Default
        val rawAnxietyScore = intent.getFloatExtra("ANXIETY_SCORE_RAW", -1f)
        val anxietyPercentageStr = intent.getStringExtra("ANXIETY_PERCENTAGE") ?: "0%"
        val imageUriString = intent.getStringExtra("IMAGE_URI")

        // Display Image if available
        val ivAnalyzedFace = findViewById<ImageView>(R.id.ivAnalyzedFace)
        imageUriString?.let { uriString ->
            try {
                val uri = android.net.Uri.parse(uriString)
                val bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
                ivAnalyzedFace.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Display Patient Info
        tvPatientNameLabel.text = patientName
        tvPatientIdLabel.text = "ID: $patientId"
        
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
        // --- Apply Logic for New UI Guide ---
        // 0-39: Low, 40-69: Moderate, 70-100: High
        val (levelText, colorRes) = when {
            finalScore < 40 -> Pair("Low Anxiety", R.color.anxiety_low)
            finalScore < 70 -> Pair("Moderate Anxiety", R.color.anxiety_moderate)
            else -> Pair("High Anxiety", R.color.anxiety_high)
        }

        val badgeBgRes = when {
            finalScore < 40 -> R.color.anxiety_low_bg
            finalScore < 70 -> R.color.anxiety_moderate_bg
            else -> R.color.anxiety_high_bg
        }

        // Update Score Card
        tvAnxietyScoreBig.text = finalScore.toString()
        tvAnxietyLevelText.text = levelText
        pbAnxietyCircle.progress = finalScore
        
        // Apply color to circle
        pbAnxietyCircle.progressTintList = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, colorRes))
        
        // Apply badge styling
        tvAnxietyLevelText.backgroundTintList = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, badgeBgRes))
        tvAnxietyLevelText.setTextColor(ContextCompat.getColor(this, colorRes))

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
                    tvPercentage.text = String.format(Locale.getDefault(), "%.1f%%", value)
                    progressBar.progress = value.toInt()
                    
                    // Optional: Color code metrics if desired, or keep uniform
                    // Color code metrics if desired using editorial palette
                     val metricColorRes = when(emotion.lowercase()) {
                        "angry" -> R.color.emotion_angry
                        "happy" -> R.color.emotion_happy
                        "sad" -> R.color.emotion_sad
                        "fear" -> R.color.emotion_fear
                        "surprise" -> R.color.emotion_surprise
                        "disgust" -> R.color.emotion_disgust
                        else -> R.color.emotion_neutral
                    }
                    progressBar.progressTintList = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, metricColorRes))
                    
                    llKeyMetricsContainer.addView(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Back Button
        btnBack.setOnClickListener { finish() }
        
        val btnExportPdf: ImageView = findViewById(R.id.btnExportPdf)
        btnExportPdf.setOnClickListener {
            generatePdfReport(
                fileNameSuffix = "$patientName - Assessment",
                patientNameData = patientName,
                patientIdData = patientId,
                scoreData = "$finalScore%",
                levelData = levelText,
                emotionDataJson = emotionData,
                dateData = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            )
        }

        // New Scan Button -> QuickScanActivity
        btnNewScan.setOnClickListener {
             val intent = Intent(this, QuickScanActivity::class.java)
             startActivity(intent)
             finish()
        }

        val isQuickScan = intent.getBooleanExtra("IS_QUICK_SCAN", false)
        if (isQuickScan) {
            btnViewRecommendations.text = "Save to Records"
        }

        val internalPatientId = intent.getIntExtra("INTERNAL_PATIENT_ID", -1)

        // View Recommendations (Acts as Save & Continue)
        btnViewRecommendations.setOnClickListener {
            if (isQuickScan && internalPatientId <= 0) {
                // Quick Scan results first - now navigate to PatientInformationActivity to enter details
                val intent = Intent(this, PatientInformationActivity::class.java)
                intent.putExtra("IS_QUICK_SCAN", true)
                intent.putExtra("PATIENT_NAME", patientName)
                intent.putExtra("PATIENT_ID", patientId)
                intent.putExtra("ANXIETY_SCORE_RAW", rawAnxietyScore)
                intent.putExtra("ANXIETY_LEVEL_RAW", levelText)
                intent.putExtra("ANXIETY_PERCENTAGE", finalScore.toString())
                intent.putExtra("DOMINANT_EMOTION", this@AnalysisResultsActivity.intent.getStringExtra("DOMINANT_EMOTION"))
                intent.putExtra("EMOTION_DATA", emotionData)
                intent.putExtra("IMAGE_URI", imageUriString)
                startActivity(intent)
            } else {
                // Normal flow OR Reversed Quick Scan (where patient is already created)
                @Suppress("DEPRECATION")
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
    }

    private fun generatePdfReport(
        fileNameSuffix: String,
        patientNameData: String,
        patientIdData: String,
        scoreData: String,
        levelData: String,
        emotionDataJson: String?,
        dateData: String
    ) {
        // 1. Inflate the PDF Layout
        val pdfView = layoutInflater.inflate(R.layout.layout_pdf_report, null)
        
        // 2. Bind Data to the PDF View
        val tvName = pdfView.findViewById<TextView>(R.id.pdfPatientName)
        val tvId = pdfView.findViewById<TextView>(R.id.pdfPatientId)
        val tvReportId = pdfView.findViewById<TextView>(R.id.pdfReportId)
        val tvScore = pdfView.findViewById<TextView>(R.id.pdfScore)
        val tvLevel = pdfView.findViewById<TextView>(R.id.pdfLevel)
        val tvDominantEmotion = pdfView.findViewById<TextView>(R.id.pdfDominantEmotion)
        val tvDate = pdfView.findViewById<TextView>(R.id.pdfReportDate)
        val metricsContainer = pdfView.findViewById<LinearLayout>(R.id.pdfDynamicMetricsContainer)
        
        tvName.text = patientNameData
        tvId.text = patientIdData
        tvReportId.text = String.format(Locale.getDefault(), "#RPT-%s", System.currentTimeMillis().toString().takeLast(5))
        tvScore.text = scoreData
        tvLevel.text = levelData
        tvDate.text = String.format(Locale.getDefault(), "Date: %s", dateData)

        // Load Patient Photo
        val pdfPhoto = pdfView.findViewById<ImageView>(R.id.pdfPatientPhoto)
        val imageUriString = intent.getStringExtra("IMAGE_URI")
        if (imageUriString != null) {
            try {
                val uri = android.net.Uri.parse(imageUriString)
                @Suppress("DEPRECATION")
                val bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
                pdfPhoto.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                pdfPhoto.setImageResource(R.mipmap.ic_launcher) // Fallback
            }
        } else {
             pdfPhoto.setImageResource(R.mipmap.ic_launcher)
        }

        // Dynamic Metrics & Dominant Emotion
        if (emotionDataJson != null) {
             try {
                val emotionsJson = org.json.JSONObject(emotionDataJson)
                val emotionList = mutableListOf<Pair<String, Double>>()
                
                val keys = emotionsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = emotionsJson.getDouble(key)
                    emotionList.add(key to value)
                }
                emotionList.sortByDescending { it.second }
                
                if (emotionList.isNotEmpty()) {
                    val dominant = emotionList.first()
                    val capitalizedDominant = dominant.first.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    tvDominantEmotion.text = String.format(Locale.getDefault(), "Dominant Emotion: %s", capitalizedDominant)
                } else {
                     tvDominantEmotion.text = "Dominant Emotion: N/A"
                }

                // Add rows for each emotion
                metricsContainer.removeAllViews()
                for ((emotion, value) in emotionList) {
                    if (value < 1.0) continue // Skip clutter

                    // Create Row Layout Programmatically or Inflate simple row
                    val row = LinearLayout(this).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 16, 0, 16)
                    }

                    // Metric Name
                    val tvMetric = TextView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        setPadding(16, 0, 0, 0)
                        text = emotion.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        textSize = 13f
                        setTextColor(ContextCompat.getColor(this@AnalysisResultsActivity, R.color.editorial_text_secondary))
                    }

                    // Value
                    val tvValue = TextView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(100.dpToPx(), LinearLayout.LayoutParams.WRAP_CONTENT)
                        text = String.format(Locale.getDefault(), "%.1f%%", value)
                        textSize = 13f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(ContextCompat.getColor(this@AnalysisResultsActivity, R.color.editorial_text_primary))
                    }

                    // Status
                    val tvStatus = TextView(this).apply {
                         layoutParams = LinearLayout.LayoutParams(100.dpToPx(), LinearLayout.LayoutParams.WRAP_CONTENT)
                         textSize = 13f
                         if (value > 50) {
                             text = "Dominant"
                             setTextColor(ContextCompat.getColor(this@AnalysisResultsActivity, R.color.anxiety_high))
                         } else if (value > 20) {
                             text = "Present"
                             setTextColor(ContextCompat.getColor(this@AnalysisResultsActivity, R.color.anxiety_moderate))
                         } else {
                             text = "Trace"
                             setTextColor(ContextCompat.getColor(this@AnalysisResultsActivity, R.color.anxiety_low))
                         }
                    }

                    row.addView(tvMetric)
                    row.addView(tvValue)
                    row.addView(tvStatus)
                    
                    metricsContainer.addView(row)
                    
                    // Separator
                    val separator = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                        setBackgroundColor(ContextCompat.getColor(this@AnalysisResultsActivity, R.color.editorial_divider))
                    }
                    metricsContainer.addView(separator)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                tvDominantEmotion.text = "Error"
            }
        } else {
            tvDominantEmotion.text = "N/A"
        }

        // 3. Measure and Layout the View (A4 dimensions: 595 x 842 points)
        val density = resources.displayMetrics.density
        val pageWidthPoints = 595
        val pageHeightPoints = 842

        val pageWidthPixels = (pageWidthPoints * density).toInt()
        
        pdfView.measure(
            View.MeasureSpec.makeMeasureSpec(pageWidthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED) 
        )
        
        val contentHeightPixels = pdfView.measuredHeight
        pdfView.layout(0, 0, pageWidthPixels, contentHeightPixels)

        // 4. Create PDF Document
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidthPoints, pageHeightPoints, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        // Scale canvas
        page.canvas.scale(1f / density, 1f / density)

        // 5. Draw View to PDF Canvas
        pdfView.draw(page.canvas)
        
        pdfDocument.finishPage(page)

        // 6. Save PDF
        val cleanFileName = "Report_${fileNameSuffix.replace(Regex("[^a-zA-Z0-9]"), "_")}.pdf"
        
        try {
            savePdfToStorage(pdfDocument, cleanFileName)
            Toast.makeText(this, "PDF Report Saved: $cleanFileName", Toast.LENGTH_LONG).show()
        } catch (e: java.io.IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun savePdfToStorage(pdfDocument: android.graphics.pdf.PdfDocument, fileName: String) {
        val outputStream: java.io.OutputStream?

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            outputStream = uri?.let { contentResolver.openOutputStream(it) }
        } else {
            val directory = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!directory.exists()) directory.mkdirs()
            val file = java.io.File(directory, fileName)
            outputStream = java.io.FileOutputStream(file)
        }

        outputStream?.use {
            pdfDocument.writeTo(it)
        } ?: throw java.io.IOException("Failed to create output stream")
    }
    
    // Helper extension
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    @Suppress("DEPRECATION")
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
                        
                        // Proceed directly to the report detail screen
                        val detIntent = Intent(this@AnalysisResultsActivity, AssessmentDetailActivity::class.java)
                        detIntent.putExtra("patient_name", patientName)
                        detIntent.putExtra("patient_code", patientId)
                        detIntent.putExtra("anxiety_score", anxietyScore)
                        detIntent.putExtra("anxiety_level", anxietyLevel)
                        detIntent.putExtra("dominant_emotion", dominantEmotion)
                        detIntent.putExtra("created_at", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))
                        
                        detIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(detIntent)
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
