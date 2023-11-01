package com.thedroiddiv.audio.utils.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.daiatech.audio.utils.marker.AudioMarkerUi
import com.thedroiddiv.audio.utils.AudioManager

@Composable
fun Marker(audioFilePath: String) {
    val markers = remember { mutableStateListOf<Float>() }

    Column(Modifier.fillMaxSize()) {
        val context = LocalContext.current

        Spacer(modifier = Modifier.height(12.dp))
        var amplitudes by remember { mutableStateOf(listOf<Int>()) }
        var durationMs by remember { mutableLongStateOf(0L) }
        LaunchedEffect(Unit) {
            val (amp, dur) = AudioManager.getAmplitudes(context, audioFilePath)
            amplitudes = amp
            durationMs = dur
        }
        AudioMarkerUi(
            audioFilePath = audioFilePath,
            durationMs = durationMs,
            amplitudes = amplitudes,
            markers = markers,
            addMarker = { marker ->
                markers.add(marker)
            },
            removeMarker = { idx ->
                markers.removeAt(idx)
            }
        )
    }
}