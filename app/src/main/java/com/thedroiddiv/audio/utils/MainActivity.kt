package com.thedroiddiv.audio.utils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.daiatech.audio.utils.marker.AudioMarkerUi
import com.thedroiddiv.audio.utils.ui.theme.AudioUtilsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private val audioFilePath = MutableStateFlow<String?>(null)
    private val markers = MutableStateFlow(listOf<Float>())

    private val contentPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val file = File(filesDir, "temp.wav")
            if (uri != null) {
                audioFilePath.update { null }
                contentResolver.openInputStream(uri)?.run {
                    val outputStream = FileOutputStream(file)
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int

                    while (this.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.close()
                    this.close()
                }
            }
            audioFilePath.update { file.path }
            markers.update { listOf() }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioUtilsTheme {
                // A surface container using the 'background' color from the theme
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        val audioFilePath by this@MainActivity.audioFilePath.collectAsState()
                        val markers by this@MainActivity.markers.collectAsState()
                        val context = LocalContext.current
                        audioFilePath?.let {
                            Spacer(modifier = Modifier.height(12.dp))
                            var amplitudes by remember { mutableStateOf(listOf<Int>()) }
                            var durationMs by remember { mutableLongStateOf(0L) }
                            LaunchedEffect(Unit) {
                                val (amp, dur) = AudioManager.getAmplitudes(context, it)
                                amplitudes = amp
                                durationMs = dur
                            }
                            AudioMarkerUi(
                                audioFilePath = it,
                                durationMs = durationMs,
                                amplitudes = amplitudes,
                                markers = markers,
                                addMarker = { marker ->
                                    this@MainActivity.markers.update { m -> m + marker }
                                },
                                removeMarker = { idx ->
                                    this@MainActivity.markers.update { m ->
                                        m.toMutableList().apply { removeAt(idx) }
                                    }
                                }
                            )
                        } ?: run {
                            Button({ contentPicker.launch("audio/*") }) {
                                Text("Pick Audio")
                            }
                        }
                    }
                }
            }
        }
    }
}
