package com.example.yourappname

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.compose.ui.layout.layout
import com.example.myapplication.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myButton: Button = findViewById(R.id.myButton)
        myButton.setOnClickListener {
            // Create an Intent to start the SecondActivity
            val intent = Intent(this, SecondActivity::class.java)
            // Start the new activity
            startActivity(intent)
        }
    }

    class SecondActivity {

    }
}