package SnakeDetection

import android.content.Context
import com.SnakeDetection.R

class SnakeImageMapper {
    companion object {
        fun getImageResourceForSnake(context: Context, snakeName: String): Int {
            val packageName = context.packageName

            // Map snake names to image resource names
            val imageName = when (snakeName) {
                "Flowerpot Snake" -> "flowerpot_snake"
                "King Cobra" -> "king_cobra"
                "Pit Viper" -> "pit_viper"
                "Rat Snake" -> "rat_snake"
                "Reticulated Python" -> "reticulated_python"
                "Sea Snake" -> "sea_snake"
                else -> "placeholder_snake"
            }

            // Get resource ID for the image
            val resourceId = context.resources.getIdentifier(imageName, "drawable", packageName)

            // Return placeholder if resource not found
            return if (resourceId != 0) resourceId else R.drawable.placeholder_snake
        }
    }
}