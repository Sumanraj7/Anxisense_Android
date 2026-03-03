package com.simats.anxisense

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class PatientInformationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_information)

        // Initialize Views
        val tvTitle: android.widget.TextView = findViewById(R.id.tvTitle)
        val tvSubtitle: android.widget.TextView = findViewById(R.id.tvSubtitle)
        val etPatientId: android.widget.EditText = findViewById(R.id.etPatientId)
        val etFullName: android.widget.EditText = findViewById(R.id.etFullName)
        val etAge: android.widget.EditText = findViewById(R.id.etAge)
        val etHealthIssues: android.widget.EditText = findViewById(R.id.etHealthIssues)
        val etHistory: android.widget.EditText = findViewById(R.id.etHistory)
        
        val genderSpinner: Spinner = findViewById(R.id.spinnerGender)
        val procedureSpinner: Spinner = findViewById(R.id.spinnerProcedure)

        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnCancel: AppCompatButton = findViewById(R.id.btnCancel)
        val btnProceed: AppCompatButton = findViewById(R.id.btnProceed)
        
        // Pre-fill from Quick Scan if available
        val isQuickScan = intent.getBooleanExtra("IS_QUICK_SCAN", false)
        intent.getStringExtra("PATIENT_NAME")?.let { etFullName.setText(it) }
        intent.getStringExtra("PATIENT_ID")?.let { etPatientId.setText(it) }

        if (isQuickScan) {
            tvTitle.text = "Save Assessment"
            tvSubtitle.text = "Complete patient registration to save results"
            btnProceed.text = "Save to Records"
        }

        // Gender Adapter
        val genderAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Select", "Male", "Female", "Other")
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Procedure Adapter
        val procedureAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Select procedure type", "Dental Surgery", "MRI Scan", "General Checkup", "Vaccination")
        )
        procedureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        procedureSpinner.adapter = procedureAdapter

        // Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // Cancel Button
        btnCancel.setOnClickListener {
            finish()
        }

        // Proceed Button
        btnProceed.setOnClickListener {
            val patientId = etPatientId.text.toString().trim()
            val fullName = etFullName.text.toString().trim()
            val age = etAge.text.toString().trim()
            val genderSelection = genderSpinner.selectedItemPosition
            val procedureSelection = procedureSpinner.selectedItemPosition

            if (patientId.isEmpty()) {
                etPatientId.error = "Patient ID is required"
                etPatientId.requestFocus()
                return@setOnClickListener
            }

            if (fullName.isEmpty()) {
                etFullName.error = "Full Name is required"
                etFullName.requestFocus()
                return@setOnClickListener
            }

            if (age.isEmpty()) {
                etAge.error = "Age is required"
                etAge.requestFocus()
                return@setOnClickListener
            }

            if (genderSelection == 0) { // Assuming \"Select\" is at index 0
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (procedureSelection == 0) { // Assuming \"Select procedure type\" is at index 0
                Toast.makeText(this, "Please select a procedure", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validation Success
            
            // Get Doctor ID from SharedPreferences
            val sharedPreferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE)
            val doctorId = sharedPreferences.getInt("DOCTOR_ID", -1)

            if (doctorId == -1) {
                Toast.makeText(this, "Session Expired. Please Login Again.", Toast.LENGTH_SHORT).show()
                // Redirect to Login if needed
                return@setOnClickListener
            }

            // Show Loading
            val progressDialog = android.app.ProgressDialog(this)
            progressDialog.setMessage("Saving Patient Details...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val request = com.simats.anxisense.api.DoctorApi.CreatePatientRequest(
                doctorid = doctorId,
                fullname = fullName,
                patientid = patientId,
                age = age,
                gender = genderSpinner.selectedItem.toString(),
                proceduretype = procedureSpinner.selectedItem.toString(),
                healthissue = etHealthIssues.text.toString(),
                previousanxietyhistory = etHistory.text.toString()
            )

            com.simats.anxisense.api.RetrofitClient.instance.createPatient(request)
                .enqueue(object : retrofit2.Callback<com.simats.anxisense.api.DoctorApi.CreatePatientResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.CreatePatientResponse>,
                        response: retrofit2.Response<com.simats.anxisense.api.DoctorApi.CreatePatientResponse>
                    ) {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("PatientInfo", "Patient created successfully. ID: ${response.body()?.data?.id}")
                            val isQuickScan = intent.getBooleanExtra("IS_QUICK_SCAN", false)
                            val internalPatientId = response.body()?.data?.id ?: -1

                            if (isQuickScan && internalPatientId != -1) {
                                // Original Quick Scan flow: results exist, now saving to new patient
                                savePendingAssessment(internalPatientId)
                            } else {
                                val nextIntent = android.content.Intent(this@PatientInformationActivity, FacialScanActivity::class.java)
                                nextIntent.putExtra("PATIENT_NAME", fullName)
                                nextIntent.putExtra("PATIENT_ID", patientId)
                                if (internalPatientId != -1) {
                                    nextIntent.putExtra("INTERNAL_PATIENT_ID", internalPatientId)
                                }
                                startActivity(nextIntent)
                                finish()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("PatientInfo", "Failed to create patient: $errorBody")
                            Toast.makeText(this@PatientInformationActivity, "Failed: ${response.body()?.message ?: "Server Error"}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.CreatePatientResponse>, t: Throwable) {
                        progressDialog.dismiss()
                        Toast.makeText(this@PatientInformationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun savePendingAssessment(internalPatientId: Int) {
        val sharedPreferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE)
        val doctorId = sharedPreferences.getInt("DOCTOR_ID", -1)

        val score = intent.getFloatExtra("ANXIETY_SCORE_RAW", 0f)
        val level = intent.getStringExtra("ANXIETY_LEVEL_RAW") ?: "Moderate"
        val dominantEmotion = intent.getStringExtra("DOMINANT_EMOTION")

        val request = com.simats.anxisense.api.DoctorApi.SaveAssessmentRequest(
            patient_id = internalPatientId,
            doctor_id = doctorId,
            anxiety_score = score,
            anxiety_level = level,
            dominant_emotion = dominantEmotion
        )

        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Finalizing Assessment...")
        progressDialog.show()

        com.simats.anxisense.api.RetrofitClient.instance.saveAssessment(request)
            .enqueue(object : retrofit2.Callback<com.simats.anxisense.api.DoctorApi.SaveAssessmentResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.SaveAssessmentResponse>,
                    response: retrofit2.Response<com.simats.anxisense.api.DoctorApi.SaveAssessmentResponse>
                ) {
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@PatientInformationActivity, "Assessment Saved to Records!", Toast.LENGTH_SHORT).show()
                        
                        // Navigate directly to Detail Activity to enable PDF export as requested
                        val detailIntent = android.content.Intent(this@PatientInformationActivity, AssessmentDetailActivity::class.java)
                        detailIntent.putExtra("patient_name", intent.getStringExtra("PATIENT_NAME") ?: "Patient")
                        detailIntent.putExtra("patient_code", intent.getStringExtra("PATIENT_ID") ?: "--")
                        detailIntent.putExtra("anxiety_score", score)
                        detailIntent.putExtra("anxiety_level", level)
                        detailIntent.putExtra("dominant_emotion", dominantEmotion)
                        detailIntent.putExtra("created_at", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
                        
                        detailIntent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(detailIntent)
                        finish()
                    } else {
                        Toast.makeText(this@PatientInformationActivity, "Failed to link assessment", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.SaveAssessmentResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@PatientInformationActivity, "Network error saving assessment", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }
}
