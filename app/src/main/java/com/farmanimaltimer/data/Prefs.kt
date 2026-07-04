package com.farmanimaltimer.data

import android.content.Context
import com.farmanimaltimer.model.Animal

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("fat_prefs", Context.MODE_PRIVATE)

    var lastAnimal: Animal
        get() = Animal.fromNameOrDefault(sp.getString(KEY_ANIMAL, null))
        set(value) { sp.edit().putString(KEY_ANIMAL, value.name).apply() }

    var lastDurationSeconds: Long
        get() = sp.getLong(KEY_DURATION, 0L)
        set(value) { sp.edit().putLong(KEY_DURATION, value).apply() }

    private companion object {
        const val KEY_ANIMAL = "last_animal"
        const val KEY_DURATION = "last_duration"
    }
}
