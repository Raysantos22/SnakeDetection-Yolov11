package com.SnakeDetection

import SnakeDetection.EmergencyHotlineActivity
import SnakeDetection.FirstAidActivity
import SnakeDetection.SnakeLibraryActivity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Set up button click listeners
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        // Start Scanning Button
        findViewById<CardView>(R.id.startScanningCard).setOnClickListener {
            showSafetyGuidelinesDialog()
        }

        // First Aid Tips Button
        findViewById<CardView>(R.id.firstAidTipsCard).setOnClickListener {
            startActivity(Intent(this, FirstAidActivity::class.java))
        }

        // Snake Library Button
        findViewById<CardView>(R.id.snakeLibraryCard).setOnClickListener {
            startActivity(Intent(this, SnakeLibraryActivity::class.java))
        }

        // Emergency Hotline Button
        findViewById<CardView>(R.id.emergencyHotlineCard).setOnClickListener {
            startActivity(Intent(this, EmergencyHotlineActivity::class.java))
        }
    }

    private fun showSafetyGuidelinesDialog() {
        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_safety_guidelines)

        val nextButton = dialog.findViewById<Button>(R.id.nextButton)
        val checkBox = dialog.findViewById<CheckBox>(R.id.readGuidelinesCheckBox)

        // Initially hide the Next button
        nextButton.visibility = View.GONE

        // Add click listener to checkbox
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Show/hide next button based on checkbox state
            nextButton.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        nextButton.setOnClickListener {
            dialog.dismiss()

            // Start the MainActivity for scanning
            startActivity(Intent(this, MainActivity::class.java))
        }

        dialog.show()
    }
}