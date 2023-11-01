@file:Suppress("LocalVariableName")

package com.daiatech.audio.utils.waveform

import android.view.MotionEvent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.daiatech.audio.utils.common.MAX_PROGRESS
import com.daiatech.audio.utils.common.MIN_PROGRESS
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AmplitudeBarGraph(
    modifier: Modifier = Modifier,
    style: DrawStyle = Fill,
    waveformBrush: Brush = SolidColor(Color.White),
    progressBrush: Brush = SolidColor(Color.Blue),
    waveformAlignment: WaveformAlignment = WaveformAlignment.Center,
    amplitudeType: AmplitudeType = AmplitudeType.AVG,
    onProgressChangeFinished: (() -> Unit)? = null,
    spikeAnimationSpec: AnimationSpec<Float> = tween(500),
    spikeWidth: Dp = 4.dp,
    spikeRadius: Dp = 2.dp,
    spikePadding: Dp = 1.dp,
    progress: Float = 0F,
    amplitudes: List<Int>,
    onProgressChange: (Float) -> Unit
) {
    val _progress = remember(progress) { progress.coerceIn(MIN_PROGRESS, MAX_PROGRESS) }
    val _spikeWidth = remember(spikeWidth) { spikeWidth.coerceIn(minSpikeWidthDp, maxSpikeWidthDp) }
    val _spikePadding = remember(spikePadding) { spikePadding.coerceIn(minSpikePaddingDp, maxSpikePaddingDp) }
    val _spikeRadius = remember(spikeRadius) { spikeRadius.coerceIn(minSpikeRadiusDp, maxSpikeRadiusDp) }
    val _spikeTotalWidth = remember(spikeWidth, spikePadding) { _spikeWidth + _spikePadding }
    var canvasSize by remember { mutableStateOf(Size(0f, 0f)) }
    var spikes by remember { mutableStateOf(0F) }
    val spikesAmplitudes = remember(amplitudes, spikes, amplitudeType) {
        amplitudes.toDrawableAmplitudes(
            amplitudeType = amplitudeType,
            spikes = spikes.toInt(),
            minHeight = MIN_SPIKE_HEIGHT,
            maxHeight = canvasSize.height.coerceAtLeast(MIN_SPIKE_HEIGHT)
        )
    }.map { animateFloatAsState(it, spikeAnimationSpec, label = "spike amplitudes").value }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(48.dp)
            .pointerInteropFilter {
                return@pointerInteropFilter when (it.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (it.x in 0F..canvasSize.width) {
                            onProgressChange(it.x / canvasSize.width)
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        onProgressChangeFinished?.invoke()
                        true
                    }
                    else -> false
                }
            }
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

            if (_progress != 0F) {
                drawRoundRect(
                    brush = progressBrush,
                    size = Size(
                        width = 2.dp.toPx(),
                        height = size.height
                    ),
                    topLeft = Offset(
                        x = _progress * size.width,
                        y = 0f
                    )
                )
            }
        }
    }
}
