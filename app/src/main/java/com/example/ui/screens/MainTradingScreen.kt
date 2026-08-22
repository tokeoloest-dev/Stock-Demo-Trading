package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.PositionEntity
import com.example.data.model.AppScreen
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTradingScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoForward by viewModel.canGoForward.collectAsState()

    val stocks by viewModel.stocks.collectAsState()
    val watchlistSymbols by viewModel.watchlistSymbols.collectAsState()
    val selectedStock by viewModel.selectedStock.collectAsState()
    val candles by viewModel.candles.collectAsState()
    val selectedTimeFrame by viewModel.selectedTimeFrame.collectAsState()
    val selectedIndicators by viewModel.selectedIndicators.collectAsState()

    val activePositionsWithPnL by viewModel.activePositionsWithPnL.collectAsState()
    val totalMarginUtilized by viewModel.totalMarginUtilized.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val allOrderDates by viewModel.allOrderDates.collectAsState()
    val selectedOrderDate by viewModel.selectedOrderDate.collectAsState()
    val demoAccount by viewModel.demoAccount.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val pushNotificationsEnabled by viewModel.pushNotificationsEnabled.collectAsState()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Handle system back press
    BackHandler {
        if (canGoBack) {
            viewModel.navigateBack()
        } else {
            showExitDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentScreen != AppScreen.STOCK_DETAIL) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_bull_bear_logo),
                                    contentDescription = "Bull and Bear Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentScreen) {
                                    AppScreen.WATCHLIST -> "Markets & Watchlist"
                                    AppScreen.POSITIONS -> "Active Positions"
                                    AppScreen.ORDERS -> "Order History"
                                    AppScreen.ACCOUNT -> "Demo Funds & Ledger"
                                    AppScreen.SETTINGS -> "Settings & Themes"
                                    else -> "Stock Demo Trading"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        // Quick Balance Pill (Tapping opens Account/Funds)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { viewModel.navigateTo(AppScreen.ACCOUNT) }
                                .testTag("topbar_balance_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = StockGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%,.0f", demoAccount.balance)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Share App Action
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "📊 Stock Demo Trading - Real-time Indian Stock Market Simulator for Nifty 50, Sensex & Equities!")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share App"))
                            },
                            modifier = Modifier.testTag("topbar_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share App"
                            )
                        }

                        // Settings Icon in Corner (Opens Settings Page as requested)
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            modifier = Modifier.testTag("topbar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (currentScreen == AppScreen.SETTINGS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.STOCK_DETAIL) {
                Column {
                    // Custom Bottom Navigation Bar with Back, Forward & Tabs
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Main Navigation Items Row
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.height(64.dp)
                            ) {
                                // 1. Watchlist
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.WATCHLIST,
                                    onClick = { viewModel.navigateTo(AppScreen.WATCHLIST) },
                                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Watchlist") },
                                    label = { Text("Watchlist", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_watchlist")
                                )

                                // 2. Positions (with live badge)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.POSITIONS,
                                    onClick = { viewModel.navigateTo(AppScreen.POSITIONS) },
                                    icon = {
                                        if (activePositionsWithPnL.isNotEmpty()) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                        Text("${activePositionsWithPnL.size}")
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.TrendingUp, contentDescription = "Positions")
                                            }
                                        } else {
                                            Icon(Icons.Default.TrendingUp, contentDescription = "Positions")
                                        }
                                    },
                                    label = { Text("Trades", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_positions")
                                )

                                // 3. Orders
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.ORDERS,
                                    onClick = { viewModel.navigateTo(AppScreen.ORDERS) },
                                    icon = { Icon(Icons.Default.History, contentDescription = "Orders") },
                                    label = { Text("Orders", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_orders")
                                )

                                // 4. Funds / Account
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.ACCOUNT,
                                    onClick = { viewModel.navigateTo(AppScreen.ACCOUNT) },
                                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Funds") },
                                    label = { Text("Funds", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_account")
                                )
                            }

                            // Dedicated Back & Forward Navigation Toolbar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // "Back" Button
                                TextButton(
                                    onClick = {
                                        if (canGoBack) {
                                            viewModel.navigateBack()
                                        } else {
                                            showExitDialog = true
                                        }
                                    },
                                    modifier = Modifier.testTag("bottom_nav_back")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Back",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "Stock Demo Trading",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )

                                // "Forward" Button
                                TextButton(
                                    onClick = { viewModel.navigateForward() },
                                    enabled = canGoForward,
                                    modifier = Modifier.testTag("bottom_nav_forward")
                                ) {
                                    Text(
                                        text = "Forward",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Forward",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.WATCHLIST -> {
                    WatchlistScreen(
                        stocks = stocks,
                        watchlistSymbols = watchlistSymbols,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onStockClick = { stock -> viewModel.selectStock(stock) },
                        onToggleWatchlist = { stock -> viewModel.toggleWatchlist(stock) }
                    )
                }
                AppScreen.STOCK_DETAIL -> {
                    selectedStock?.let { stock ->
                        val isInWatchlist = watchlistSymbols.contains(stock.symbol)
                        StockDetailScreen(
                            stock = stock,
                            candles = candles,
                            isInWatchlist = isInWatchlist,
                            selectedTimeFrame = selectedTimeFrame,
                            selectedIndicators = selectedIndicators,
                            availableBalance = demoAccount.balance - demoAccount.marginUtilized,
                            onBackClick = { viewModel.navigateBack() },
                            onToggleWatchlist = { viewModel.toggleWatchlist(stock) },
                            onTimeFrameChange = { tf -> viewModel.selectTimeFrame(tf) },
                            onToggleIndicator = { ind -> viewModel.toggleIndicator(ind) },
                            onExecuteOrder = { side, orderType, productType, quantity, limitPrice, stopLossPrice, targetPrice ->
                                viewModel.executeOrder(
                                    stock = stock,
                                    side = side,
                                    orderType = orderType,
                                    productType = productType,
                                    quantity = quantity,
                                    limitPrice = limitPrice,
                                    stopLossPrice = stopLossPrice,
                                    targetPrice = targetPrice
                                )
                            }
                        )
                    }
                }
                AppScreen.POSITIONS -> {
                    PositionsScreen(
                        positions = activePositionsWithPnL,
                        totalMarginUtilized = totalMarginUtilized,
                        onSquareOff = { position -> viewModel.squareOffPosition(position) },
                        onNavigateToWatchlist = { viewModel.navigateTo(AppScreen.WATCHLIST) }
                    )
                }
                AppScreen.ORDERS -> {
                    OrdersHistoryScreen(
                        orders = orders,
                        allDates = allOrderDates,
                        selectedDate = selectedOrderDate,
                        onSelectDate = { date -> viewModel.selectOrderDate(date) }
                    )
                }
                AppScreen.ACCOUNT -> {
                    DemoAccountScreen(
                        account = demoAccount,
                        transactions = transactions,
                        onCreditFunds = { amount -> viewModel.creditFunds(amount) },
                        onDebitFunds = { amount -> viewModel.debitFunds(amount) },
                        onResetAccount = { viewModel.resetDemoAccount() }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        userProfile = userProfile,
                        currentTheme = appTheme,
                        isDarkMode = isDarkMode,
                        pushNotificationsEnabled = pushNotificationsEnabled,
                        soundEffectsEnabled = soundEffectsEnabled,
                        onThemeChange = { theme -> viewModel.setAppTheme(theme) },
                        onDarkModeToggle = { dark -> viewModel.toggleDarkMode(dark) },
                        onPushNotificationsToggle = { enabled -> viewModel.togglePushNotifications(enabled) },
                        onSoundEffectsToggle = { enabled -> viewModel.toggleSoundEffects(enabled) },
                        onResetAccount = { viewModel.resetDemoAccount() },
                        onLogout = { viewModel.logout() }
                    )
                }
                else -> {}
            }
        }
    }

    // Exit Application Confirmation Dialog (Prompt as requested by user)
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Stock Demo Trading?") },
            text = { Text("Do you want to exit the demo trading application?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("confirm_exit_yes")
                ) {
                    Text("Yes, Exit")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitDialog = false },
                    modifier = Modifier.testTag("confirm_exit_no")
                ) {
                    Text("No, Stay")
                }
            },
            modifier = Modifier.testTag("exit_prompt_dialog")
        )
    }
}
