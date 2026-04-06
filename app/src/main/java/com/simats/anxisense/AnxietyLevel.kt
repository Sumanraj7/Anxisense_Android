package com.simats.anxisense

import android.graphics.Color

/**
 * Data class representing an anxiety level on a 0-10 scale
 */
data class AnxietyLevel(
    val level: Int,              // 0-10 numeric level
    val severityName: String,    // e.g., "Mild Anxiety", "Severe Anxiety"
    val severityCategory: String, // e.g., "Mild", "Moderate", "Severe"
    val color: Int,              // Color for this level
    val description: String,     // Brief description
    val emojiResId: Int         // Resource ID for emoji face drawable
)

/**
 * Utility object for anxiety level calculations and conversions
 */
object AnxietyLevelUtils {
    
    // Color definitions for each range (Aligned with colors.xml)
    private val COLOR_LOW = Color.parseColor("#10B981")          // Emerald-500
    private val COLOR_MODERATE = Color.parseColor("#F59E0B")     // Amber-500
    private val COLOR_HIGH = Color.parseColor("#EF4444")         // Red-500
    
    /**
     * Convert percentage (0-100) to anxiety level (0-10)
     * Backend: <30 Low, <60 Moderate, >=60 High
     */
    fun percentageToLevel(percentage: Int): Int {
        return when {
            percentage < 0 -> 0
            percentage > 100 -> 10
            else -> (percentage / 10.0).toInt().coerceIn(0, 10)
        }
    }
    
    /**
     * Convert percentage string (e.g., "74%") to anxiety level
     */
    fun percentageStringToLevel(percentageStr: String): Int {
        val percentage = percentageStr.replace("%", "").toIntOrNull() ?: 0
        return percentageToLevel(percentage)
    }
    
    /**
     * Get AnxietyLevel object from numeric level (0-10)
     */
    fun getAnxietyLevel(level: Int): AnxietyLevel {
        val clampedLevel = level.coerceIn(0, 10)
        
        return when (clampedLevel) {
            in 0..3 -> AnxietyLevel( // 0-39% (Low)
                level = clampedLevel,
                severityName = "Low Anxiety",
                severityCategory = "Low",
                color = COLOR_LOW,
                description = "Patient shows low signs of anxiety",
                emojiResId = if(clampedLevel == 0) R.drawable.ic_anxiety_face_0 else R.drawable.ic_anxiety_face_1
            )
            in 4..6 -> AnxietyLevel( // 40-69% (Moderate)
                level = clampedLevel,
                severityName = "Moderate Anxiety",
                severityCategory = "Moderate",
                color = COLOR_MODERATE,
                description = "Patient shows moderate anxiety requiring attention",
                emojiResId = R.drawable.ic_anxiety_face_4
            )
            else -> AnxietyLevel( // 70-100% (High)
                level = clampedLevel,
                severityName = "High Anxiety",
                severityCategory = "High",
                color = COLOR_HIGH,
                description = "Patient shows high anxiety requiring intervention",
                emojiResId = R.drawable.ic_anxiety_face_10
            )
        }
    }
    
    /**
     * Get AnxietyLevel from percentage
     */
    fun getAnxietyLevelFromPercentage(percentage: Int): AnxietyLevel {
        val level = percentageToLevel(percentage)
        return getAnxietyLevel(level)
    }
    
    /**
     * Get AnxietyLevel from percentage string
     */
    fun getAnxietyLevelFromPercentageString(percentageStr: String): AnxietyLevel {
        val level = percentageStringToLevel(percentageStr)
        return getAnxietyLevel(level)
    }
    
    /**
     * Get color for a specific level
     */
    fun getColorForLevel(level: Int): Int {
        return getAnxietyLevel(level).color
    }
}
