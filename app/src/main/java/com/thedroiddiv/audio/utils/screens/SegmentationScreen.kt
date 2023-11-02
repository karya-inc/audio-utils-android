package com.thedroiddiv.audio.utils.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.daiatech.audio.utils.segmentation.AudioSegmentationUi
import com.thedroiddiv.audio.utils.AudioManager

@Composable
fun SegmentationScreen(audioFilePath: String) {

    val context = LocalContext.current
    var amplitudes by remember { mutableStateOf(listOf<Int>()) }
    var durationMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val (amp, dur) = AudioManager.getAmplitudes(context, audioFilePath)
        amplitudes = amp
        durationMs = dur
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)) {
        AudioSegmentationUi(
            audioFilePath = audioFilePath,
            durationMs = durationMs,
            amplitudes = amplitudes
        )
    }
}