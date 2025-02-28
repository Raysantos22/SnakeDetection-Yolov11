package SnakeDetection

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.SnakeDetection.R

class EmergencyHotlineActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_hotline)
        
        // Set up back button
        findViewById<CardView>(R.id.backButton).setOnClickListener {
            finish() // Return to previous screen
        }
        
        // Set up emergency call buttons
        findViewById<Button>(R.id.callEmergencyButton).setOnClickListener {
            dialPhoneNumber("911")
        }
        
        findViewById<Button>(R.id.callPoisonControlButton).setOnClickListener {
            dialPhoneNumber("18002221222")
        }
    }
    
    // Function to initiate phone call intent
    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}