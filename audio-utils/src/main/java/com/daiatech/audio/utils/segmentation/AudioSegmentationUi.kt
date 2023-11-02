@file:Suppress("LocalVariableName")

package com.daiatech.audio.utils.segmentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.daiatech.audio.utils.common.MIN_GRAPH_HEIGHT
import com.daiatech.audio.utils.common.MIN_SPIKE_HEIGHT
import com.daiatech.audio.utils.common.maxSpikePaddingDp
import com.daiatech.audio.utils.common.maxSpikeRadiusDp
import com.daiatech.audio.utils.common.maxSpikeWidthDp
import com.daiatech.audio.utils.common.minSpikePaddingDp
import com.daiatech.audio.utils.common.minSpikeRadiusDp
import com.daiatech.audio.utils.common.minSpikeWidthDp
import com.daiatech.audio.utils.common.models.AmplitudeType
import com.daiatech.audio.utils.common.models.Segment
import com.daiatech.audio.utils.common.models.WaveformAlignment
import com.daiatech.audio.utils.common.toDrawableAmplitudes
import com.daiatech.audio.utils.common.touchTargetSize
import com.daiatech.audio.utils.marker.AudioPlayer
import kotlin.math.abs

@Composable
fun AudioSegmentationUi(
    modifier: Modifier = Modifier,
    audioFilePath: String,
    durationMs: Long,
    amplitudes: List<Int>,


    graphHeight: Dp = MIN_GRAPH_HEIGHT,
    style: DrawStyle = Fill,
    waveformBrush: Brush = SolidColor(Color.White),
    progressBrush: Brush = SolidColor(Color.Blue),
    markerBrush: Brush = SolidColor(Color.Cyan),
    waveformAlignment: WaveformAlignment = WaveformAlignment.Center,
    amplitudeType: AmplitudeType = AmplitudeType.AVG,
    spikeWidth: Dp = 2.dp,
    spikeRadius: Dp = 2.dp,
    spikePadding: Dp = 1.dp,
) {
    var progressMs by remember { mutableLongStateOf(0L) }

    // Added segments on the audio
    val segments = remember { mutableStateListOf<Segment>() }

    // Segment which is being edited currently
    var activeSegment by remember { mutableStateOf<Int?>(null) }

    val _spikeWidth = remember(spikeWidth) {
        spikeWidth.coerceIn(minSpikeWidthDp, maxSpikeWidthDp)
    }
    val _spikePadding = remember(spikePadding) {
        spikePadding.coerceIn(minSpikePaddingDp, maxSpikePaddingDp)
    }
    val _spikeRadius = remember(spikeRadius) {
        spikeRadius.coerceIn(minSpikeRadiusDp, maxSpikeRadiusDp)
    }
    val _spikeTotalWidth = remember(spikeWidth, spikePadding) { _spikeWidth + _spikePadding }
    var canvasSize by remember { mutableStateOf(Size(0f, 0f)) }
    var spikes by remember { mutableFloatStateOf(0F) }
    val spikesAmplitudes = remember(amplitudes, spikes, amplitudeType) {
        val maxHeight = canvasSize.height.coerceAtLeast(MIN_SPIKE_HEIGHT)
        amplitudes.toDrawableAmplitudes(
            amplitudeType = amplitudeType,
            spikes = spikes.toInt(),
            minHeight = MIN_SPIKE_HEIGHT,
            maxHeight = maxHeight
        )
    }

    val durationToPx: (Long) -> Float = remember(canvasSize, durationMs) {
        { canvasSize.width / durationMs.toFloat() * it }
    }

    val pxToDuration: (Float) -> Long = remember(canvasSize, durationMs) {
        { (durationMs.toFloat() / canvasSize.width * it).toLong() }
    }


    Column(
        modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                canvasSize = Size(it.size.width.toFloat(), canvasSize.height)
            }
    ) {
        // Player
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AudioPlayer(
                audioFilePath = audioFilePath,
                durationMs = durationMs,
                onProgressUpdate = { progressMs = it }
            )

        }

        // Main Waveform
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(graphHeight)
                .pointerInput(segments, activeSegment, canvasSize) {
                    detectTapGestures { tappedAt ->
                        val tappedSegmentIdx = segments.indexOfFirst { segment ->
                            val xStart = durationToPx(segment.start)
                            val xEnd = durationToPx(segment.end)
                            tappedAt.x in xStart..xEnd
                        }
                        if (tappedSegmentIdx != -1) {
                            activeSegment = if (activeSegment == tappedSegmentIdx) {
                                null
                            } else {
                                tappedSegmentIdx
                            }
                        }
                    }
                }
                .pointerInput(segments, activeSegment, canvasSize) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.position
                            // If there is a segment to move
                            activeSegment?.let {
                                val segment = segments[it]
                                val xStart = durationToPx(segment.start)
                                val xEnd = durationToPx(segment.end)

                                if (abs(xEnd - change.position.x) <= 10.dp.toPx()) {
                                    val nextSegment = segments.getOrNull(it + 1)
                                    val newXEnd = pxToDuration(change.position.x + dragAmount)
                                        .coerceIn(
                                            segment.start,
                                            nextSegment?.start ?: durationMs
                                        )
                                    segments[it] = Segment(segment.start, newXEnd)
                                }

                                if (abs(xStart - change.position.x) <= touchTargetSize.toPx()) {
                                    val prevSegment = segments.getOrNull(it - 1)
                                    val newXStart = pxToDuration(change.position.x + dragAmount)
                                        .coerceIn(
                                            prevSegment?.end ?: 0,
                                            segment.end,
                                        )
                                    segments[it] = Segment(newXStart, segment.end)
                                }

                                if (change.position.x in (xStart + (touchTargetSize).toPx())..(xEnd - touchTargetSize.toPx())) {
                                    val prevSegment = segments.getOrNull(it - 1)
                                    val nextSegment = segments.getOrNull(it + 1)
                                    val newXStart = pxToDuration(xStart + dragAmount)
                                        .coerceIn(
                                            prevSegment?.end ?: 0,
                                            segment.end
                                        )
                                    val newXEnd = pxToDuration(xEnd + dragAmount)
                                        .coerceIn(
                                            segment.start,
                                            nextSegment?.start ?: durationMs
                                        )
                                    segments[it] = Segment(newXStart, newXEnd)
                                }

                            }
                        }
                    )
                }

        ) {
            canvasSize = size
            spikes = size.width / _spikeTotalWidth.toPx()

            spikesAmplitudes.forEachIndexed { index, amplitude ->
                drawRoundRect(
                    brush = waveformBrush,
                    topLeft = Offset(
                        x = index * _spikeTotalWidth.toPx(),
                        y = when (waveformAlignment) {
                            WaveformAlignment.Top -> 0F
                            WaveformAlignment.Bottom -> size.height - amplitude
                            WaveformAlignment.Center -> size.height / 2F - amplitude / 2F
                        }
                    ),
                    size = Size(
                        width = _spikeWidth.toPx(),
                        height = amplitude
                    ),
                    cornerRadius = CornerRadius(_spikeRadius.toPx(), _spikeRadius.toPx()),
                    style = style
                )
            }

            segments.forEachIndexed { idx, segment ->
                if (activeSegment != idx) {
                    val xStart = size.width / durationMs.toFloat() * segment.start
                    val xEnd = size.width / durationMs.toFloat() * segment.end
                    // draw a window from xStart to xEnd
                    drawRoundRect(
                        brush = SolidColor(Color.Gray.copy(0.6F)),
                        topLeft = Offset(xStart, 0F),
                        size = Size(xEnd - xStart, size.height),
                        style = Stroke(width = spikeWidth.toPx())
                    )
                }
            }

            activeSegment?.let {
                val segment = segments[it]
                val xStart = size.width / durationMs.toFloat() * segment.start
                val xEnd = size.width / durationMs.toFloat() * segment.end
                // draw a window from xStart to xEnd
                drawRoundRect(
                    brush = markerBrush,
                    topLeft = Offset(xStart, 0F),
                    size = Size(xEnd - xStart, size.height),
                    style = Stroke(width = spikeWidth.toPx())
                )
            }

            if (progressMs != 0L) {
                val xCoordinate = durationToPx(progressMs)
                drawLine(
                    brush = progressBrush,
                    start = Offset(xCoordinate, 0F),
                    end = Offset(xCoordinate, size.height),
                    strokeWidth = _spikeWidth.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val lastSegment = segments.lastOrNull()
                    if (lastSegment?.end != durationMs) {
                        val start = lastSegment?.end ?: 0L
                        val end = (start + durationMs / 3).coerceAtMost(durationMs)
                        segments.add(Segment(start, end))
                    }

                }
            ) {
                Text(text = "+ Add Segment")
            }

            activeSegment?.let {
                Button(
                    onClick = {
                        segments.removeAt(it)
                        activeSegment = null
                    }
                ) {
                    Text(text = "- Remove Segment")
                }
            }
        }
    }
}