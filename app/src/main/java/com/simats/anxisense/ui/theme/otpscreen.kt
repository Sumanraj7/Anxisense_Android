package com.simats.anxisense.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.simats.anxisense.EmailSender

@Composable
fun OtpScreen() {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        // EMAIL FIELD
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // SEND OTP BUTTON
        Button(
            onClick = {

                if (email.isBlank()) {
                    Toast.makeText(context, "Enter Email", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                generatedOtp = (100000..999999).random().toString()

                Thread {
                    EmailSender.sendOtpEmail(
                        senderEmail = "yourgmail@gmail.com",       // 🔥 replace
                        senderPassword = "your_app_password",     // 🔥 replace
                        receiverEmail = email,
                        otp = generatedOtp
                    )
                }.start()

                otpSent = true

                Toast.makeText(context, "OTP Sent ✅", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send OTP")
        }

        // OTP FIELD (only after sending)
        if (otpSent) {

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = enteredOtp,
                onValueChange = { enteredOtp = it },
                label = { Text("Enter OTP") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {

                    if (enteredOtp == generatedOtp) {
                        Toast.makeText(
                            context,
                            "OTP Verified Successfully ✅",
                            Toast.LENGTH_LONG
                        ).show()

                        // TODO: Navigate to Dashboard

                    } else {
                        Toast.makeText(
                            context,
                            "Invalid OTP ❌",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify OTP")
            }
        }
    }
}
