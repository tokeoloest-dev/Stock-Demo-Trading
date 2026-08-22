package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class TradeOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val stockName: String,
    val side: String, // "BUY" or "SELL"
    val orderType: String, // "MARKET", "LIMIT", "STOP_LOSS"
    val productType: String, // "INTRADAY" or "DELIVERY"
    val quantity: Int,
    val executionPrice: Double,
    val limitPrice: Double = 0.0,
    val stopLossPrice: Double = 0.0,
    val targetPrice: Double = 0.0,
    val status: String, // "EXECUTED", "CANCELLED", "PENDING"
    val pointsDeducted: Double = 20.0, // demo brokerage/STT in points/Rs
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // "2026-08-21" for date-specific filtering
)

@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val stockName: String,
    val side: String, // "BUY" or "SELL"
    val productType: String, // "INTRADAY" or "DELIVERY"
    val quantity: Int,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLossPrice: Double = 0.0,
    val targetPrice: Double = 0.0,
    val pointsDeducted: Double = 20.0,
    val isOpen: Boolean = true,
    val realizedPnL: Double = 0.0,
    val openedTimestamp: Long = System.currentTimeMillis(),
    val closedTimestamp: Long? = null,
    val dateString: String
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "CREDIT", "DEBIT", "POINTS_DEDUCTED", "PROFIT_BOOKED", "LOSS_DEDUCTED"
    val amount: Double,
    val points: Double = 0.0,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String
)

@Entity(tableName = "demo_account")
data class DemoAccountEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 1000000.0, // Default ₹10,00,000 (10 Lakhs) demo funds
    val marginUtilized: Double = 0.0,
    val totalRealizedPnL: Double = 0.0,
    val totalPointsDeducted: Double = 0.0
)

@Entity(tableName = "watchlist_items")
data class WatchlistItemEntity(
    @PrimaryKey val symbol: String,
    val isCustomAdded: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
