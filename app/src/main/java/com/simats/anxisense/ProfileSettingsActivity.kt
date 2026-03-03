package com.simats.anxisense

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.simats.anxisense.api.DoctorApi
import com.simats.anxisense.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var navDashboard: LinearLayout
    private lateinit var navScan: LinearLayout
    private lateinit var navRecords: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var etFullName: EditText
    private lateinit var etSpecialization: EditText
    private lateinit var etClinicName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSaveChanges: AppCompatButton
    private lateinit var btnLogout: AppCompatButton
    private lateinit var ivProfileImage: ImageView

    private var doctorId: Int = -1
    private var selectedImageUri: android.net.Uri? = null

    private val pickImage = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let {
            selectedImageUri = it
            ivProfileImage.setPadding(0, 0, 0, 0)
            ivProfileImage.imageTintList = null
            Glide.with(this).load(it).into(ivProfileImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        // Initialize Views
        etFullName = findViewById(R.id.etFullName)
        etSpecialization = findViewById(R.id.etSpecialization)
        etClinicName = findViewById(R.id.etClinicName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnLogout = findViewById(R.id.btnLogout)
        ivProfileImage = findViewById(R.id.ivProfileImage)

        // Profile Image Click
        ivProfileImage.setOnClickListener {
            pickImage.launch("image/*")
        }
        
        // Initialize Nav Items
        navDashboard = findViewById(R.id.navDashboard)
        navScan = findViewById(R.id.navScan)
        navRecords = findViewById(R.id.navRecords)
        navProfile = findViewById(R.id.navProfile)

        // Set Initial State (Profile is selected)
        updateBottomNavStyle(navProfile)
        setupNavigation()

        // Get Doctor ID
        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
        doctorId = sharedPreferences.getInt("DOCTOR_ID", -1)

        if (doctorId != -1) {
            loadProfileData()
        } else {
            Toast.makeText(this, "Error: Doctor ID not found", Toast.LENGTH_SHORT).show()
        }

        // Save Changes Button
        btnSaveChanges.setOnClickListener {
            saveProfileChanges()
        }

        // Logout Button
        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun loadProfileData() {
        if (doctorId == -1) return

        RetrofitClient.instance.getDoctorProfile(doctorId)
            .enqueue(object : Callback<DoctorApi.GetProfileResponse> {
                override fun onResponse(
                    call: Call<DoctorApi.GetProfileResponse>,
                    response: Response<DoctorApi.GetProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val profile = response.body()?.data
                        profile?.let {
                            etFullName.setText(it.fullname ?: "")
                            etSpecialization.setText(it.specialization ?: "")
                            etClinicName.setText(it.clinic_name ?: "")
                            etPhone.setText(it.phone ?: "")
                            etEmail.setText(it.email)
                            
                            if (!it.profile_photo.isNullOrEmpty()) {
                                ivProfileImage.setPadding(0, 0, 0, 0)
                                ivProfileImage.imageTintList = null
                                Glide.with(this@ProfileSettingsActivity)
                                    .load(it.profile_photo)
                                    .placeholder(R.drawable.ic_person)
                                    .into(ivProfileImage)
                            } else {
                                ivProfileImage.setPadding(16, 16, 16, 16)
                                ivProfileImage.setImageResource(R.drawable.ic_person)
                            }
                        }
                    } else {
                        Toast.makeText(this@ProfileSettingsActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DoctorApi.GetProfileResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileSettingsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveProfileChanges() {
        if (doctorId == -1) return

        val fullname = etFullName.text.toString().trim()
        val specialization = etSpecialization.text.toString().trim()
        val clinicOfName = etClinicName.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (fullname.isEmpty()) {
            etFullName.error = "Name is required"
            return
        }

        val request = DoctorApi.UpdateProfileRequest(
            doctorid = doctorId,
            fullname = fullname,
            phone = phone,
            specialization = specialization,
            clinic_name = clinicOfName
        )

        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Saving Profile...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        RetrofitClient.instance.updateDoctorProfile(request)
            .enqueue(object : Callback<DoctorApi.UpdateProfileResponse> {
                override fun onResponse(
                    call: Call<DoctorApi.UpdateProfileResponse>,
                    response: Response<DoctorApi.UpdateProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Update Shared Prefs
                        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putString("DOCTOR_NAME", fullname).apply()

                        // If image selected, upload it now
                        if (selectedImageUri != null) {
                            uploadProfileImage(progressDialog)
                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(this@ProfileSettingsActivity, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this@ProfileSettingsActivity, "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DoctorApi.UpdateProfileResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@ProfileSettingsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun uploadProfileImage(progressDialog: android.app.ProgressDialog) {
        val uri = selectedImageUri ?: return
        progressDialog.setMessage("Uploading Photo...")

        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val file = java.io.File(cacheDir, "temp_profile.jpg")
            val outputStream = java.io.FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), file)
            val body = okhttp3.MultipartBody.Part.createFormData("image", file.name, requestFile)

            RetrofitClient.instance.uploadProfilePhoto(doctorId, body)
                .enqueue(object : Callback<DoctorApi.UpdateProfileResponse> {
                    override fun onResponse(
                        call: Call<DoctorApi.UpdateProfileResponse>,
                        response: Response<DoctorApi.UpdateProfileResponse>
                    ) {
                        progressDialog.dismiss()
                        if (response.isSuccessful && response.body()?.success == true) {
                            val photoUrl = response.body()?.profile_photo
                            if (!photoUrl.isNullOrEmpty()) {
                                ivProfileImage.setPadding(0, 0, 0, 0)
                                ivProfileImage.imageTintList = null
                                Glide.with(this@ProfileSettingsActivity)
                                    .load(photoUrl)
                                    .into(ivProfileImage)
                            }
                            selectedImageUri = null // Reset selection
                            Toast.makeText(this@ProfileSettingsActivity, "Profile & Photo Updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            android.util.Log.e("ProfileSettings", "Photo upload failed: ${response.code()}, Body: $errorBody")
                            Toast.makeText(this@ProfileSettingsActivity, "Profile saved, but photo upload failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DoctorApi.UpdateProfileResponse>, t: Throwable) {
                        progressDialog.dismiss()
                        Toast.makeText(this@ProfileSettingsActivity, "Network error uploading photo: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, DoctorLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupNavigation() {
        // Dashboard
        navDashboard.setOnClickListener {
            val intent = Intent(this, DoctorDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish() // Finish this activity to avoid huge stack
        }

        // Scan
        navScan.setOnClickListener {
            val intent = Intent(this, QuickScanActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Analysis

        // Records
        navRecords.setOnClickListener {
            val intent = Intent(this, PatientRecordsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Profile (Already Here)
        navProfile.setOnClickListener {
            // Do nothing
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
