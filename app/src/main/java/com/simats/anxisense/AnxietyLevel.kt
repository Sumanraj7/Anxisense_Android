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
    
    // Color definitions for each range
    private val COLOR_NO_ANXIETY = Color.parseColor("#22C55E")      // Green
    private val COLOR_MILD = Color.parseColor("#84CC16")            // Light green
    private val COLOR_MODERATE = Color.parseColor("#FBBF24")        // Yellow/Orange
    private val COLOR_SEVERE = Color.parseColor("#F97316")          // Orange
    private val COLOR_VERY_SEVERE = Color.parseColor("#EF4444")     // Red
    private val COLOR_WORST = Color.parseColor("#DC2626")           // Dark red
    
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
            in 0..2 -> AnxietyLevel( // 0-29% (Backend: Low)
                level = clampedLevel,
                severityName = "Low Anxiety",
                severityCategory = "Low",
                color = COLOR_MILD,
                description = "Patient shows low signs of anxiety",
                emojiResId = if(clampedLevel == 0) R.drawable.ic_anxiety_face_0 else R.drawable.ic_anxiety_face_1
            )
            in 3..5 -> AnxietyLevel( // 30-59% (Backend: Moderate)
                level = clampedLevel,
                severityName = "Moderate Anxiety",
                severityCategory = "Moderate",
                color = COLOR_MODERATE,
                description = "Patient shows moderate anxiety requiring attention",
                emojiResId = R.drawable.ic_anxiety_face_4
            )
            in 6..8 -> AnxietyLevel( // 60-89% (Backend: High start at 60)
                level = clampedLevel,
                severityName = "High Anxiety",
                severityCategory = "High",
                color = COLOR_SEVERE,
                description = "Patient shows high anxiety requiring intervention",
                emojiResId = R.drawable.ic_anxiety_face_7
            )
            else -> AnxietyLevel( // 90-100%
                level = 10,
                severityName = "Severe Anxiety",
                severityCategory = "Severe",
                color = COLOR_WORST,
                description = "Patient shows severe anxiety requiring urgent intervention",
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
