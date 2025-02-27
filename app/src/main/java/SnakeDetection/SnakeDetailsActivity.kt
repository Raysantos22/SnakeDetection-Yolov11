package com.SnakeDetection

import SnakeDetection.SnakeImageMapper
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.SnakeDetection.SnakeInfoProvider
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

        // Update UI with snake info
        findViewById<TextView>(R.id.textSnakeName).text = snakeInfo.name
        findViewById<TextView>(R.id.textScientificName).text = snakeInfo.scientificName
        findViewById<TextView>(R.id.textDangerLevel).text = "Danger Level: ${snakeInfo.dangerLevel}"
        findViewById<TextView>(R.id.textDescription).text = snakeInfo.description
        findViewById<TextView>(R.id.textHabitat).text = "Habitat: ${snakeInfo.habitat}"

        // Set the actual snake image
        val imageView = findViewById<ImageView>(R.id.imageSnake)
        imageView.setImageResource(snakeInfo.imageResId)

        // Set danger level color indicator
        val dangerIndicator = findViewById<View>(R.id.viewDangerIndicator)
        when (snakeInfo.dangerLevel) {
            "Highly Venomous - Fatal", "Highly Venomous" ->
                dangerIndicator.setBackgroundColor(Color.RED)
            "Venomous - Medical emergency" ->
                dangerIndicator.setBackgroundColor(Color.rgb(255, 165, 0)) // Orange
            "Non-venomous but potentially dangerous" ->
                dangerIndicator.setBackgroundColor(Color.YELLOW)
            else ->
                dangerIndicator.setBackgroundColor(Color.GREEN)
        }

        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }
}