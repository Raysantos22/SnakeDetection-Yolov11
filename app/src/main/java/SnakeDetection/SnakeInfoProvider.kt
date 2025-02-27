package com.SnakeDetection

import android.content.Context

data class SnakeInfo(
    val name: String,
    val scientificName: String,
    val description: String,
    val dangerLevel: String,
    val habitat: String,
    val imageResId: Int
)

// Updated SnakeInfoProvider with image IDs
class SnakeInfoProvider {
    private val snakeInfoMap = mutableMapOf<String, SnakeInfo>()

    init {
        // Initialize snake data with actual image resource IDs
        // You'll need to add these images to your drawable folder
        snakeInfoMap["Flowerpot Snake"] = SnakeInfo(
            name = "Flowerpot Snake",
            scientificName = "Indotyphlops braminus",
            description = "Also known as the Brahminy Blind Snake, this is one of the smallest snake species. They are non-venomous and completely harmless to humans.",
            dangerLevel = "Harmless",
            habitat = "Found in soil and leaf litter in tropical regions. Often transported in potted plants.",
            imageResId = R.drawable.placeholder_snake
        )

        snakeInfoMap["King Cobra"] = SnakeInfo(
            name = "King Cobra",
            scientificName = "Ophiophagus hannah",
            description = "The world's longest venomous snake, capable of growing up to 18 feet. Known for its distinctive hood and deadly venom.",
            dangerLevel = "Highly Venomous - Fatal",
            habitat = "Forests of Southeast Asia and India. Usually avoids human contact.",
            imageResId = R.drawable.placeholder_snake
        )

        snakeInfoMap["Pit Viper"] = SnakeInfo(
            name = "Pit Viper",
            scientificName = "Crotalinae",
            description = "A subfamily of venomous snakes that have heat-sensing pits between their eyes and nostrils, helping them detect prey.",
            dangerLevel = "Venomous - Medical emergency",
            habitat = "Diverse habitats across Americas and Asia, including forests, grasslands, and deserts.",
            imageResId = R.drawable.placeholder_snake
        )

        snakeInfoMap["Rat Snake"] = SnakeInfo(
            name = "Rat Snake",
            scientificName = "Ptyas mucosa",
            description = "Non-venomous snake that plays an important role in controlling rodent populations. Fast-moving and good climbers.",
            dangerLevel = "Harmless",
            habitat = "Found in a variety of habitats including agricultural areas, forests, and near human settlements.",
            imageResId = R.drawable.placeholder_snake
        )

        snakeInfoMap["Reticulated Python"] = SnakeInfo(
            name = "Reticulated Python",
            scientificName = "Malayopython reticulatus",
            description = "One of the world's longest snakes, known for its complex geometric patterns. A powerful constrictor that's not venomous.",
            dangerLevel = "Non-venomous but potentially dangerous",
            habitat = "Tropical rainforests, woodlands, and nearby human habitations in Southeast Asia.",
            imageResId = R.drawable.placeholder_snake
        )

        snakeInfoMap["Sea Snake"] = SnakeInfo(
            name = "Sea Snake",
            scientificName = "Hydrophiinae",
            description = "Highly adapted for marine life with paddle-like tails and valved nostrils. Many species have potent venom.",
            dangerLevel = "Highly Venomous",
            habitat = "Warm coastal waters of the Indian and Pacific Oceans.",
            imageResId = R.drawable.placeholder_snake
        )
    }

    fun getSnakeInfo(snakeName: String): SnakeInfo? {
        return snakeInfoMap[snakeName]
    }

    fun getAllSnakeNames(): List<String> {
        return snakeInfoMap.keys.toList()
    }
}