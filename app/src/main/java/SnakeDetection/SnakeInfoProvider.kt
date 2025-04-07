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
        // Existing snakes
        snakeInfoMap["Flowerpot Snake"] = SnakeInfo(
            name = "Flowerpot Snake",
            scientificName = "Indotyphlops braminus",
            description = "Also known as the Brahminy Blind Snake, this is one of the smallest snake species. They are non-venomous and completely harmless to humans.",
            dangerLevel = "Non-Venomous",
            habitat = "Found in soil and leaf litter in tropical regions. Often transported in potted plants.",
            imageResId = R.drawable.flowerpot
        )

        snakeInfoMap["King Cobra"] = SnakeInfo(
            name = "King Cobra",
            scientificName = "Ophiophagus hannah",
            description = "The world's longest venomous snake, capable of growing up to 18 feet. Known for its distinctive hood and deadly venom.",
            dangerLevel = "Venomous",
            habitat = "Forests of Southeast Asia and India. Usually avoids human contact.",
            imageResId = R.drawable.kingcobra
        )

        snakeInfoMap["Pit Viper"] = SnakeInfo(
            name = "Pit Viper",
            scientificName = "Crotalinae",
            description = "A subfamily of venomous snakes that have heat-sensing pits between their eyes and nostrils, helping them detect prey.",
            dangerLevel = "Venomous",
            habitat = "Diverse habitats across Americas and Asia, including forests, grasslands, and deserts.",
            imageResId = R.drawable.pitviper
        )

        snakeInfoMap["Rat Snake"] = SnakeInfo(
            name = "Rat Snake",
            scientificName = "Ptyas mucosa",
            description = "Non-venomous snake that plays an important role in controlling rodent populations. Fast-moving and good climbers.",
            dangerLevel = "Non-Venomous",
            habitat = "Found in a variety of habitats including agricultural areas, forests, and near human settlements.",
            imageResId = R.drawable.ratsnake
        )

        snakeInfoMap["Reticulated Python"] = SnakeInfo(
            name = "Reticulated Python",
            scientificName = "Malayopython reticulatus",
            description = "One of the world's longest snakes, known for its complex geometric patterns. A powerful constrictor that's not venomous.",
            dangerLevel = "Non-Venomous",
            habitat = "Tropical rainforests, woodlands, and nearby human habitations in Southeast Asia.",
            imageResId = R.drawable.python
        )

        snakeInfoMap["Seasnake"] = SnakeInfo(
            name = "Seasnake",
            scientificName = "Hydrophiinae",
            description = "Highly adapted for marine life with paddle-like tails and valved nostrils. Many species have potent venom.",
            dangerLevel = "Venomous",
            habitat = "Warm coastal waters of the Indian and Pacific Oceans.",
            imageResId = R.drawable.seasnake
        )

        // New snakes added
        snakeInfoMap["Bronzeback Tree Snake"] = SnakeInfo(
            name = "Bronzeback Tree Snake",
            scientificName = "Dendrelaphis",
            description = "Slender, agile snakes found in trees and bushes. Generally non-aggressive and non-venomous.",
            dangerLevel = "Non-Venomous",
            habitat = "Forested areas in Southeast Asia",
            imageResId = R.drawable.bronzeback
        )

        snakeInfoMap["Cat Snake"] = SnakeInfo(
            name = "Cat Snake",
            scientificName = "Boiga",
            description = "Nocturnal snakes with vertically slit pupils similar to cat eyes. Some species are mildly venomous.",
            dangerLevel = "Venomous",
            habitat = "Tropical and subtropical regions of Asia",
            imageResId = R.drawable.catsnake
        )

        snakeInfoMap["Common Krait Snake"] = SnakeInfo(
            name = "Common Krait Snake",
            scientificName = "Bungarus caeruleus",
            description = "Highly venomous snake found in the Indian subcontinent. Known for its potent neurotoxic venom.",
            dangerLevel = "Venomous",
            habitat = "Plains and low-lying areas of India and surrounding regions",
            imageResId = R.drawable.commonkrait
        )

        snakeInfoMap["Common Wolf Snake"] = SnakeInfo(
            name = "Common Wolf Snake",
            scientificName = "Lycodon aulicus",
            description = "Small, non-venomous snake that feeds primarily on lizards and geckos.",
            dangerLevel = "Non-Venomous",
            habitat = "Forests, grasslands, and human-modified landscapes in South Asia",
            imageResId = R.drawable.commonwolf
        )

        snakeInfoMap["Coral Snake"] = SnakeInfo(
            name = "Coral Snake",
            scientificName = "Elapidae",
            description = "Highly venomous snake known for its bright, distinctive coloration. Typically shy and non-aggressive.",
            dangerLevel = "Venomous",
            habitat = "Forests and woodland areas in various tropical regions",
            imageResId = R.drawable.coralsnake
        )

        snakeInfoMap["Dog-faced Water Snake"] = SnakeInfo(
            name = "Dog-faced Water Snake",
            scientificName = "Cerberus rynchops",
            description = "Aquatic snake found in mangrove swamps and coastal areas. Mildly venomous but generally not dangerous to humans.",
            dangerLevel = "Non-Venomous",
            habitat = "Coastal regions and mangrove swamps in Asia",
            imageResId = R.drawable.dogface
        )

        snakeInfoMap["Gervais Worm Snake"] = SnakeInfo(
            name = "Gervais Worm Snake",
            scientificName = "Typhlops jamaicensis",
            description = "Tiny, burrowing snake that spends most of its time underground. Completely harmless to humans.",
            dangerLevel = "Non-Venomous",
            habitat = "Soil and underground areas in tropical regions",
            imageResId = R.drawable.gervais
        )

        snakeInfoMap["Lake Taal Snake"] = SnakeInfo(
            name = "Lake Taal Snake",
            scientificName = "Endemic to Philippines",
            description = "A rare snake species found in specific regions of the Philippines. Limited information available.",
            dangerLevel = "Venomous",
            habitat = "Lake Taal region in the Philippines",
            imageResId = R.drawable.taal
        )

        snakeInfoMap["Milk Snake"] = SnakeInfo(
            name = "Milk Snake",
            scientificName = "Lampropeltis triangulum",
            description = "Non-venomous snake known for its colorful mimicry of venomous coral snakes. Harmless to humans.",
            dangerLevel = "Non-Venomous",
            habitat = "Forests and grasslands in North and Central America",
            imageResId = R.drawable.milksnake
        )

        snakeInfoMap["Northern Short-headed Snake"] = SnakeInfo(
            name = "Northern Short-headed Snake",
            scientificName = "Asymbolus vertebralis",
            description = "Small, ground-dwelling snake found in specific regions. Generally non-venomous.",
            dangerLevel = "Non-Venomous",
            habitat = "Specific regions with particular habitat conditions",
            imageResId = R.drawable.northernsnake
        )

        snakeInfoMap["Philippine Grey-tailed Ratsnake"] = SnakeInfo(
            name = "Philippine Grey-tailed Ratsnake",
            scientificName = "Ptyas carinata",
            description = "Non-venomous snake endemic to the Philippines. Helps control rodent populations.",
            dangerLevel = "Non-Venomous",
            habitat = "Forests and agricultural areas in the Philippines",
            imageResId = R.drawable.greyratsnake
        )

        snakeInfoMap["Stokes Sea Snake"] = SnakeInfo(
            name = "Stokes Sea Snake",
            scientificName = "Hydrophis stokesii",
            description = "Highly venomous sea snake found in marine environments. Potentially dangerous to humans.",
            dangerLevel = "Venomous",
            habitat = "Coastal waters of the Indo-Pacific region",
            imageResId = R.drawable.stokesnake
        )

        snakeInfoMap["Turtle Head Snake"] = SnakeInfo(
            name = "Turtle Head Snake",
            scientificName = "Genus unclear",
            description = "A rare snake species with limited documented information.",
            dangerLevel = "Venomous",
            habitat = "Specific regional habitats",
            imageResId = R.drawable.turlesnake
        )

        snakeInfoMap["Yellow Bellied Snake"] = SnakeInfo(
            name = "Yellow Bellied Snake",
            scientificName = "Species identification needed",
            description = "Snake characterized by a yellow belly. Specific details require further research.",
            dangerLevel = "Venomous",
            habitat = "Varies by specific species",
            imageResId = R.drawable.yellowsnake
        )
    }

    fun getSnakeInfo(snakeName: String): SnakeInfo? {
        return snakeInfoMap[snakeName]
    }

    fun getAllSnakeNames(): List<String> {
        return snakeInfoMap.keys.toList()
    }
}