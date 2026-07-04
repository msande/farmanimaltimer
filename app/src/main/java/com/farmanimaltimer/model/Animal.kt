package com.farmanimaltimer.model

enum class Animal(val displayName: String, val hasPhoto: Boolean = false) {
    COW("Cow"),
    PIG("Pig"),
    CHICKEN("Chicken"),
    SHEEP("Sheep"),
    HORSE("Horse"),
    DUCK("Duck"),
    AVA("Ava", hasPhoto = true);

    companion object {
        fun fromNameOrDefault(name: String?): Animal =
            entries.firstOrNull { it.name == name } ?: COW
    }
}
