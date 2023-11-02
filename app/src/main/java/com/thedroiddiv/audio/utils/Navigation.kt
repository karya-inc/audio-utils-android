package com.thedroiddiv.audio.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thedroiddiv.audio.utils.screens.HomeScreen
import com.thedroiddiv.audio.utils.screens.Marker
import com.thedroiddiv.audio.utils.screens.SegmentationScreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val dest = currentDestination?.destination?.route
            CenterAlignedTopAppBar(
                title = { Text(text = "Audio Utils") },
                navigationIcon = {
                    if (dest != "home_screen") {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),

            ) {
            NavHost(navController = navController, startDestination = "home_screen") {
                composable("home_screen") {
                    HomeScreen(
                        navigateToAmplitude = { navController.navigate("amplitude_graph_screen") },
                        navigateToMarker = { navController.navigate("audio_marker_screen") },
                        navigateToSegmentation = { navController.navigate("audio_segmentation_screen") }
                    )
                }

                composable("amplitude_graph_screen") {

                }

                composable("audio_marker_screen") {
                    val file = File(context.filesDir, "temp.wav")
                    Marker(audioFilePath = file.path)
                }

                composable("audio_segmentation_screen") {
                    val file = File(context.filesDir, "temp.wav")
                    SegmentationScreen(audioFilePath = file.path)
                }
            }
        }
    }
}
