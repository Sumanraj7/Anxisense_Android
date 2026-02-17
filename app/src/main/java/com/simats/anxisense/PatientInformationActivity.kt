package com.simats.anxisense

import android.os.Bundle
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

            if (genderSelection == 0) { // Assuming "Select" is at index 0
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (procedureSelection == 0) { // Assuming "Select procedure type" is at index 0
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
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@PatientInformationActivity, "Patient Added Successfully", Toast.LENGTH_SHORT).show()
                            
                            val intent = android.content.Intent(this@PatientInformationActivity, FacialScanActivity::class.java)
                            intent.putExtra("PATIENT_NAME", fullName)
                            intent.putExtra("PATIENT_ID", patientId)
                            
                            // Pass database ID if available
                            response.body()?.data?.id?.let { id ->
                                intent.putExtra("INTERNAL_PATIENT_ID", id)
                            }
                            
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@PatientInformationActivity, "Failed: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<com.simats.anxisense.api.DoctorApi.CreatePatientResponse>, t: Throwable) {
                        progressDialog.dismiss()
                        Toast.makeText(this@PatientInformationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
