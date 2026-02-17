package com.simats.anxisense

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DoctorDashboardActivity : AppCompatActivity() {

    private lateinit var navDashboard: LinearLayout
    private lateinit var navScan: LinearLayout
    private lateinit var navAnalysis: LinearLayout
    private lateinit var navRecords: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val username = intent.getStringExtra("username") ?: "Doctor"
        tvWelcome.text = "Welcome, $username"




        // Bind Views
        val btnBeginAssessment = findViewById<View>(R.id.btnBeginAssessment)

        // Initialize Nav Items from the included layout
        navDashboard = findViewById(R.id.navDashboard)
        navScan = findViewById(R.id.navScan)
        navAnalysis = findViewById(R.id.navAnalysis)
        navRecords = findViewById(R.id.navRecords)
        navProfile = findViewById(R.id.navProfile)

        // Main Action
        btnBeginAssessment.setOnClickListener {
            val intent = android.content.Intent(this, PatientInformationActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation Listeners
        navDashboard.setOnClickListener { updateBottomNavState(navDashboard, "Dashboard") }
        navScan.setOnClickListener { 
            updateBottomNavState(navScan, "Scan")
            val intent = android.content.Intent(this, QuickScanActivity::class.java)
            startActivity(intent)
        }
        navAnalysis.setOnClickListener { 
            val intent = android.content.Intent(this, AnalysisActivity::class.java)
            startActivity(intent)
        }
        navRecords.setOnClickListener { 
            val intent = android.content.Intent(this, PatientRecordsActivity::class.java)
            startActivity(intent)
        }
        navProfile.setOnClickListener { 
            val intent = android.content.Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDashboardStats()
    }

    private fun fetchDashboardStats() {
        val sharedPreferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE)
        val doctorId = sharedPreferences.getInt("DOCTOR_ID", -1)

        if (doctorId == -1) return

        com.simats.anxisense.api.RetrofitClient.instance.getDashboardStats(doctorId)
            .enqueue(object : retrofit2.Callback<com.simats.anxisense.api.DoctorApi.DashboardStatsResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.DashboardStatsResponse>,
                    response: retrofit2.Response<com.simats.anxisense.api.DoctorApi.DashboardStatsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        if (data != null) {
                            findViewById<TextView>(R.id.tvStatToday).text = data.today.toString()
                            findViewById<TextView>(R.id.tvStatTotal).text = data.total.toString()
                            findViewById<TextView>(R.id.tvStatAccuracy).text = data.accuracy
                        }
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.DashboardStatsResponse>,
                    t: Throwable
                ) {
                   t.printStackTrace()
                }
            })
    }

    private fun updateBottomNavState(selectedItem: LinearLayout, itemName: String) {
        // List of all nav items
        val navItems = listOf(navDashboard, navScan, navAnalysis, navRecords, navProfile)

        // Colors
        val selectedColor = Color.parseColor("#1A365D")
        val unselectedColor = Color.parseColor("#718096")

        for (item in navItems) {
            val isSelected = item == selectedItem
            val imageView = item.getChildAt(0) as ImageView
            val textView = item.getChildAt(1) as TextView

            if (isSelected) {
                // Apply Selected Style
                item.background = ContextCompat.getDrawable(this, R.drawable.bg_nav_item_selected)
                imageView.setColorFilter(selectedColor)
                textView.setTextColor(selectedColor)
                textView.setTypeface(null, Typeface.BOLD)
            } else {
                // Apply Unselected Style
                item.background = null
                imageView.setColorFilter(unselectedColor)
                textView.setTextColor(unselectedColor)
                textView.setTypeface(null, Typeface.NORMAL)
            }
        }

        Toast.makeText(this, "Navigated to $itemName", Toast.LENGTH_SHORT).show()
    }
}
