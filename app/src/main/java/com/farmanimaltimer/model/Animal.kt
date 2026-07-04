package com.farmanimaltimer.model

enum class Animal(val displayName: String) {
    COW("Cow"),
    PIG("Pig"),
    CHICKEN("Chicken"),
    SHEEP("Sheep"),
    HORSE("Horse"),
    DUCK("Duck");

    companion object {
        fun fromNameOrDefault(name: String?): Animal =
            entries.firstOrNull { it.name == name } ?: COW
    }
}
