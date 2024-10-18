package com.example.notefinal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class StartScreenActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launch_scanner)

        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to the Start Screen Activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the Splash Screen Activity
        }, 3000)
    }
}