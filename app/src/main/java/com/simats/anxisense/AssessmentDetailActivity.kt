package com.simats.anxisense

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class AssessmentDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment_detail)

        val tvPatientTitle = findViewById<TextView>(R.id.tvPatientTitle)
        val tvPatientSubtitle = findViewById<TextView>(R.id.tvPatientSubtitle)
        
        val tvScoreLarge = findViewById<TextView>(R.id.tvScoreLarge)
        val tvStatusBadge = findViewById<TextView>(R.id.tvStatusBadge)
        val pbAnxietyCircleDetail = findViewById<android.widget.ProgressBar>(R.id.pbAnxietyCircleDetail)
        val tvInterpretationSummary = findViewById<TextView>(R.id.tvInterpretationSummary)
        val tvAnalysisDate = findViewById<TextView>(R.id.tvAnalysisDate)
        
        val btnBack = findViewById<View>(R.id.btnBack)
        val btnFinish = findViewById<AppCompatButton>(R.id.btnFinish)
        val btnExport = findViewById<AppCompatButton>(R.id.btnExport)

        // Retrieve data from intent
        val patientName = intent.getStringExtra("patient_name") ?: "Unknown Patient"
        val patientId = intent.getStringExtra("patient_code") ?: "N/A"

        val scoreFloat = intent.getFloatExtra("anxiety_score", -1f)
        val anxietyScore = if (scoreFloat != -1f) "${scoreFloat.toInt()}%" else "N/A"

        val anxietyLevel = intent.getStringExtra("anxiety_level") ?: "Unknown"
        val dominantEmotion = intent.getStringExtra("dominant_emotion") ?: "N/A"
        val date = intent.getStringExtra("created_at") ?: ""

        val percentage = if (scoreFloat != -1f) scoreFloat.toInt() else 0
        val anxietyLevelData = AnxietyLevelUtils.getAnxietyLevelFromPercentage(percentage)

        // Set data
        tvPatientTitle.text = patientName
        tvPatientSubtitle.text = "ID: $patientId"
        tvAnalysisDate.text = date
        
        tvScoreLarge.text = percentage.toString()
        tvStatusBadge.text = anxietyLevel
        pbAnxietyCircleDetail.progress = percentage

        // Color Logic from Editorial Palette
        val colorRes = when {
            percentage < 30 -> R.color.anxiety_low
            percentage < 60 -> R.color.anxiety_moderate
            else -> R.color.anxiety_high
        }
        val badgeBgRes = when {
            percentage < 30 -> R.color.anxiety_low_bg
            percentage < 60 -> R.color.anxiety_moderate_bg
            else -> R.color.anxiety_high_bg
        }

        pbAnxietyCircleDetail.progressTintList = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(this, colorRes))
        tvStatusBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(this, badgeBgRes))
        tvStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(this, colorRes))
        
        tvInterpretationSummary.text = when {
            percentage < 30 -> "The AI detected low physiological stress markers. The patient appears relatively stable."
            percentage < 60 -> "The AI detected moderate physiological stress markers consistent with elevated anxiety levels."
            else -> "The AI detected high physiological stress markers. Immediate attention to patient comfort is advised."
        }

        findViewById<TextView>(R.id.tvDominantEmotion).text = dominantEmotion

        btnBack.setOnClickListener { finish() }
        btnFinish.setOnClickListener { finish() }

        btnExport.setOnClickListener {
            generatePdfReport(
                fileNameSuffix = "$patientName - $date",
                patientNameData = patientName,
                patientIdData = patientId,
                scoreData = anxietyScore,
                levelData = anxietyLevel,
                emotionData = dominantEmotion,
                dateData = date
            )
        }
    }

    private fun generatePdfReport(fileNameSuffix: String, patientNameData: String, patientIdData: String, scoreData: String, levelData: String, emotionData: String, dateData: String) {
        // 1. Inflate the PDF Layout
        val pdfView = layoutInflater.inflate(R.layout.layout_pdf_report, null)
        
        // 2. Bind Data to the PDF View
        val tvName = pdfView.findViewById<TextView>(R.id.pdfPatientName)
        val tvId = pdfView.findViewById<TextView>(R.id.pdfPatientId)
        val tvReportId = pdfView.findViewById<TextView>(R.id.pdfReportId)
        val tvScore = pdfView.findViewById<TextView>(R.id.pdfScore)
        val tvLevel = pdfView.findViewById<TextView>(R.id.pdfLevel)
        val tvEmotion = pdfView.findViewById<TextView>(R.id.pdfDominantEmotion)
        val tvDate = pdfView.findViewById<TextView>(R.id.pdfReportDate)
        
        // Set data
        tvName.text = patientNameData
        tvId.text = patientIdData
        tvReportId.text = "#RPT-${System.currentTimeMillis().toString().takeLast(5)}"
        tvScore.text = scoreData
        tvLevel.text = levelData
        tvEmotion.text = emotionData
        tvDate.text = "Date: $dateData"

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
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPoints, pageHeightPoints, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        
        // Scale canvas to match density
        page.canvas.scale(1f / density, 1f / density)

        // 5. Draw View to PDF Canvas (Vector)
        pdfView.draw(page.canvas)
        
        pdfDocument.finishPage(page)

        // 6. Save PDF
        val cleanFileName = "Report_${fileNameSuffix.replace(Regex("[^a-zA-Z0-9]"), "_")}.pdf"
        
        try {
            savePdfToStorage(pdfDocument, cleanFileName)
            Toast.makeText(this, "PDF Report Saved: $cleanFileName", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun savePdfToStorage(pdfDocument: PdfDocument, fileName: String) {
        val outputStream: OutputStream?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            outputStream = uri?.let { contentResolver.openOutputStream(it) }
        } else {
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!directory.exists()) directory.mkdirs()
            val file = File(directory, fileName)
            outputStream = FileOutputStream(file)
        }

        outputStream?.use {
            pdfDocument.writeTo(it)
        } ?: throw IOException("Failed to create output stream")
    }
}
