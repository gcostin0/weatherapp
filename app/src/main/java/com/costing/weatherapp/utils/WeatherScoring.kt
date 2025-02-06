package com.costing.weatherapp.utils

import com.costing.weatherapp.data.Day
import kotlin.math.max
import kotlin.math.min

object WeatherScoring {
    fun calculateBikeRidingScore(day: Day): Int {
        // Temperature score (ideal range: 10-20°C for running)
        val tempScore = calculateTemperatureScore(day.avgtemp_c)
        
        // Precipitation score
        val precipScore = calculatePrecipitationScore(
            day.daily_chance_of_rain,
            day.daily_chance_of_snow,
            day.totalprecip_mm
        )
        
        // Wind score (ideal: 0-15 kph)
        val windScore = calculateWindScore(day.maxwind_kph)
        
        // Weighted average (temperature more important for running)
        val finalScore = (tempScore * 0.45 + precipScore * 0.35 + windScore * 0.20)
        
        return min(100, max(0, finalScore.toInt()))
    }
    
    private fun calculateTemperatureScore(temp: Double): Double {
        return when {
            temp <= 0.5 -> 5.0 // Extremely cold, dangerous for running
            temp <= 1 -> 10.0 // Too cold for running
            temp < 5 -> 30.0 + (temp - 1) * 5 // Very cold
            temp < 10 -> 50.0 + (temp - 5) * 8 // Getting better
            temp < 15 -> 90.0 + (temp - 10) * 2 // Good
            temp <= 20 -> 100.0 // Perfect for running
            temp <= 25 -> 100.0 - (temp - 20) * 8 // Getting too warm
            temp <= 30 -> 60.0 - (temp - 25) * 8 // Too warm
            else -> 20.0 // Too hot for running
        }
    }
    
    private fun calculatePrecipitationScore(rainChance: Double, snowChance: Double, precipMm: Double): Double {
        val precipitationChance = max(rainChance, snowChance)
        val baseScore = 100 - precipitationChance
        
        // Additional penalty for expected precipitation amount
        val precipPenalty = when {
            precipMm <= 0.0 -> 0.0
            precipMm <= 1.0 -> precipMm * 10
            precipMm <= 5.0 -> 10 + (precipMm - 1) * 15
            else -> 70.0
        }
        
        return max(0.0, baseScore - precipPenalty)
    }
    
    private fun calculateWindScore(windKph: Double): Double {
        return when {
            windKph <= 15 -> 100.0 // Perfect for running
            windKph <= 25 -> 80.0 - ((windKph - 15) * 3) // Linear decrease from 80% to 50%
            else -> max(0.0, 50.0 - ((windKph - 25) * 2)) // Linear decrease below 50%
        }
    }
} 