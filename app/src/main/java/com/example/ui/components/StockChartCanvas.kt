package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BollingerBandPoint
import com.example.data.model.Candle
import com.example.data.model.IndicatorType
import com.example.data.model.MacdPoint
import com.example.data.model.TechnicalCalculator
import com.example.ui.theme.BollingerAreaColor
import com.example.ui.theme.BollingerUpperColor
import com.example.ui.theme.EmaColor
import com.example.ui.theme.MacdLineColor
import com.example.ui.theme.MacdSignalColor
import com.example.ui.theme.RsiColor
import com.example.ui.theme.SmaColor
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockChartCanvas(
    candles: List<Candle>,
    selectedIndicators: Set<IndicatorType>,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading real-time tick chart...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var touchedIndex by remember { mutableStateOf<Int?>(null) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    // Pre-calculate technical indicators
    val smaList = remember(candles, selectedIndicators) {
        if (selectedIndicators.contains(IndicatorType.SMA_20)) TechnicalCalculator.calculateSma(candles, 20) else emptyList()
    }
    val emaList = remember(candles, selectedIndicators) {
        if (selectedIndicators.contains(IndicatorType.EMA_50)) TechnicalCalculator.calculateEma(candles, 50) else emptyList()
    }
    val bbList = remember(candles, selectedIndicators) {
        if (selectedIndicators.contains(IndicatorType.BOLLINGER_BANDS)) TechnicalCalculator.calculateBollingerBands(candles, 20, 2.0) else emptyList()
    }
    val rsiList = remember(candles, selectedIndicators) {
        if (selectedIndicators.contains(IndicatorType.RSI)) TechnicalCalculator.calculateRsi(candles, 14) else emptyList()
    }
    val macdList = remember(candles, selectedIndicators) {
        if (selectedIndicators.contains(IndicatorType.MACD)) TechnicalCalculator.calculateMacd(candles) else emptyList()
    }

    val hasBottomPanel = selectedIndicators.contains(IndicatorType.RSI) || selectedIndicators.contains(IndicatorType.MACD)

    val activeCandle = touchedIndex?.let { idx -> candles.getOrNull(idx) } ?: candles.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("stock_chart_container")
    ) {
        // Dynamic Floating HUD Header (OHLCV + Touch Scrubber)
        if (activeCandle != null) {
            val isBull = activeCandle.close >= activeCandle.open
            val candleColor = if (isBull) StockGreen else StockRed

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "T: ${timeFormat.format(Date(activeCandle.timestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (touchedIndex != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Crosshair",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.2f", activeCandle.close)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = candleColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "O: ${String.format(Locale.getDefault(), "%.1f", activeCandle.open)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "H: ${String.format(Locale.getDefault(), "%.1f", activeCandle.high)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = StockGreen
                    )
                    Text(
                        text = "L: ${String.format(Locale.getDefault(), "%.1f", activeCandle.low)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = StockRed
                    )
                    Text(
                        text = "C: ${String.format(Locale.getDefault(), "%.1f", activeCandle.close)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = candleColor
                    )
                    Text(
                        text = "Vol: ${formatVolume(activeCandle.volume)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (hasBottomPanel) 280.dp else 220.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles) {
                        detectTapGestures(
                            onPress = { offset ->
                                val count = candles.size
                                if (count > 0) {
                                    val candleW = size.width / count
                                    val idx = (offset.x / candleW).toInt().coerceIn(0, count - 1)
                                    touchedIndex = idx
                                }
                            },
                            onTap = {
                                touchedIndex = null
                            }
                        )
                    }
                    .pointerInput(candles) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val count = candles.size
                                if (count > 0) {
                                    val candleW = size.width / count
                                    val idx = (offset.x / candleW).toInt().coerceIn(0, count - 1)
                                    touchedIndex = idx
                                }
                            },
                            onDragEnd = {
                                touchedIndex = null
                            },
                            onDragCancel = {
                                touchedIndex = null
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val count = candles.size
                                if (count > 0) {
                                    val candleW = size.width / count
                                    val idx = (change.position.x / candleW).toInt().coerceIn(0, count - 1)
                                    touchedIndex = idx
                                }
                            }
                        )
                    }
            ) {
                val fullWidth = size.width
                val fullHeight = size.height
                val mainHeight = if (hasBottomPanel) fullHeight * 0.70f else fullHeight * 0.85f
                val subPanelHeight = if (hasBottomPanel) fullHeight * 0.28f else 0f
                val subPanelTop = if (hasBottomPanel) fullHeight * 0.72f else fullHeight

                val count = candles.size
                if (count == 0) return@Canvas

                // Find global min and max prices
                var minPrice = candles.minOf { it.low }
                var maxPrice = candles.maxOf { it.high }

                // Include Bollinger bands in scale if enabled
                if (selectedIndicators.contains(IndicatorType.BOLLINGER_BANDS) && bbList.isNotEmpty()) {
                    val validBB = bbList.filterNotNull()
                    if (validBB.isNotEmpty()) {
                        minPrice = minOf(minPrice, validBB.minOf { it.lower })
                        maxPrice = maxOf(maxPrice, validBB.maxOf { it.upper })
                    }
                }

                val priceRange = (maxPrice - minPrice).coerceAtLeast(0.1)
                val paddedMin = minPrice - priceRange * 0.05
                val paddedMax = maxPrice + priceRange * 0.05
                val paddedRange = paddedMax - paddedMin

                val candleStep = fullWidth / count
                val candleBodyWidth = (candleStep * 0.65f).coerceIn(2.dp.toPx(), 18.dp.toPx())

                // Draw Horizontal Price Grid Lines
                val gridCount = 4
                for (i in 0..gridCount) {
                    val y = mainHeight * (i.toFloat() / gridCount)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(fullWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Bollinger Bands Area & Lines
                if (selectedIndicators.contains(IndicatorType.BOLLINGER_BANDS) && bbList.size == count) {
                    drawBollingerBands(
                        bbList = bbList,
                        candleStep = candleStep,
                        mainHeight = mainHeight,
                        paddedMin = paddedMin,
                        paddedRange = paddedRange
                    )
                }

                // Draw Volume Bars at bottom of main chart if enabled
                if (selectedIndicators.contains(IndicatorType.VOLUME)) {
                    val maxVol = candles.maxOf { it.volume }.coerceAtLeast(1L)
                    val volHeightMax = mainHeight * 0.25f

                    candles.forEachIndexed { i, candle ->
                        val x = i * candleStep + (candleStep / 2)
                        val volHeight = (candle.volume.toFloat() / maxVol) * volHeightMax
                        val color = if (candle.isBullish) StockGreen.copy(alpha = 0.35f) else StockRed.copy(alpha = 0.35f)
                        drawRect(
                            color = color,
                            topLeft = Offset(x - candleBodyWidth / 2, mainHeight - volHeight),
                            size = Size(candleBodyWidth, volHeight)
                        )
                    }
                }

                // Draw Candlesticks (Wick & Body)
                candles.forEachIndexed { i, candle ->
                    val x = i * candleStep + (candleStep / 2)
                    val openY = (mainHeight - ((candle.open - paddedMin) / paddedRange * mainHeight)).toFloat()
                    val closeY = (mainHeight - ((candle.close - paddedMin) / paddedRange * mainHeight)).toFloat()
                    val highY = (mainHeight - ((candle.high - paddedMin) / paddedRange * mainHeight)).toFloat()
                    val lowY = (mainHeight - ((candle.low - paddedMin) / paddedRange * mainHeight)).toFloat()

                    val color = if (candle.isBullish) StockGreen else StockRed

                    // Draw Wick
                    drawLine(
                        color = color,
                        start = Offset(x, highY),
                        end = Offset(x, lowY),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Draw Body
                    val topBody = minOf(openY, closeY)
                    val bodyHeight = maxOf(Math.abs(closeY - openY), 2.dp.toPx())
                    drawRect(
                        color = color,
                        topLeft = Offset(x - candleBodyWidth / 2, topBody),
                        size = Size(candleBodyWidth, bodyHeight)
                    )
                }

                // Draw SMA 20 Line
                if (selectedIndicators.contains(IndicatorType.SMA_20) && smaList.size == count) {
                    drawIndicatorLine(
                        values = smaList,
                        color = SmaColor,
                        candleStep = candleStep,
                        mainHeight = mainHeight,
                        paddedMin = paddedMin,
                        paddedRange = paddedRange
                    )
                }

                // Draw EMA 50 Line
                if (selectedIndicators.contains(IndicatorType.EMA_50) && emaList.size == count) {
                    drawIndicatorLine(
                        values = emaList,
                        color = EmaColor,
                        candleStep = candleStep,
                        mainHeight = mainHeight,
                        paddedMin = paddedMin,
                        paddedRange = paddedRange
                    )
                }

                // Sub-Panel: RSI or MACD
                if (hasBottomPanel) {
                    // Divider between main chart and sub-panel
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, subPanelTop - 4.dp.toPx()),
                        end = Offset(fullWidth, subPanelTop - 4.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )

                    if (selectedIndicators.contains(IndicatorType.RSI) && rsiList.size == count) {
                        drawRsiSubPanel(
                            rsiList = rsiList,
                            candleStep = candleStep,
                            topY = subPanelTop,
                            height = subPanelHeight,
                            width = fullWidth
                        )
                    } else if (selectedIndicators.contains(IndicatorType.MACD) && macdList.size == count) {
                        drawMacdSubPanel(
                            macdList = macdList,
                            candleStep = candleStep,
                            candleWidth = candleBodyWidth,
                            topY = subPanelTop,
                            height = subPanelHeight,
                            width = fullWidth
                        )
                    }
                }

                // Crosshair Guide if touched
                touchedIndex?.let { idx ->
                    if (idx in candles.indices) {
                        val candle = candles[idx]
                        val x = idx * candleStep + (candleStep / 2)
                        val closeY = (mainHeight - ((candle.close - paddedMin) / paddedRange * mainHeight)).toFloat()

                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        // Vertical Crosshair Line
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = Offset(x, 0f),
                            end = Offset(x, fullHeight),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )

                        // Horizontal Crosshair Line
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = Offset(0f, closeY),
                            end = Offset(fullWidth, closeY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )

                        // Dot at close price
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(x, closeY)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawIndicatorLine(
    values: List<Double?>,
    color: Color,
    candleStep: Float,
    mainHeight: Float,
    paddedMin: Double,
    paddedRange: Double
) {
    val path = Path()
    var isStarted = false

    values.forEachIndexed { i, value ->
        if (value != null) {
            val x = i * candleStep + (candleStep / 2)
            val y = (mainHeight - ((value - paddedMin) / paddedRange * mainHeight)).toFloat()
            if (!isStarted) {
                path.moveTo(x, y)
                isStarted = true
            } else {
                path.lineTo(x, y)
            }
        }
    }

    if (isStarted) {
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawBollingerBands(
    bbList: List<BollingerBandPoint?>,
    candleStep: Float,
    mainHeight: Float,
    paddedMin: Double,
    paddedRange: Double
) {
    val upperPath = Path()
    val lowerPath = Path()
    val areaPath = Path()
    var isStarted = false

    val validPoints = bbList.mapIndexedNotNull { index, bb -> if (bb != null) index to bb else null }

    validPoints.forEach { (i, bb) ->
        val x = i * candleStep + (candleStep / 2)
        val upperY = (mainHeight - ((bb.upper - paddedMin) / paddedRange * mainHeight)).toFloat()
        val lowerY = (mainHeight - ((bb.lower - paddedMin) / paddedRange * mainHeight)).toFloat()

        if (!isStarted) {
            upperPath.moveTo(x, upperY)
            lowerPath.moveTo(x, lowerY)
            areaPath.moveTo(x, upperY)
            isStarted = true
        } else {
            upperPath.lineTo(x, upperY)
            lowerPath.lineTo(x, lowerY)
            areaPath.lineTo(x, upperY)
        }
    }

    // Close area polygon
    validPoints.reversed().forEach { (i, bb) ->
        val x = i * candleStep + (candleStep / 2)
        val lowerY = (mainHeight - ((bb.lower - paddedMin) / paddedRange * mainHeight)).toFloat()
        areaPath.lineTo(x, lowerY)
    }
    areaPath.close()

    if (isStarted) {
        drawPath(path = areaPath, color = BollingerAreaColor)
        drawPath(path = upperPath, color = BollingerUpperColor, style = Stroke(width = 1.2.dp.toPx()))
        drawPath(path = lowerPath, color = BollingerUpperColor, style = Stroke(width = 1.2.dp.toPx()))
    }
}

private fun DrawScope.drawRsiSubPanel(
    rsiList: List<Double?>,
    candleStep: Float,
    topY: Float,
    height: Float,
    width: Float
) {
    // 70 and 30 guidelines
    val y70 = topY + height * 0.30f
    val y30 = topY + height * 0.70f
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

    drawLine(color = StockRed.copy(alpha = 0.4f), start = Offset(0f, y70), end = Offset(width, y70), strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
    drawLine(color = StockGreen.copy(alpha = 0.4f), start = Offset(0f, y30), end = Offset(width, y30), strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)

    val path = Path()
    var started = false

    rsiList.forEachIndexed { i, rsi ->
        if (rsi != null) {
            val x = i * candleStep + (candleStep / 2)
            val y = (topY + height * (1f - (rsi.toFloat() / 100f))).coerceIn(topY, topY + height)
            if (!started) {
                path.moveTo(x, y)
                started = true
            } else {
                path.lineTo(x, y)
            }
        }
    }

    if (started) {
        drawPath(path = path, color = RsiColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawMacdSubPanel(
    macdList: List<MacdPoint?>,
    candleStep: Float,
    candleWidth: Float,
    topY: Float,
    height: Float,
    width: Float
) {
    val validMacds = macdList.filterNotNull()
    if (validMacds.isEmpty()) return

    val maxVal = validMacds.maxOf { maxOf(Math.abs(it.macd), Math.abs(it.signal), Math.abs(it.histogram)) }.coerceAtLeast(1.0)
    val midY = topY + (height / 2)

    // Center Zero line
    drawLine(color = Color.Gray.copy(alpha = 0.3f), start = Offset(0f, midY), end = Offset(width, midY), strokeWidth = 1.dp.toPx())

    // Histogram Bars
    macdList.forEachIndexed { i, pt ->
        if (pt != null) {
            val x = i * candleStep + (candleStep / 2)
            val barH = (pt.histogram / maxVal * (height / 2)).toFloat()
            val color = if (pt.histogram >= 0) StockGreen.copy(alpha = 0.7f) else StockRed.copy(alpha = 0.7f)
            if (barH >= 0) {
                drawRect(color = color, topLeft = Offset(x - candleWidth / 2, midY - barH), size = Size(candleWidth, barH))
            } else {
                drawRect(color = color, topLeft = Offset(x - candleWidth / 2, midY), size = Size(candleWidth, -barH))
            }
        }
    }

    // MACD line
    val macdPath = Path()
    val signalPath = Path()
    var started = false

    macdList.forEachIndexed { i, pt ->
        if (pt != null) {
            val x = i * candleStep + (candleStep / 2)
            val my = (midY - (pt.macd / maxVal * (height / 2))).toFloat()
            val sy = (midY - (pt.signal / maxVal * (height / 2))).toFloat()

            if (!started) {
                macdPath.moveTo(x, my)
                signalPath.moveTo(x, sy)
                started = true
            } else {
                macdPath.lineTo(x, my)
                signalPath.lineTo(x, sy)
            }
        }
    }

    if (started) {
        drawPath(path = macdPath, color = MacdLineColor, style = Stroke(width = 1.8.dp.toPx()))
        drawPath(path = signalPath, color = MacdSignalColor, style = Stroke(width = 1.8.dp.toPx()))
    }
}

private fun formatVolume(vol: Long): String {
    return when {
        vol >= 10_000_000 -> String.format(Locale.getDefault(), "%.2f Cr", vol / 10_000_000.0)
        vol >= 100_000 -> String.format(Locale.getDefault(), "%.2f L", vol / 100_000.0)
        vol >= 1_000 -> String.format(Locale.getDefault(), "%.1f K", vol / 1_000.0)
        else -> vol.toString()
    }
}
