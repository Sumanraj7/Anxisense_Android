package com.simats.anxisense

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Removed redundant layout to achieve single-screen seamless transition
        // The screen design is now handled directly by the Theme (windowBackground)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, DoctorLoginActivity::class.java))
            finish()
        }, 1500) // 1.5 second delay
    }
}
