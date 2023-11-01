package com.thedroiddiv.audio.utils.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun HomeScreen(
    navigateToAmplitude: () -> Unit,
    navigateToMarker: () -> Unit,
    navigateToSegmentation: () -> Unit,
) {
    var audioFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val audioPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val file = File(context.filesDir, "temp.wav")
            if (uri != null) {
                context.contentResolver.openInputStream(uri)?.run {
                    val outputStream = FileOutputStream(file)
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int

                    while (this.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.close()
                    this.close()
                }
                audioFilePath = file.path
            }
        }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (audioFilePath == null) {
            // Pick audio
            Button(onClick = { audioPicker.launch("audio/*") }) {
                Text(text = "Pick Audio")
            }
        } else {

            Button(onClick = navigateToAmplitude) {
                Text(text = "Amplitude Bar Graph")
            }

            Button(onClick = navigateToMarker) {
                Text(text = "Audio Marker")
            }

            Button(onClick = navigateToSegmentation) {
                Text(text = "Audio Segmentation")
            }
        }
    }


}
