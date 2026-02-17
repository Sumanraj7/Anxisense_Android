package com.simats.anxisense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.anxisense.api.DoctorApi
import com.simats.anxisense.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityDoctorOTP : AppCompatActivity() {

    private lateinit var otpEditText: EditText
    private lateinit var verifyButton: Button
    private lateinit var emailText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_otp)

        otpEditText = findViewById(R.id.otp_edit_text)
        verifyButton = findViewById(R.id.verify_button)
        emailText = findViewById(R.id.email_text)

        val email = intent.getStringExtra("email") ?: "doctor@email.com"

        // Display the email address
        emailText.text = email

        verifyButton.setOnClickListener {
            val enteredOtp = otpEditText.text.toString()

            if (enteredOtp.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.verifyOtp(DoctorApi.VerifyOtpRequest(email, enteredOtp))
                .enqueue(object : Callback<DoctorApi.VerifyOtpResponse> {
                    override fun onResponse(
                        call: Call<DoctorApi.VerifyOtpResponse>,
                        response: Response<DoctorApi.VerifyOtpResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {

                            Toast.makeText(
                                this@ActivityDoctorOTP,
                                "OTP Verified Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ Get username from API response
                            val doctor = response.body()?.doctor
                            val username = doctor?.username ?: "Doctor"
                            val doctorId = doctor?.id ?: -1

                            // Save to SharedPreferences
                            val sharedPreferences = getSharedPreferences("DoctorPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putInt("DOCTOR_ID", doctorId)
                            editor.putString("DOCTOR_NAME", username)
                            editor.apply()

                            // ✅ Open dashboard and pass username
                            val intent = Intent(this@ActivityDoctorOTP, DoctorDashboardActivity::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            finish()

                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = com.google.gson.Gson()
                                    .fromJson(errorBody, DoctorApi.VerifyOtpResponse::class.java)

                                val errorMsg = errorResponse.message ?: "Invalid OTP"
                                Toast.makeText(this@ActivityDoctorOTP, errorMsg, Toast.LENGTH_LONG).show()

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    this@ActivityDoctorOTP,
                                    "Verification Failed: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<DoctorApi.VerifyOtpResponse>, t: Throwable) {
                        Toast.makeText(this@ActivityDoctorOTP, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
