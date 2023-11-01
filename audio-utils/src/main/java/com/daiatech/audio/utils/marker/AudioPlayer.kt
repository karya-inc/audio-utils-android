package com.daiatech.audio.utils.marker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.daiatech.audio.utils.R
import com.daiatech.audio.utils.common.millisecondsToMmSs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun RowScope.AudioPlayer(
    audioFilePath: String,
    durationMs: Long,
    onProgressUpdate: (Long) -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var progressMs by remember { mutableLongStateOf(0L) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPaused by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            private var timeoutJob: Job? = null
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_ENDED, ExoPlayer.STATE_IDLE -> {
                        timeoutJob?.cancel()
                        progressMs = 0
                    }

                    else -> {}
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                super.onIsPlayingChanged(playing)
                isPlaying = playing
                timeoutJob?.cancel()
                if (playing) {
                    isPaused = false
                    timeoutJob = scope.launch(Dispatchers.Main) {
                        while (isActive) {
                            delay(100)
                            progressMs += 100
                            onProgressUpdate(progressMs)
                            // endMs?.let { if (exoPlayer.currentPosition >= it) exoPlayer.stop() }
                        }
                    }
                }
            }
        }

        exoPlayer.addListener(listener)

        // Cleanup when component is destroyed
        onDispose {
            exoPlayer.release()
        }
    }

    AudioPlayer(
        isPlaying = isPlaying,
        currentPosition = progressMs,
        durationMS = durationMs,
        onPlay = {
            if (!isPaused) {
                val mediaItem = MediaItem.fromUri(audioFilePath)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                // startMs?.let { exoPlayer.seekTo(it) }
            }
            exoPlayer.play()
        },
        onPause = {
            exoPlayer.pause()
            isPaused = true
        }
    )
}

@Composable
private fun RowScope.AudioPlayer(
    isPlaying: Boolean,
    currentPosition: Long,
    durationMS: Long,
    onPlay: () -> Unit,
    onPause: () -> Unit
) {
    val progress = if (durationMS == 0L) 0f else currentPosition.toFloat().div(durationMS)
    Icon(
        painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
        contentDescription = "play",
        modifier = Modifier
            .size(48.dp)
            .clickable { if (isPlaying) onPause() else onPlay() },
        tint = MaterialTheme.colorScheme.primary
    )
    LinearProgressIndicator(progress = progress, modifier = Modifier.weight(1f))
    Text(
        text = "\t${millisecondsToMmSs(currentPosition)}/${millisecondsToMmSs(durationMS)}\t",
        color = MaterialTheme.colorScheme.inversePrimary
    )
}
