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

        val scrollView = findViewById<NestedScrollView>(R.id.detailScrollView)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnFinish = findViewById<AppCompatButton>(R.id.btnFinish)
        val btnExport = findViewById<AppCompatButton>(R.id.btnExport)

        val tvPatientSubtitle = findViewById<TextView>(R.id.tvPatientSubtitle)
        val tvScoreLarge = findViewById<TextView>(R.id.tvScoreLarge)
        val tvStatusBadge = findViewById<TextView>(R.id.tvStatusBadge)
        val tvNumericLevel = findViewById<TextView>(R.id.tvNumericLevel)
        val ivAnxietyEmojiDetail = findViewById<ImageView>(R.id.ivAnxietyEmojiDetail)
        val assessmentScoreCard = findViewById<CardView>(R.id.assessmentScoreCard)

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
        tvPatientSubtitle.text = "$patientName • $patientId\n$date"
        tvScoreLarge.text = anxietyScore
        tvNumericLevel.text = "${anxietyLevelData.level}/10"
        tvStatusBadge.text = anxietyLevel
        ivAnxietyEmojiDetail.setImageResource(anxietyLevelData.emojiResId)

        assessmentScoreCard.setCardBackgroundColor(anxietyLevelData.color)

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
        val tvReportId = pdfView.findViewById<TextView>(R.id.pdfReportId) // New Field
        val tvScore = pdfView.findViewById<TextView>(R.id.pdfScore)
        val tvLevel = pdfView.findViewById<TextView>(R.id.pdfLevel)
        val tvInterpretation = pdfView.findViewById<TextView>(R.id.pdfInterpretation) // New Field
        val tvEmotion = pdfView.findViewById<TextView>(R.id.pdfDominantEmotion)
        val tvDate = pdfView.findViewById<TextView>(R.id.pdfReportDate)
        
        tvName.text = patientNameData
        tvId.text = patientIdData
        tvReportId.text = "#RPT-${System.currentTimeMillis().toString().takeLast(5)}"
        tvScore.text = scoreData
        tvLevel.text = levelData
        tvEmotion.text = emotionData
        tvDate.text = "Date: $dateData"

        // Set Interpretation based on Level
        val interpretation = when {
            levelData.contains("Severe", true) || levelData.contains("High", true) -> 
                "Patient exhibits significant signs of physiological stress and elevated anxiety markers. Clinical evaluation is strongly recommended."
            levelData.contains("Moderate", true) -> 
                "Patient shows indications of moderate physiological stress. Regular monitoring and follow-up assessments are advised."
            else -> 
                "Patient exhibits normal physiological responses. No significant anxiety markers were detected at this time."
        }
        tvInterpretation.text = interpretation

        // 3. Measure and Layout the View (A4 dimensions: 595 x 842 points)
        val pageWidth = 595
        val pageHeight = 842
        
        pdfView.measure(
            View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(pageHeight, View.MeasureSpec.UNSPECIFIED) // Check generic height
        )
        
        // Ensure it fits or just take measured height if less than page
        val contentHeight = pdfView.measuredHeight
        pdfView.layout(0, 0, pageWidth, contentHeight)

        // 4. Create PDF Document
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)

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
