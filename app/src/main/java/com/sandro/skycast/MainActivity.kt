package com.sandro.skycast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.sandro.skycast.data.AppDatabase
import com.sandro.skycast.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
                return WeatherViewModel(database.cityDao()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Makes the content go behind Status and Navigation bars
        enableEdgeToEdge()

        setContent {
            SkyCastTheme {
                SkyCastMainApp(viewModel)
            }
        }
    }
}

@Composable
fun SkyCastMainApp(viewModel: WeatherViewModel) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color.Transparent
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            SkyCastNavGraph(navController, viewModel)
        }
    }
}
