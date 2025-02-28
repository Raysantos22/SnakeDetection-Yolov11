package SnakeDetection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.SnakeDetection.R

class FirstAidActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_aid)
        
        // Set up back button
        findViewById<CardView>(R.id.backButton).setOnClickListener {
            finish() // Return to previous screen
        }
    }
}