package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.db.PositionEntity
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockRed
import com.example.ui.viewmodel.ActivePositionWithLivePnL
import java.util.Locale

@Composable
fun PositionsScreen(
    positions: List<ActivePositionWithLivePnL>,
    totalMarginUtilized: Double,
    onSquareOff: (PositionEntity) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalUnrealizedPnL = positions.sumOf { it.liveUnrealizedPnL }
    val isOverallProfit = totalUnrealizedPnL >= 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Overall Portfolio Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("positions_summary_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live Active P&L",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (isOverallProfit) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%,.2f", Math.abs(totalUnrealizedPnL))}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isOverallProfit) StockGreen else StockRed
                        )
                    }

                    // Share P&L Action Button
                    if (positions.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    val summary = buildString {
                                        append("📊 Stock Demo Trading Portfolio Update\n\n")
                                        append("Total Active P&L: ${if (isOverallProfit) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%,.2f", Math.abs(totalUnrealizedPnL))}\n")
                                        append("Active Positions: ${positions.size}\n\n")
                                        positions.forEach { pos ->
                                            append("• ${pos.entity.side} ${pos.entity.quantity} ${pos.entity.symbol} @ ₹${pos.entity.entryPrice} -> LTP ₹${pos.liveCurrentPrice} (P&L: ₹${String.format(Locale.getDefault(), "%.2f", pos.liveUnrealizedPnL)})\n")
                                        }
                                        append("\nSimulated Indian Stock Market Practice")
                                    }
                                    putExtra(Intent.EXTRA_TEXT, summary)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Trading P&L"))
                            },
                            modifier = Modifier.testTag("share_positions_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share P&L Card",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Margin Utilized",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.2f", totalMarginUtilized)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Open Positions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${positions.size} Active",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Positions List
        if (positions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Open Positions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You do not have any active live trades right now.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onNavigateToWatchlist,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("explore_watchlist_button")
                    ) {
                        Text("Explore Market & Trade")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(positions, key = { it.entity.id }) { pos ->
                    PositionCardItem(
                        position = pos,
                        onSquareOff = { onSquareOff(pos.entity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionCardItem(
    position: ActivePositionWithLivePnL,
    onSquareOff: () -> Unit
) {
    val isBuy = position.entity.side == "BUY"
    val isProfit = position.liveUnrealizedPnL >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("position_item_${position.entity.symbol}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Symbol, Side badge, Product badge, Live PnL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = position.entity.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isBuy) StockGreen.copy(alpha = 0.2f) else StockRed.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = position.entity.side,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isBuy) StockGreen else StockRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = position.entity.productType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                // Live P&L
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isProfit) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%,.2f", Math.abs(position.liveUnrealizedPnL))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) StockGreen else StockRed
                    )
                    Text(
                        text = "(${if (isProfit) "+" else "-"}${String.format(Locale.getDefault(), "%.2f", Math.abs(position.livePercentagePnL))}%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isProfit) StockGreen else StockRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Body: Quantity, Entry Price, Current LTP, Point Change
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Qty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${position.entity.quantity}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = "Avg Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${String.format(Locale.getDefault(), "%.2f", position.entity.entryPrice)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text(text = "LTP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${String.format(Locale.getDefault(), "%.2f", position.liveCurrentPrice)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Point Diff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${if (position.livePointChange >= 0) "+₹" else "-₹"}${String.format(Locale.getDefault(), "%.2f", Math.abs(position.livePointChange))}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (position.livePointChange >= 0) StockGreen else StockRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Square Off Button
            OutlinedButton(
                onClick = onSquareOff,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("square_off_${position.entity.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = StockRed
                )
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Square Off Position",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
