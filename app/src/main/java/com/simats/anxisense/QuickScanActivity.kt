package com.simats.anxisense

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class QuickScanActivity : AppCompatActivity() {

    private lateinit var navDashboard: LinearLayout
    private lateinit var navScan: LinearLayout
    private lateinit var navRecords: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_scan)

        // Bind Views
        val btnBeginQuickScan: AppCompatButton = findViewById(R.id.btnBeginQuickScan)
        
        // Initialize Nav Items
        navDashboard = findViewById(R.id.navDashboard)
        navScan = findViewById(R.id.navScan)
        navRecords = findViewById(R.id.navRecords)
        navProfile = findViewById(R.id.navProfile)

        // Set initial state (Scan selected)
        updateBottomNavStyle(navScan)

        btnBeginQuickScan.setOnClickListener {
            val intent = Intent(this, FacialScanActivity::class.java)
            intent.putExtra("IS_QUICK_SCAN", true)
            intent.putExtra("PATIENT_NAME", "Quick Assessment")
            intent.putExtra("PATIENT_ID", "TEMP-${System.currentTimeMillis().toString().takeLast(4)}")
            startActivity(intent)
        }

        // Nav Listeners
        navDashboard.setOnClickListener {
            val intent = Intent(this, DoctorDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }
        navScan.setOnClickListener { /* Already here */ }
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

    private fun updateBottomNavStyle(selectedItem: LinearLayout) {
        val navItems = listOf(navDashboard, navScan, navRecords, navProfile)
        val selectedColor = Color.parseColor("#1A365D")
        val unselectedColor = Color.parseColor("#718096")

        for (item in navItems) {
            val isSelected = item == selectedItem
            val imageView = item.getChildAt(0) as ImageView
            val textView = item.getChildAt(1) as TextView

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
