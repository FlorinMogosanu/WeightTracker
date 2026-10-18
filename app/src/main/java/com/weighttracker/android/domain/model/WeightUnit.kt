package com.weighttracker.android.domain.model

import java.util.Locale

enum class WeightUnit(
    val id: String,
    val symbol: String,
    val kgMultiplier: Double
) {
    KG("kg", "kg", 1.0),
    LBS("lbs", "lbs", 2.20462262185);

    fun toDisplay(weightKg: Double): Double {
        return when (this) {
            KG -> weightKg
            LBS -> weightKg * kgMultiplier
        }
    }

    fun toKg(displayWeight: Double): Double {
        return when (this) {
            KG -> displayWeight
            LBS -> displayWeight / kgMultiplier
        }
    }

    fun format(weightKg: Double): String {
        val converted = toDisplay(weightKg)
        return String.format(Locale.US, "%.1f %s", converted, symbol)
    }

    companion object {
        fun fromId(id: String): WeightUnit {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: KG
        }
    }
}
