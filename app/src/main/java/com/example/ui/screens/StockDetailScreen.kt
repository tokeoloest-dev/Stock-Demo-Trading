package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.IndicatorType
import com.example.data.model.Stock
import com.example.data.model.TimeFrame
import com.example.ui.components.BuySellOrderSheet
import com.example.ui.components.StockChartCanvas
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: Stock,
    candles: List<Candle>,
    isInWatchlist: Boolean,
    selectedTimeFrame: TimeFrame,
    selectedIndicators: Set<IndicatorType>,
    availableBalance: Double,
    onBackClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onTimeFrameChange: (TimeFrame) -> Unit,
    onToggleIndicator: (IndicatorType) -> Unit,
    onExecuteOrder: (side: String, orderType: String, productType: String, quantity: Int, limitPrice: Double, stopLossPrice: Double, targetPrice: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showOrderSheet by remember { mutableStateOf(false) }
    var orderSheetInitialSide by remember { mutableStateOf("BUY") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stock.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = stock.exchange,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = stock.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Watchlist Toggle
                    IconButton(onClick = onToggleWatchlist, modifier = Modifier.testTag("detail_watchlist_toggle")) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Watchlist",
                            tint = if (isInWatchlist) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share Stock Card Action
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                val text = buildString {
                                    append("📈 Stock Demo Trading - Indian Markets\n\n")
                                    append("${stock.symbol} (${stock.name})\n")
                                    append("LTP: ₹${String.format(Locale.getDefault(), "%,.2f", stock.currentPrice)}\n")
                                    append("Point Movement: ${if (stock.isPositive) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%,.2f", Math.abs(stock.pointChange))} (${String.format(Locale.getDefault(), "%.2f", stock.percentageChange)}%)\n")
                                    append("Day Range: ₹${stock.lowPrice} - ₹${stock.highPrice}\n")
                                    append("Volume: ${stock.volume}\n\n")
                                    append("Simulated via Stock Demo Trading App")
                                }
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share ${stock.symbol} Quote"))
                        },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Quote"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Fixed Bottom Action Bar with BUY and SELL Buttons
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            orderSheetInitialSide = "BUY"
                            showOrderSheet = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("bottom_buy_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StockGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BUY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            orderSheetInitialSide = "SELL"
                            showOrderSheet = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("bottom_sell_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StockRed,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SELL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Price & Point Movement Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.2f", stock.currentPrice)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (stock.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (stock.isPositive) StockGreen else StockRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (stock.isPositive) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%,.2f", Math.abs(stock.pointChange))} (${String.format(Locale.getDefault(), "%.2f", stock.percentageChange)}%)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (stock.isPositive) StockGreen else StockRed
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Prev Close: ₹${String.format(Locale.getDefault(), "%,.2f", stock.prevClose)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Lot Size: ${stock.lotSize}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeframe Selector Tabs (1m, 5m, 15m, 1h, 1D, 1W)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimeFrame.values().forEach { tf ->
                    val isSelected = selectedTimeFrame == tf
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onTimeFrameChange(tf) }
                            .testTag("tf_${tf.label}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Technical Indicator Multi-Select Chips Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IndicatorType.values().forEach { ind ->
                    val isSelected = selectedIndicators.contains(ind)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleIndicator(ind) },
                        label = {
                            Text(
                                text = ind.shortName,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("indicator_${ind.shortName}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Candlestick Interactive Chart Canvas
            StockChartCanvas(
                candles = candles,
                selectedIndicators = selectedIndicators
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Live Simulated Market Depth (Top 5 Bids vs Asks)
            MarketDepthCard(stock = stock)

            Spacer(modifier = Modifier.height(16.dp))

            // Stock Details & Key Statistics Grid
            StockStatsCard(stock = stock)
        }
    }

    // Buy/Sell Bottom Sheet Dialog
    if (showOrderSheet) {
        BuySellOrderSheet(
            stock = stock,
            initialSide = orderSheetInitialSide,
            availableBalance = availableBalance,
            onDismiss = { showOrderSheet = false },
            onExecuteOrder = { side, orderType, productType, quantity, limitPrice, stopLossPrice, targetPrice ->
                onExecuteOrder(side, orderType, productType, quantity, limitPrice, stopLossPrice, targetPrice)
                showOrderSheet = false
            }
        )
    }
}

@Composable
private fun MarketDepthCard(stock: Stock) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_depth_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Market Depth",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time Order Book",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // BID (BUYERS)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Bid Qty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Bid Price", style = MaterialTheme.typography.labelSmall, color = StockGreen, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    val basePrice = stock.currentPrice
                    for (i in 1..5) {
                        val bidP = basePrice - (i * 0.25)
                        val bidQ = 500 * (6 - i) + 120
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "$bidQ", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f", bidP),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = StockGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // ASK (SELLERS)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Ask Price", style = MaterialTheme.typography.labelSmall, color = StockRed, fontWeight = FontWeight.Bold)
                        Text(text = "Ask Qty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    val basePrice = stock.currentPrice
                    for (i in 1..5) {
                        val askP = basePrice + (i * 0.25)
                        val askQ = 450 * (6 - i) + 90
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f", askP),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = StockRed,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "$askQ", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockStatsCard(stock: Stock) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_stats_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance & Overview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Day Range Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Low: ₹${stock.lowPrice}", style = MaterialTheme.typography.labelSmall, color = StockRed)
                    Text(text = "Day Range", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "High: ₹${stock.highPrice}", style = MaterialTheme.typography.labelSmall, color = StockGreen)
                }

                Spacer(modifier = Modifier.height(4.dp))

                val range = (stock.highPrice - stock.lowPrice).coerceAtLeast(0.1)
                val currentProgress = ((stock.currentPrice - stock.lowPrice) / range).toFloat().coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(currentProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Statistics Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(label = "Open", value = "₹${stock.openPrice}", modifier = Modifier.weight(1f))
                StatItem(label = "Prev. Close", value = "₹${stock.prevClose}", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(label = "Volume", value = "${stock.volume}", modifier = Modifier.weight(1f))
                StatItem(label = "Sector", value = stock.sector, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(label = "52W High", value = "₹${String.format(Locale.getDefault(), "%,.2f", stock.highPrice * 1.15)}", modifier = Modifier.weight(1f))
                StatItem(label = "52W Low", value = "₹${String.format(Locale.getDefault(), "%,.2f", stock.lowPrice * 0.82)}", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
