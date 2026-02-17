package com.simats.anxisense

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

class PatientRecordsActivity : AppCompatActivity() {

    private lateinit var rvPatients: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: View
    private lateinit var fabAddPatient: com.google.android.material.floatingactionbutton.FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_records)

        // Initialize Views
        rvPatients = findViewById(R.id.rvPatients)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)
        fabAddPatient = findViewById(R.id.fabAddPatient)

        // Setup RecyclerView
        rvPatients.layoutManager = LinearLayoutManager(this)

        // Back Button
        btnBack.setOnClickListener {
            // Navigate back to Dashboard
            val intent = Intent(this, DoctorDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Add Patient FAB
        fabAddPatient.setOnClickListener {
            val intent = Intent(this, PatientInformationActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPatients()
    }

    private fun loadPatients() {
        // Get Doctor ID
        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
        val doctorId = sharedPreferences.getInt("DOCTOR_ID", -1)

        if (doctorId == -1) {
            Toast.makeText(this, "Session Error: Doctor ID not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Show loading state
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        rvPatients.visibility = View.GONE

        // API Call
        RetrofitClient.instance.getPatients(doctorId).enqueue(object : Callback<DoctorApi.GetPatientsResponse> {
            override fun onResponse(
                call: Call<DoctorApi.GetPatientsResponse>,
                response: Response<DoctorApi.GetPatientsResponse>
            ) {
                progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success) {
                        val patients = result.data ?: emptyList()
                        
                        if (patients.isNotEmpty()) {
                            rvPatients.visibility = View.VISIBLE
                            tvEmptyState.visibility = View.GONE
                            
                            val adapter = PatientAdapter(patients) { patient ->
                                val intent = Intent(this@PatientRecordsActivity, AssessmentHistoryActivity::class.java)
                                intent.putExtra("PATIENT_NAME", patient.fullname)
                                intent.putExtra("PATIENT_ID", patient.patientid)
                                intent.putExtra("INTERNAL_PATIENT_ID", patient.id)
                                startActivity(intent)
                            }
                            rvPatients.adapter = adapter
                        } else {
                            rvPatients.visibility = View.GONE
                            tvEmptyState.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(this@PatientRecordsActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PatientRecordsActivity, "Failed to load patients", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DoctorApi.GetPatientsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@PatientRecordsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
