package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Stock
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockGreenBg
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedBg
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuySellOrderSheet(
    stock: Stock,
    initialSide: String = "BUY", // "BUY" or "SELL"
    availableBalance: Double,
    onDismiss: () -> Unit,
    onExecuteOrder: (side: String, orderType: String, productType: String, quantity: Int, limitPrice: Double, stopLossPrice: Double, targetPrice: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var side by remember { mutableStateOf(initialSide) }
    var orderType by remember { mutableStateOf("MARKET") } // "MARKET", "LIMIT", "STOP_LOSS"
    var productType by remember { mutableStateOf("INTRADAY") } // "INTRADAY", "DELIVERY"

    val baseLot = if (stock.isIndex) stock.lotSize else 1
    var quantity by remember { mutableIntStateOf(baseLot) }
    var limitPriceInput by remember { mutableStateOf(String.format(Locale.getDefault(), "%.2f", stock.currentPrice)) }
    var stopLossInput by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("") }

    val limitPrice = limitPriceInput.toDoubleOrNull() ?: stock.currentPrice
    val effectivePrice = if (orderType == "LIMIT") limitPrice else stock.currentPrice
    val leverage = if (productType == "INTRADAY") 5 else 1
    val orderValue = effectivePrice * quantity
    val requiredMargin = orderValue / leverage
    val brokeragePoints = 20.0 // Demo brokerage points

    val isBuy = side == "BUY"
    val accentColor = if (isBuy) StockGreen else StockRed
    val accentBg = if (isBuy) StockGreenBg else StockRedBg

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("buy_sell_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stock.exchange,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "LTP: ₹${String.format(Locale.getDefault(), "%,.2f", stock.currentPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (stock.isPositive) StockGreen else StockRed
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BUY / SELL Segmented Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // BUY Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isBuy) StockGreen else Color.Transparent)
                        .clickable { side = "BUY" }
                        .testTag("select_buy_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isBuy) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BUY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isBuy) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // SELL Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isBuy) StockRed else Color.Transparent)
                        .clickable { side = "SELL" }
                        .testTag("select_sell_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (!isBuy) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SELL",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (!isBuy) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product Type (Intraday vs Delivery)
            Text(
                text = "Product Type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = productType == "INTRADAY",
                    onClick = { productType = "INTRADAY" },
                    label = { Text("Intraday (MIS 5x)") },
                    leadingIcon = if (productType == "INTRADAY") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f).testTag("product_mis")
                )
                FilterChip(
                    selected = productType == "DELIVERY",
                    onClick = { productType = "DELIVERY" },
                    label = { Text(if (stock.isIndex) "Carry Forward (NRML)" else "Delivery (CNC)") },
                    leadingIcon = if (productType == "DELIVERY") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f).testTag("product_cnc")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Type (Market, Limit, Stop Loss)
            Text(
                text = "Order Type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("MARKET", "LIMIT", "STOP_LOSS").forEach { type ->
                    FilterChip(
                        selected = orderType == type,
                        onClick = { orderType = type },
                        label = { Text(type.replace("_", " ")) },
                        modifier = Modifier.weight(1f).testTag("order_type_$type")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity Selector
            Text(
                text = if (stock.isIndex) "Quantity (Lot Size: ${stock.lotSize})" else "Quantity",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            if (quantity > baseLot) quantity -= baseLot
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Quantity")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .height(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$quantity Qty ${if (stock.isIndex) "(${quantity / stock.lotSize} Lots)" else ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            quantity += baseLot
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Quantity")
                    }
                }
            }

            // Quick Lot / Qty presets
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val multipliers = if (stock.isIndex) listOf(1, 2, 5, 10) else listOf(10, 25, 50, 100)
                multipliers.forEach { mult ->
                    val q = if (stock.isIndex) mult * stock.lotSize else mult
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (quantity == q) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { quantity = q }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = if (stock.isIndex) "${mult}L ($q)" else "$q Qty",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (quantity == q) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Limit Price Input (if LIMIT or STOP_LOSS)
            if (orderType == "LIMIT" || orderType == "STOP_LOSS") {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = limitPriceInput,
                    onValueChange = { limitPriceInput = it },
                    label = { Text("Limit Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("limit_price_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Margin & Charges Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Required Margin:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.2f", requiredMargin)} (${leverage}x)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Brokerage / Points:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹$brokeragePoints (Flat)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Demo Cash:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.2f", availableBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (availableBalance >= requiredMargin + brokeragePoints) StockGreen else StockRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Execute Trade Action Button
            val canExecute = availableBalance >= (requiredMargin + brokeragePoints)

            Button(
                onClick = {
                    onExecuteOrder(
                        side,
                        orderType,
                        productType,
                        quantity,
                        limitPrice,
                        stopLossInput.toDoubleOrNull() ?: 0.0,
                        targetInput.toDoubleOrNull() ?: 0.0
                    )
                },
                enabled = canExecute,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("execute_order_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (isBuy) Color.Black else Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = if (canExecute) "$side $quantity ${stock.symbol} (₹${String.format(Locale.getDefault(), "%,.2f", requiredMargin)})" else "Insufficient Demo Balance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
