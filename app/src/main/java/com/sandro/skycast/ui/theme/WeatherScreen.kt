package com.sandro.skycast.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    var cityName by remember { mutableStateOf("") }
    val weather = viewModel.citiesWeather[cityName]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = cityName,
            onValueChange = { cityName = it },
            label = { Text("Enter City") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.fetchWeather(cityName) },
            modifier = Modifier.padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B59C3))
        ) {
            Text("SEARCH")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (weather != null) {
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B59C3))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = weather.cityName,
                        style = TextStyle(color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = viewModel.formatTemp(weather.main.temp),
                        style = TextStyle(color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        } else if (viewModel.isRefreshing) {
            CircularProgressIndicator(color = Color(0xFF2B59C3))
        }
    }
}
