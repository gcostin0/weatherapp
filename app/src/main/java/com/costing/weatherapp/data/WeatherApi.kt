package com.costing.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast.json")
    suspend fun getWeatherForecast(
        @Query("q") location: String = "Bucharest",
        @Query("days") days: Int = 7,
        @Query("key") apiKey: String = "64ed483363e54299905110913250602" // Replace with your WeatherAPI.com key
    ): WeatherResponse
} 