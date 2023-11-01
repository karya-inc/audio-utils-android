package com.daiatech.audio.utils.waveform

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    currentAmp: Float,
    noOfPoints: Int = 200,
    maxAmplitude: Float = 12000f,
    strokeWidth: Dp = 2.dp,
    strokeColor: Color = Color.White
) {
    var _maxAmplitude by remember { mutableStateOf(maxAmplitude) }
    LaunchedEffect(key1 = currentAmp) {
        if (currentAmp > _maxAmplitude) {
            _maxAmplitude = currentAmp
        }
    }

    var amplitudes by remember { mutableStateOf(List(noOfPoints) { 1f }) }

    val animationSpec = remember { tween<Float>(durationMillis = 200) }
    val animateFloat by animateFloatAsState(
        targetValue = currentAmp,
        animationSpec = animationSpec,
        label = ""
    )
    amplitudes = amplitudes.takeLast(noOfPoints - 1) + animateFloat

    AmplitudeGraph(modifier, amplitudes, _maxAmplitude, strokeWidth, strokeColor)
}

/** Amplitudes should be normalized between 0 and 1 */
@Composable
private fun AmplitudeGraph(
    modifier: Modifier = Modifier,
    amplitudeValues: List<Float>,
    maxAmplitude: Float,
    strokeWidth: Dp = 2.dp,
    strokeColor: Color = Color.White
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val graphHeight = size.height - 4.dp.toPx()
        val graphWidth = size.width - 4.dp.toPx()

        val xStep = graphWidth / amplitudeValues.size

        val path = Path()

        amplitudeValues.normalized(size.height, 0f, maxAmplitude).forEachIndexed { idx, yCoordinate ->
            val x = 10.dp.toPx() + idx * xStep
            val y = graphHeight - yCoordinate
            if (idx == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}

private fun List<Float>.normalized(max: Float, min: Float, lMax: Float): List<Float> {
    val lMin = this.min()

    // If the list min == max, then return as it is
    if (lMax == lMin) return this

    /**
     * y = mx + c
     * m = (y2-y1) / (x2 - x1)
     */
    val slope = (max - min) / (lMax - lMin)
    val yIntercept = max - (slope * lMax)

    val y: (Float) -> Float = { x ->
        slope * x + yIntercept + 1f
    }

    return map(y)
}

@Preview
@Composable
fun GraphPrev() {
    AmplitudeGraph(
        amplitudeValues = listOf(200f, 30f, 45f, 5f, 16f),
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp),
        maxAmplitude = 1800f
    )
}
