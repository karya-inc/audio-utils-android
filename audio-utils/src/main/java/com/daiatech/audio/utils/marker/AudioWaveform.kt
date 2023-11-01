@file:Suppress("LocalVariableName")

package com.daiatech.audio.utils.marker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import com.daiatech.audio.utils.common.MIN_SPIKE_HEIGHT
import com.daiatech.audio.utils.common.maxSpikePaddingDp
import com.daiatech.audio.utils.common.maxSpikeRadiusDp
import com.daiatech.audio.utils.common.maxSpikeWidthDp
import com.daiatech.audio.utils.common.minSpikePaddingDp
import com.daiatech.audio.utils.common.minSpikeRadiusDp
import com.daiatech.audio.utils.common.minSpikeWidthDp
import com.daiatech.audio.utils.common.models.AmplitudeType
import com.daiatech.audio.utils.common.models.WaveformAlignment
import com.daiatech.audio.utils.common.toDrawableAmplitudes
@Composable
internal fun AudioWaveform(
    modifier: Modifier,
    height: Dp,
    style: DrawStyle,
    waveformBrush: Brush,
    markerBrush: Brush,
    waveformAlignment: WaveformAlignment,
    amplitudeType: AmplitudeType,
    spikeWidth: Dp,
    spikeRadius: Dp,
    spikePadding: Dp,
    amplitudes: List<Int>,

    // extra params
    markers: List<Float>,
    drawOnTop: DrawScope.() -> Unit
) {
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
    var spikes by remember { mutableStateOf(0F) }

    val spikesAmplitudes = remember(amplitudes, spikes, amplitudeType) {
        val maxHeight = canvasSize.height.coerceAtLeast(MIN_SPIKE_HEIGHT)
        amplitudes.toDrawableAmplitudes(
            amplitudeType = amplitudeType,
            spikes = spikes.toInt(),
            minHeight = MIN_SPIKE_HEIGHT,
            maxHeight = maxHeight
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(height)
            .then(modifier)
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

        markers.forEach { loc ->
            val xCoordinate = size.width.times(loc).coerceIn(0F, size.width)
            drawRoundRect(
                brush = markerBrush,
                topLeft = Offset(
                    x = xCoordinate,
                    y = 0f
                ),
                size = Size(
                    width = _spikeWidth.toPx(),
                    height = size.height
                ),
                cornerRadius = CornerRadius(_spikeRadius.toPx(), _spikeRadius.toPx())
            )
        }
        drawOnTop()
    }
}
