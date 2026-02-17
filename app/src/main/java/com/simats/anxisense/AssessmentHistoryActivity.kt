package com.simats.anxisense

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.anxisense.api.DoctorApi
import com.simats.anxisense.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssessmentHistoryActivity : AppCompatActivity() {

    private lateinit var rvAssessments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var tvPatientSubtitle: TextView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment_history)

        // Initialize Views
        rvAssessments = findViewById(R.id.rvAssessments)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvPatientSubtitle = findViewById(R.id.tvPatientSubtitle)
        btnBack = findViewById(R.id.btnBack)

        // Get extras
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Unknown"
        val patientId = intent.getStringExtra("PATIENT_ID") ?: "--"
        val internalPatientId = intent.getIntExtra("INTERNAL_PATIENT_ID", -1)

        tvPatientSubtitle.text = "$patientName • $patientId"

        rvAssessments.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        if (internalPatientId != -1) {
            loadAssessments(internalPatientId, patientName, patientId)
        } else {
            Toast.makeText(this, "Error: Invalid Patient ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadAssessments(internalPatientId: Int, patientName: String, patientIdStr: String) {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        rvAssessments.visibility = View.GONE

        RetrofitClient.instance.getAssessments(patientId = internalPatientId).enqueue(object : Callback<DoctorApi.GetAssessmentsResponse> {
            override fun onResponse(
                call: Call<DoctorApi.GetAssessmentsResponse>,
                response: Response<DoctorApi.GetAssessmentsResponse>
            ) {
                progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val assessments = response.body()?.data ?: emptyList()
                    
                    if (assessments.isNotEmpty()) {
                        rvAssessments.visibility = View.VISIBLE
                        // Pass navigation logic
                        val adapter = AssessmentAdapter(assessments) { assessment ->
                             val intent = Intent(this@AssessmentHistoryActivity, AssessmentDetailActivity::class.java)
                             // Map AssessmentData to Intent extras expected by DetailActivity
                             intent.putExtra("patient_name", patientName)
                             intent.putExtra("patient_code", patientIdStr)
                             intent.putExtra("anxiety_score", assessment.anxiety_score)
                             intent.putExtra("anxiety_level", assessment.anxiety_level)
                             intent.putExtra("dominant_emotion", assessment.dominant_emotion)
                             intent.putExtra("created_at", assessment.created_at)
                             startActivity(intent)
                        }
                        rvAssessments.adapter = adapter
                    } else {
                        tvEmptyState.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@AssessmentHistoryActivity, "Failed to load history", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DoctorApi.GetAssessmentsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AssessmentHistoryActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
