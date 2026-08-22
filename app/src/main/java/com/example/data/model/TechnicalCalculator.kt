package com.example.data.model

import kotlin.math.pow
import kotlin.math.sqrt

object TechnicalCalculator {

    fun calculateSma(candles: List<Candle>, period: Int = 20): List<Double?> {
        val result = mutableListOf<Double?>()
        for (i in candles.indices) {
            if (i < period - 1) {
                result.add(null)
            } else {
                val slice = candles.subList(i - period + 1, i + 1)
                val avg = slice.sumOf { it.close } / period
                result.add(avg)
            }
        }
        return result
    }

    fun calculateEma(candles: List<Candle>, period: Int = 50): List<Double?> {
        val result = mutableListOf<Double?>()
        if (candles.isEmpty()) return result
        
        val multiplier = 2.0 / (period + 1)
        var previousEma: Double? = null

        for (i in candles.indices) {
            if (i < period - 1) {
                result.add(null)
            } else if (i == period - 1) {
                val sma = candles.subList(0, period).sumOf { it.close } / period
                previousEma = sma
                result.add(sma)
            } else {
                val currentClose = candles[i].close
                val currentEma = (currentClose - (previousEma ?: currentClose)) * multiplier + (previousEma ?: currentClose)
                previousEma = currentEma
                result.add(currentEma)
            }
        }
        return result
    }

    fun calculateBollingerBands(candles: List<Candle>, period: Int = 20, multiplier: Double = 2.0): List<BollingerBandPoint?> {
        val result = mutableListOf<BollingerBandPoint?>()
        for (i in candles.indices) {
            if (i < period - 1) {
                result.add(null)
            } else {
                val slice = candles.subList(i - period + 1, i + 1)
                val middle = slice.sumOf { it.close } / period
                val variance = slice.sumOf { (it.close - middle).pow(2) } / period
                val stdDev = sqrt(variance)
                val upper = middle + (multiplier * stdDev)
                val lower = middle - (multiplier * stdDev)
                result.add(BollingerBandPoint(upper, middle, lower))
            }
        }
        return result
    }

    fun calculateRsi(candles: List<Candle>, period: Int = 14): List<Double?> {
        val result = mutableListOf<Double?>()
        if (candles.size <= period) {
            return List(candles.size) { null }
        }

        var avgGain = 0.0
        var avgLoss = 0.0

        for (i in 0 until candles.size) {
            if (i == 0) {
                result.add(null)
                continue
            }

            val change = candles[i].close - candles[i - 1].close
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0

            if (i < period) {
                avgGain += gain
                avgLoss += loss
                result.add(null)
            } else if (i == period) {
                avgGain = (avgGain + gain) / period
                avgLoss = (avgLoss + loss) / period
                val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
                val rsi = 100.0 - (100.0 / (1.0 + rs))
                result.add(rsi)
            } else {
                avgGain = (avgGain * (period - 1) + gain) / period
                avgLoss = (avgLoss * (period - 1) + loss) / period
                val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
                val rsi = 100.0 - (100.0 / (1.0 + rs))
                result.add(rsi)
            }
        }
        return result
    }

    fun calculateMacd(candles: List<Candle>): List<MacdPoint?> {
        val ema12 = calculateEma(candles, 12)
        val ema26 = calculateEma(candles, 26)
        val macdLine = mutableListOf<Double?>()

        for (i in candles.indices) {
            val e12 = ema12.getOrNull(i)
            val e26 = ema26.getOrNull(i)
            if (e12 != null && e26 != null) {
                macdLine.add(e12 - e26)
            } else {
                macdLine.add(null)
            }
        }

        // Calculate 9-period Signal line on macdLine
        val signalLine = mutableListOf<Double?>()
        val validMacd = macdLine.mapIndexedNotNull { index, v -> if (v != null) index to v else null }
        
        val result = mutableListOf<MacdPoint?>()
        var prevSignal: Double? = null
        val signalMultiplier = 2.0 / (9 + 1)

        val signalMap = mutableMapOf<Int, Double>()
        var count = 0
        var sum = 0.0

        for ((idx, macdVal) in validMacd) {
            count++
            if (count < 9) {
                sum += macdVal
            } else if (count == 9) {
                sum += macdVal
                val sig = sum / 9.0
                prevSignal = sig
                signalMap[idx] = sig
            } else {
                val sig = (macdVal - (prevSignal ?: macdVal)) * signalMultiplier + (prevSignal ?: macdVal)
                prevSignal = sig
                signalMap[idx] = sig
            }
        }

        for (i in candles.indices) {
            val m = macdLine.getOrNull(i)
            val s = signalMap[i]
            if (m != null && s != null) {
                result.add(MacdPoint(macd = m, signal = s, histogram = m - s))
            } else {
                result.add(null)
            }
        }
        return result
    }
}
