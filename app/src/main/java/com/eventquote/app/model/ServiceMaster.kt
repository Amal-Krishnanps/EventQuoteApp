package com.eventquote.app.model

import kotlinx.serialization.Serializable

/**
 * Master service template that users configure once.
 * When selected in an estimate, default sub-items auto-populate.
 */
@Serializable
data class ServiceMaster(
    val id: String,
    val name: String = "",
    val description: String = "",
    val defaultAmount: Double = 0.0,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val defaultSubItems: List<SubItemTemplate> = emptyList()
)

/**
 * Default sub-item template for a master service.
 */
@Serializable
data class SubItemTemplate(
    val id: String,
    val name: String = "",
    val description: String = "",
    val defaultCost: Double = 0.0
)

/**
 * Pre-defined master services with sub-items to seed the database on first run.
 */
object DefaultServices {
    fun getDefaultMasterServices(): List<Pair<String, List<String>>> = listOf(
        "Food" to listOf(
            "Breakfast", "Lunch", "Dinner", "Tea", "Juice",
            "Welcome Drink", "Starters", "Vegetarian Menu",
            "Non-Vegetarian Menu", "Desserts", "Ice Cream", "Sweets"
        ),
        "Stage Decoration" to listOf(
            "Main Stage", "Entrance Arch", "House Lighting", "Walkway",
            "Photo Booth", "Flower Decoration", "LED Wall", "Welcome Board",
            "Ceiling Decoration", "Table Decoration"
        ),
        "Photography" to listOf(
            "Pre-Wedding Shoot", "Event Photography", "Candid Photography",
            "Drone Photography", "Album", "Editing", "Photo Frames"
        ),
        "Videography" to listOf(
            "Event Videography", "Cinematic Video", "Drone Video",
            "Live Streaming", "Highlight Reel", "Full Length Video", "Editing"
        ),
        "DJ" to listOf(
            "DJ Setup", "Sound System", "Speakers", "Subwoofer",
            "DJ Console", "Music Library", "Anchor/Host"
        ),
        "Lighting" to listOf(
            "LED Lights", "Fairy Lights", "Spotlights", "Wash Lights",
            "Laser Lights", "Projector", "LED Screen", "Moving Heads"
        ),
        "Car Decoration" to listOf(
            "Flower Decoration", "Balloon Decoration", "Ribbon Decoration",
            "Door Handles", "Bonnet Decoration", "Theme Decoration"
        ),
        "Flower Decoration" to listOf(
            "Entrance Gate", "Stage Backdrop", "Mandap", "Aisle",
            "Tables", "Welcome Garland", "Petals", "Floral Arch"
        ),
        "Welcome Team" to listOf(
            "Reception Staff", "Ushers", "Greeters", "Security",
            "Car Parking", "Guest Management"
        ),
        "Accommodation" to listOf(
            "Rooms", "Deluxe Rooms", "Suites", "Tent Houses",
            "Dormitory", "Guest House"
        ),
        "Transportation" to listOf(
            "Tempo Traveller", "Bus", "Car", "Auto Rickshaw",
            "Luxury Car", "Vintage Car", "Helicopter"
        ),
        "Makeup" to listOf(
            "Bridal Makeup", "Groom Makeup", "Family Makeup",
            "Hair Styling", "Saree Draping", "Mehendi"
        ),
        "Entertainment" to listOf(
            "Live Band", "Folk Dance", "Classical Dance", "Magic Show",
            "Comedian", "Fire Show", "Fireworks", "Balloon Artist"
        ),
        "Return Gifts" to listOf(
            "Gift Boxes", "Packing", "Labels", "Distribution Team",
            "Custom Gifts", "Traditional Gifts"
        ),
        "Generator" to listOf(
            "Power Generator", "Backup Generator", "Technician", "Fuel"
        ),
        "Catering Equipment" to listOf(
            "Utensils", "Serving Dishes", "Tables", "Chairs",
            "Dining Tables", "Buffet Setup", "Chafing Dishes"
        ),
        "Tent & Pandal" to listOf(
            "Tent Setup", "Flooring", "Carpet", "Furniture",
            "Ceiling Drapes", "Side Curtains", "AC Tent"
        )
    )
}
