package SnakeDetection

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.SnakeDetection.R
import com.SnakeDetection.SnakeDetailsActivity
import com.SnakeDetection.SnakeInfoProvider

class SnakeLibraryActivity : AppCompatActivity() {
    
    private val snakeInfoProvider = SnakeInfoProvider()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_snake_library)
        
        // Set up back button
        findViewById<CardView>(R.id.backButton).setOnClickListener {
            finish() // Return to previous screen
        }
        
        // Set up click listeners for each snake card
        setupSnakeCards()
    }
    
    private fun setupSnakeCards() {
        // Card 1 - King Cobra
        findViewById<CardView>(R.id.snake1Card).setOnClickListener {
            openSnakeDetails("King Cobra")
        }
        
        // Card 2 - Pit Viper
        findViewById<CardView>(R.id.snake2Card).setOnClickListener {
            openSnakeDetails("Pit Viper")
        }
        
        // Card 3 - Rat Snake
        findViewById<CardView>(R.id.snake3Card).setOnClickListener {
            openSnakeDetails("Rat Snake")
        }
        
        // Card 4 - Python
        findViewById<CardView>(R.id.snake4Card).setOnClickListener {
            openSnakeDetails("Reticulated Python")
        }
        
        // Card 5 - Sea Snake
        findViewById<CardView>(R.id.snake5Card).setOnClickListener {
            openSnakeDetails("Sea Snake")
        }
        
        // Card 6 - Flowerpot Snake
        findViewById<CardView>(R.id.snake6Card).setOnClickListener {
            openSnakeDetails("Flowerpot Snake")
        }
    }
    
    private fun openSnakeDetails(snakeName: String) {
        val intent = Intent(this, SnakeDetailsActivity::class.java).apply {
            putExtra(SnakeDetailsActivity.EXTRA_SNAKE_NAME, snakeName)
        }
        startActivity(intent)
    }
}