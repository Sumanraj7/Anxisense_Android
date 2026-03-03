package com.simats.anxisense

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
import java.util.Locale

class PatientRecordsActivity : AppCompatActivity() {

    private lateinit var rvPatients: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: View
    private lateinit var btnCalendar: View
    private lateinit var btnPrev: TextView
    private lateinit var btnNext: TextView
    private lateinit var tvPageInfo: TextView
    private lateinit var etSearch: EditText
    
    private lateinit var adapter: PatientAdapter
    private var allPatients: List<DoctorApi.PatientData> = emptyList()
    
    private var currentPage = 1
    private var totalPages = 1
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_records)

        rvPatients = findViewById(R.id.rvPatients)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        tvPageInfo = findViewById(R.id.tvPageInfo)
        etSearch = findViewById(R.id.etSearch)

        rvPatients.layoutManager = LinearLayoutManager(this)

        // Pagination Listeners
        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                loadPatients()
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                loadPatients()
            }
        }

        // Back Button
        btnBack.setOnClickListener {
            val intent = Intent(this, DoctorDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnCalendar.setOnClickListener {
            showDatePicker()
        }

        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                loadPatients()
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                loadPatients()
            }
        }


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navDashboard: View = findViewById(R.id.navDashboard)
        val navScan: View = findViewById(R.id.navScan)
        val navRecords: View = findViewById(R.id.navRecords)
        val navProfile: View = findViewById(R.id.navProfile)

        // Style Records as active
        updateBottomNavStyle(navRecords as LinearLayout)

        navDashboard.setOnClickListener {
            val intent = Intent(this, DoctorDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }
        navScan.setOnClickListener {
            startActivity(Intent(this, QuickScanActivity::class.java))
            finish()
        }
        navRecords.setOnClickListener { /* Already here */ }
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
            finish()
        }
    }

    private fun updateBottomNavStyle(selectedItem: LinearLayout) {
        val navDashboard = findViewById<LinearLayout>(R.id.navDashboard)
        val navScan = findViewById<LinearLayout>(R.id.navScan)
        val navRecords = findViewById<LinearLayout>(R.id.navRecords)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        
        val navItems = listOf(navDashboard, navScan, navRecords, navProfile)
        val selectedColor = android.graphics.Color.parseColor("#1A365D")
        val unselectedColor = android.graphics.Color.parseColor("#718096")

        for (item in navItems) {
            val isSelected = item == selectedItem
            val imageView = item.getChildAt(0) as android.widget.ImageView
            val textView = item.getChildAt(1) as TextView

            if (isSelected) {
                item.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_nav_item_selected)
                imageView.setColorFilter(selectedColor)
                textView.setTextColor(selectedColor)
                textView.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                item.background = null
                imageView.setColorFilter(unselectedColor)
                textView.setTextColor(unselectedColor)
                textView.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPatients()
    }

    private fun loadPatients() {
        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
        val doctorId = sharedPreferences.getInt("DOCTOR_ID", -1)

        if (doctorId == -1) return

        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        rvPatients.visibility = View.GONE

        RetrofitClient.instance.getPatients(doctorId, currentPage, 10).enqueue(object : Callback<DoctorApi.GetPatientsResponse> {
            override fun onResponse(call: Call<DoctorApi.GetPatientsResponse>, response: Response<DoctorApi.GetPatientsResponse>) {
                android.util.Log.d("AnxiSense", "API Call: ${call.request().url()}")
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.success == true) {
                    allPatients = response.body()?.data ?: emptyList()
                    android.util.Log.d("AnxiSense", "Received ${allPatients.size} patients for page $currentPage")
                    val pagination = response.body()?.pagination
                    
                    if (pagination != null) {
                        totalPages = pagination.total_pages
                        currentPage = pagination.current_page
                        updatePaginationUI()
                    }

                    if (allPatients.isNotEmpty()) {
                        rvPatients.visibility = View.VISIBLE
                        adapter = PatientAdapter(allPatients) { patient ->
                            val intent = Intent(this@PatientRecordsActivity, AssessmentHistoryActivity::class.java)
                            intent.putExtra("PATIENT_NAME", patient.fullname)
                            intent.putExtra("PATIENT_ID", patient.patientid)
                            intent.putExtra("INTERNAL_PATIENT_ID", patient.id)
                            startActivity(intent)
                        }
                        rvPatients.adapter = adapter
                        
                        // Re-apply filters if any
                        if (etSearch.text.isNotEmpty() || selectedDate != null) {
                            filter(etSearch.text.toString())
                        }
                    } else {
                        tvEmptyState.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@PatientRecordsActivity, "Failed to load patients", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DoctorApi.GetPatientsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@PatientRecordsActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePaginationUI() {
        tvPageInfo.text = "Page $currentPage of $totalPages"
        
        btnPrev.isEnabled = currentPage > 1
        btnPrev.alpha = if (currentPage > 1) 1.0f else 0.5f
        btnPrev.isClickable = currentPage > 1

        btnNext.isEnabled = currentPage < totalPages
        btnNext.alpha = if (currentPage < totalPages) 1.0f else 0.5f
        btnNext.isClickable = currentPage < totalPages
    }

    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(this, { _, y, m, d ->
            val formattedMonth = String.format("%02d", m + 1)
            val formattedDay = String.format("%02d", d)
            selectedDate = "$y-$formattedMonth-$formattedDay"
            
            // Show clear option or hint
            Toast.makeText(this, "Filter: $selectedDate", Toast.LENGTH_SHORT).show()
            filter(etSearch.text.toString())
        }, year, month, day)

        // Add "Clear" button to DatePickerDialog
        datePickerDialog.setButton(android.content.DialogInterface.BUTTON_NEUTRAL, "Clear") { _, _ ->
            selectedDate = null
            filter(etSearch.text.toString())
        }
        
        datePickerDialog.show()
    }


    private fun filter(text: String) {
        val filteredList = mutableListOf<DoctorApi.PatientData>()
        val query = text.lowercase(Locale.getDefault())

        for (item in allPatients) {
            val matchesId = item.patientid?.lowercase(Locale.getDefault())?.contains(query) == true
            val matchesName = item.fullname.lowercase(Locale.getDefault()).contains(query)
            
            // Filter by selected date if present (checks last_assessment_date)
            val matchesDate = if (selectedDate != null) {
                item.last_assessment_date?.startsWith(selectedDate!!) == true
            } else {
                true
            }

            if ((matchesId || matchesName) && matchesDate) {
                filteredList.add(item)
            }
        }
        if (::adapter.isInitialized) {
            adapter.filterList(filteredList)
        }
    }
}
