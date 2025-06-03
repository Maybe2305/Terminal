package com.may.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.may.terminal.data.Bar
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun Terminal(bars: List<Bar>) {

    var terminalState by rememberTerminalState(bars)

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val visibleBarsCount = (terminalState.visibleBarsCount / zoomChange)
            .roundToInt()
            .coerceIn(MIN_VISIBLE_BARS_COUNT, bars.size)

        val scrolledBy = (terminalState.scrolledBy + panChange.x)
            .coerceIn(0f, bars.size * terminalState.barWidth - terminalState.terminalWidth)

        terminalState = terminalState.copy(
            visibleBarsCount = visibleBarsCount,
            scrolledBy = scrolledBy
        )

    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 32.dp, bottom = 32.dp)
            .transformable(transformableState)
            .onSizeChanged {
                terminalState = terminalState.copy(terminalWidth = it.width.toFloat())
            }
    ) {

        val max = terminalState.visibleBars.maxOf { it.high }
        val min = terminalState.visibleBars.minOf { it.low }
        val pxPerPoint = size.height / (max - min)
        translate(left = terminalState.scrolledBy) {
            bars.forEachIndexed { index, bar ->
                val offsetX = size.width - index * terminalState.barWidth
                drawLine(
                    color = Color.White,
                    start = Offset(x = offsetX, y = size.height - ((bar.low - min) * pxPerPoint)),
                    end = Offset(x = offsetX, y = size.height - ((bar.high - min) * pxPerPoint)),
                    strokeWidth = 2f
                )

                drawLine(
                    color = if (bar.open < bar.close) Color.Green else Color.Red,
                    start = Offset(x = offsetX, y = size.height - ((bar.open - min) * pxPerPoint)),
                    end = Offset(x = offsetX, y = size.height - ((bar.close - min) * pxPerPoint)),
                    strokeWidth = terminalState.barWidth / 2
                )
            }
        }

        bars.firstOrNull()?.let {
            drawPrices(
                min = min,
                pxPerPoint = pxPerPoint,
                lastPrice = it.close,
                textMeasurer = textMeasurer,
                max = max
            )
        }
    }
}

private fun DrawScope.drawPrices(
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float,
    textMeasurer: TextMeasurer
) {
    // max

    val maxPriceOffsetY = 0f

    drawDashLine(
        start = Offset(x = 0f, y = maxPriceOffsetY),
        end = Offset(x = size.width, y = maxPriceOffsetY),
    )

    drawTextPrice(
        textMeasurer = textMeasurer,
        price = max,
        offsetY = maxPriceOffsetY
    )

    // last price

    val lastPriceOffsetY = size.height - ((lastPrice - min) * pxPerPoint)

    drawDashLine(
        start = Offset(x = 0f, y = lastPriceOffsetY),
        end = Offset(x = size.width, y = lastPriceOffsetY),
    )

    drawTextPrice(
        textMeasurer = textMeasurer,
        price = lastPrice,
        offsetY = lastPriceOffsetY
    )

    // min
    val minPriceOffsetY = size.height
    drawDashLine(
        start = Offset(x = 0f, y = minPriceOffsetY),
        end = Offset(x = size.width, y = minPriceOffsetY),
    )

    drawTextPrice(
        textMeasurer = textMeasurer,
        price = min,
        offsetY = minPriceOffsetY
    )
}

private fun DrawScope.drawTextPrice(
    textMeasurer: TextMeasurer,
    price: Float,
    offsetY: Float
) {
    val textLayoutResult = textMeasurer.measure(
        text = price.toString(),
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )

    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(size.width - textLayoutResult.size.width, y = offsetY)
    )
}

private fun DrawScope.drawDashLine(
    color: Color = Color.White,
    start: Offset,
    end: Offset,
    strokeWidth: Float = 1f
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )
}