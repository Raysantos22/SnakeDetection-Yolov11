package com.SnakeDetection

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.SnakeDetection.R

class SnakeDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SNAKE_NAME = "extra_snake_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake_details)

        val snakeName = intent.getStringExtra(EXTRA_SNAKE_NAME) ?: return
        val snakeInfoProvider = SnakeInfoProvider()
        val snakeInfo = snakeInfoProvider.getSnakeInfo(snakeName) ?: return

        // Update header title
        findViewById<TextView>(R.id.snakeTitleHeader).text = snakeInfo.name.uppercase()

        // Update content area text
        findViewById<TextView>(R.id.textSnakeName).text = snakeInfo.name.uppercase()
        findViewById<TextView>(R.id.textScientificName).text = snakeInfo.scientificName
        findViewById<TextView>(R.id.textDangerLevel).text = "Danger Level: ${snakeInfo.dangerLevel}"
        findViewById<TextView>(R.id.textDescription).text = snakeInfo.description
        findViewById<TextView>(R.id.textHabitat).text = snakeInfo.habitat

        // Set the actual snake image if available
        val snakeIconId = getSnakeDrawableId(snakeInfo.name)
        findViewById<ImageView>(R.id.snakeIcon).setImageResource(snakeIconId)

        // Also update the full-size image if it exists in the layout
        val imageView = findViewById<ImageView>(R.id.imageSnake)
        if (imageView != null) {
            imageView.setImageResource(snakeInfo.imageResId)
        }

        // Simplified danger level color indicator - only RED or GREEN
        val dangerIndicator = findViewById<View>(R.id.viewDangerIndicator)
        when {
            // If contains "Venomous" anywhere in the danger level, mark as venomous (RED)
            snakeInfo.dangerLevel.contains("Venomous", ignoreCase = true) ->
                dangerIndicator.setBackgroundColor(Color.RED)
            // Otherwise mark as non-venomous (GREEN)
            else ->
                dangerIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.nonVenomousGreen))
        }

        // Set up back button
        findViewById<CardView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    // Helper function to get the custom drawable for each snake
    private fun getSnakeDrawableId(snakeName: String): Int {
        return when(snakeName) {
            "King Cobra" -> R.drawable.snake_logo
            "Pit Viper" -> R.drawable.snake_logo
            "Rat Snake" -> R.drawable.snake_logo
            "Reticulated Python" -> R.drawable.snake_logo
            "Sea Snake" -> R.drawable.snake_logo
            "Flowerpot Snake" -> R.drawable.snake_logo
            else -> R.drawable.snake_logo
        }
    }
}