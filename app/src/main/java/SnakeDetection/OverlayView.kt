package com.SnakeDetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import java.util.LinkedList
import kotlin.math.max
import kotlin.math.sqrt

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var infoBackgroundPaint = Paint()
    private var infoTextPaint = Paint()
    private var dangerLevelPaint = Paint()
    private var infoButtonPaint = Paint()
    private var infoButtonTextPaint = Paint()

    private var bounds = Rect()
    private var showExtendedInfo = false
    private var currentSnakeInfo: SnakeInfo? = null
    private var snakeInfoProvider = SnakeInfoProvider()
    private var vibrator: Vibrator? = null

    private var onSnakeClickListener: ((SnakeInfo) -> Unit)? = null

    init {
        initPaints()
        vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun setOnSnakeClickListener(listener: (SnakeInfo) -> Unit) {
        onSnakeClickListener = listener
    }

    fun clear() {
        results = listOf()
        // Don't reset showExtendedInfo here - let it persist until explicitly closed
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        infoBackgroundPaint.color = Color.argb(220, 0, 0, 0) // Semi-transparent black
        infoBackgroundPaint.style = Paint.Style.FILL

        infoTextPaint.color = Color.WHITE
        infoTextPaint.style = Paint.Style.FILL
        infoTextPaint.textSize = 40f

        dangerLevelPaint.style = Paint.Style.FILL

        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE

        // Info button paints
        infoButtonPaint.color = Color.argb(230, 70, 130, 180) // Semi-transparent blue
        infoButtonPaint.style = Paint.Style.FILL

        infoButtonTextPaint.color = Color.WHITE
        infoButtonTextPaint.textSize = 40f
        infoButtonTextPaint.textAlign = Paint.Align.CENTER
        infoButtonTextPaint.typeface = Typeface.DEFAULT_BOLD
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // If showing extended info, draw it and return
        if (showExtendedInfo && currentSnakeInfo != null) {
            drawSnakeInfoCard(canvas, currentSnakeInfo!!)
            return
        }

        var isVeryCloseSnake = false
        var isExtremelyCloseSnake = false

        results.forEach { box ->
            // Get snake info to determine danger level
            val snakeInfo = snakeInfoProvider.getSnakeInfo(box.clsName)

            // Determine color and venomous status
            val isVenomous = when (snakeInfo?.dangerLevel) {
                "Venomous", "venomous", "non venomous", "Non Venomous" -> true
                else -> false
            }

            // Set box color based on venomous status
            val boxColor = if (isVenomous) {
                ContextCompat.getColor(context, R.color.red)
            } else {
                ContextCompat.getColor(context, R.color.darkGreen)
            }
            boxPaint.color = boxColor

            val left = box.x1 * width
            val top = box.y1 * height
            val right = box.x2 * width
            val bottom = box.y2 * height

            // Calculate area of the bounding box relative to screen
            val boxArea = (right - left) * (bottom - top)
            val screenArea = width * height
            val areaRatio = boxArea / screenArea

            // Check if snake is very close (occupies more than 30% of screen)
            if (areaRatio > 0.3) {
                isVeryCloseSnake = true

                // Check if snake is extremely close (occupies more than 60% of screen)
                if (areaRatio > 0.6) {
                    isExtremelyCloseSnake = true
                }
            }

            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Snake name in upper left outside the box
            val nameText = box.clsName
            val namePaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.white)
                style = Paint.Style.FILL
                textSize = 50f
                typeface = Typeface.DEFAULT_BOLD
            }

            // Measure name text
            val nameBounds = Rect()
            namePaint.getTextBounds(nameText, 0, nameText.length, nameBounds)
            val nameWidth = nameBounds.width()
            val nameHeight = nameBounds.height()

            // Draw name background outside the box
            val nameBackgroundPaint = Paint().apply {
                color = if (isVenomous) {
                    ContextCompat.getColor(context, R.color.red)
                } else {
                    ContextCompat.getColor(context, R.color.darkGreen)
                }
                style = Paint.Style.FILL
            }
            canvas.drawRect(
                left - 5f,
                top - nameHeight - 15f,
                left + nameWidth + 20f,
                top,
                nameBackgroundPaint
            )

            // Draw name text outside the box
            canvas.drawText(
                nameText,
                left + 5f,
                top - 10f,
                namePaint
            )

            // Status text below the name
            val statusText = if (isVenomous) "VENOMOUS" else "NON-VENOMOUS"
            val statusPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.white)
                style = Paint.Style.FILL
                textSize = 40f
                typeface = Typeface.DEFAULT_BOLD
            }

            // Measure status text
            val statusBounds = Rect()
            statusPaint.getTextBounds(statusText, 0, statusText.length, statusBounds)
            val statusWidth = statusBounds.width()
            val statusHeight = statusBounds.height()

            // Draw status background below the name
            val statusBackgroundPaint = Paint().apply {
                color = if (isVenomous) {
                    ContextCompat.getColor(context, R.color.red)
                } else {
                    ContextCompat.getColor(context, R.color.darkGreen)
                }
                style = Paint.Style.FILL
            }
            canvas.drawRect(
                left - 5f,
                top,
                left + statusWidth + 20f,
                top + statusHeight + 15f,
                statusBackgroundPaint
            )

            // Draw status text below the name
            canvas.drawText(
                statusText,
                left + 5f,
                top + statusHeight + 5f,
                statusPaint
            )

            // Draw an attractive "Tap for info" button BELOW at the right side of the bounding box
            val infoText = "Tap for info"
            val infoButtonTextPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.white)
                textSize = 40f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            infoButtonTextPaint.getTextBounds(infoText, 0, infoText.length, bounds)
            val textPadding = 12f
            val buttonWidth = bounds.width() + 2 * textPadding
            val buttonHeight = bounds.height() + 2 * textPadding

            // Position the button BELOW the bounding box, aligned with the right side
            val buttonLeft = right - buttonWidth
            val buttonTop = bottom  // Start at the bottom edge of the bounding box

            // Create and draw the button background with rounded corners
            val infoButtonPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.scanAgainBlue)
                style = Paint.Style.FILL
            }
            val buttonRect = RectF(buttonLeft, buttonTop, right, buttonTop + buttonHeight)
            canvas.drawRoundRect(buttonRect, 10f, 10f, infoButtonPaint)

            // Draw text (no icon)
            canvas.drawText(
                infoText,
                buttonRect.centerX(),
                buttonRect.centerY() + bounds.height()/3,
                infoButtonTextPaint
            )
        }

        // Handle very close or extremely close snake warnings
        if (isExtremelyCloseSnake) {
            // Display warning and vibrate
            drawWarning(canvas, "WARNING: SNAKE EXTREMELY CLOSE!", true)
        } else if (isVeryCloseSnake) {
            // Just vibrate
            vibrate()
        }
    }
    private fun drawWarning(canvas: Canvas, warningText: String, stopScanning: Boolean) {
        // Draw red warning background
        val warningPaint = Paint().apply {
            color = Color.argb(200, 255, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), warningPaint)

        // Draw warning text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(warningText, width / 2f, height / 2f, textPaint)

        if (stopScanning) {
            val subText = "Move away immediately!"
            textPaint.textSize = 40f
            canvas.drawText(subText, width / 2f, height / 2f + 80f, textPaint)

            // Vibrate with a longer pattern for danger
            vibrateWarning()
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(200)
        }
    }

    private fun vibrateWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a pattern: wait 0ms, vibrate 500ms, wait 200ms, vibrate 500ms
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    }

    private fun drawSnakeInfoCard(canvas: Canvas, snakeInfo: SnakeInfo) {
        // Draw semi-transparent background over the whole view
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), infoBackgroundPaint)

        // Draw info card
        val cardMargin = 50f
        val cardWidth = width - 2 * cardMargin
        val cardHeight = height / 2f
        val cardTop = (height - cardHeight) / 2

        val cardRect = RectF(cardMargin, cardTop, cardMargin + cardWidth, cardTop + cardHeight)

        // Draw card background with rounded corners
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRoundRect(cardRect, 30f, 30f, paint)

        // Draw danger level indicator
        val indicatorPaint = Paint()
        when (snakeInfo.dangerLevel) {
            "Venomous" ->
                indicatorPaint.color = Color.RED
            "Venomous" ->
                indicatorPaint.color = Color.rgb(255, 165, 0) // Orange
            "Non Venomous" ->
                indicatorPaint.color = Color.GREEN
            else ->
                indicatorPaint.color = Color.GREEN
        }
        canvas.drawRoundRect(
            RectF(cardRect.left, cardRect.top, cardRect.right, cardRect.top + 30),
            30f, 30f, indicatorPaint
        )

        // Draw text
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
        }
        val textPadding = 30f

        // Snake name
        canvas.drawText(snakeInfo.name, cardRect.left + textPadding, cardRect.top + 100, textPaint)

        // Scientific name
        textPaint.textSize = 35f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(snakeInfo.scientificName, cardRect.left + textPadding, cardRect.top + 150, textPaint)

        // Reset text style
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 40f

        // Danger level
        val dangerText = "Danger level: ${snakeInfo.dangerLevel}"
        canvas.drawText(dangerText, cardRect.left + textPadding, cardRect.top + 210, textPaint)

        // Description - split into multiple lines if needed
        val descLines = splitTextIntoLines(snakeInfo.description, textPaint, cardWidth - 2 * textPadding)
        var y = cardRect.top + 280f
        descLines.forEach { line ->
            canvas.drawText(line, cardRect.left + textPadding, y, textPaint)
            y += 50f
        }

        // Draw close button and "More info" button
        val buttonPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
        }
        val buttonTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 120f
            textAlign = Paint.Align.CENTER
        }

        // Close button (X)
        canvas.drawCircle(cardRect.right - 40f, cardRect.top + 40f, 50f, buttonPaint)
        buttonTextPaint.textSize = 120f
        canvas.drawText("×", cardRect.right - 40f, cardRect.top + 80f, buttonTextPaint)

        // More info button
        val moreInfoRect = RectF(
            cardRect.right - 200f,
            cardRect.bottom - 120f,
            cardRect.right - 1f,
            cardRect.bottom - 30f
        )
        buttonPaint.color = Color.rgb(70, 130, 180) // Steel blue
        canvas.drawRoundRect(moreInfoRect, 50f, 50f, buttonPaint)
        buttonTextPaint.color = Color.WHITE
        buttonTextPaint.textSize = 35f
        canvas.drawText("MORE INFO", moreInfoRect.centerX(), moreInfoRect.centerY() + 12f, buttonTextPaint)
    }

    private fun splitTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            val measureWidth = paint.measureText(testLine)

            if (measureWidth <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }

    fun showSnakeInfo(snakeName: String) {
        currentSnakeInfo = snakeInfoProvider.getSnakeInfo(snakeName)
        showExtendedInfo = currentSnakeInfo != null
        invalidate()
    }

    fun hideSnakeInfo() {
        showExtendedInfo = false
        currentSnakeInfo = null
        invalidate()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (showExtendedInfo && currentSnakeInfo != null) {
                // Check if close button was clicked
                val cardMargin = 50f
                val cardWidth = width - 2 * cardMargin
                val cardHeight = height / 2f
                val cardTop = (height - cardHeight) / 2
                val cardRect = RectF(cardMargin, cardTop, cardMargin + cardWidth, cardTop + cardHeight)

                // Close button hit test
                val closeButtonX = cardRect.right - 40f
                val closeButtonY = cardRect.top + 40f
                val distance = sqrt(
                    (event.x - closeButtonX).pow(2) +
                            (event.y - closeButtonY).pow(2)
                )

                if (distance <= 30.0) {
                    hideSnakeInfo()
                    return true
                }

                // More info button hit test
                val moreInfoRect = RectF(
                    cardRect.right - 200f,
                    cardRect.bottom - 80f,
                    cardRect.right - 30f,
                    cardRect.bottom - 30f
                )

                if (moreInfoRect.contains(event.x, event.y)) {
                    currentSnakeInfo?.let { onSnakeClickListener?.invoke(it) }
                    return true
                }

                // Any other touch on the info card just returns - we don't want to dismiss it
                return true
            } else {
                // Check for clicks on "Tap for info" buttons below the bounding boxes
                for (box in results) {
                    val left = box.x1 * width
                    val top = box.y1 * height
                    val right = box.x2 * width
                    val bottom = box.y2 * height

                    // Calculate button dimensions and position for this bounding box
                    val infoText = "Tap for info"
                    infoButtonTextPaint.getTextBounds(infoText, 0, infoText.length, bounds)
                    val textPadding = 12f
                    val buttonWidth = bounds.width() + 2 * textPadding
                    val buttonHeight = bounds.height() + 2 * textPadding

                    // Position of the button BELOW the bounding box, aligned with the right side
                    val buttonLeft = right - buttonWidth
                    val buttonTop = bottom
                    val buttonRect = RectF(buttonLeft, buttonTop, right, buttonTop + buttonHeight)

                    // Check if the tap for info button was clicked
                    if (buttonRect.contains(event.x, event.y)) {
                        showSnakeInfo(box.clsName)
                        return true
                    }

                    // Also allow clicking on the snake itself to show info
                    if (event.x >= left && event.x <= right && event.y >= top && event.y <= bottom) {
                        showSnakeInfo(box.clsName)
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8

        // Extension function to calculate power of 2
        private fun Float.pow(n: Int): Float = Math.pow(this.toDouble(), n.toDouble()).toFloat()
    }
}