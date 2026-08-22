package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.DemoAccountEntity
import com.example.data.db.PositionEntity
import com.example.data.db.TradeOrderEntity
import com.example.data.db.WalletTransactionEntity
import com.example.data.db.WatchlistItemEntity
import com.example.data.model.Candle
import com.example.data.model.Stock
import com.example.data.model.TimeFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class TradingRepository(private val database: AppDatabase) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Initial base master list of Indian stocks & indices
    private val baseStocks = listOf(
        Stock(
            symbol = "NIFTY 50",
            name = "Nifty 50 Benchmark Index",
            exchange = "NSE",
            currentPrice = 24852.15,
            prevClose = 24780.40,
            openPrice = 24810.00,
            highPrice = 24920.65,
            lowPrice = 24765.20,
            volume = 485920000L,
            isIndex = true,
            lotSize = 50,
            sector = "Index"
        ),
        Stock(
            symbol = "BANK NIFTY",
            name = "Nifty Bank Index",
            exchange = "NSE",
            currentPrice = 51480.30,
            prevClose = 51220.80,
            openPrice = 51300.00,
            highPrice = 51650.00,
            lowPrice = 51180.00,
            volume = 295400000L,
            isIndex = true,
            lotSize = 15,
            sector = "Index"
        ),
        Stock(
            symbol = "SENSEX",
            name = "BSE SENSEX 30 Index",
            exchange = "BSE",
            currentPrice = 81320.75,
            prevClose = 81100.50,
            openPrice = 81150.00,
            highPrice = 81590.20,
            lowPrice = 81040.10,
            volume = 380120000L,
            isIndex = true,
            lotSize = 10,
            sector = "Index"
        ),
        Stock(
            symbol = "RELIANCE",
            name = "Reliance Industries Ltd",
            exchange = "NSE",
            currentPrice = 2985.40,
            prevClose = 2962.10,
            openPrice = 2970.00,
            highPrice = 3012.00,
            lowPrice = 2955.00,
            volume = 8450120L,
            sector = "Energy & Conglomerate"
        ),
        Stock(
            symbol = "TCS",
            name = "Tata Consultancy Services",
            exchange = "NSE",
            currentPrice = 4192.50,
            prevClose = 4175.00,
            openPrice = 4180.00,
            highPrice = 4220.00,
            lowPrice = 4160.00,
            volume = 3120450L,
            sector = "Information Technology"
        ),
        Stock(
            symbol = "HDFCBANK",
            name = "HDFC Bank Ltd",
            exchange = "NSE",
            currentPrice = 1664.80,
            prevClose = 1652.30,
            openPrice = 1655.00,
            highPrice = 1678.50,
            lowPrice = 1648.00,
            volume = 15420300L,
            sector = "Banking & Finance"
        ),
        Stock(
            symbol = "INFY",
            name = "Infosys Limited",
            exchange = "NSE",
            currentPrice = 1874.25,
            prevClose = 1860.00,
            openPrice = 1865.00,
            highPrice = 1892.00,
            lowPrice = 1852.00,
            volume = 7890120L,
            sector = "Information Technology"
        ),
        Stock(
            symbol = "ICICIBANK",
            name = "ICICI Bank Ltd",
            exchange = "NSE",
            currentPrice = 1242.60,
            prevClose = 1235.00,
            openPrice = 1238.00,
            highPrice = 1255.00,
            lowPrice = 1230.00,
            volume = 12300400L,
            sector = "Banking & Finance"
        ),
        Stock(
            symbol = "TATAMOTORS",
            name = "Tata Motors Ltd",
            exchange = "NSE",
            currentPrice = 1092.15,
            prevClose = 1078.50,
            openPrice = 1082.00,
            highPrice = 1108.00,
            lowPrice = 1075.00,
            volume = 9420500L,
            sector = "Automobile"
        ),
        Stock(
            symbol = "SBIN",
            name = "State Bank of India",
            exchange = "NSE",
            currentPrice = 824.70,
            prevClose = 818.00,
            openPrice = 820.00,
            highPrice = 832.00,
            lowPrice = 815.00,
            volume = 14230000L,
            sector = "Banking & Finance"
        ),
        Stock(
            symbol = "BHARTIARTL",
            name = "Bharti Airtel Ltd",
            exchange = "NSE",
            currentPrice = 1485.60,
            prevClose = 1472.00,
            openPrice = 1475.00,
            highPrice = 1498.00,
            lowPrice = 1468.00,
            volume = 6120000L,
            sector = "Telecommunications"
        ),
        Stock(
            symbol = "ITC",
            name = "ITC Limited",
            exchange = "NSE",
            currentPrice = 496.35,
            prevClose = 492.10,
            openPrice = 493.00,
            highPrice = 501.50,
            lowPrice = 490.00,
            volume = 18450000L,
            sector = "FMCG"
        ),
        Stock(
            symbol = "LT",
            name = "Larsen & Toubro Ltd",
            exchange = "NSE",
            currentPrice = 3624.80,
            prevClose = 3598.00,
            openPrice = 3605.00,
            highPrice = 3655.00,
            lowPrice = 3585.00,
            volume = 2840000L,
            sector = "Infrastructure & Eng."
        ),
        Stock(
            symbol = "BAJFINANCE",
            name = "Bajaj Finance Ltd",
            exchange = "NSE",
            currentPrice = 7150.00,
            prevClose = 7080.00,
            openPrice = 7100.00,
            highPrice = 7240.00,
            lowPrice = 7050.00,
            volume = 1920000L,
            sector = "Financial Services"
        ),
        Stock(
            symbol = "MARUTI",
            name = "Maruti Suzuki India",
            exchange = "NSE",
            currentPrice = 12450.00,
            prevClose = 12380.00,
            openPrice = 12400.00,
            highPrice = 12580.00,
            lowPrice = 12320.00,
            volume = 820000L,
            sector = "Automobile"
        ),
        Stock(
            symbol = "KOTAKBANK",
            name = "Kotak Mahindra Bank",
            exchange = "NSE",
            currentPrice = 1785.40,
            prevClose = 1772.00,
            openPrice = 1775.00,
            highPrice = 1798.00,
            lowPrice = 1765.00,
            volume = 4320000L,
            sector = "Banking & Finance"
        ),
        Stock(
            symbol = "SUNPHARMA",
            name = "Sun Pharma Industries",
            exchange = "NSE",
            currentPrice = 1715.20,
            prevClose = 1702.00,
            openPrice = 1705.00,
            highPrice = 1730.00,
            lowPrice = 1695.00,
            volume = 3650000L,
            sector = "Healthcare & Pharma"
        ),
        Stock(
            symbol = "AXISBANK",
            name = "Axis Bank Ltd",
            exchange = "NSE",
            currentPrice = 1184.60,
            prevClose = 1175.00,
            openPrice = 1178.00,
            highPrice = 1195.00,
            lowPrice = 1170.00,
            volume = 8740000L,
            sector = "Banking & Finance"
        ),
        Stock(
            symbol = "TITAN",
            name = "Titan Company Ltd",
            exchange = "NSE",
            currentPrice = 3542.10,
            prevClose = 3520.00,
            openPrice = 3525.00,
            highPrice = 3570.00,
            lowPrice = 3505.00,
            volume = 2150000L,
            sector = "Consumer Discretionary"
        ),
        Stock(
            symbol = "WIPRO",
            name = "Wipro Limited",
            exchange = "NSE",
            currentPrice = 542.80,
            prevClose = 538.50,
            openPrice = 540.00,
            highPrice = 548.00,
            lowPrice = 535.00,
            volume = 9820000L,
            sector = "Information Technology"
        )
    )

    private val _stocksState = MutableStateFlow<List<Stock>>(baseStocks)
    val stocksState: StateFlow<List<Stock>> = _stocksState.asStateFlow()

    // Candlestick history cache: symbol -> (timeframe -> list of candles)
    private val candleCache = mutableMapOf<String, MutableMap<TimeFrame, MutableList<Candle>>>()

    init {
        // Generate historical initial candles for all stocks across all timeframes
        baseStocks.forEach { stock ->
            val stockMap = mutableMapOf<TimeFrame, MutableList<Candle>>()
            TimeFrame.values().forEach { tf ->
                stockMap[tf] = generateInitialCandles(stock.currentPrice, tf)
            }
            candleCache[stock.symbol] = stockMap
        }

        // Start continuous real-time market tick engine
        startLiveMarketTickEngine()
    }

    private fun generateInitialCandles(currentPrice: Double, timeFrame: TimeFrame): MutableList<Candle> {
        val count = 60
        val list = mutableListOf<Candle>()
        val intervalMs = timeFrame.minutes * 60 * 1000L
        var price = currentPrice * (1.0 - (count * 0.002))
        val now = System.currentTimeMillis()

        for (i in 0 until count) {
            val candleTime = now - ((count - i) * intervalMs)
            val volatility = currentPrice * 0.004
            val delta = (Random.nextDouble() - 0.48) * volatility
            val open = price
            val close = (open + delta).coerceAtLeast(1.0)
            val high = maxOf(open, close) + Random.nextDouble() * (volatility * 0.6)
            val low = minOf(open, close) - Random.nextDouble() * (volatility * 0.6)
            val volume = (Random.nextLong(10000, 250000))

            list.add(
                Candle(
                    timestamp = candleTime,
                    open = Math.round(open * 100.0) / 100.0,
                    high = Math.round(high * 100.0) / 100.0,
                    low = Math.round(low * 100.0) / 100.0,
                    close = Math.round(close * 100.0) / 100.0,
                    volume = volume
                )
            )
            price = close
        }
        return list
    }

    private fun startLiveMarketTickEngine() {
        scope.launch {
            while (true) {
                delay(1000L) // 1 second real-time tick interval
                tickMarketPrices()
            }
        }
    }

    private suspend fun tickMarketPrices() {
        val currentList = _stocksState.value.toMutableList()
        val updatedList = mutableListOf<Stock>()

        for (stock in currentList) {
            // Realistic Indian market tick volatility
            val tickVolPercent = if (stock.isIndex) 0.0006 else 0.0012
            val randomFactor = (Random.nextDouble() - 0.495)
            val tickAmount = stock.currentPrice * tickVolPercent * randomFactor
            val newPrice = Math.round((stock.currentPrice + tickAmount) * 100.0) / 100.0
            val newHigh = maxOf(stock.highPrice, newPrice)
            val newLow = minOf(stock.lowPrice, newPrice)
            val newVolume = stock.volume + Random.nextLong(200, 5000)

            val updatedStock = stock.copy(
                currentPrice = newPrice,
                highPrice = newHigh,
                lowPrice = newLow,
                volume = newVolume,
                lastPriceChange = tickAmount
            )
            updatedList.add(updatedStock)

            // Update candle cache for 1m
            val symbolCandles = candleCache[stock.symbol]
            if (symbolCandles != null) {
                TimeFrame.values().forEach { tf ->
                    val candles = symbolCandles[tf]
                    if (candles != null && candles.isNotEmpty()) {
                        val lastCandle = candles.last()
                        val intervalMs = tf.minutes * 60 * 1000L
                        val now = System.currentTimeMillis()

                        if (now - lastCandle.timestamp < intervalMs) {
                            // Update current running candle
                            candles[candles.lastIndex] = lastCandle.copy(
                                high = maxOf(lastCandle.high, newPrice),
                                low = minOf(lastCandle.low, newPrice),
                                close = newPrice,
                                volume = lastCandle.volume + Random.nextLong(50, 500)
                            )
                        } else {
                            // Add new candle and drop oldest to keep max 60 candles
                            if (candles.size >= 80) {
                                candles.removeAt(0)
                            }
                            candles.add(
                                Candle(
                                    timestamp = now,
                                    open = lastCandle.close,
                                    high = maxOf(lastCandle.close, newPrice),
                                    low = minOf(lastCandle.close, newPrice),
                                    close = newPrice,
                                    volume = Random.nextLong(1000, 20000)
                                )
                            )
                        }
                    }
                }
            }
        }

        _stocksState.value = updatedList
    }

    fun getCandlesForStock(symbol: String, timeFrame: TimeFrame): List<Candle> {
        val map = candleCache[symbol] ?: return emptyList()
        val list = map[timeFrame] ?: return emptyList()
        return list.toList()
    }

    // Room DB Observables
    val allOrders: Flow<List<TradeOrderEntity>> = database.tradeOrderDao().getAllOrders()
    val allOrderDates: Flow<List<String>> = database.tradeOrderDao().getAllOrderDates()
    val openPositions: Flow<List<PositionEntity>> = database.positionDao().getOpenPositions()
    val demoAccount: Flow<DemoAccountEntity?> = database.demoAccountDao().getDemoAccount()
    val walletTransactions: Flow<List<WalletTransactionEntity>> = database.walletTransactionDao().getAllTransactions()
    val watchlistItems: Flow<List<WatchlistItemEntity>> = database.watchlistDao().getAllWatchlistItems()

    fun getOrdersByDate(dateString: String): Flow<List<TradeOrderEntity>> {
        return database.tradeOrderDao().getOrdersByDate(dateString)
    }

    // Watchlist Actions
    suspend fun toggleWatchlist(symbol: String, isAdded: Boolean) {
        if (isAdded) {
            database.watchlistDao().addToWatchlist(WatchlistItemEntity(symbol = symbol))
        } else {
            database.watchlistDao().removeFromWatchlist(symbol)
        }
    }

    suspend fun isInWatchlist(symbol: String): Boolean {
        val list = database.watchlistDao().getAllSymbolsSync()
        return list.contains(symbol)
    }

    // Trading Operations
    suspend fun executeOrder(
        stock: Stock,
        side: String, // "BUY" or "SELL"
        orderType: String, // "MARKET", "LIMIT", "STOP_LOSS"
        productType: String, // "INTRADAY" or "DELIVERY"
        quantity: Int,
        limitPrice: Double = 0.0,
        stopLossPrice: Double = 0.0,
        targetPrice: Double = 0.0
    ): Result<String> {
        val executionPrice = if (orderType == "LIMIT" && limitPrice > 0) limitPrice else stock.currentPrice
        val leverage = if (productType == "INTRADAY") 5 else 1
        val totalOrderValue = executionPrice * quantity
        val requiredMargin = totalOrderValue / leverage
        val pointsCharges = 20.0 // Flat ₹20 demo brokerage / points per trade

        val account = database.demoAccountDao().getDemoAccountSync() ?: DemoAccountEntity()
        val availableMargin = account.balance - account.marginUtilized

        if (availableMargin < (requiredMargin + pointsCharges)) {
            return Result.failure(Exception("Insufficient demo balance. Required: ₹${String.format(Locale.getDefault(), "%,.2f", requiredMargin + pointsCharges)}"))
        }

        val todayDate = dateFormat.format(Date())

        // 1. Insert Order
        val order = TradeOrderEntity(
            symbol = stock.symbol,
            stockName = stock.name,
            side = side,
            orderType = orderType,
            productType = productType,
            quantity = quantity,
            executionPrice = executionPrice,
            limitPrice = limitPrice,
            stopLossPrice = stopLossPrice,
            targetPrice = targetPrice,
            status = "EXECUTED",
            pointsDeducted = pointsCharges,
            dateString = todayDate
        )
        database.tradeOrderDao().insertOrder(order)

        // 2. Open Position
        val position = PositionEntity(
            symbol = stock.symbol,
            stockName = stock.name,
            side = side,
            productType = productType,
            quantity = quantity,
            entryPrice = executionPrice,
            currentPrice = executionPrice,
            stopLossPrice = stopLossPrice,
            targetPrice = targetPrice,
            pointsDeducted = pointsCharges,
            isOpen = true,
            realizedPnL = 0.0,
            dateString = todayDate
        )
        database.positionDao().insertPosition(position)

        // 3. Update Demo Account
        val updatedAccount = account.copy(
            balance = account.balance - pointsCharges, // Deduct brokerage points
            marginUtilized = account.marginUtilized + requiredMargin,
            totalPointsDeducted = account.totalPointsDeducted + pointsCharges
        )
        database.demoAccountDao().insertOrUpdate(updatedAccount)

        // 4. Log Wallet Transaction for Brokerage Points Deducted
        database.walletTransactionDao().insertTransaction(
            WalletTransactionEntity(
                type = "POINTS_DEDUCTED",
                amount = -pointsCharges,
                points = pointsCharges,
                description = "Brokerage & Exchange charges for $side $quantity ${stock.symbol}",
                dateString = todayDate
            )
        )

        return Result.success("Order Executed: $side $quantity ${stock.symbol} at ₹$executionPrice")
    }

    suspend fun squareOffPosition(position: PositionEntity, currentMarketPrice: Double): Result<String> {
        val todayDate = dateFormat.format(Date())
        val leverage = if (position.productType == "INTRADAY") 5 else 1
        val requiredMargin = (position.entryPrice * position.quantity) / leverage
        val pointsCharges = 20.0 // Square-off points deduction

        // Calculate Realized P&L
        val pnl = if (position.side == "BUY") {
            (currentMarketPrice - position.entryPrice) * position.quantity
        } else {
            (position.entryPrice - currentMarketPrice) * position.quantity
        }

        // 1. Update Position
        val closedPosition = position.copy(
            isOpen = false,
            currentPrice = currentMarketPrice,
            realizedPnL = pnl,
            closedTimestamp = System.currentTimeMillis()
        )
        database.positionDao().updatePosition(closedPosition)

        // 2. Insert Exit Order
        val exitSide = if (position.side == "BUY") "SELL" else "BUY"
        database.tradeOrderDao().insertOrder(
            TradeOrderEntity(
                symbol = position.symbol,
                stockName = position.stockName,
                side = exitSide,
                orderType = "MARKET",
                productType = position.productType,
                quantity = position.quantity,
                executionPrice = currentMarketPrice,
                status = "EXECUTED",
                pointsDeducted = pointsCharges,
                dateString = todayDate
            )
        )

        // 3. Update Demo Account: release margin, apply P&L and charges
        val account = database.demoAccountDao().getDemoAccountSync() ?: DemoAccountEntity()
        val newBalance = account.balance + pnl - pointsCharges
        val newMargin = (account.marginUtilized - requiredMargin).coerceAtLeast(0.0)
        val newTotalPnL = account.totalRealizedPnL + pnl
        val newPointsDeducted = account.totalPointsDeducted + pointsCharges

        database.demoAccountDao().insertOrUpdate(
            account.copy(
                balance = newBalance,
                marginUtilized = newMargin,
                totalRealizedPnL = newTotalPnL,
                totalPointsDeducted = newPointsDeducted
            )
        )

        // 4. Log Wallet Transaction
        val pnlType = if (pnl >= 0) "PROFIT_BOOKED" else "LOSS_DEDUCTED"
        database.walletTransactionDao().insertTransaction(
            WalletTransactionEntity(
                type = pnlType,
                amount = pnl,
                points = pointsCharges,
                description = "Position Closed: ${position.symbol} (P&L: ${if (pnl >= 0) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%,.2f", Math.abs(pnl))})",
                dateString = todayDate
            )
        )

        return Result.success("Squared off ${position.symbol}: P&L ${if (pnl >= 0) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%,.2f", Math.abs(pnl))}")
    }

    // Demo Funds: Credit & Debit
    suspend fun addDemoFunds(amount: Double, note: String = "Manual Demo Funds Deposit"): Result<String> {
        if (amount <= 0) return Result.failure(Exception("Amount must be greater than zero"))
        val account = database.demoAccountDao().getDemoAccountSync() ?: DemoAccountEntity()
        val updated = account.copy(balance = account.balance + amount)
        database.demoAccountDao().insertOrUpdate(updated)

        val todayDate = dateFormat.format(Date())
        database.walletTransactionDao().insertTransaction(
            WalletTransactionEntity(
                type = "CREDIT",
                amount = amount,
                points = 0.0,
                description = note,
                dateString = todayDate
            )
        )
        return Result.success("Demo Funds Credited: ₹${String.format(Locale.getDefault(), "%,.2f", amount)}")
    }

    suspend fun withdrawDemoFunds(amount: Double, note: String = "Demo Funds Withdrawal"): Result<String> {
        if (amount <= 0) return Result.failure(Exception("Amount must be greater than zero"))
        val account = database.demoAccountDao().getDemoAccountSync() ?: DemoAccountEntity()
        val available = account.balance - account.marginUtilized
        if (available < amount) {
            return Result.failure(Exception("Insufficient available balance. Available: ₹${String.format(Locale.getDefault(), "%,.2f", available)}"))
        }

        val updated = account.copy(balance = account.balance - amount)
        database.demoAccountDao().insertOrUpdate(updated)

        val todayDate = dateFormat.format(Date())
        database.walletTransactionDao().insertTransaction(
            WalletTransactionEntity(
                type = "DEBIT",
                amount = -amount,
                points = 0.0,
                description = note,
                dateString = todayDate
            )
        )
        return Result.success("Demo Funds Withdrawn: ₹${String.format(Locale.getDefault(), "%,.2f", amount)}")
    }

    suspend fun resetDemoAccount(): Result<String> {
        database.positionDao().clearPositions()
        database.tradeOrderDao().clearOrders()
        database.walletTransactionDao().clearTransactions()
        database.demoAccountDao().insertOrUpdate(
            DemoAccountEntity(
                id = 1,
                balance = 1000000.0,
                marginUtilized = 0.0,
                totalRealizedPnL = 0.0,
                totalPointsDeducted = 0.0
            )
        )
        val todayDate = dateFormat.format(Date())
        database.walletTransactionDao().insertTransaction(
            WalletTransactionEntity(
                type = "CREDIT",
                amount = 1000000.0,
                points = 0.0,
                description = "Account Reset - Initial ₹10,00,000 Demo Capital",
                dateString = todayDate
            )
        )
        return Result.success("Demo Trading Account Reset to ₹10,00,000")
    }
}
