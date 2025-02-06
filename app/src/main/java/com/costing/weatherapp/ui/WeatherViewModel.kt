package com.costing.weatherapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.costing.weatherapp.data.ForecastDay
import com.costing.weatherapp.data.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(val forecasts: List<ForecastDay>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadWeatherData()
    }

    private fun loadWeatherData() {
        viewModelScope.launch {
            try {
                val response = repository.getWeatherForecast()
                _uiState.value = WeatherUiState.Success(response.forecast.forecastday)
            } catch (e: IOException) {
                _uiState.value = WeatherUiState.Error("Network error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Error: ${e.message}")
            }
        }
    }
} 