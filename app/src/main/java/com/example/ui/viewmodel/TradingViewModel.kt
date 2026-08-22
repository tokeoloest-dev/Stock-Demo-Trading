package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.DemoAccountEntity
import com.example.data.db.PositionEntity
import com.example.data.db.TradeOrderEntity
import com.example.data.db.WalletTransactionEntity
import com.example.data.model.AppScreen
import com.example.data.model.AppTheme
import com.example.data.model.Candle
import com.example.data.model.IndicatorType
import com.example.data.model.Stock
import com.example.data.model.TimeFrame
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String = "Demo Trader Pro",
    val email: String = "trader@nse-bse.demo",
    val provider: String = "Guest Demo",
    val isLoggedIn: Boolean = false
)

data class ActivePositionWithLivePnL(
    val entity: PositionEntity,
    val liveCurrentPrice: Double,
    val liveUnrealizedPnL: Double,
    val livePointChange: Double,
    val livePercentagePnL: Double
)

class TradingViewModel(
    private val repository: TradingRepository
) : ViewModel() {

    // Screens and Navigation History
    private val _currentScreen = MutableStateFlow(AppScreen.LOADING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _navHistory = mutableListOf<AppScreen>()
    private val _forwardHistory = mutableListOf<AppScreen>()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    // Loading Screen Progress (5 seconds)
    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    private val _loadingStatusText = MutableStateFlow("Connecting to NSE/BSE Feeds...")
    val loadingStatusText: StateFlow<String> = _loadingStatusText.asStateFlow()

    // User Profile
    private val _userProfile = MutableStateFlow(UserProfile(isLoggedIn = false))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Theme and Preferences
    private val _appTheme = MutableStateFlow(AppTheme.SLATE_PRO)
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _pushNotificationsEnabled = MutableStateFlow(true)
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()

    private val _soundEffectsEnabled = MutableStateFlow(true)
    val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Stocks and Watchlist
    val stocks: StateFlow<List<Stock>> = repository.stocksState

    val watchlistSymbols: StateFlow<Set<String>> = repository.watchlistItems
        .combine(MutableStateFlow(Unit)) { items, _ ->
            items.map { it.symbol }.toSet()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            setOf("NIFTY 50", "BANK NIFTY", "SENSEX", "RELIANCE", "TCS")
        )

    private val _selectedStock = MutableStateFlow<Stock?>(null)
    val selectedStock: StateFlow<Stock?> = _selectedStock.asStateFlow()

    private val _selectedTimeFrame = MutableStateFlow(TimeFrame.TF_5M)
    val selectedTimeFrame: StateFlow<TimeFrame> = _selectedTimeFrame.asStateFlow()

    private val _selectedIndicators = MutableStateFlow(setOf(IndicatorType.SMA_20, IndicatorType.VOLUME))
    val selectedIndicators: StateFlow<Set<IndicatorType>> = _selectedIndicators.asStateFlow()

    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

    // Active Positions with Live PnL
    val activePositionsWithPnL: StateFlow<List<ActivePositionWithLivePnL>> = combine(
        repository.openPositions,
        repository.stocksState
    ) { positions, stockList ->
        val priceMap = stockList.associateBy({ it.symbol }, { it.currentPrice })
        positions.map { pos ->
            val livePrice = priceMap[pos.symbol] ?: pos.currentPrice
            val pnl = if (pos.side == "BUY") {
                (livePrice - pos.entryPrice) * pos.quantity
            } else {
                (pos.entryPrice - livePrice) * pos.quantity
            }
            val pointDiff = if (pos.side == "BUY") (livePrice - pos.entryPrice) else (pos.entryPrice - livePrice)
            val pctPnL = if (pos.entryPrice > 0) (pointDiff / pos.entryPrice) * 100.0 else 0.0

            ActivePositionWithLivePnL(
                entity = pos,
                liveCurrentPrice = livePrice,
                liveUnrealizedPnL = pnl,
                livePointChange = pointDiff,
                livePercentagePnL = pctPnL
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMarginUtilized: StateFlow<Double> = repository.openPositions
        .combine(MutableStateFlow(Unit)) { positions: List<PositionEntity>, _ ->
            positions.sumOf { pos ->
                val leverage = if (pos.productType == "INTRADAY") 5.0 else 1.0
                (pos.entryPrice * pos.quantity) / leverage
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Orders History
    val orders: StateFlow<List<TradeOrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrderDates: StateFlow<List<String>> = repository.allOrderDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedOrderDate = MutableStateFlow<String?>(null)
    val selectedOrderDate: StateFlow<String?> = _selectedOrderDate.asStateFlow()

    // Demo Account & Transactions
    val demoAccount: StateFlow<DemoAccountEntity> = repository.demoAccount
        .combine(MutableStateFlow(Unit)) { acc, _ ->
            acc ?: DemoAccountEntity()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DemoAccountEntity())

    val transactions: StateFlow<List<WalletTransactionEntity>> = repository.walletTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        startLoadingTimer()
        observeSelectedStockCandles()
    }

    private fun startLoadingTimer() {
        viewModelScope.launch {
            val totalSteps = 50
            for (i in 1..totalSteps) {
                delay(100L) // 50 * 100ms = 5000ms (5 seconds)
                _loadingProgress.value = i / 50f
                when (i) {
                    10 -> _loadingStatusText.value = "Connected to NSE/BSE Tick Stream (18ms)..."
                    25 -> _loadingStatusText.value = "Calibrating Nifty 50, Sensex & Bank Nifty..."
                    40 -> _loadingStatusText.value = "Initializing ₹10,00,000 Demo Capital..."
                    48 -> _loadingStatusText.value = "Simulator Ready!"
                }
            }
            _currentScreen.value = AppScreen.LOGIN
        }
    }

    private fun observeSelectedStockCandles() {
        viewModelScope.launch {
            combine(_selectedStock, _selectedTimeFrame, repository.stocksState) { stock, tf, _ ->
                if (stock != null) {
                    repository.getCandlesForStock(stock.symbol, tf)
                } else {
                    emptyList()
                }
            }.collect { candleList ->
                _candles.value = candleList
            }
        }
    }

    fun login(name: String, provider: String) {
        _userProfile.value = UserProfile(
            name = name,
            email = "${name.lowercase().replace(" ", "")}@trading.demo",
            provider = provider,
            isLoggedIn = true
        )
        _navHistory.clear()
        _forwardHistory.clear()
        _currentScreen.value = AppScreen.WATCHLIST
        updateNavState()
        showSnackbar("Welcome to Stock Demo Trading, $name!")
    }

    fun logout() {
        _userProfile.value = UserProfile(isLoggedIn = false)
        _navHistory.clear()
        _forwardHistory.clear()
        _currentScreen.value = AppScreen.LOGIN
        updateNavState()
        showSnackbar("Logged out successfully")
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _navHistory.add(_currentScreen.value)
            _forwardHistory.clear()
            _currentScreen.value = screen
            updateNavState()
        }
    }

    fun navigateBack() {
        if (_currentScreen.value == AppScreen.STOCK_DETAIL) {
            _selectedStock.value = null
        }
        if (_navHistory.isNotEmpty()) {
            val prev = _navHistory.removeAt(_navHistory.lastIndex)
            _forwardHistory.add(_currentScreen.value)
            _currentScreen.value = prev
            updateNavState()
        }
    }

    fun navigateForward() {
        if (_forwardHistory.isNotEmpty()) {
            val next = _forwardHistory.removeAt(_forwardHistory.lastIndex)
            _navHistory.add(_currentScreen.value)
            _currentScreen.value = next
            updateNavState()
        }
    }

    private fun updateNavState() {
        _canGoBack.value = _navHistory.isNotEmpty()
        _canGoForward.value = _forwardHistory.isNotEmpty()
    }

    fun selectStock(stock: Stock) {
        _selectedStock.value = stock
        _candles.value = repository.getCandlesForStock(stock.symbol, _selectedTimeFrame.value)
        navigateTo(AppScreen.STOCK_DETAIL)
    }

    fun toggleWatchlist(stock: Stock) {
        viewModelScope.launch {
            val isFavorite = watchlistSymbols.value.contains(stock.symbol)
            repository.toggleWatchlist(stock.symbol, !isFavorite)
            showSnackbar(if (!isFavorite) "Added ${stock.symbol} to Watchlist" else "Removed ${stock.symbol} from Watchlist")
        }
    }

    fun selectTimeFrame(tf: TimeFrame) {
        _selectedTimeFrame.value = tf
        _selectedStock.value?.let { stock ->
            _candles.value = repository.getCandlesForStock(stock.symbol, tf)
        }
    }

    fun toggleIndicator(ind: IndicatorType) {
        val current = _selectedIndicators.value.toMutableSet()
        if (current.contains(ind)) {
            current.remove(ind)
        } else {
            current.add(ind)
        }
        _selectedIndicators.value = current
    }

    fun selectOrderDate(date: String?) {
        _selectedOrderDate.value = date
    }

    fun executeOrder(
        stock: Stock,
        side: String,
        orderType: String,
        productType: String,
        quantity: Int,
        limitPrice: Double,
        stopLossPrice: Double,
        targetPrice: Double
    ) {
        viewModelScope.launch {
            val result = repository.executeOrder(
                stock = stock,
                side = side,
                orderType = orderType,
                productType = productType,
                quantity = quantity,
                limitPrice = limitPrice,
                stopLossPrice = stopLossPrice,
                targetPrice = targetPrice
            )
            if (result.isSuccess) {
                showSnackbar(result.getOrNull() ?: "Order Executed Successfully")
            } else {
                showSnackbar(result.exceptionOrNull()?.message ?: "Order Failed")
            }
        }
    }

    fun squareOffPosition(position: PositionEntity) {
        viewModelScope.launch {
            val stock = stocks.value.firstOrNull { it.symbol == position.symbol }
            val currentPrice = stock?.currentPrice ?: position.currentPrice
            val result = repository.squareOffPosition(position, currentPrice)
            if (result.isSuccess) {
                showSnackbar(result.getOrNull() ?: "Position Squared Off")
            } else {
                showSnackbar(result.exceptionOrNull()?.message ?: "Square Off Failed")
            }
        }
    }

    fun creditFunds(amount: Double) {
        viewModelScope.launch {
            val result = repository.addDemoFunds(amount)
            showSnackbar(result.getOrNull() ?: (result.exceptionOrNull()?.message ?: ""))
        }
    }

    fun debitFunds(amount: Double) {
        viewModelScope.launch {
            val result = repository.withdrawDemoFunds(amount)
            showSnackbar(result.getOrNull() ?: (result.exceptionOrNull()?.message ?: ""))
        }
    }

    fun resetDemoAccount() {
        viewModelScope.launch {
            val result = repository.resetDemoAccount()
            showSnackbar(result.getOrNull() ?: "Demo Account Reset to ₹10,00,000")
        }
    }

    fun setAppTheme(theme: AppTheme) {
        _appTheme.value = theme
        _isDarkMode.value = theme != AppTheme.PURE_WHITE
        showSnackbar("Theme set to ${theme.displayName}")
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        if (!enabled && _appTheme.value != AppTheme.PURE_WHITE) {
            _appTheme.value = AppTheme.PURE_WHITE
        } else if (enabled && _appTheme.value == AppTheme.PURE_WHITE) {
            _appTheme.value = AppTheme.SLATE_PRO
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _pushNotificationsEnabled.value = enabled
        showSnackbar(if (enabled) "Push notifications enabled" else "Push notifications disabled")
    }

    fun toggleSoundEffects(enabled: Boolean) {
        _soundEffectsEnabled.value = enabled
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

class TradingViewModelFactory(
    private val repository: TradingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradingViewModel::class.java)) {
            return TradingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
