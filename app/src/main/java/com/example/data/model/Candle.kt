package com.example.data.model

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    val isBullish: Boolean get() = close >= open
}

enum class TimeFrame(val label: String, val minutes: Int) {
    TF_1M("1m", 1),
    TF_5M("5m", 5),
    TF_15M("15m", 15),
    TF_1H("1h", 60),
    TF_1D("1D", 1440),
    TF_1W("1W", 10080)
}

enum class IndicatorType(val displayName: String, val shortName: String) {
    SMA_20("Moving Average (SMA 20)", "SMA 20"),
    EMA_50("Exponential MA (EMA 50)", "EMA 50"),
    BOLLINGER_BANDS("Bollinger Bands (20, 2)", "BOLL"),
    RSI("Relative Strength Index (14)", "RSI"),
    MACD("MACD (12, 26, 9)", "MACD"),
    VOLUME("Volume Bars", "VOL")
}

data class BollingerBandPoint(
    val upper: Double,
    val middle: Double,
    val lower: Double
)

data class MacdPoint(
    val macd: Double,
    val signal: Double,
    val histogram: Double
)

data class IndicatorValues(
    val sma20: List<Double?>,
    val ema50: List<Double?>,
    val bollingerBands: List<BollingerBandPoint?>,
    val rsi14: List<Double?>,
    val macd: List<MacdPoint?>
)
