package com.costing.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.costing.weatherapp.data.ForecastDay
import com.costing.weatherapp.ui.WeatherUiState
import com.costing.weatherapp.ui.WeatherViewModel
import com.costing.weatherapp.ui.theme.WeatherAppTheme
import com.costing.weatherapp.utils.WeatherScoring
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen()
                }
            }
        }
    }
}

// Add this data class for rain drops
private data class RainDrop(
    val initialX: Float,
    val initialY: Float,
    val speed: Float,
    val length: Float
)

// Update the AnimatedClouds function to include more clouds
@Composable
private fun AnimatedClouds() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Update particle counts
    val cloudCount = remember { if (screenWidth > 600) 16 else 8 }  // Increased from 8/4
    val rainCount = remember { if (screenWidth > 600) 12 else 6 }  // Increased from 10/5
    
    // Create clouds with optimized values
    val clouds = remember {
        List(cloudCount) {
            Cloud(
                initialX = Random.nextFloat() * 2000,
                initialY = Random.nextFloat() * 1500,
                scale = 2.5f + Random.nextFloat() * 3f,
                speed = 0.5f + Random.nextFloat() * 0.8f  // Slower animation
            )
        }
    }
    
    // Optimize rain drops
    val rainDrops = remember {
        List(rainCount) {
            RainDrop(
                initialX = Random.nextFloat() * 2000,
                initialY = Random.nextFloat() * -1000,
                speed = 8f + Random.nextFloat() * 12f,  // Slightly slower
                length = 35f + Random.nextFloat() * 45f
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Enable hardware acceleration
                renderEffect = null
                clip = false
            }
    ) {
        // Create individual animations
        clouds.forEach { cloud ->
            AnimatedCloud(cloud)
        }
        
        rainDrops.forEach { drop ->
            AnimatedRainDrop(drop)
        }
    }
}

@Composable
private fun AnimatedCloud(cloud: Cloud) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud_${cloud.initialX}")
    val xPosition by infiniteTransition.animateFloat(
        initialValue = cloud.initialX,
        targetValue = cloud.initialX + 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (30000 / cloud.speed).toInt(),  // Slower animation
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_x"
    )
    
    Canvas(modifier = Modifier
        .fillMaxSize()
        .alpha(0.12f)
        .graphicsLayer {
            // Enable hardware acceleration
            renderEffect = null
            clip = false
        }
    ) {
        drawPath(
            path = createCloudPath(xPosition, cloud.initialY, cloud.scale, size),
            color = Color.White
        )
    }
}

@Composable
private fun AnimatedRainDrop(drop: RainDrop) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain_${drop.initialX}")
    val yPosition by infiniteTransition.animateFloat(
        initialValue = drop.initialY,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2000 / drop.speed * 20).toInt(),  // Slower animation
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_y"
    )
    
    Canvas(modifier = Modifier
        .fillMaxSize()
        .alpha(0.4f)
        .graphicsLayer {
            // Enable hardware acceleration
            renderEffect = null
            clip = false
        }
    ) {
        drawLine(
            color = Color.White,
            start = Offset(drop.initialX % (size.width + 500) - 250, yPosition % (size.height + 500) - 250),
            end = Offset(
                drop.initialX % (size.width + 500) - 250,
                (yPosition % (size.height + 500)) - 250 + drop.length
            ),
            strokeWidth = 2.5f
        )
    }
}

// Move path creation to separate function
private fun createCloudPath(x: Float, initialY: Float, scale: Float, size: Size): Path {
    return Path().apply {
        val adjustedX = x % (size.width + 500) - 250
        val y = initialY % (size.height - 100)
        
        moveTo(adjustedX, y + 20 * scale)
        cubicTo(
            adjustedX - 15 * scale, y + 20 * scale,
            adjustedX - 15 * scale, y,
            adjustedX, y
        )
        cubicTo(
            adjustedX + 15 * scale, y,
            adjustedX + 25 * scale, y + 5 * scale,
            adjustedX + 25 * scale, y + 10 * scale
        )
        cubicTo(
            adjustedX + 25 * scale, y + 15 * scale,
            adjustedX + 15 * scale, y + 20 * scale,
            adjustedX, y + 20 * scale
        )
    }
}

private data class Cloud(
    val initialX: Float,
    val initialY: Float,
    val scale: Float,
    val speed: Float
)

@Composable
private fun AppBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(0f)
            .graphicsLayer {
                // Enable hardware acceleration
                renderEffect = null
                clip = false
            }
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B2838),
                            Color(0xFF2B4570),
                            Color(0xFF4A6FA5)
                        )
                    )
                )
            }
    ) {
        AnimatedClouds()
    }
}

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            Text(
                text = "Weather in Bucharest for running",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(2f, 2f),
                        blurRadius = 3f
                    )
                ),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center
            )
            
            when (val state = uiState) {
                is WeatherUiState.Loading -> LoadingScreen()
                is WeatherUiState.Success -> WeatherList(forecasts = state.forecasts)
                is WeatherUiState.Error -> ErrorScreen(message = state.message)
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White
        )
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun WeatherList(forecasts: List<ForecastDay>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(forecasts) { forecast ->
            WeatherCard(forecast = forecast)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun WeatherCard(forecast: ForecastDay) {
    val bikeScore = WeatherScoring.calculateBikeRidingScore(forecast.day)
    val isVeryCold = forecast.day.avgtemp_c <= 0.5
    
    val scoreColor = when {
        isVeryCold -> Color(0xFF8B0000) // Dark Red for very cold
        bikeScore >= 65 -> Color(0xFF2E7D32) // Darker green
        bikeScore >= 30 -> Color(0xFFFFA000) // Darker yellow
        else -> Color(0xFFD32F2F) // Darker red
    }
    
    val backgroundColor = when {
        isVeryCold -> Color(0xFF8B0000).copy(alpha = 0.15f)  // More saturated dark red
        bikeScore >= 80 -> Color(0xFF1565C0).copy(alpha = 0.15f)  // More saturated blue
        bikeScore >= 65 -> Color(0xFF1976D2).copy(alpha = 0.15f)  // More saturated lighter blue
        bikeScore >= 30 -> Color(0xFFF57C00).copy(alpha = 0.15f)  // More saturated orange
        else -> Color(0xFFD32F2F).copy(alpha = 0.15f)  // More saturated red
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier
                .background(backgroundColor)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(forecast.date_epoch),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = forecast.day.condition.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Weather details with icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherDetailWithRange(
                        icon = Icons.Rounded.Thermostat,
                        label = "Temp",
                        mainValue = "${forecast.day.avgtemp_c}°C",
                        minValue = "${forecast.day.mintemp_c}°C",
                        maxValue = "${forecast.day.maxtemp_c}°C"
                    )
                    WeatherDetail(
                        icon = Icons.Rounded.Air,
                        label = "Wind",
                        value = "${forecast.day.maxwind_kph} km/h"
                    )
                    WeatherDetail(
                        icon = Icons.Rounded.WaterDrop,
                        label = "Rain",
                        value = "${forecast.day.daily_chance_of_rain}%"
                    )
                }
            }
            
            // Score circle with border
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = scoreColor.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "$bikeScore%",
                        color = scoreColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "for running",
                        color = scoreColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDetail(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun WeatherDetailWithRange(
    icon: ImageVector,
    label: String,
    mainValue: String,
    minValue: String,
    maxValue: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mainValue,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Text(
                text = "↓$minValue",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "↑$maxValue",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}