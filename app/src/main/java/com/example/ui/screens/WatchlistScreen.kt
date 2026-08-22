package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Stock
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed
import java.util.Locale

@Composable
fun WatchlistScreen(
    stocks: List<Stock>,
    watchlistSymbols: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStockClick: (Stock) -> Unit,
    onToggleWatchlist: (Stock) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0: Watchlist, 1: All Indian Stocks, 2: Indices
    val majorIndices = stocks.filter { it.isIndex }

    val filteredStocks = stocks.filter { stock ->
        val matchesSearch = stock.symbol.contains(searchQuery, ignoreCase = true) ||
                stock.name.contains(searchQuery, ignoreCase = true) ||
                stock.sector.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (selectedFilterIndex) {
            0 -> watchlistSymbols.contains(stock.symbol)
            1 -> !stock.isIndex
            2 -> stock.isIndex
            else -> true
        }

        matchesSearch && (searchQuery.isNotBlank() || matchesTab)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search Nifty 50, Sensex, TCS, Reliance...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("watchlist_search_bar"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // Major Indices Cards (NIFTY 50, SENSEX, BANK NIFTY)
        if (searchQuery.isEmpty() && majorIndices.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                majorIndices.forEach { indexStock ->
                    IndexSummaryCard(
                        stock = indexStock,
                        onClick = { onStockClick(indexStock) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Watchlist Tabs (My Watchlist, All Stocks, Indices)
        TabRow(
            selectedTabIndex = selectedFilterIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFilterIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedFilterIndex == 0,
                onClick = { selectedFilterIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedFilterIndex == 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watchlist (${watchlistSymbols.size})")
                    }
                },
                modifier = Modifier.testTag("tab_my_watchlist")
            )
            Tab(
                selected = selectedFilterIndex == 1,
                onClick = { selectedFilterIndex = 1 },
                text = { Text("All Equities") },
                modifier = Modifier.testTag("tab_all_equities")
            )
            Tab(
                selected = selectedFilterIndex == 2,
                onClick = { selectedFilterIndex = 2 },
                text = { Text("Indices") },
                modifier = Modifier.testTag("tab_indices")
            )
        }

        // Stock List
        if (filteredStocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedFilterIndex == 0) "Watchlist is empty" else "No stocks match your query",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedFilterIndex == 0) "Tap the star on any stock in 'All Equities' to add it to your watchlist" else "Try searching with a different symbol",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStocks, key = { it.symbol }) { stock ->
                    val isFavorite = watchlistSymbols.contains(stock.symbol)
                    StockListItemCard(
                        stock = stock,
                        isInWatchlist = isFavorite,
                        onStockClick = { onStockClick(stock) },
                        onToggleWatchlist = { onToggleWatchlist(stock) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IndexSummaryCard(
    stock: Stock,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick() }
            .testTag("index_card_${stock.symbol}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = stock.exchange,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "₹${String.format(Locale.getDefault(), "%,.2f", stock.currentPrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (stock.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (stock.isPositive) StockGreen else StockRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${if (stock.isPositive) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%.1f", Math.abs(stock.pointChange))} (${String.format(Locale.getDefault(), "%.2f", stock.percentageChange)}%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (stock.isPositive) StockGreen else StockRed
                )
            }
        }
    }
}

@Composable
private fun StockListItemCard(
    stock: Stock,
    isInWatchlist: Boolean,
    onStockClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    val flashColor by animateColorAsState(
        targetValue = if (stock.lastPriceChange > 0) StockGreen.copy(alpha = 0.15f)
        else if (stock.lastPriceChange < 0) StockRed.copy(alpha = 0.15f)
        else Color.Transparent,
        animationSpec = tween(durationMillis = 600),
        label = "flashColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStockClick() }
            .testTag("stock_item_${stock.symbol}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(flashColor)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Symbol & Name
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.symbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stock.exchange,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stock.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Live Price & Point Change
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.2f", stock.currentPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${if (stock.isPositive) "+" else "-"}${String.format(Locale.getDefault(), "%.2f", Math.abs(stock.pointChange))} (${String.format(Locale.getDefault(), "%.2f", stock.percentageChange)}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (stock.isPositive) StockGreen else StockRed
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Watchlist Star Icon
                IconButton(
                    onClick = onToggleWatchlist,
                    modifier = Modifier.size(36.dp).testTag("star_${stock.symbol}")
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Toggle Watchlist",
                        tint = if (isInWatchlist) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
