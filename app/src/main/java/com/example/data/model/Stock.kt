package com.example.data.model

data class Stock(
    val symbol: String,
    val name: String,
    val exchange: String = "NSE",
    val currentPrice: Double,
    val prevClose: Double,
    val openPrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Long,
    val isIndex: Boolean = false,
    val lotSize: Int = 1,
    val sector: String = "Equities",
    val lastPriceChange: Double = 0.0 // positive or negative from previous tick
) {
    val pointChange: Double
        get() = currentPrice - prevClose

    val percentageChange: Double
        get() = if (prevClose > 0) (pointChange / prevClose) * 100.0 else 0.0

    val isPositive: Boolean
        get() = pointChange >= 0
}

enum class OrderType {
    MARKET,
    LIMIT,
    STOP_LOSS
}

enum class ProductType(val displayName: String, val leverage: Int) {
    INTRADAY("Intraday (MIS - 5x Margin)", 5),
    DELIVERY("Delivery (CNC / Carry Forward)", 1)
}

enum class OrderStatus {
    EXECUTED,
    PENDING,
    CANCELLED,
    SQUARED_OFF
}

enum class TransactionType {
    CREDIT,
    DEBIT,
    POINTS_DEDUCTED,
    PROFIT_BOOKED,
    LOSS_DEDUCTED
}
