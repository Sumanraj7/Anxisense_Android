package com.simats.anxisense

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.anxisense.api.DoctorApi
import com.simats.anxisense.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoctorLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_login)

        val emailEditText = findViewById<EditText>(R.id.email_edit_text)
        val sendOtpButton = findViewById<Button>(R.id.send_otp_button)

        sendOtpButton.setOnClickListener {

            val receiverEmail = emailEditText.text.toString().trim()

            if (receiverEmail.isEmpty()) {
                Toast.makeText(this, "Enter Email Address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(receiverEmail).matches()) {
                Toast.makeText(this, "Enter Valid Email Address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.sendOtp(DoctorApi.OtpRequest(receiverEmail))
                .enqueue(object : retrofit2.Callback<DoctorApi.OtpResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<DoctorApi.OtpResponse>,
                        response: retrofit2.Response<DoctorApi.OtpResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@DoctorLoginActivity, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@DoctorLoginActivity, ActivityDoctorOTP::class.java)
                            intent.putExtra("email", receiverEmail)
                            startActivity(intent)
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("DoctorLoginActivity", "Failed to send OTP. Code: ${response.code()}, Body: $errorBody")
                            try {
                                val errorResponse = com.google.gson.Gson().fromJson(errorBody, DoctorApi.OtpResponse::class.java)
                                val errorMsg = errorResponse.message ?: "Failed to send OTP"
                                Toast.makeText(this@DoctorLoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e("DoctorLoginActivity", "Error parsing error body", e)
                                Toast.makeText(this@DoctorLoginActivity, "Failed to send OTP: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<DoctorApi.OtpResponse>, t: Throwable) {
                        Log.e("DoctorLoginActivity", "Error sending OTP", t)
                        Toast.makeText(this@DoctorLoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
