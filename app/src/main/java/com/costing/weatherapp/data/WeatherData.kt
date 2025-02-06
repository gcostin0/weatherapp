package com.costing.weatherapp.data

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("forecast")
    val forecast: Forecast
)

data class Forecast(
    @SerializedName("forecastday")
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    @SerializedName("date")
    val date: String,
    @SerializedName("date_epoch")
    val date_epoch: Long,
    @SerializedName("day")
    val day: Day
)

data class Day(
    @SerializedName("maxtemp_c")
    val maxtemp_c: Double,
    @SerializedName("mintemp_c")
    val mintemp_c: Double,
    @SerializedName("avgtemp_c")
    val avgtemp_c: Double,
    @SerializedName("condition")
    val condition: Condition,
    @SerializedName("daily_chance_of_rain")
    val daily_chance_of_rain: Double,
    @SerializedName("daily_chance_of_snow")
    val daily_chance_of_snow: Double,
    @SerializedName("maxwind_kph")
    val maxwind_kph: Double,
    @SerializedName("totalprecip_mm")
    val totalprecip_mm: Double
)

data class Condition(
    @SerializedName("text")
    val text: String,
    @SerializedName("icon")
    val icon: String
) 