package com.simats.anxisense

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.anxisense.api.DoctorApi
import com.simats.anxisense.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnalysisActivity : AppCompatActivity() {

    private lateinit var navDashboard: LinearLayout
    private lateinit var navScan: LinearLayout
    private lateinit var navAnalysis: LinearLayout
    private lateinit var navRecords: LinearLayout
    private lateinit var navProfile: LinearLayout
    
    private lateinit var rvAssessments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        // Robust initialization of included navigation
        val bottomNavRoot = findViewById<View>(R.id.includedBottomNav)
        navDashboard = bottomNavRoot.findViewById(R.id.navDashboard)
        navScan = bottomNavRoot.findViewById(R.id.navScan)
        navAnalysis = bottomNavRoot.findViewById(R.id.navAnalysis)
        navRecords = bottomNavRoot.findViewById(R.id.navRecords)
        navProfile = bottomNavRoot.findViewById(R.id.navProfile)

        // Set Initial State (Analysis is selected)
        updateBottomNavStyle(navAnalysis)

        // Header Profile Icon Listener
        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Initialize Content Views
        rvAssessments = findViewById(R.id.rvAssessments)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        
        rvAssessments.layoutManager = LinearLayoutManager(this)

        // Bottom Navigation Listeners
        navDashboard.setOnClickListener {
            val intent = Intent(this, DoctorDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }

        navScan.setOnClickListener { 
            val intent = Intent(this, QuickScanActivity::class.java)
            startActivity(intent)
            finish()
        }

        navAnalysis.setOnClickListener { 
            // Already here
        }

        navRecords.setOnClickListener { 
            val intent = Intent(this, PatientRecordsActivity::class.java)
            startActivity(intent)
            finish()
        }

        navProfile.setOnClickListener { 
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadAssessments()
    }

    private fun loadAssessments() {
        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
        val doctorId = sharedPreferences.getInt("DOCTOR_ID", -1)

        if (doctorId == -1) {
            Toast.makeText(this, "Session Error", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        rvAssessments.visibility = View.GONE
        
        RetrofitClient.instance.getAssessments(doctorId = doctorId).enqueue(object : Callback<DoctorApi.GetAssessmentsResponse> {
            override fun onResponse(
                call: Call<DoctorApi.GetAssessmentsResponse>,
                response: Response<DoctorApi.GetAssessmentsResponse>
            ) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success) {
                        val assessments = result.data ?: emptyList()
                        if (assessments.isNotEmpty()) {
                            rvAssessments.visibility = View.VISIBLE
                            rvAssessments.adapter = AssessmentAdapter(assessments) { assessment ->
                                // On Click -> Go to Detail
                                val intent = Intent(this@AnalysisActivity, AssessmentDetailActivity::class.java)
                                intent.putExtra("patient_name", assessment.patient_name)
                                intent.putExtra("patient_code", assessment.patient_code)
                                intent.putExtra("anxiety_score", assessment.anxiety_score)
                                intent.putExtra("anxiety_level", assessment.anxiety_level)
                                intent.putExtra("dominant_emotion", assessment.dominant_emotion)
                                intent.putExtra("created_at", assessment.created_at)
                                startActivity(intent)
                            }
                        } else {
                            tvEmptyState.visibility = View.VISIBLE 
                        }
                    } else {
                        Toast.makeText(this@AnalysisActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AnalysisActivity, "Failed to load history", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DoctorApi.GetAssessmentsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AnalysisActivity, "Connection Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateBottomNavStyle(selectedItem: LinearLayout) {
        val navItems = listOf(navDashboard, navScan, navAnalysis, navRecords, navProfile)
        val selectedColor = Color.parseColor("#1A365D")
        val unselectedColor = Color.parseColor("#718096")

        for (item in navItems) {
            val isSelected = item == selectedItem
            if (item.childCount >= 2) {
                val imageView = item.getChildAt(0) as? ImageView
                val textView = item.getChildAt(1) as? TextView

                if (imageView != null && textView != null) {
                    if (isSelected) {
                        item.background = ContextCompat.getDrawable(this, R.drawable.bg_nav_item_selected)
                        imageView.setColorFilter(selectedColor)
                        textView.setTextColor(selectedColor)
                        textView.setTypeface(null, Typeface.BOLD)
                    } else {
                        item.background = null
                        imageView.setColorFilter(unselectedColor)
                        textView.setTextColor(unselectedColor)
                        textView.setTypeface(null, Typeface.NORMAL)
                    }
                }
            }
        }
    }
}
